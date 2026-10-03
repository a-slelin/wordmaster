package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.dto.deck.*;
import a.slelin.work.word.master.dto.general.PageResponse;
import a.slelin.work.word.master.dto.general.SheetResponse;
import a.slelin.work.word.master.entity.*;
import a.slelin.work.word.master.exception.AccessForbiddenException;
import a.slelin.work.word.master.exception.BusinessFault;
import a.slelin.work.word.master.exception.EntityNotFoundByIdException;
import a.slelin.work.word.master.mapper.card.CardMapper;
import a.slelin.work.word.master.mapper.deck.DeckMapper;
import a.slelin.work.word.master.repository.*;
import a.slelin.work.word.master.repository.projection.DeckCount;
import a.slelin.work.word.master.repository.projection.DeckStatusCount;
import a.slelin.work.word.master.security.Actor;
import a.slelin.work.word.master.service.DeckService;
import a.slelin.work.word.master.service.GamificationService;
import a.slelin.work.word.master.service.LanguageService;
import a.slelin.work.word.master.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeckServiceImpl implements DeckService {

    public static final String DEFAULT_ICON = "📚";

    public static final String DEFAULT_COLOR = "violet";

    private final DeckRepository deckRepository;

    private final CardRepository cardRepository;

    private final CardProgressRepository cardProgressRepository;

    private final DeckLikesRepository deckLikesRepository;

    private final UserRepository userRepository;

    private final LanguageService languageService;

    private final TagService tagService;

    private final GamificationService gamificationService;

    private final DeckMapper deckMapper;

    private final CardMapper cardMapper;

    private final Clock clock;

    @Override
    public SheetResponse<DeckSummaryResponse> getMyDecks(Actor actor, String search,
                                                         Pageable pageable) {
        Specification<Deck> spec = allOf(
                DeckSpecifications.ownedBy(actor.id()),
                DeckSpecifications.titleOrDescriptionContains(search));

        Pageable sorted = pageable.getSort().isSorted() ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Deck> page = deckRepository.findAll(spec, sorted);
        List<UUID> ids = page.getContent().stream().map(Deck::getId).toList();

        Map<UUID, Long> cards = toMap(ids.isEmpty() ? List.of() : cardRepository.countByDeckIds(ids));
        Map<UUID, Long> due = toMap(ids.isEmpty() ? List.of()
                : cardProgressRepository.countDue(actor.id(), ids, LocalDateTime.now(clock)));
        Map<UUID, Long> known = new HashMap<>();
        if (!ids.isEmpty()) {
            for (DeckStatusCount count : cardProgressRepository.countByStatus(actor.id(), ids)) {
                if (count.status() == Status.KNOWN) {
                    known.put(count.deckId(), count.count());
                }
            }
        }

        List<DeckSummaryResponse> content = page.getContent().stream()
                .map(deck -> deckMapper.toSummaryDto(deck,
                        cards.getOrDefault(deck.getId(), 0L),
                        due.getOrDefault(deck.getId(), 0L),
                        known.getOrDefault(deck.getId(), 0L)))
                .toList();

        return new SheetResponse<>(content, PageResponse.of(page));
    }

    @Override
    public SheetResponse<PublicDeckCatalogItemResponse> getCatalog(Actor actor,
                                                                   DeckCatalogFilter filter,
                                                                   Pageable pageable) {
        Specification<Deck> spec = allOf(
                DeckSpecifications.isPublic(),
                DeckSpecifications.titleOrDescriptionContains(filter.search()),
                DeckSpecifications.targetLanguage(filter.languageId()),
                DeckSpecifications.hasTag(filter.tagId()),
                DeckSpecifications.officialOnly(filter.official()));

        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), catalogSort(filter.sort()));
        Page<Deck> page = deckRepository.findAll(spec, sorted);
        List<UUID> ids = page.getContent().stream().map(Deck::getId).toList();

        Map<UUID, Long> cards = toMap(ids.isEmpty() ? List.of() : cardRepository.countByDeckIds(ids));
        Set<UUID> liked = actor.isAnonymous() || ids.isEmpty() ? Set.of()
                : deckLikesRepository.findLikedDeckIds(actor.id(), ids);

        List<PublicDeckCatalogItemResponse> content = page.getContent().stream()
                .map(deck -> deckMapper.toCatalogDto(deck,
                        cards.getOrDefault(deck.getId(), 0L),
                        isOwner(actor, deck),
                        liked.contains(deck.getId())))
                .toList();

        return new SheetResponse<>(content, PageResponse.of(page));
    }

    @Override
    public DeckResponse getById(Actor actor, UUID deckId) {
        return toDto(actor, getReadableDeck(actor, deckId));
    }

    @Override
    @Transactional
    public DeckResponse create(Actor actor, DeckCreateRequest request) {
        Deck deck = deckMapper.toEntity(request);
        deck.setTitle(request.title().trim());
        deck.setOwner(userRepository.getReferenceById(actor.id()));
        deck.setSourceLanguage(languageService.getEntityById(request.sourceLanguageId()));
        deck.setTargetLanguage(languageService.getEntityById(request.targetLanguageId()));
        deck.setTags(tagService.getEntitiesByIds(request.tagIds()));
        deck.setIcon(request.icon() == null || request.icon().isBlank() ? DEFAULT_ICON : request.icon());
        deck.setColor(request.color() == null || request.color().isBlank() ? DEFAULT_COLOR : request.color());

        Deck saved = deckRepository.save(deck);
        gamificationService.checkAchievements(actor.id());
        return toDto(actor, saved);
    }

    @Override
    @Transactional
    public DeckResponse update(Actor actor, UUID deckId, DeckUpdateRequest request) {
        Deck deck = getEditableDeck(actor, deckId);
        deckMapper.patch(deck, request);

        if (request.title() != null) {
            deck.setTitle(request.title().trim());
        }
        if (request.sourceLanguageId() != null) {
            deck.setSourceLanguage(languageService.getEntityById(request.sourceLanguageId()));
        }
        if (request.targetLanguageId() != null) {
            deck.setTargetLanguage(languageService.getEntityById(request.targetLanguageId()));
        }
        if (request.tagIds() != null) {
            deck.setTags(tagService.getEntitiesByIds(request.tagIds()));
        }

        deckRepository.saveAndFlush(deck);
        if (Boolean.TRUE.equals(request.isPublic())) {
            gamificationService.checkAchievements(actor.id());
        }
        return toDto(actor, deck);
    }

    @Override
    @Transactional
    public void delete(Actor actor, UUID deckId) {
        deckRepository.deleteDeck(getEditableDeck(actor, deckId).getId());
    }

    @Override
    @Transactional
    public DeckCopyResponse copy(Actor actor, UUID deckId) {
        Deck source = getReadableDeck(actor, deckId);
        if (isOwner(actor, source)) {
            throw new BusinessFault("You already own this deck.");
        }

        Deck copy = Deck.builder()
                .owner(userRepository.getReferenceById(actor.id()))
                .sourceDeck(source)
                .title(source.getTitle())
                .description(source.getDescription())
                .icon(source.getIcon())
                .color(source.getColor())
                .sourceLanguage(source.getSourceLanguage())
                .targetLanguage(source.getTargetLanguage())
                .isPublic(false)
                .isOfficial(false)
                .likesCount(0L)
                .copiesCount(0L)
                .tags(new ArrayList<>(source.getTags()))
                .build();
        Deck saved = deckRepository.save(copy);

        List<Card> cards = cardRepository.findByDeckIdOrderByPositionAsc(source.getId()).stream()
                .map(card -> {
                    Card copied = cardMapper.copy(card);
                    copied.setDeck(saved);
                    return copied;
                })
                .toList();
        cardRepository.saveAll(cards);
        deckRepository.incrementCopiesCount(source.getId());

        gamificationService.checkAchievements(actor.id());
        return DeckCopyResponse.builder()
                .newDeckId(saved.getId().toString())
                .sourceDeckId(source.getId().toString())
                .cardsCopiedCount((long) cards.size())
                .build();
    }

    @Override
    @Transactional
    public DeckLikeResponse like(Actor actor, UUID deckId) {
        Deck deck = getReadableDeck(actor, deckId);
        if (!Boolean.TRUE.equals(deck.getIsPublic())) {
            throw new BusinessFault("Only public decks can be liked.");
        }

        if (deckLikesRepository.insert(actor.id(), deckId) > 0) {
            deckRepository.changeLikesCount(deckId, 1);
            gamificationService.checkAchievements(actor.id());
        }

        return deckMapper.toLikeDto(deckId, true, deckRepository.findLikesCount(deckId));
    }

    @Override
    @Transactional
    public DeckLikeResponse unlike(Actor actor, UUID deckId) {
        getReadableDeck(actor, deckId);
        if (deckLikesRepository.deleteByUserAndDeck(actor.id(), deckId) > 0) {
            deckRepository.changeLikesCount(deckId, -1);
        }

        return deckMapper.toLikeDto(deckId, false, deckRepository.findLikesCount(deckId));
    }

    @Override
    public DeckProgressResponse getProgress(Actor actor, UUID deckId) {
        return progress(actor, getReadableDeck(actor, deckId).getId());
    }

    @Override
    public Deck getReadableDeck(Actor actor, UUID deckId) {
        Deck deck = findDeck(deckId);
        if (Boolean.TRUE.equals(deck.getIsPublic()) || actor.canEdit(deck.getOwner().getId())) {
            return deck;
        }

        throw new EntityNotFoundByIdException(Deck.class, deckId);
    }

    @Override
    public Deck getEditableDeck(Actor actor, UUID deckId) {
        Deck deck = getReadableDeck(actor, deckId);
        if (!actor.canEdit(deck.getOwner().getId())) {
            throw new AccessForbiddenException("Only the owner can change this deck.");
        }

        return deck;
    }

    private Deck findDeck(UUID deckId) {
        return deckRepository.findById(deckId)
                .orElseThrow(() -> new EntityNotFoundByIdException(Deck.class, deckId));
    }

    private DeckResponse toDto(Actor actor, Deck deck) {
        boolean liked = !actor.isAnonymous() && deckLikesRepository.existsByUserAndDeck(actor.id(), deck.getId());
        DeckProgressResponse progress = actor.isAnonymous() ? null : progress(actor, deck.getId());
        return deckMapper.toDto(deck, cardRepository.countByDeckId(deck.getId()), isOwner(actor, deck), liked, progress);
    }

    private DeckProgressResponse progress(Actor actor, UUID deckId) {
        long total = cardRepository.countByDeckId(deckId);
        long learning = cardProgressRepository.countByCard_Deck_IdAndUser_IdAndStatus(deckId, actor.id(), Status.LEARNING);
        long known = cardProgressRepository.countByCard_Deck_IdAndUser_IdAndStatus(deckId, actor.id(), Status.KNOWN);
        long due = cardProgressRepository.countByCard_Deck_IdAndUser_IdAndNextReviewAtLessThanEqual(
                deckId, actor.id(), LocalDateTime.now(clock));
        return deckMapper.toProgressDto(deckId, total, learning, known, due);
    }

    private static boolean isOwner(Actor actor, Deck deck) {
        return actor.id() != null && actor.id().equals(deck.getOwner().getId());
    }

    private static Sort catalogSort(String sort) {
        return switch (sort == null ? "popular" : sort) {
            case "likes" -> Sort.by(Sort.Order.desc("likesCount"), Sort.Order.desc("createdAt"));
            case "new" -> Sort.by(Sort.Order.desc("createdAt"));
            case "title" -> Sort.by(Sort.Order.asc("title"));
            default -> Sort.by(Sort.Order.desc("isOfficial"), Sort.Order.desc("copiesCount"),
                    Sort.Order.desc("likesCount"), Sort.Order.desc("createdAt"));
        };
    }

    @SafeVarargs
    private static Specification<Deck> allOf(Specification<Deck>... specs) {
        List<Specification<Deck>> present = Arrays.stream(specs).filter(Objects::nonNull).toList();
        return Specification.allOf(present);
    }

    private static Map<UUID, Long> toMap(List<DeckCount> counts) {
        return counts.stream().collect(Collectors.toMap(DeckCount::deckId, DeckCount::count));
    }
}
