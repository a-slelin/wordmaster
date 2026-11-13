package a.slelin.work.word.master.entity;

import a.slelin.work.word.master.utility.EnumUtil;
import a.slelin.work.word.master.utility.StandardEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FORWARD - show the word (target language), ask for the translation;
 * REVERSE - show the translation, ask for the word.
 */
@Getter
@AllArgsConstructor
public enum TrainingDirection implements StandardEnum {
    FORWARD("forward", "f"),
    REVERSE("reverse", "r");

    private final String displayName;

    private final String shortName;

    public static TrainingDirection of(String key) {
        return EnumUtil.of(TrainingDirection.class, key);
    }
}
