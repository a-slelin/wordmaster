package a.slelin.work.word.master.entity;

import a.slelin.work.word.master.utility.EnumUtil;
import a.slelin.work.word.master.utility.StandardEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role implements StandardEnum {
    USER("user", "u"),
    ADMIN("admin", "a");

    private final String displayName;

    private final String shortName;

    public static Role of(String key) {
        return EnumUtil.of(Role.class, key);
    }
}
