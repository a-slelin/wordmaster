package a.slelin.work.word.master.entity;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DeckLikesId {

    private UUID user;

    private UUID deck;
}
