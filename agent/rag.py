# agent/rag.py — Лаб. 4: поиск по документации (keyword из лаб. 3 → поиск по смыслу)

import os
import re
import sys
import time
from pathlib import Path

import numpy as np
from dotenv import load_dotenv
from openai import OpenAI

AGENT_DIR = Path(__file__).parent.resolve()
WORKSPACE = AGENT_DIR / "workspace"   # песочница агента
DOCS_DIR = WORKSPACE / "docs"         # корпус для search_docs

load_dotenv(AGENT_DIR / ".env")

# Эмбеддинги: по умолчанию Ollama локально (OpenAI-совместимый /v1/embeddings).
# Ключ обязателен для клиента OpenAI, но Ollama его игнорирует.
EMBED_BASE_URL = os.getenv("EMBED_BASE_URL", "http://localhost:11434/v1")
EMBED_API_KEY = os.getenv("EMBED_API_KEY", "ollama")
EMBED_MODEL = os.getenv("EMBED_MODEL", "bge-m3")

embed_client = OpenAI(base_url=EMBED_BASE_URL, api_key=EMBED_API_KEY)


# ======================= ШАГ 1: ЭМБЕДДИНГ И КОСИНУС =======================

def embed(texts: list[str]) -> list[list[float]]:
    """Эмбеддинги для списка текстов ОДНИМ запросом. Порядок векторов = порядок texts."""
    if not texts:
        return []
    response = embed_client.embeddings.create(model=EMBED_MODEL, input=texts)
    data = sorted(response.data, key=lambda d: d.index)  # на всякий случай восстанавливаем порядок
    return [d.embedding for d in data]


def cosine(a, b) -> float:
    """Косинусная близость: cos = a·b / (|a|·|b|). 1 — одно направление, 0 — ортогональны."""
    a = np.asarray(a, dtype=np.float32)
    b = np.asarray(b, dtype=np.float32)
    norm = np.linalg.norm(a) * np.linalg.norm(b)
    if norm == 0:
        return 0.0
    return float(np.dot(a, b) / norm)


# ======================= KEYWORD-ПОИСК (из лаб. 3, для сравнения) =======================

STOP_WORDS = {"как", "что", "где", "какой", "какая", "какие", "когда", "для", "или", "это", "найди",
              "документации", "документация", "про", "есть"}


def stem(word: str) -> str:
    """Грубый стемминг: первые 5 букв. 'выученным' и 'выучено' → 'выуче'."""
    return word[:5]


def words(text: str) -> list[str]:
    """Слова в нижнем регистре, 'ё' → 'е'."""
    return re.findall(r"\w+", text.lower().replace("ё", "е"))


def keyword_search_docs(query: str, limit: int = 3) -> str:
    """Keyword-поиск по абзацам файлов workspace/docs/ (реализация лаб. 3 без изменений)."""
    if not query or not query.strip():
        return "ОШИБКА: пустой запрос"
    try:
        limit = int(limit)  # модель иногда присылает "3" строкой
    except (TypeError, ValueError):
        return "ОШИБКА: limit должен быть целым числом от 1 до 5"
    if not 1 <= limit <= 5:
        return "ОШИБКА: limit должен быть целым числом от 1 до 5"
    if not DOCS_DIR.is_dir():
        return "ОШИБКА: папка docs/ не найдена"

    terms = {stem(w) for w in words(query) if len(w) >= 3 and w not in STOP_WORDS}
    if not terms:
        return "ОШИБКА: в запросе нет значимых слов"

    hits = []  # (score, путь, абзац)
    for file in sorted(DOCS_DIR.rglob("*")):
        if file.suffix.lower() not in (".md", ".txt"):
            continue
        rel = file.relative_to(WORKSPACE).as_posix()
        text = file.read_text(encoding="utf-8")
        for para in re.split(r"\n\s*\n", text):  # абзацы разделены пустой строкой
            score = len(terms & {stem(w) for w in words(para)})  # сколько РАЗНЫХ слов запроса нашлось
            if score:
                hits.append((score, rel, para.strip()))

    if not hits:
        return f"Ничего не найдено по запросу '{query}' (искал слова: {', '.join(sorted(terms))})"
    hits.sort(key=lambda h: -h[0])  # sort стабильный: при равном score — порядок файлов
    return "\n\n".join(f"[{score}/{len(terms)}] {rel}:\n{para[:400]}"
                       for score, rel, para in hits[:limit])


# ======================= ДЕМО ДЛЯ ПРОВЕРКИ ШАГОВ =======================

# 6 пар фраз из предметной области WordMaster: (тип пары, фраза A, фраза B)
PAIRS = [
    ("синонимы", "удалить колоду", "стереть набор карточек"),
    ("синонимы", "забыл пароль от аккаунта", "не получается войти в профиль"),
    ("общее слово", "скопировать колоду себе", "скопировать текст в буфер обмена"),
    ("общее слово", "карточка со словом", "банковская карточка"),
    ("не связаны", "тренировка неправильных глаголов", "погода в Москве"),
    ("не связаны", "сделать колоду публичной", "рецепт борща"),
]


def demo_pairs() -> None:
    """Шаг 1: длина вектора и таблица косинусов для 6 пар (один вызов embed на все фразы)."""
    print(f"Модель эмбеддингов: {EMBED_MODEL} ({EMBED_BASE_URL})")
    vec = embed(["удалить аккаунт"])[0]
    print(f"Длина вектора для одного текста: {len(vec)}")

    phrases = [p for _, a, b in PAIRS for p in (a, b)]  # 12 фраз подряд: A1, B1, A2, B2, ...
    t0 = time.perf_counter()
    vectors = embed(phrases)  # ОДИН запрос на все фразы
    print(f"embed({len(phrases)} фраз) одним запросом: {time.perf_counter() - t0:.2f} с\n")

    print("| N | Тип пары    | Фраза A                          | Фраза B                           | cos   |")
    print("|---|-------------|----------------------------------|-----------------------------------|-------|")
    for i, (kind, a, b) in enumerate(PAIRS):
        cos = cosine(vectors[2 * i], vectors[2 * i + 1])
        print(f"| {i + 1} | {kind:<11} | {a:<32} | {b:<33} | {cos:.3f} |")

    # Ориентир из задания: «удалить аккаунт» / «стереть профиль» ≈ 0.78, «… / погода в Москве» ≈ 0.41
    ref = embed(["удалить аккаунт", "стереть профиль", "погода в Москве"])
    print(f"\nОриентир: 'удалить аккаунт' / 'стереть профиль' = {cosine(ref[0], ref[1]):.3f}, "
          f"'удалить аккаунт' / 'погода в Москве' = {cosine(ref[0], ref[2]):.3f}")


if __name__ == "__main__":
    # py agent/rag.py keyword — шаг 0: старый поиск работает из нового модуля
    # py agent/rag.py pairs   — шаг 1: длина вектора и косинусы 6 пар
    command = sys.argv[1] if len(sys.argv) > 1 else "keyword"
    if command == "keyword":
        print(keyword_search_docs("тема по умолчанию", 2))
    elif command == "pairs":
        demo_pairs()
    else:
        print(f"Неизвестная команда '{command}'. Доступно: keyword, pairs")