# Модель данных

## User
id, email, passwordHash, createdAt.

## Deck (колода)
id, title, description, ownerId (ссылка на User), isPublic, sourceDeckId (ссылка на оригинал, если колода — копия), createdAt.

## Card (карточка)
id, deckId (ссылка на Deck), word, translation, transcription, correctCount, incorrectCount, lastReviewedAt.

## Связи
Один пользователь — много колод, одна колода — много карточек. Копия публичной колоды — новая строка Deck с собственным набором Card.

## Хранение в агенте-тренажёре
Агент хранит карточки в файле workspace/cards.json. У каждой карточки: id, word, translation, topic, correct, wrong и last_review.
