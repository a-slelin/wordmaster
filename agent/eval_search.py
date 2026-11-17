# agent/eval_search.py — Лаб. 4, шаг 5: сравнение keyword vs vector (hit@1, hit@3)
#
# Запуск: py agent/eval_search.py
# Оба поиска оцениваются ровно так, как их видит агент: вызываем функцию инструмента и разбираем
# её текстовый результат (для vector — с порогом MIN_SCORE).

import re

from rag import CHUNK_OVERLAP, CHUNK_SIZE, EMBED_MODEL, MIN_SCORE, get_index, keyword_search_docs, search_docs

# 10 вопросов СВОИМИ словами: (вопрос, тип, файлы, где лежит ответ)
# «перефраз» — в документе нет слов вопроса, смысл тот же; «термин» — точное слово/код из документа.
QUESTIONS = [
    ("как стереть свой профиль?", "перефраз", {"security.md"}),
    ("сколько раз нужно угадать слово, чтобы оно считалось запомненным?", "перефраз", {"training.md"}),
    ("можно ли отметить, что мне понравился чужой набор слов?", "перефраз", {"catalog.md"}),
    ("будут ли слова повторяться через растущие промежутки времени?", "перефраз", {"roadmap.md"}),
    ("что будет, если пять раз подряд ошибиться при входе?", "перефраз", {"security.md"}),
    ("на каком языке и фреймворке написан сервер?", "перефраз", {"architecture.md", "about.md"}),
    ("что означает ответ 401?", "термин", {"api.md"}),
    ("какой эндпоинт выдаёт следующую карточку для тренировки?", "термин", {"api.md"}),
    ("сколько живёт JWT-токен?", "термин", {"security.md"}),
    ("можно ли загрузить слова из CSV?", "термин", {"faq.md", "roadmap.md"}),
]

KEYWORD_FILE_RE = re.compile(r"^\[\d+/\d+\] docs/([^:\n]+):$", re.MULTILINE)  # "[2/3] docs/security.md:"
VECTOR_FILE_RE = re.compile(r"^\[([^\]\n§]+?) § [^\]\n]+\] cos=", re.MULTILINE)  # "[security.md § ...] cos=0.62"


def keyword_files(query: str, k: int) -> list[str]:
    """Файлы top-k результатов keyword_search_docs (по порядку, с повторами — как видит агент)."""
    return KEYWORD_FILE_RE.findall(keyword_search_docs(query, k))


def vector_files(query: str, k: int) -> list[str]:
    """Файлы top-k результатов векторного search_docs (фрагменты ниже MIN_SCORE уже отброшены)."""
    return VECTOR_FILE_RE.findall(search_docs(query, k))


def hybrid_files(query: str, k: int, rrf_k: int = 60) -> list[str]:
    """Бонус: reciprocal rank fusion на уровне файлов. score(file) = Σ 1 / (rrf_k + ранг в каждом поиске)."""
    scores = {}
    for files in (keyword_files(query, 5), vector_files(query, 5)):
        ranked = list(dict.fromkeys(files))  # убираем повторы файла, сохраняя порядок
        for rank, file in enumerate(ranked, start=1):
            scores[file] = scores.get(file, 0) + 1 / (rrf_k + rank)
    return sorted(scores, key=scores.get, reverse=True)[:k]


SEARCHES = {"keyword": keyword_files, "vector": vector_files, "hybrid": hybrid_files}


def hit_at_k(search, k: int) -> float:
    """Доля вопросов, для которых нужный файл есть среди top-k результатов search."""
    hits = sum(1 for question, _, expected in QUESTIONS if expected & set(search(question, k)))
    return hits / len(QUESTIONS)


def short(files: list[str]) -> str:
    """'security.md, faq.md' → 'security, faq' — чтобы таблица помещалась."""
    return ", ".join(f.removesuffix(".md") for f in files) or "—"


def main() -> None:
    get_index()  # индекс строим/грузим заранее, чтобы лог INDEX не разрывал таблицу
    print(f"Модель эмбеддингов: {EMBED_MODEL}, size={CHUNK_SIZE}, overlap={CHUNK_OVERLAP}, "
          f"MIN_SCORE={MIN_SCORE}, вопросов: {len(QUESTIONS)}\n")

    print("| N  | Вопрос | Тип | Где ответ | keyword top-3 | vector top-3 | hybrid top-3 |")
    print("|----|--------|-----|-----------|---------------|--------------|--------------|")
    for n, (question, kind, expected) in enumerate(QUESTIONS, start=1):
        cells = []
        for search in SEARCHES.values():
            files = search(question, 3)
            mark = "+" if expected & set(files) else "-"
            cells.append(f"{mark} ({short(files)})")
        print(f"| {n:<2} | {question} | {kind} | {short(sorted(expected))} | " + " | ".join(cells) + " |")

    totals = [f"{hit_at_k(s, 1):.1f} / {hit_at_k(s, 3):.1f}" for s in SEARCHES.values()]
    print(f"|    | **hit@1 / hit@3** | | | " + " | ".join(f"**{t}**" for t in totals) + " |")

    print("\nПо типам вопросов (hit@3):")
    for kind in ("перефраз", "термин"):
        subset = [q for q in QUESTIONS if q[1] == kind]
        row = []
        for name, search in SEARCHES.items():
            hits = sum(1 for q, _, exp in subset if exp & set(search(q, 3)))
            row.append(f"{name} {hits}/{len(subset)}")
        print(f"  {kind:<9} " + ", ".join(row))

    # Шаг 6: сколько контекста добавляет один вызов инструмента (средняя длина результата по 10 вопросам)
    print("\nДлина результата search_docs (среднее по 10 вопросам, символы / оценка len//4 из лаб. 3):")
    for k in (3, 5):
        row = []
        for name, tool in (("keyword", keyword_search_docs), ("vector", search_docs)):
            lengths = [len(tool(q, k)) for q, _, _ in QUESTIONS]
            avg = sum(lengths) / len(lengths)
            row.append(f"{name} {avg:.0f} симв. ≈ {avg / 4:.0f} ток. (макс. {max(lengths)})")
        print(f"  k={k}: " + "; ".join(row))


if __name__ == "__main__":
    main()
