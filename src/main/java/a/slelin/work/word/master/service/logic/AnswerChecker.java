package a.slelin.work.word.master.service.logic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks a typed answer against the expected one, forgiving case, punctuation, "ё/е",
 * accents (for "almost" answers), optional parts in brackets, a leading "to " and small typos.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnswerChecker {

    public enum Result {
        EXACT,
        TYPO,
        WRONG
    }

    private static final Pattern ALTERNATIVES_SEPARATOR = Pattern.compile("[,;/]");

    private static final Pattern BRACKETS = Pattern.compile("\\([^)]*\\)");

    public static Result check(String expected, String actual) {
        if (expected == null || actual == null || actual.isBlank()) {
            return Result.WRONG;
        }

        String given = normalize(actual);
        Set<String> variants = variants(expected);

        if (variants.contains(given)) {
            return Result.EXACT;
        }

        String givenWithoutAccents = stripAccents(given);
        for (String variant : variants) {
            if (stripAccents(variant).equals(givenWithoutAccents)) {
                return Result.TYPO;
            }
        }

        for (String variant : variants) {
            int allowed = allowedTypos(variant.length());
            if (allowed > 0 && levenshtein(variant, given, allowed) <= allowed) {
                return Result.TYPO;
            }
        }

        return Result.WRONG;
    }

    /**
     * All acceptable normalized forms of the expected answer.
     */
    static Set<String> variants(String expected) {
        Set<String> result = new LinkedHashSet<>();
        List<String> raw = new ArrayList<>();
        raw.add(expected);
        raw.add(BRACKETS.matcher(expected).replaceAll(" "));
        raw.add(expected.replace("(", " ").replace(")", " "));

        for (String option : List.copyOf(raw)) {
            if (ALTERNATIVES_SEPARATOR.matcher(option).find()) {
                for (String part : ALTERNATIVES_SEPARATOR.split(option)) {
                    if (!part.isBlank()) {
                        raw.add(part);
                    }
                }
            }
        }

        for (String option : raw) {
            String normalized = normalize(option);
            if (!normalized.isEmpty()) {
                result.add(normalized);
                if (normalized.startsWith("to ") && normalized.length() > 3) {
                    result.add(normalized.substring(3));
                }
            }
        }

        return result;
    }

    static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replace('’', '\'')
                .replaceAll("[–—\\-/,;]", " ")
                .replaceAll("[.!?¿¡\"«»:()]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    static String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('ß', 's');
    }

    private static int allowedTypos(int length) {
        if (length >= 9) {
            return 2;
        }
        if (length >= 4) {
            return 1;
        }
        return 0;
    }

    /**
     * Levenshtein distance with early exit when it exceeds {@code limit}.
     */
    static int levenshtein(String a, String b, int limit) {
        if (Math.abs(a.length() - b.length()) > limit) {
            return limit + 1;
        }

        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            int rowMin = current[0];
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
                rowMin = Math.min(rowMin, current[j]);
            }
            if (rowMin > limit) {
                return limit + 1;
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[b.length()];
    }
}
