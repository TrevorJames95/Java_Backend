package account.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityLogRepo extends JpaRepository<SecurityLog, Long> {
    List<SecurityLog> findByOrderByIdAsc();
}
