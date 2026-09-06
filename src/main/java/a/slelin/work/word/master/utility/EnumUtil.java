package a.slelin.work.word.master.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnumUtil {

    @SuppressWarnings("rawtypes")
    public static <T extends Enum & StandardEnum> T of(Class<T> enumClass, String key) {
        if (key == null) {
            return null;
        }

        key = key.trim();

        for (T enumerate : enumClass.getEnumConstants()) {
            if (key.equalsIgnoreCase(enumerate.name()) ||
                    key.equalsIgnoreCase(enumerate.getDisplayName()) ||
                    key.equalsIgnoreCase(enumerate.getShortName())) {
                return enumerate;
            }
        }

        throw new IllegalArgumentException(String.format("'%s' key '%s' not found.", enumClass.getSimpleName(), key));
    }
}
