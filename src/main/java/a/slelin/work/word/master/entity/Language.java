package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = Language.TABLE_NAME)
@Entity(name = Language.ENTITY_NAME)
public class Language implements BaseEntity {

    public static final String ENTITY_NAME = "Language";

    public static final String TABLE_NAME = "language";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 255)
    @Column(nullable = false, unique = true)
    private String code;

    @NotBlank
    @Size(min = 3, max = 255)
    @Column(nullable = false)
    private String name;
}
