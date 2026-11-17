# agent/rag.py — Лаб. 4: поиск по документации (keyword из лаб. 3 → поиск по смыслу)

import re
from pathlib import Path

AGENT_DIR = Path(__file__).parent.resolve()
WORKSPACE = AGENT_DIR / "workspace"   # песочница агента
DOCS_DIR = WORKSPACE / "docs"         # корпус для search_docs


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


if __name__ == "__main__":
    # Быстрая проверка шага 0: старый поиск работает из нового модуля
    print(keyword_search_docs("тема по умолчанию", 2))