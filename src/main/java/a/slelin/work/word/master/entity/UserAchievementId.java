package a.slelin.work.word.master.entity;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class UserAchievementId implements Serializable {

    private UUID userId;

    private Achievement code;
}
