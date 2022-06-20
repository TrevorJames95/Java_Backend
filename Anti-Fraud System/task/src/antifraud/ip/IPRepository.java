package antifraud.ip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface IPRepository extends JpaRepository<Ip, Long> {
    @Query(value = "SELECT * FROM Ip  WHERE number = ?1",
            nativeQuery = true)
    Optional<Ip> findByNumber(String number);
}
