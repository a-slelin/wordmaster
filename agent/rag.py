# agent/rag.py — Лаб. 4: поиск по документации (keyword из лаб. 3 → поиск по смыслу)

import hashlib
import json
import os
import re
import sys
import time
from pathlib import Path

import numpy as np
from dotenv import load_dotenv
from openai import OpenAI

AGENT_DIR = Path(__file__).parent.resolve()
WORKSPACE = AGENT_DIR / "workspace"  # песочница агента
DOCS_DIR = WORKSPACE / "docs"  # корпус для search_docs

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


# ======================= ШАГ 2: CHUNKING =======================

CHUNK_SIZE = int(os.getenv("CHUNK_SIZE", "800"))  # макс. длина фрагмента в символах
CHUNK_OVERLAP = int(os.getenv("CHUNK_OVERLAP", "150"))  # перекрытие соседних окон одного раздела

HEADING_RE = re.compile(r"^(#{1,3})\s+(.*?)\s*$")  # заголовки markdown: #, ##, ###


def split_windows(text: str, size: int, overlap: int) -> list[str]:
    """Режет текст окнами по size символов; каждое следующее окно начинается на overlap раньше конца
    предыдущего (шаг = size - overlap). Короткий текст возвращается одним окном."""
    if len(text) <= size:
        return [text]
    step = size - overlap
    windows = []
    start = 0
    while True:
        windows.append(text[start:start + size])
        if start + size >= len(text):  # последнее окно дошло до конца текста
            break
        start += step
    return windows


def chunk_markdown(path, size: int = CHUNK_SIZE, overlap: int = CHUNK_OVERLAP) -> list[dict]:
    """Фрагменты markdown-файла: [{"file", "title", "text"}, ...].
    1) делим по заголовкам #/##/###; 2) длинный раздел режем окнами size/overlap;
    3) раздел без текста (только заголовок) пропускаем."""
    if not 0 <= overlap < size:
        raise ValueError(f"нужно 0 <= overlap < size, получено size={size}, overlap={overlap}")
    path = Path(path)
    text = path.read_text(encoding="utf-8")

    sections = []  # [(заголовок, [строки раздела])]
    title = path.stem  # текст до первого заголовка получает имя файла
    lines = []
    for line in text.splitlines():
        match = HEADING_RE.match(line)
        if match:
            sections.append((title, lines))
            title, lines = match.group(2) or path.stem, []
        else:
            lines.append(line)
    sections.append((title, lines))

    chunks = []
    for title, lines in sections:
        body = "\n".join(lines).strip()
        if not body:  # только заголовок — пропускаем
            continue
        for piece in split_windows(body, size, overlap):
            chunks.append({"file": path.name, "title": title, "text": piece})
    return chunks


def chunk_docs(size: int = CHUNK_SIZE, overlap: int = CHUNK_OVERLAP) -> list[dict]:
    """Фрагменты всех .md из docs/ в стабильном порядке (по имени файла)."""
    chunks = []
    for path in sorted(DOCS_DIR.glob("*.md")):
        chunks.extend(chunk_markdown(path, size, overlap))
    return chunks


# ======================= ШАГ 3: ИНДЕКС НА ДИСКЕ С ПЕРЕСЧЁТОМ ПО ХЭШУ =======================

# Индекс лежит ВНЕ песочницы workspace/: агент через write_file не может его затереть или подменить
INDEX_DIR = AGENT_DIR / "index"
INDEX_FILE = INDEX_DIR / "docs_index.json"
EMBED_BATCH = 64  # сколько фрагментов в одном запросе к эмбеддинг-модели


def docs_hash(size: int, overlap: int) -> str:
    """sha256 от всего, что влияет на индекс: модель, size/overlap, имена и содержимое всех .md в docs/."""
    h = hashlib.sha256()
    h.update(f"model={EMBED_MODEL}|size={size}|overlap={overlap}".encode("utf-8"))
    for path in sorted(DOCS_DIR.glob("*.md")):
        h.update(b"\0" + path.name.encode("utf-8") + b"\0")  # \0 — разделитель, чтобы файлы не «склеились»
        h.update(path.read_bytes())
    return h.hexdigest()


