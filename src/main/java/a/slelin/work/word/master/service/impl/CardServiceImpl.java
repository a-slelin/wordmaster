package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.dto.card.*;
import a.slelin.work.word.master.entity.Card;
import a.slelin.work.word.master.entity.CardProgress;
import a.slelin.work.word.master.entity.Deck;
import a.slelin.work.word.master.entity.Status;
import a.slelin.work.word.master.exception.AccessForbiddenException;
import a.slelin.work.word.master.exception.EntityNotFoundByIdException;
import a.slelin.work.word.master.mapper.card.CardMapper;
import a.slelin.work.word.master.repository.CardProgressRepository;
import a.slelin.work.word.master.repository.CardRepository;
import a.slelin.work.word.master.security.Actor;
import a.slelin.work.word.master.service.CardService;
import a.slelin.work.word.master.service.DeckService;
import a.slelin.work.word.master.service.logic.SpacedRepetition;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    private final CardProgressRepository cardProgressRepository;

    private final DeckService deckService;

    private final CardMapper cardMapper;

    @Override
    public List<CardWithProgressResponse> getCards(Actor actor, UUID deckId) {
        Deck deck = deckService.getReadableDeck(actor, deckId);
        Map<UUID, CardProgress> progress = cardProgressRepository.findByUser_IdAndCard_Deck_Id(actor.id(), deck.getId())
                .stream()
                .collect(Collectors.toMap(p -> p.getCard().getId(), Function.identity()));

        return cardRepository.findByDeckIdOrderByPositionAsc(deck.getId()).stream()
                .map(card -> cardMapper.toDto(card, progress.getOrDefault(card.getId(), newProgress())))
                .toList();
    }

    @Override
    public List<CardResponse> getPublicPreview(UUID deckId, int limit) {
        Deck deck = deckService.getReadableDeck(Actor.anonymous(), deckId);
        return cardRepository.findByDeckIdOrderByPositionAsc(deck.getId()).stream()
                .limit(limit)
                .map(cardMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CardResponse create(Actor actor, UUID deckId, CardCreateRequest request) {
        Deck deck = deckService.getEditableDeck(actor, deckId);
        Card card = build(deck, request, cardRepository.findMaxPosition(deck.getId()) + 1);
        touch(deck);
        return cardMapper.toDto(cardRepository.save(card));
    }

    @Override
    @Transactional
    public List<CardResponse> createBulk(Actor actor, UUID deckId,
                                         CardBulkCreateRequest request) {
        Deck deck = deckService.getEditableDeck(actor, deckId);
        long position = cardRepository.findMaxPosition(deck.getId());

        List<Card> cards = new ArrayList<>();
        for (CardCreateRequest item : request.cards()) {
            cards.add(build(deck, item, ++position));
        }

        touch(deck);
        return cardRepository.saveAll(cards).stream().map(cardMapper::toDto).toList();
    }

    @Override
    @Transactional
    public CardResponse update(Actor actor, UUID cardId, CardUpdateRequest request) {
        Card card = getEditableCard(actor, cardId);
        cardMapper.patch(card, request);
        card.setWord(card.getWord().trim());
        card.setTranslation(card.getTranslation().trim());
        touch(card.getDeck());
        return cardMapper.toDto(cardRepository.saveAndFlush(card));
    }

    @Override
    @Transactional
    public void delete(Actor actor, UUID cardId) {
        Card card = getEditableCard(actor, cardId);
        touch(card.getDeck());
        cardRepository.deleteCard(card.getId());
    }

    private Card getEditableCard(Actor actor, UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundByIdException(Card.class, cardId));

        if (!actor.canEdit(card.getDeck().getOwner().getId())) {
            throw new AccessForbiddenException("Only the owner of the deck can change its cards.");
        }

        return card;
    }

    private Card build(Deck deck, CardCreateRequest request, long defaultPosition) {
        Card card = cardMapper.toEntity(request);
        card.setDeck(deck);
        card.setWord(request.word().trim());
        card.setTranslation(request.translation().trim());
        card.setPosition(request.position() == null ? defaultPosition : request.position());
        return card;
    }

    /**
     * Marks the deck as recently changed (decks are sorted by update time).
     */
    private static void touch(Deck deck) {
        deck.setUpdatedAtNow();
    }

    /**
     * Progress of a card the user has never trained.
     */
    private static CardProgress newProgress() {
        return CardProgress.builder()
                .status(Status.NEW)
                .easeFactor(SpacedRepetition.DEFAULT_EASE_FACTOR)
                .intervalDays(0)
                .repetitions(0L)
                .correctCount(0L)
                .incorrectCount(0L)
                .build();
    }
}
