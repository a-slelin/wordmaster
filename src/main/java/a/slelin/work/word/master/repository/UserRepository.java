package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.Role;
import a.slelin.work.word.master.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    long countByRole(Role role);

    /**
     * Decks, progress, sessions and tokens are removed by "ON DELETE CASCADE" in the database.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from users where id = :userId", nativeQuery = true)
    void deleteUser(@Param("userId") UUID userId);
}