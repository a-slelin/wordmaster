# agent/agent.py — Лаб. 3: сессии, бюджет контекста, память (на основе агента из лаб. 2)

import random
from datetime import date

import argparse
import json
import os
import re
from pathlib import Path

from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()

API_KEY = os.getenv("OPENROUTER_API_KEY")
MODEL = os.getenv("MODEL", "openai/gpt-4o-mini")

if not API_KEY:
    raise SystemExit("Не найден OPENROUTER_API_KEY. Проверьте файл agent/.env")

client = OpenAI(base_url="https://openrouter.ai/api/v1", api_key=API_KEY)

MAX_STEPS = 10  # максимум итераций агентного цикла на один вопрос
CONTEXT_BUDGET = int(os.getenv("CONTEXT_BUDGET", "6000"))  # бюджет в «условных токенах» estimate_tokens
STRATEGY = os.getenv("STRATEGY", "summarize").lower()  # trim | summarize

# Песочница: папка workspace рядом с agent.py (не зависит от того, откуда запускаем)
WORKSPACE = (Path(__file__).parent / "workspace").resolve()
WORKSPACE.mkdir(exist_ok=True)

SYSTEM_PROMPT = (
    "Ты — ассистент-агент для проекта пользователя. "
    "Файлы проекта лежат в рабочей папке, доступ к ним — только через инструменты. "
    "Если вопрос касается проекта, сначала посмотри список файлов, затем прочитай нужные. "
    "Не выдумывай содержимое файлов. Отвечай кратко, по-русски."
    "Ты также тренажёр слов WordMaster: по просьбе пользователя давай карточки, "
    "показывай только слово, проверяй его ответ (синонимы засчитывай) и сохраняй результат."
)

# ======================= СЕССИИ (lab3, шаг 1) =======================

# Краткосрочная память: история диалога хранится в agent/sessions/<id>.json
SESSIONS_DIR = (Path(__file__).parent / "sessions").resolve()
SESSIONS_DIR.mkdir(exist_ok=True)
SESSION_ID_RE = re.compile(r"^[A-Za-z0-9_-]{1,64}$")


def session_path(session_id: str) -> Path:
    """Путь к файлу сессии. Id проверяем, чтобы '../../.env' не вышел за sessions/."""
    if not SESSION_ID_RE.match(session_id):
        raise SystemExit(f"Некорректный id сессии '{session_id}': только буквы, цифры, '_' и '-'")
    return SESSIONS_DIR / f"{session_id}.json"


def save_session(session_id: str, messages: list) -> None:
    """Сохраняет историю БЕЗ system: промпт берётся из кода при каждом запуске."""
    path = session_path(session_id)
    data = [m for m in messages if m["role"] != "system" or is_memory(m)]  # память сохраняем
    tmp = path.with_suffix(".tmp")
    tmp.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    tmp.replace(path)  # атомарная замена: файл не останется «наполовину записанным»


def load_session(session_id: str) -> list:
    """Загружает историю и ставит свежий system-промпт первым (ровно один раз)."""
    path = session_path(session_id)
    history = []
    if path.exists():
        history = json.loads(path.read_text(encoding="utf-8"))
        history = [m for m in history if m["role"] != "system" or is_memory(m)]
        print(f"Сессия '{session_id}': загружено {len(history)} сообщений из {path.name}")
    else:
        print(f"Новая сессия '{session_id}' → {path.name}")
    return [{"role": "system", "content": SYSTEM_PROMPT}] + history


# ======================= ПЕСОЧНИЦА =======================

def safe_path(path: str) -> Path:
    """Превращает путь от модели в абсолютный и проверяет, что он внутри WORKSPACE."""
    full = (WORKSPACE / path).resolve()  # resolve() раскрывает '..' и симлинки
    if not full.is_relative_to(WORKSPACE):  # ← ЭТА СТРОКА не пускает за песочницу
        raise PermissionError(f"доступ запрещён: путь '{path}' вне рабочей папки")
    return full


# ======================= ИНСТРУМЕНТЫ =======================

