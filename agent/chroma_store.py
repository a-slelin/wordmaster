# agent/chroma_store.py — Лаб. 4, бонус: тот же индекс в Chroma вместо numpy + JSON
#
# Запуск: py agent/chroma_store.py — построить/загрузить коллекцию и сравнить с numpy-версией.
# В агенте: $env:SEARCH_IMPL="chroma"; py agent/agent.py
#
# Эмбеддинги считаем САМИ (bge-m3 через embed() из rag.py) и передаём в Chroma готовыми:
# встроенная модель Chroma (all-MiniLM, английская) не используется — embedding_function=None.

import time

import chromadb
import numpy as np

from rag import (CHUNK_OVERLAP, CHUNK_SIZE, EMBED_MODEL, INDEX_DIR, MIN_SCORE, docs_hash, embed, get_index,
                 load_or_build_index, search_docs)

CHROMA_DIR = INDEX_DIR / "chroma"  # постоянное хранилище внутри agent/index/ (вне песочницы, не в git)
COLLECTION = "docs"

_client = None
_collection = None


def get_collection():
    """Коллекция Chroma, актуальная для текущих docs. Хэш тот же, что у JSON-индекса (шаг 3):
    совпал — открываем коллекцию с диска; нет — пересоздаём из векторов JSON-индекса."""
    global _client, _collection
    current_hash = docs_hash(CHUNK_SIZE, CHUNK_OVERLAP)
    if _collection is not None and _collection.metadata.get("docs_hash") == current_hash:
        return _collection

    t0 = time.perf_counter()
    if _client is None:
        _client = chromadb.PersistentClient(path=str(CHROMA_DIR),
                                            settings=chromadb.Settings(anonymized_telemetry=False))
    try:
        collection = _client.get_collection(COLLECTION, embedding_function=None)
        if collection.metadata.get("docs_hash") == current_hash and collection.count() > 0:
            print(f"CHROMA: cache hit, {collection.count()} chunks, {time.perf_counter() - t0:.2f} s")
            _collection = collection
            return collection
        _client.delete_collection(COLLECTION)  # устарела — пересоздаём целиком
    except chromadb.errors.NotFoundError:
        pass  # коллекции ещё нет

    # Векторы берём из JSON-индекса: те же фрагменты и та же модель → сравнение numpy vs Chroma честное,
    # а bge-m3 не вызывается второй раз (при cache hit JSON-индекса — ни одного запроса к модели).
    index = load_or_build_index()
    collection = _client.create_collection(
        COLLECTION,
        metadata={"hnsw:space": "cosine", "docs_hash": current_hash, "model": EMBED_MODEL},
        embedding_function=None,
    )
    if index["chunks"]:
        collection.add(
            ids=[str(i) for i in range(len(index["chunks"]))],
            embeddings=index["vectors"],
            documents=[c["text"] for c in index["chunks"]],
            metadatas=[{"file": c["file"], "title": c["title"]} for c in index["chunks"]],
        )
    print(f"CHROMA: rebuilt, {collection.count()} chunks, {time.perf_counter() - t0:.2f} s")
    _collection = collection
    return collection


def chroma_by_vector(q_vec: list[float], k: int) -> list[tuple[float, dict]]:
    """Top-k из Chroma по готовому вектору запроса: [(cos, {"file", "title", "text"}), ...]."""
    collection = get_collection()
    if collection.count() == 0:
        return []
    result = collection.query(query_embeddings=[q_vec], n_results=min(k, collection.count()),
                              include=["distances", "documents", "metadatas"])
    hits = []
    for distance, text, meta in zip(result["distances"][0], result["documents"][0], result["metadatas"][0]):
        # у "hnsw:space": "cosine" Chroma возвращает расстояние = 1 − cos
        hits.append((1 - distance, {"file": meta["file"], "title": meta["title"], "text": text}))
    return hits