def chunk_input(chunk: dict) -> str:
    """Что отдаём эмбеддинг-модели: заголовок раздела + текст (заголовок добавляет смысла коротким окнам)."""
    return f"{chunk['title']}\n{chunk['text']}"


def load_or_build_index(size: int = CHUNK_SIZE, overlap: int = CHUNK_OVERLAP) -> dict:
    """Загружает индекс с диска, если хэш совпал (без вызовов модели), иначе пересчитывает и сохраняет.
    Индекс: {"hash", "model", "size", "overlap", "chunks": [{file, title, text}], "vectors": [[...]]}."""
    t0 = time.perf_counter()
    current_hash = docs_hash(size, overlap)

    if INDEX_FILE.exists():
        try:
            index = json.loads(INDEX_FILE.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            index = None  # битый файл — просто пересоберём
        if index and index.get("hash") == current_hash:
            print(f"INDEX: cache hit, {len(index['chunks'])} chunks, {time.perf_counter() - t0:.2f} s")
            return index
        reason = "хэш не совпал" if index else "файл индекса повреждён"
    else:
        reason = "файла индекса нет"

    chunks = chunk_docs(size, overlap)
    vectors = []
    for i in range(0, len(chunks), EMBED_BATCH):  # пачками, а не запросом на каждый фрагмент
        vectors.extend(embed([chunk_input(c) for c in chunks[i:i + EMBED_BATCH]]))

    index = {"hash": current_hash, "model": EMBED_MODEL, "size": size, "overlap": overlap,
             "chunks": chunks, "vectors": vectors}
    INDEX_DIR.mkdir(exist_ok=True)
    tmp = INDEX_FILE.with_suffix(".tmp")  # пишем во временный файл и подменяем: не останется «половины» индекса
    tmp.write_text(json.dumps(index, ensure_ascii=False), encoding="utf-8")
    tmp.replace(INDEX_FILE)
    print(f"INDEX: rebuilt, {len(chunks)} chunks, {time.perf_counter() - t0:.2f} s ({reason})")
    return index


# ======================= ШАГ 4: ВЕКТОРНЫЙ search_docs =======================

# Порог «похожести»: ниже него считаем, что в docs ответа нет. Подобран на своих данных (demo_calibrate),
# у каждой модели свой масштаб косинуса и свой «фон» несвязанных текстов.
MIN_SCORE = float(os.getenv("MIN_SCORE", "0.49"))  # середина зазора по demo_calibrate: 0.445 … 0.531
SNIPPET_LEN = 400  # сколько символов фрагмента отдаём модели

_cache = {"hash": None, "index": None, "matrix": None}  # индекс в памяти процесса


def get_index() -> tuple[dict, np.ndarray]:
    """Индекс + матрица нормированных векторов. Хэш docs проверяется на каждый вызов (дёшево: чтение
    10 файлов), поэтому правка docs/ во время работы агента тоже приведёт к пересборке."""
    if _cache["hash"] != docs_hash(CHUNK_SIZE, CHUNK_OVERLAP):
        index = load_or_build_index()
        matrix = np.asarray(index["vectors"], dtype=np.float32)
        if len(matrix):
            matrix /= np.linalg.norm(matrix, axis=1, keepdims=True)  # строки длины 1
        _cache.update(hash=index["hash"], index=index, matrix=matrix)
    return _cache["index"], _cache["matrix"]


def vector_search(query: str, k: int = 3) -> list[tuple[float, dict]]:
    """Top-k фрагментов по косинусу: [(cos, {"file", "title", "text"}), ...] по убыванию cos."""
    index, matrix = get_index()
    if not index["chunks"]:
        return []
    q = np.asarray(embed([query])[0], dtype=np.float32)  # запрос — ТОЙ ЖЕ моделью, что и индекс
    q /= np.linalg.norm(q)
    scores = matrix @ q  # для векторов длины 1 скалярное произведение = cos (формула cosine() из шага 1)
    top = np.argsort(-scores)[:k]
    return [(float(scores[i]), index["chunks"][i]) for i in top]


def search_docs(query: str, limit: int = 3, *, searcher=None) -> str:
    """Векторный поиск по docs/. Интерфейс для модели прежний: query, limit 1–5.
    searcher — чем искать top-k (по умолчанию numpy-индекс; бонус: chroma_store.chroma_search)."""
    if not query or not query.strip():
        return "ОШИБКА: пустой запрос"
    try:
        limit = int(limit)  # модель иногда присылает "3" строкой
    except (TypeError, ValueError):
        return "ОШИБКА: limit должен быть целым числом от 1 до 5"
    if not 1 <= limit <= 5:
        return "ОШИБКА: limit должен быть целым числом от 1 до 5"
    if not DOCS_DIR.is_dir() or not any(DOCS_DIR.glob("*.md")):
        return "ОШИБКА: docs/ пуста — нет .md файлов для поиска"

    try:
        hits = (searcher or vector_search)(query.strip(), limit)
    except Exception as e:  # Ollama не запущена, модель не скачана и т. п.
        return f"ОШИБКА: эмбеддинг-модель {EMBED_MODEL} недоступна ({e})"
    if not hits:
        return "ОШИБКА: docs/ пуста — в индексе нет ни одного фрагмента"

    best = hits[0][0]
    if best < MIN_SCORE:
        return (f"ОШИБКА: нет похожих фрагментов (лучший cos={best:.2f} < MIN_SCORE={MIN_SCORE}). "
                f"В документации нет ответа на этот вопрос — так и скажи пользователю.")
    hits = [(score, c) for score, c in hits if score >= MIN_SCORE]  # слабые хвосты не тратят контекст
    parts = []
    for score, c in hits:
        text = c["text"] if len(c["text"]) <= SNIPPET_LEN else c["text"][:SNIPPET_LEN] + "…"
        parts.append(f"[{c['file']} § {c['title']}] cos={score:.2f}\n{text}")
    best_ref = f"[{hits[0][1]['file']} § {hits[0][1]['title']}]"
    # Напоминание рядом с данными: правило из системного промпта модель на длинном контексте «забывает».
    # Формулировка не должна звучать как «отвечай сейчас»: попытка 2 из-за неё пропустила get_random_card.
    return "\n\n".join(parts) + (f"\n\nИсточник: {best_ref}. Если для задачи нужны ещё инструменты — "
                                 f"сначала вызови их, а источник укажи в итоговом ответе пользователю.")


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


def demo_chunks(size: int = CHUNK_SIZE, overlap: int = CHUNK_OVERLAP) -> None:
    """Шаг 2: число фрагментов по файлам, автоматические проверки и 3 фрагмента целиком."""
    print(f"chunk_markdown: size={size}, overlap={overlap}\n")
    all_chunks = []
    for path in sorted(DOCS_DIR.glob("*.md")):
        chunks = chunk_markdown(path, size, overlap)
        all_chunks.extend(chunks)
        longest = max(len(c["text"]) for c in chunks)
        print(f"  {path.name:<16} {len(chunks):>2} фрагм., самый длинный {longest} симв.")
    print(f"  {'ИТОГО':<16} {len(all_chunks):>2} фрагм.\n")

    # Проверки из задания
    too_long = [c for c in all_chunks if len(c["text"]) > size]
    no_meta = [c for c in all_chunks if not c["file"] or not c["title"]]
    pairs = [(a, b) for a, b in zip(all_chunks, all_chunks[1:])
             if a["file"] == b["file"] and a["title"] == b["title"]]  # соседние окна одного раздела
    bad_overlap = [(a, b) for a, b in pairs if overlap and a["text"][-overlap:] != b["text"][:overlap]]
    print(f"Проверка: длиннее size={size}: {len(too_long)}; без file/title: {len(no_meta)}; "
          f"пар соседних окон: {len(pairs)}, из них без перекрытия: {len(bad_overlap)}")

    # Показываем 3 фрагмента целиком: два соседних окна длинного раздела + один обычный
    shown = [pairs[0][0], pairs[0][1]] if pairs else []
    shown.append(next(c for c in all_chunks if c["file"] == "training.md"))
    for c in shown:
        print(f"\n----- [{c['file']} § {c['title']}] {len(c['text'])} симв. -----\n{c['text']}")
    if pairs:
        a, b = pairs[0]
        print(f"\n----- перекрытие {overlap} симв. (конец окна 1 == начало окна 2) -----\n{a['text'][-overlap:]}")


def demo_index(size: int = CHUNK_SIZE, overlap: int = CHUNK_OVERLAP) -> None:
    """Шаг 3: загрузить или пересобрать индекс и показать, что в нём лежит."""
    index = load_or_build_index(size, overlap)
    print(f"  файл:    {INDEX_FILE.relative_to(AGENT_DIR.parent)} ({INDEX_FILE.stat().st_size / 1024:.0f} KB)")
    print(f"  модель:  {index['model']}, size={index['size']}, overlap={index['overlap']}")
    print(f"  хэш:     {index['hash'][:16]}...")
    print(f"  векторы: {len(index['vectors'])} x {len(index['vectors'][0]) if index['vectors'] else 0}")


# Запросы для подбора MIN_SCORE: по теме docs (своими словами) и заведомо не по теме
CALIBRATE_ON = ["как стереть свой профиль", "что будет, если много раз ввести неверный пароль",
                "можно ли править чужой набор слов", "с какой темы начинается тренировка",
                "на чём написан сервер", "когда слово считается запомненным"]
CALIBRATE_OFF = ["рецепт борща", "курс биткоина на сегодня", "как поменять масло в машине",
                 "погода в Москве на завтра", "кто выиграл чемпионат мира по футболу",
                 "сколько лап у паука"]


def demo_calibrate() -> None:
    """Шаг 4: лучший cos для запросов по теме и не по теме → где поставить MIN_SCORE."""
    get_index()
    rows = []
    for kind, queries in (("по теме", CALIBRATE_ON), ("НЕ по теме", CALIBRATE_OFF)):
        for q in queries:
            score, c = vector_search(q, 1)[0]
            rows.append((kind, score))
            print(f"  {kind:<10} cos={score:.3f}  {q!r:<55} → [{c['file']} § {c['title']}]")
    on = [s for k, s in rows if k == "по теме"]
    off = [s for k, s in rows if k != "по теме"]
    print(f"\nпо теме: min={min(on):.3f}; не по теме: max={max(off):.3f}; "
          f"середина зазора = {(min(on) + max(off)) / 2:.3f}; сейчас MIN_SCORE={MIN_SCORE}")


if __name__ == "__main__":
    # py agent/rag.py keyword — шаг 0: старый поиск работает из нового модуля
    # py agent/rag.py pairs   — шаг 1: длина вектора и косинусы 6 пар
    # py agent/rag.py chunks [size] [overlap] — шаг 2: нарезка docs на фрагменты
    # py agent/rag.py index [size] [overlap]  — шаг 3: индекс на диске (cache hit / rebuilt)
    # py agent/rag.py calibrate               — шаг 4: подбор порога MIN_SCORE
    # py agent/rag.py search "запрос" [limit] — шаг 4: векторный search_docs (и keyword для сравнения)
    command = sys.argv[1] if len(sys.argv) > 1 else "keyword"
    if command == "keyword":
        print(keyword_search_docs("тема по умолчанию", 2))
    elif command == "pairs":
        demo_pairs()
    elif command == "chunks":
        size = int(sys.argv[2]) if len(sys.argv) > 2 else CHUNK_SIZE
        overlap = int(sys.argv[3]) if len(sys.argv) > 3 else CHUNK_OVERLAP
        demo_chunks(size, overlap)
    elif command == "index":
        size = int(sys.argv[2]) if len(sys.argv) > 2 else CHUNK_SIZE
        overlap = int(sys.argv[3]) if len(sys.argv) > 3 else CHUNK_OVERLAP
        demo_index(size, overlap)
    elif command == "calibrate":
        demo_calibrate()
    elif command == "search":
        query = sys.argv[2] if len(sys.argv) > 2 else "как стереть свой профиль"
        limit = int(sys.argv[3]) if len(sys.argv) > 3 else 3
        print(f"=== vector search_docs({query!r}, {limit}) ===\n{search_docs(query, limit)}")
        print(f"\n=== keyword_search_docs({query!r}, {limit}) ===\n{keyword_search_docs(query, limit)}")
    else:
        print(f"Неизвестная команда '{command}'. Доступно: keyword, pairs, chunks, index, calibrate, search")
