package account.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);

    List<User> findByOrderByIdAsc();

    @Query("UPDATE User u SET u.failedAttempt = ?1 WHERE u.email = ?2")
    @Modifying
    public void updateFailedAttempts(int failAttempts, String email);


}
