import os

from dotenv import load_dotenv  # читает файл .env
from openai import OpenAI  # клиент OpenAI-совместимого API

# 1. Загружаем переменные из .env в окружение процесса
load_dotenv()

API_KEY = os.getenv("OPENROUTER_API_KEY")
MODEL = os.getenv("MODEL")  # второй аргумент — значение по умолчанию

if not API_KEY:
    raise SystemExit("Не найден OPENROUTER_API_KEY. Проверьте файл agent/.env")

# 2. Создаём клиента. Библиотека openai, но base_url указывает на OpenRouter,
#    поэтому запросы уходят туда (API у них совместимый)
client = OpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=API_KEY,
)

# 3. История сообщений. Пока одно сообщение от пользователя.
#    role: "system" — инструкции, "user" — пользователь, "assistant" — модель
messages = [
    {"role": "user", "content": "Что такое токен?"},
]

# 4. Один запрос к модели
response = client.chat.completions.create(
    model=MODEL,
    messages=messages,
)

# 5. Разбираем ответ.
#    choices — список вариантов ответа; по умолчанию он один, поэтому [0]
choice = response.choices[0]

print("=== content ===")
print(choice.message.content)  # текст ответа модели

print("\n=== finish_reason ===")
print(choice.finish_reason)  # stop — модель закончила сама,
# length — упёрлась в лимит токенов

print("\n=== usage ===")
usage = response.usage
print(f"prompt_tokens (вход):      {usage.prompt_tokens}")
print(f"completion_tokens (выход): {usage.completion_tokens}")
print(f"total_tokens:              {usage.total_tokens}")