def list_files(path: str = ".") -> str:
    """Список файлов и папок внутри рабочей папки."""
    folder = safe_path(path)
    if not folder.is_dir():
        raise NotADirectoryError(f"'{path}' не является папкой")
    items = []
    for p in sorted(folder.iterdir()):
        rel = p.relative_to(WORKSPACE).as_posix()
        items.append(rel + "/" if p.is_dir() else rel)
    return "\n".join(items) if items else "(папка пуста)"


def read_file(path: str) -> str:
    """Читает текстовый файл из рабочей папки."""
    file = safe_path(path)
    if not file.is_file():
        raise FileNotFoundError(f"файл '{path}' не найден")
    text = file.read_text(encoding="utf-8")
    limit = 20_000  # не раздуваем контекст огромными файлами
    if len(text) > limit:
        text = text[:limit] + f"\n...[обрезано, всего {len(text)} символов]"
    return text


def write_file(path: str, content: str) -> str:
    """Записывает файл в рабочую папку — только с подтверждения пользователя."""
    file = safe_path(path)
    print(f"\n  Агент хочет записать файл '{path}' ({len(content)} символов):")
    print("  " + content[:300].replace("\n", "\n  ") + ("..." if len(content) > 300 else ""))
    answer = input("  Разрешить? [y/N] ").strip().lower()
    if answer != "y":
        # Модель получит эту строку как результат инструмента
        return "ОТКАЗ: пользователь не разрешил запись файла."
    file.parent.mkdir(parents=True, exist_ok=True)
    file.write_text(content, encoding="utf-8")
    return f"Файл '{path}' записан ({len(content)} символов)."


# ======================= ИНСТРУМЕНТЫ WORDMASTER =======================

CARDS_FILE = WORKSPACE / "cards.json"
TOPICS = ["food", "travel", "irregular_verbs"]

# Стартовый набор карточек — создаётся, если cards.json ещё нет
SEED_CARDS = [
    {"id": 1, "word": "apple", "translation": "яблоко", "topic": "food"},
    {"id": 2, "word": "bread", "translation": "хлеб", "topic": "food"},
    {"id": 3, "word": "cheese", "translation": "сыр", "topic": "food"},
    {"id": 4, "word": "luggage", "translation": "багаж", "topic": "travel"},
    {"id": 5, "word": "departure", "translation": "отправление", "topic": "travel"},
    {"id": 6, "word": "ticket", "translation": "билет", "topic": "travel"},
    {"id": 7, "word": "go — went — gone", "translation": "идти", "topic": "irregular_verbs"},
    {"id": 8, "word": "take — took — taken", "translation": "брать", "topic": "irregular_verbs"},
    {"id": 9, "word": "write — wrote — written", "translation": "писать", "topic": "irregular_verbs"},
]


def load_cards() -> list:
    """Читает карточки из JSON; при первом запуске создаёт файл со стартовым набором."""
    if not CARDS_FILE.exists():
        cards = [dict(c, correct=0, wrong=0, last_review=None) for c in SEED_CARDS]
        save_cards(cards)
        return cards
    return json.loads(CARDS_FILE.read_text(encoding="utf-8"))


def save_cards(cards: list) -> None:
    CARDS_FILE.write_text(json.dumps(cards, ensure_ascii=False, indent=2), encoding="utf-8")


def get_random_card(topic: str = "any") -> str:
    """Случайная карточка по теме. Возвращает JSON-строку с id, словом и переводом."""
    cards = load_cards()
    if topic != "any":
        if topic not in TOPICS:
            raise ValueError(f"неизвестная тема '{topic}'. Доступны: {', '.join(TOPICS)}, any")
        cards = [c for c in cards if c["topic"] == topic]
    if not cards:
        raise LookupError(f"нет карточек по теме '{topic}'")
    card = random.choice(cards)
    # Короткий результат: только то, что нужно модели
    return json.dumps(
        {"card_id": card["id"], "word": card["word"],
         "translation": card["translation"], "topic": card["topic"]},
        ensure_ascii=False,
    )


def save_result(card_id: int, correct: bool) -> str:
    """Сохраняет результат ответа пользователя по карточке."""
    cards = load_cards()
    card = next((c for c in cards if c["id"] == card_id), None)
    if card is None:
        raise LookupError(f"карточка с id={card_id} не найдена")
    if correct:
        card["correct"] += 1
    else:
        card["wrong"] += 1
    card["last_review"] = date.today().isoformat()
    save_cards(cards)
    mark = "верно" if correct else "неверно"
    return (f"Сохранено: карточка #{card_id} '{card['word']}' — {mark}. "
            f"Всего: верно {card['correct']}, ошибок {card['wrong']}.")


