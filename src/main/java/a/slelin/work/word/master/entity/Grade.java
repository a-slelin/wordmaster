package a.slelin.work.word.master.entity;

import a.slelin.work.word.master.utility.EnumUtil;
import a.slelin.work.word.master.utility.StandardEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Self-assessment grade of an answer (SM-2 quality: again = 1, hard = 3, good = 4, easy = 5).
 */
@Getter
@AllArgsConstructor
public enum Grade implements StandardEnum {
    AGAIN("again", "a"),
    HARD("hard", "h"),
    GOOD("good", "g"),
    EASY("easy", "e");

    private final String displayName;

    private final String shortName;

    public static Grade of(String key) {
        return EnumUtil.of(Grade.class, key);
    }
}
