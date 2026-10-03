package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.Deck;
import a.slelin.work.word.master.entity.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Subquery;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Filters of the public deck catalog.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeckSpecifications {

    public static Specification<Deck> isPublic() {
        return (root, query, cb) -> cb.isTrue(root.get("isPublic"));
    }

    public static Specification<Deck> ownedBy(UUID ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Deck> titleOrDescriptionContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern));
    }

    public static Specification<Deck> targetLanguage(Long languageId) {
        if (languageId == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(root.get("targetLanguage").get("id"), languageId);
    }

    public static Specification<Deck> hasTag(Long tagId) {
        if (tagId == null) {
            return null;
        }

        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            var subRoot = sub.from(Deck.class);
            Join<Deck, Tag> tags = subRoot.join("tags");
            sub.select(tags.get("id"))
                    .where(cb.equal(subRoot.get("id"), root.get("id")), cb.equal(tags.get("id"), tagId));
            return cb.exists(sub);
        };
    }

    public static Specification<Deck> officialOnly(Boolean official) {
        if (official == null || !official) {
            return null;
        }

        return (root, query, cb) -> cb.isTrue(root.get("isOfficial"));
    }
}
