package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Entity(name = User.ENTITY_NAME)
@EqualsAndHashCode(callSuper = false)
@Table(name = User.TABLE_NAME)
public class User extends Audit {

    public static final String ENTITY_NAME = "User";

    public static final String TABLE_NAME = "users";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(min = 3, max = 50)
    @Pattern(regexp = "[A-Za-z0-9._-]+")
    @Column(length = 50,
            nullable = false,
            unique = true)
    private String username;

    @NotBlank
    @ToString.Exclude
    @Column(nullable = false,
            name = "password_hash")
    @Size(min = 8, max = 255)
    private String passwordHash;

    @Email
    @Size(min = 5, max = 50)
    @Column(length = 50,
            unique = true)
    private String email;
}
