package a.slelin.work.word.master.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Complexity {

    ELEMENTARY("elementary", "a"),
    EASY("easy", "e"),
    MEDIUM("medium", "m"),
    HARD("hard", "h"),
    IMPOSSIBLE("impossible", "i");

    private final String displayName;

    private final String shortName;

    public static Complexity of(String key) {
        if (key == null) {
            return null;
        }

        key = key.trim();

        for (Complexity complexity : Complexity.values()) {
            if (key.equalsIgnoreCase(complexity.name()) ||
                    key.equalsIgnoreCase(complexity.displayName) ||
                    key.equalsIgnoreCase(complexity.shortName)) {
                return complexity;
            }
        }

        throw new IllegalArgumentException(String.format("Complexity key '%s' not found.", key));
    }
}