def numpy_by_vector(q_vec: list[float], k: int) -> list[tuple[float, dict]]:
    """То же самое на numpy-индексе (как rag.vector_search, но без вызова embed) — для сравнения."""
    index, matrix = get_index()
    if not index["chunks"]:
        return []
    q = np.asarray(q_vec, dtype=np.float32)
    scores = matrix @ (q / np.linalg.norm(q))
    top = np.argsort(-scores)[:k]
    return [(float(scores[i]), index["chunks"][i]) for i in top]


def chroma_search(query: str, k: int = 3) -> list[tuple[float, dict]]:
    """Top-k из Chroma в том же формате, что rag.vector_search. Запрос — той же моделью bge-m3."""
    return chroma_by_vector(embed([query])[0], k)


def chroma_search_docs(query: str, limit: int = 3) -> str:
    """Инструмент search_docs поверх Chroma: валидация, порог и формат вывода — общие с numpy-версией."""
    return search_docs(query, limit, searcher=chroma_search)


def compare() -> None:
    """Бонус: numpy vs Chroma на 10 вопросах из eval_search.py — совпадение top-3, косинусы, hit@k, время."""
    from eval_search import QUESTIONS, VECTOR_FILE_RE, hit_at_k

    get_collection()
    get_index()  # numpy-индекс тоже грузим заранее, чтобы его лог не разрывал таблицу
    print(f"Модель: {EMBED_MODEL}, size={CHUNK_SIZE}, overlap={CHUNK_OVERLAP}, MIN_SCORE={MIN_SCORE}\n")
    print("| N  | Вопрос | numpy top-3 (cos) | Chroma top-3 (cos) | top-3 совпал |")
    print("|----|--------|-------------------|--------------------|--------------|")
    same, max_diff = 0, 0.0
    t_numpy = t_chroma = 0.0
    for n, (question, _, _) in enumerate(QUESTIONS, start=1):
        q_vec = embed([question])[0]  # один эмбеддинг на оба поиска — сравниваем только сам поиск
        t0 = time.perf_counter()
        a = numpy_by_vector(q_vec, 3)
        t_numpy += time.perf_counter() - t0
        t0 = time.perf_counter()
        b = chroma_by_vector(q_vec, 3)
        t_chroma += time.perf_counter() - t0
        ids_a = [(c["file"], c["title"], c["text"]) for _, c in a]
        ids_b = [(c["file"], c["title"], c["text"]) for _, c in b]
        same += ids_a == ids_b
        max_diff = max([max_diff] + [abs(sa - sb) for (sa, _), (sb, _) in zip(a, b)])
        fmt = lambda hits: ", ".join(f"{c['file'].removesuffix('.md')} {s:.3f}" for s, c in hits)
        print(f"| {n:<2} | {question} | {fmt(a)} | {fmt(b)} | {'да' if ids_a == ids_b else '**нет**'} |")

    chroma_files = lambda q, k: VECTOR_FILE_RE.findall(chroma_search_docs(q, k))
    numpy_files = lambda q, k: VECTOR_FILE_RE.findall(search_docs(q, k))  # обычный векторный search_docs
    print(f"\ntop-3 совпал: {same}/{len(QUESTIONS)}; макс. |cos_numpy − cos_chroma| = {max_diff:.2e}")
    print(f"hit@1 / hit@3: numpy {hit_at_k(numpy_files, 1):.1f} / {hit_at_k(numpy_files, 3):.1f}; "
          f"Chroma {hit_at_k(chroma_files, 1):.1f} / {hit_at_k(chroma_files, 3):.1f}")
    print(f"время поиска без эмбеддинга запроса, всего на {len(QUESTIONS)} вопросов: "
          f"numpy {t_numpy * 1000:.1f} ms, Chroma {t_chroma * 1000:.1f} ms")
    size_kb = sum(f.stat().st_size for f in CHROMA_DIR.rglob("*") if f.is_file()) / 1024
    json_kb = (INDEX_DIR / "docs_index.json").stat().st_size / 1024
    print(f"на диске: JSON {json_kb:.0f} KB, Chroma {size_kb:.0f} KB ({CHROMA_DIR.relative_to(INDEX_DIR.parent.parent)})")


if __name__ == "__main__":
    compare()