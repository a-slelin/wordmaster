package a.slelin.work.word.master.entity;

import a.slelin.work.word.master.utility.EnumUtil;
import a.slelin.work.word.master.utility.StandardEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * How the cards are presented in a training session.
 */
@Getter
@AllArgsConstructor
public enum TrainingMode implements StandardEnum {
    FLASHCARDS("flashcards", "f"),
    TYPING("typing", "t"),
    CHOICE("choice", "c"),
    LISTENING("listening", "l");

    private final String displayName;

    private final String shortName;

    public static TrainingMode of(String key) {
        return EnumUtil.of(TrainingMode.class, key);
    }
}
