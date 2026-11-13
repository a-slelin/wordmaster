package a.slelin.work.word.master.entity;

import a.slelin.work.word.master.utility.EnumUtil;
import a.slelin.work.word.master.utility.StandardEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Which cards are taken into a session: SMART - due for review first, then new ones;
 * ALL - every card of the deck; HARD - cards with the highest error rate.
 */
@Getter
@AllArgsConstructor
public enum TrainingScope implements StandardEnum {
    SMART("smart", "s"),
    ALL("all", "a"),
    HARD("hard", "h");

    private final String displayName;

    private final String shortName;

    public static TrainingScope of(String key) {
        return EnumUtil.of(TrainingScope.class, key);
    }
}
