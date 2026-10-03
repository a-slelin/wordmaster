package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.dto.stats.AchievementResponse;
import a.slelin.work.word.master.dto.training.*;
import a.slelin.work.word.master.entity.*;
import a.slelin.work.word.master.exception.BusinessFault;
import a.slelin.work.word.master.exception.EntityNotFoundByIdException;
import a.slelin.work.word.master.mapper.common.StandardEnumMapper;
import a.slelin.work.word.master.mapper.common.StatusMapper;
import a.slelin.work.word.master.mapper.training.TrainingMapper;
import a.slelin.work.word.master.repository.*;
import a.slelin.work.word.master.security.Actor;
import a.slelin.work.word.master.service.DeckService;
import a.slelin.work.word.master.service.GamificationService;
import a.slelin.work.word.master.service.TrainingService;
import a.slelin.work.word.master.service.logic.AnswerChecker;
import a.slelin.work.word.master.service.logic.Levels;
import a.slelin.work.word.master.service.logic.SpacedRepetition;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingServiceImpl implements TrainingService {

    public static final int DEFAULT_LIMIT = 20;

    /**
     * How many times a forgotten card is repeated within one session before it is left for the next one.
     */
    public static final int MAX_ATTEMPTS = 3;

    public static final int CHOICE_OPTIONS = 4;

    private final DeckService deckService;

    private final GamificationService gamificationService;

    private final CardRepository cardRepository;

    private final CardProgressRepository cardProgressRepository;

    private final TrainingSessionRepository sessionRepository;

    private final TrainingSessionCardRepository sessionCardRepository;

    private final TrainingAnswerRepository answerRepository;

    private final UserRepository userRepository;

    private final TrainingMapper trainingMapper;

    private final StatusMapper statusMapper;

    private final StandardEnumMapper enumMapper;

    private final Clock clock;

    private final Random random = new Random();

    @Override
    @Transactional
    public TrainingSessionStartResponse start(Actor actor, TrainingSessionStartRequest request) {
        Deck deck = deckService.getReadableDeck(actor, parseId(request.deckId(), Deck.class));

        TrainingMode mode = parse(request.mode(), TrainingMode.FLASHCARDS, TrainingMode::of);
        TrainingDirection direction = mode == TrainingMode.LISTENING ? TrainingDirection.FORWARD
                : parse(request.direction(), TrainingDirection.FORWARD, TrainingDirection::of);
        TrainingScope scope = parse(request.scope(), TrainingScope.SMART, TrainingScope::of);
        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();

        if (cardRepository.countByDeckId(deck.getId()) == 0) {
            throw new BusinessFault("The deck has no cards yet.");
        }

        List<Card> plan = selectCards(actor.id(), deck.getId(), scope, limit);
        LocalDateTime now = LocalDateTime.now(clock);

        TrainingSession session = sessionRepository.save(TrainingSession.builder()
                .user(userRepository.getReferenceById(actor.id()))
                .deck(deck)
                .mode(mode)
                .direction(direction)
                .startedAt(now)
                .cardsTotal((long) plan.size())
                .cardsCorrect(0L)
                .answersTotal(0L)
                .xpEarned(0L)
                .build());

        List<TrainingSessionCard> rows = new ArrayList<>();
        for (int i = 0; i < plan.size(); i++) {
            rows.add(TrainingSessionCard.builder()
                    .session(session)
                    .card(plan.get(i))
                    .position((long) i + 1)
                    .done(false)
                    .attempts(0)
                    .build());
        }
        sessionCardRepository.saveAllAndFlush(rows);

        return trainingMapper.toStartDto(session, buildNext(session).orElse(null));
    }

    @Override
    public Optional<TrainingNextCardResponse> next(Actor actor, UUID sessionId) {
        TrainingSession session = getSession(actor, sessionId);
        return session.getFinishedAt() == null ? buildNext(session) : Optional.empty();
    }

    @Override
    @Transactional
    public TrainingAnswerResponse answer(Actor actor, UUID sessionId,
                                         TrainingAnswerRequest request) {
        TrainingSession session = getSession(actor, sessionId);
        if (session.getFinishedAt() != null) {
            throw new BusinessFault("The training session is already finished.");
        }

        UUID cardId = parseId(request.cardId(), Card.class);
        TrainingSessionCard row = sessionCardRepository.findBySession_IdAndCard_Id(sessionId, cardId)
                .orElseThrow(() -> new EntityNotFoundByIdException(Card.class, cardId));
        if (Boolean.TRUE.equals(row.getDone())) {
            throw new BusinessFault("The card is already answered in this session.");
        }

        Card card = row.getCard();
        String expected = expectedAnswer(session, card);
        Grade grade = evaluate(session.getMode(), expected, request);
        boolean correct = SpacedRepetition.isCorrect(grade);
        LocalDateTime now = LocalDateTime.now(clock);

        CardProgress progress = cardProgressRepository.findByUser_IdAndCard_Id(actor.id(), cardId)
                .orElseGet(() -> CardProgress.builder()
                        .user(userRepository.getReferenceById(actor.id()))
                        .card(card)
                        .status(Status.NEW)
                        .easeFactor(SpacedRepetition.DEFAULT_EASE_FACTOR)
                        .intervalDays(0)
                        .repetitions(0L)
                        .correctCount(0L)
                        .incorrectCount(0L)
                        .build());
        SpacedRepetition.apply(progress, grade, now);
        progress = cardProgressRepository.save(progress);

        answerRepository.save(TrainingAnswer.builder()
                .session(session)
                .card(card)
                .grade(grade)
                .isCorrect(correct)
                .userAnswer(request.answer())
                .answeredAt(now)
                .build());

        row.setAttempts(row.getAttempts() + 1);
        if (!correct && row.getAttempts() < MAX_ATTEMPTS) {
            row.setPosition(sessionCardRepository.findMaxPosition(sessionId) + 1);
        } else {
            row.setDone(true);
        }
        sessionCardRepository.saveAndFlush(row);

        long xp = Levels.answerXp(grade, session.getMode());
        session.setAnswersTotal(session.getAnswersTotal() + 1);
        session.setXpEarned(session.getXpEarned() + xp);
        if (correct && row.getAttempts() == 1) {
            session.setCardsCorrect(session.getCardsCorrect() + 1);
        }

        GamificationService.ActivityResult activity = gamificationService.registerActivity(actor.id(), 1, xp);
        Optional<TrainingNextCardResponse> next = buildNext(session);

        return TrainingAnswerResponse.builder()
                .cardId(cardId.toString())
                .correct(correct)
                .grade(enumMapper.toDisplayName(grade))
                .correctAnswer(expected)
                .userAnswer(request.answer())
                .updatedProgress(trainingMapper.toDto(progress))
                .xpEarned(activity.xpGained())
                .finished(next.isEmpty())
                .nextCard(next.orElse(null))
                .build();
    }

    @Override
    @Transactional
    public TrainingSessionFinishResponse finish(Actor actor, UUID sessionId) {
        TrainingSession session = getSession(actor, sessionId);
        List<AchievementResponse> newAchievements = List.of();

        if (session.getFinishedAt() == null) {
            session.setFinishedAt(LocalDateTime.now(clock));

            long bonus = 0;
            if (session.getAnswersTotal() > 0) {
                bonus += Levels.SESSION_COMPLETE_BONUS;
            }
            boolean allDone = sessionCardRepository.countBySession_IdAndDoneFalse(sessionId) == 0;
            if (allDone && session.getCardsTotal() >= Levels.PERFECT_SESSION_MIN_CARDS
                    && Objects.equals(session.getCardsCorrect(), session.getCardsTotal())) {
                bonus += Levels.PERFECT_SESSION_BONUS;
            }

            if (bonus > 0) {
                long gained = gamificationService.registerActivity(actor.id(), 0, bonus).xpGained();
                session.setXpEarned(session.getXpEarned() + gained);
            }

            sessionRepository.saveAndFlush(session);
            newAchievements = gamificationService.checkAchievements(actor.id());
        }

        UserStats stats = gamificationService.getOrCreate(actor.id());
        int todayReviewed = gamificationService.todayReviewed(actor.id());

        return TrainingSessionFinishResponse.builder()
                .sessionId(session.getId().toString())
                .deckId(session.getDeck().getId().toString())
                .cardsTotal(session.getCardsTotal())
                .cardsCorrect(session.getCardsCorrect())
                .answersTotal(session.getAnswersTotal())
                .accuracyPercent(trainingMapper.calculateAccuracyPercent(
                        session.getCardsCorrect(), Math.max(1, sessionCardRepository.countBySession_IdAndDoneTrue(sessionId))))
                .durationSeconds(trainingMapper.calculateDurationSeconds(session))
                .xpEarned(session.getXpEarned())
                .totalXp(stats.getXp())
                .level(Levels.level(stats.getXp()))
                .currentStreak(gamificationService.effectiveStreak(stats))
                .streakExtended(session.getAnswersTotal() > 0 && todayReviewed == session.getAnswersTotal())
                .dailyGoal(stats.getDailyGoal())
                .todayReviewed(todayReviewed)
                .newAchievements(newAchievements)
                .build();
    }

    private TrainingSession getSession(Actor actor, UUID sessionId) {
        return sessionRepository.findByIdAndUser_Id(sessionId, actor.id())
                .orElseThrow(() -> new EntityNotFoundByIdException(TrainingSession.class, sessionId));
    }

    /**
     * Cards of a new session according to the scope.
     */
    private List<Card> selectCards(UUID userId, UUID deckId, TrainingScope scope, int limit) {
        List<Card> result = switch (scope) {
            case ALL -> {
                List<Card> all = new ArrayList<>(cardRepository.findByDeckIdOrderByPositionAsc(deckId));
                Collections.shuffle(all, random);
                yield all.subList(0, Math.min(limit, all.size()));
            }
            case HARD -> cardRepository.findHardCards(deckId, userId, PageRequest.of(0, limit));
            case SMART -> List.of();
        };

        if (!result.isEmpty()) {
            return result;
        }

        List<Card> plan = new ArrayList<>(cardRepository.findDueCards(deckId, userId,
                LocalDateTime.now(clock), PageRequest.of(0, limit)));
        Collections.shuffle(plan, random);

        if (plan.size() < limit) {
            plan.addAll(cardRepository.findNewCards(deckId, userId, PageRequest.of(0, limit - plan.size())));
        }

        if (plan.isEmpty()) {
            plan.addAll(cardRepository.findUpcomingCards(deckId, userId, PageRequest.of(0, limit)));
        }

        return plan;
    }

    private Optional<TrainingNextCardResponse> buildNext(TrainingSession session) {
        Optional<TrainingSessionCard> row = sessionCardRepository
                .findFirstBySession_IdAndDoneFalseOrderByPositionAsc(session.getId());
        if (row.isEmpty()) {
            return Optional.empty();
        }

        Card card = row.get().getCard();
        Deck deck = session.getDeck();
        TrainingMode mode = session.getMode();
        boolean forward = session.getDirection() == TrainingDirection.FORWARD;
        String targetLanguage = deck.getTargetLanguage().getCode();
        String sourceLanguage = deck.getSourceLanguage().getCode();

        String prompt = mode == TrainingMode.LISTENING || forward ? card.getWord() : card.getTranslation();
        String expected = expectedAnswer(session, card);

        Status status = cardProgressRepository.findByUser_IdAndCard_Id(session.getUser().getId(), card.getId())
                .map(CardProgress::getStatus)
                .orElse(Status.NEW);

        long remaining = sessionCardRepository.countBySession_IdAndDoneFalse(session.getId());
        long done = sessionCardRepository.countBySession_IdAndDoneTrue(session.getId());

        return Optional.of(TrainingNextCardResponse.builder()
                .sessionId(session.getId().toString())
                .cardId(card.getId().toString())
                .mode(enumMapper.toDisplayName(mode))
                .direction(enumMapper.toDisplayName(session.getDirection()))
                .prompt(prompt)
                .promptLanguage(mode == TrainingMode.LISTENING || forward ? targetLanguage : sourceLanguage)
                .answerLanguage(mode == TrainingMode.LISTENING || !forward ? targetLanguage : sourceLanguage)
                .transcription(forward || mode == TrainingMode.LISTENING ? card.getTranscription() : null)
                .exampleSentence(card.getExampleSentence())
                .imageUrl(card.getImageUrl())
                .audioUrl(card.getAudioUrl())
                .answer(mode == TrainingMode.FLASHCARDS ? expected : null)
                .options(mode == TrainingMode.CHOICE ? options(session, card, expected) : null)
                .status(statusMapper.toDisplayName(status))
                .position(done + 1)
                .cardsRemaining(remaining)
                .cardsTotal(session.getCardsTotal())
                .build());
    }

    /**
     * Correct answer plus up to three distractors from the same deck in random order.
     */
    private List<String> options(TrainingSession session, Card card, String expected) {
        Set<String> seen = new HashSet<>();
        seen.add(expected.trim().toLowerCase(Locale.ROOT));

        List<Card> others = new ArrayList<>(cardRepository.findByDeckIdOrderByPositionAsc(session.getDeck().getId()));
        Collections.shuffle(others, random);

        List<String> options = new ArrayList<>();
        options.add(expected);
        for (Card other : others) {
            if (options.size() >= CHOICE_OPTIONS) {
                break;
            }
            if (other.getId().equals(card.getId())) {
                continue;
            }
            String option = expectedAnswer(session, other);
            if (seen.add(option.trim().toLowerCase(Locale.ROOT))) {
                options.add(option);
            }
        }

        Collections.shuffle(options, random);
        return options;
    }

    private static String expectedAnswer(TrainingSession session, Card card) {
        if (session.getMode() == TrainingMode.LISTENING) {
            return card.getWord();
        }

        return session.getDirection() == TrainingDirection.FORWARD ? card.getTranslation() : card.getWord();
    }

    private static Grade evaluate(TrainingMode mode, String expected, TrainingAnswerRequest request) {
        return switch (mode) {
            case FLASHCARDS -> {
                if (request.grade() == null) {
                    throw new BusinessFault("Grade is required in flashcards mode.");
                }
                yield parse(request.grade(), null, Grade::of);
            }
            case CHOICE -> request.answer() != null
                    && request.answer().trim().equalsIgnoreCase(expected.trim()) ? Grade.GOOD : Grade.AGAIN;
            case TYPING, LISTENING -> switch (AnswerChecker.check(expected, request.answer())) {
                case EXACT -> Grade.GOOD;
                case TYPO -> Grade.HARD;
                case WRONG -> Grade.AGAIN;
            };
        };
    }

    private static <E> E parse(String value, E defaultValue, java.util.function.Function<String, E> parser) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return parser.apply(value);
        } catch (IllegalArgumentException e) {
            throw new BusinessFault("Unknown value '%s'.".formatted(value));
        }
    }

    private static UUID parseId(String value, Class<?> entity) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundByIdException(entity, value);
        }
    }
}
