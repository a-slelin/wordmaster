# agent/agent.py — Шаг 2. Чат с историей

import os

from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()

API_KEY = os.getenv("OPENROUTER_API_KEY")
MODEL = os.getenv("MODEL")

if not API_KEY:
    raise SystemExit("Не найден OPENROUTER_API_KEY. Проверьте файл agent/.env")

client = OpenAI(base_url="https://openrouter.ai/api/v1", api_key=API_KEY)

# Системный промпт — инструкция для модели, всегда первый в истории
SYSTEM_PROMPT = "Ты — дружелюбный помощник. Отвечай кратко, по-русски."

# Вся «память» модели — это этот список. Сама модель ничего не помнит
# между запросами: каждый раз мы отправляем ей всю переписку заново.
messages = [
    {"role": "system", "content": SYSTEM_PROMPT},
]

print("Чат запущен. Для выхода введите 'exit' или нажмите Ctrl+C.")

while True:
    try:
        question = input("\nВы: ").strip()
    except (KeyboardInterrupt, EOFError):   # Ctrl+C / Ctrl+Z — выходим без трейсбека
        break

    if question.lower() in ("exit", "quit", "выход"):
        break
    if not question:                         # пустую строку не отправляем
        continue

    user_msg = {"role": "user", "content": question}

    # В запрос уходит вся история + новый вопрос
    response = client.chat.completions.create(
        model=MODEL,
        messages=messages + [user_msg],
    )

    answer = response.choices[0].message.content or ""   # content может быть None
    print(f"\nМодель: {answer.strip()}")

    usage = response.usage
    print(f"[вход: {usage.prompt_tokens}, выход: {usage.completion_tokens}]")

    # Сохраняем вопрос и ответ в историю.
    # ПРОВЕРКА «ЗАБЫВАНИЯ»: закомментируйте эти две строки
    messages.append(user_msg)
    messages.append({"role": "assistant", "content": answer})

print("Пока!")