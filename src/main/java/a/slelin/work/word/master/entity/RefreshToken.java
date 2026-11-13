package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh token of a user session. Only SHA-256 hash of the token is stored.
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = RefreshToken.TABLE_NAME)
@Entity(name = RefreshToken.ENTITY_NAME)
public class RefreshToken implements BaseEntity {

    public static final String ENTITY_NAME = "RefreshToken";

    public static final String TABLE_NAME = "refresh_token";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "user_id",
            nullable = false)
    private User user;

    @NotBlank
    @ToString.Exclude
    @Column(nullable = false,
            unique = true,
            name = "token_hash",
            length = 128)
    private String tokenHash;

    @NotNull
    @Column(nullable = false,
            name = "expires_at")
    private LocalDateTime expiresAt;

    @NotNull
    @Column(nullable = false)
    private Boolean revoked;

    @Column(nullable = false,
            updatable = false,
            name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return !Boolean.TRUE.equals(revoked) && expiresAt.isAfter(LocalDateTime.now());
    }
}