# Описания инструментов для модели (JSON Schema). Модель видит только это,
# а не код функций: по name и description она решает, что и когда вызвать.
TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "list_files",
            "description": "Показать список файлов и папок в рабочей папке проекта. "
                           "Вызывай первым, чтобы узнать, какие файлы есть.",
            "parameters": {
                "type": "object",
                "properties": {
                    "path": {
                        "type": "string",
                        "description": "Путь к папке относительно рабочей папки. По умолчанию '.'",
                    }
                },
                "required": [],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "read_file",
            "description": "Прочитать содержимое текстового файла из рабочей папки.",
            "parameters": {
                "type": "object",
                "properties": {
                    "path": {
                        "type": "string",
                        "description": "Путь к файлу относительно рабочей папки, например 'README.md'",
                    }
                },
                "required": ["path"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "write_file",
            "description": "Создать или перезаписать текстовый файл в рабочей папке. "
                           "Пользователь должен подтвердить запись; если он откажет, "
                           "придёт строка 'ОТКАЗ: ...' — тогда не повторяй попытку, а сообщи об этом.",
            "parameters": {
                "type": "object",
                "properties": {
                    "path": {
                        "type": "string",
                        "description": "Путь к файлу относительно рабочей папки, например 'TODO.md'",
                    },
                    "content": {
                        "type": "string",
                        "description": "Полное содержимое файла",
                    },
                },
                "required": ["path", "content"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_random_card",
            "description": "Выдать случайную карточку для тренировки слов. Вызывай, когда пользователь "
                           "хочет потренироваться или просит слово. Пользователю показывай ТОЛЬКО слово, "
                           "перевод не раскрывай до его ответа.",
            "parameters": {
                "type": "object",
                "properties": {
                    "topic": {
                        "type": "string",
                        "enum": ["food", "travel", "irregular_verbs", "any"],
                        "description": "Тема карточки; 'any' — любая тема",
                    }
                },
                "required": ["topic"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "save_result",
            "description": "Сохранить результат ответа пользователя по карточке. Вызывай после того, "
                           "как пользователь ответил на карточку и ты сравнил ответ с переводом.",
            "parameters": {
                "type": "object",
                "properties": {
                    "card_id": {"type": "integer", "description": "id карточки из get_random_card"},
                    "correct": {"type": "boolean", "description": "true — ответ верный, false — ошибка"},
                },
                "required": ["card_id", "correct"],
            },
        },
    },
]

# Соответствие «имя инструмента → функция Python»
TOOL_FUNCTIONS = {
    "list_files": list_files,
    "read_file": read_file,
    "write_file": write_file,
    "get_random_card": get_random_card,
    "save_result": save_result,
}


# ======================= КОНТЕКСТ (lab3, шаг 2) =======================

def estimate_tokens(messages: list, tools: list | None = None) -> int:
    """Грубая оценка размера запроса: символы JSON // 4 (сообщения + описания tools)."""
    text = json.dumps(messages, ensure_ascii=False)
    if tools:
        text += json.dumps(tools, ensure_ascii=False)  # tools тоже уходят в каждый запрос
    return len(text) // 4


# ======================= СУММАРИЗАЦИЯ (lab3, шаг 4) =======================

MEMORY_PREFIX = "[память диалога]"

SUMMARY_PROMPT = (
    "Ты сжимаешь старую часть диалога пользователя с агентом-тренажёром WordMaster в краткую память. "
    "Ответь ОДНОЙ строкой до 80 слов, по-русски, строго в формате: "
    "'[память диалога] Пользователя зовут … Цель: … Ранее сделано: … Открытые задачи: …'. "
    "Чего нет в диалоге — пиши 'неизвестно'. Сохраняй имена, цели, числа, id карточек и принятые решения. "
    "Если дана прошлая память — объедини её с новым фрагментом, не теряя важного."
)


def is_memory(m: dict) -> bool:
    """Сообщение-память: system, которое начинается с [память диалога]."""
    return m["role"] == "system" and (m.get("content") or "").startswith(MEMORY_PREFIX)


def format_for_summary(msgs: list) -> str:
    """Превращает удаляемые сообщения в простой текст для суммаризатора."""
    lines = []
    for m in msgs:
        if m["role"] == "tool":
            lines.append(f"результат инструмента: {m['content'][:300]}")  # не раздуваем сам запрос
        elif m.get("tool_calls"):
            calls = ", ".join(f"{tc['function']['name']}({tc['function']['arguments']})"
                              for tc in m["tool_calls"])
            lines.append(f"агент вызвал: {calls}")
        else:
            lines.append(f"{m['role']}: {m['content']}")
    return "\n".join(lines)


def summarize(old_memory: str | None, dropped: list) -> str | None:
    """Отдельный короткий вызов модели БЕЗ tools. None — не получилось (тогда просто trim)."""
    text = format_for_summary(dropped)
    if old_memory:
        text = f"Прошлая память:\n{old_memory}\n\nНовый фрагмент диалога:\n{text}"
    try:
        response = client.chat.completions.create(
            model=MODEL,
            messages=[{"role": "system", "content": SUMMARY_PROMPT},
                      {"role": "user", "content": text}],
        )
        summary = (response.choices[0].message.content or "").strip()
    except Exception as e:
        print(f"  SUMMARY: ошибка вызова ({e}) → fallback на TRIM")
        return None
    if not summary:
        print("  SUMMARY: пустой ответ модели → fallback на TRIM")
        return None
    if not summary.startswith(MEMORY_PREFIX):
        summary = f"{MEMORY_PREFIX} {summary}"
    return summary


def split_turns(messages: list) -> tuple[list, list]:
    """Делит историю на «голову» (system-сообщения в начале) и список ходов.
    Ход = user + все assistant/tool после него до следующего user."""
    i = 0
    while i < len(messages) and messages[i]["role"] == "system":
        i += 1
    head, turns = messages[:i], []
    for m in messages[i:]:
        if m["role"] == "user" or not turns:
            turns.append([m])  # новый ход начинается с user
        else:
            turns[-1].append(m)  # assistant/tool — в текущий ход
    return head, turns


def ensure_context_budget(messages: list) -> None:
    """Если оценка > CONTEXT_BUDGET — убирает самые старые ходы ЦЕЛИКОМ.
    STRATEGY=trim — просто удаляет; STRATEGY=summarize — сжимает их в [память диалога].
    system, память и текущий (последний) ход не трогаем. Меняет список на месте."""
    before = estimate_tokens(messages, TOOLS)
    if before <= CONTEXT_BUDGET:
        return
    head, turns = split_turns(messages)
    flat = lambda ts: [m for t in ts for m in t]
    dropped = []
    removed_turns = 0
    while len(turns) > 1 and estimate_tokens(head + flat(turns), TOOLS) > CONTEXT_BUDGET:
        dropped += turns.pop(0)        # самый старый ход: user + его assistant/tool
        removed_turns += 1

    if STRATEGY == "summarize" and dropped:
        old_memory = next((m["content"] for m in head if is_memory(m)), None)
        summary = summarize(old_memory, dropped)
        if summary:
            head = [m for m in head if not is_memory(m)] + [{"role": "system", "content": summary}]
            print(f"  SUMMARY: {removed_turns} ход(ов) / {len(dropped)} сообщений → "
                  f"память ~{len(summary) // 4} ток.: {summary}")

    messages[:] = head + flat(turns)   # [:] — меняем тот же список, что сохраняет сессия
    after = estimate_tokens(messages, TOOLS)
    print(f"  TRIM: removed {removed_turns} ход(ов) / {len(dropped)} сообщений, "
          f"~{before} → ~{after} ток. (бюджет {CONTEXT_BUDGET}, стратегия {STRATEGY})")
    if after > CONTEXT_BUDGET:
        print(f"  TRIM: после обрезки всё ещё больше бюджета (~{after}), отправляем как есть")


# ======================= TODO 1: execute_tool =======================

def execute_tool(name: str, raw_args: str) -> str:
    """Выполняет инструмент по имени. Любая ошибка возвращается СТРОКОЙ, а не исключением:
    так модель увидит, что пошло не так, и сможет отреагировать, а программа не упадёт."""
    try:
        args = json.loads(raw_args or "{}")  # модель присылает аргументы JSON-строкой
        if name not in TOOL_FUNCTIONS:
            return f"ОШИБКА: неизвестный инструмент '{name}'"
        result = TOOL_FUNCTIONS[name](**args)  # **args: {"path": "x"} → path="x"
        return str(result)
    except Exception as e:
        return f"ОШИБКА: {e}"


# ======================= TODO 2: run_agent =======================

def run_agent(messages: list, on_step=None) -> str:
    """Агентный цикл: модель → (инструменты → модель)* → финальный ответ."""
    for step in range(1, MAX_STEPS + 1):
        ensure_context_budget(messages)  # перед КАЖДЫМ запросом: tool-результаты растут внутри цикла
        estimate = estimate_tokens(messages, TOOLS)
        print(f"\n[шаг {step}] оценка до запроса: ~{estimate} ток. ({len(messages)} сообщений)")
        # 1. Запрос к модели с описаниями инструментов
        response = client.chat.completions.create(
            model=MODEL,
            messages=messages,
            tools=TOOLS,
        )
        msg = response.choices[0].message
        usage = response.usage
        if usage and usage.prompt_tokens:
            print(f"[шаг {step}] usage: prompt={usage.prompt_tokens}, completion={usage.completion_tokens}, "
                  f"оценка/факт = {estimate / usage.prompt_tokens:.2f}")
        else:
            print(f"[шаг {step}] usage: провайдер не вернул данные")

        # 2. Сохраняем ответ ассистента в историю (вместе с tool_calls, если есть)
        assistant_msg = {"role": "assistant", "content": msg.content or ""}
        if msg.tool_calls:
            assistant_msg["tool_calls"] = [
                {
                    "id": tc.id,
                    "type": "function",
                    "function": {"name": tc.function.name, "arguments": tc.function.arguments},
                }
                for tc in msg.tool_calls
            ]
        messages.append(assistant_msg)

        # 3. Нет вызовов инструментов → это финальный ответ
        if not msg.tool_calls:
            return msg.content or ""

        # 4. Иначе выполняем каждый вызов и кладём результат в историю
        for tc in msg.tool_calls:
            print(f"  → {tc.function.name}({tc.function.arguments})")
            result = execute_tool(tc.function.name, tc.function.arguments)
            preview = result[:150].replace("\n", " ")
            print(f"  ← {preview}{'...' if len(result) > 150 else ''}")
            messages.append({
                "role": "tool",
                "tool_call_id": tc.id,  # связывает результат с конкретным вызовом
                "content": result,
            })
        if on_step:
            on_step()  # сохраняем сессию, когда ВСЕ результаты tool уже в истории
        # и снова идём к модели — уже с результатами инструментов

    return f"Достигнут лимит шагов ({MAX_STEPS}), задача не завершена."


# ======================= ЧАТ =======================

def main():
    parser = argparse.ArgumentParser(description="Агент WordMaster")
    parser.add_argument("--session", default=os.getenv("SESSION_ID", "default"),
                        help="id сессии (файл agent/sessions/<id>.json)")
    session_id = parser.parse_args().session

    messages = load_session(session_id)
    save = lambda: save_session(session_id, messages)

    print(f"Агент запущен. Рабочая папка: {WORKSPACE}")
    print("Для выхода введите 'exit' или нажмите Ctrl+C.")

    while True:
        try:
            question = input("\nВы: ").strip()
        except (KeyboardInterrupt, EOFError):
            break
        if question.lower() in ("exit", "quit", "выход"):
            break
        if not question:
            continue

        messages.append({"role": "user", "content": question})
        save()  # вопрос не потеряется, даже если API упадёт
        answer = run_agent(messages, on_step=save)  # run_agent сам дописывает историю
        save()  # финальный ответ ассистента
        print(f"\nАгент: {answer.strip()}")

    print(f"Сессия сохранена: {session_path(session_id)}")
    print("Пока!")


if __name__ == "__main__":
    main()
