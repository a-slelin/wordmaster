package a.slelin.work.word.master.entity;

import a.slelin.work.word.master.utility.EnumUtil;
import a.slelin.work.word.master.utility.StandardEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status implements StandardEnum {
    NEW("new", "n"),
    LEARNING("learning", "l"),
    KNOWN("known", "k");

    private final String displayName;

    private final String shortName;

    public static Status of(String key) {
        return EnumUtil.of(Status.class, key);
    }
}
