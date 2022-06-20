package antifraud.cardlimit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardLimitRepository extends JpaRepository<CardLimit, Long> {
    @Query(value = "SELECT * FROM CARDLIMIT  WHERE number = ?1",
            nativeQuery = true)
    Optional<CardLimit> getCardByNumber(String number);
}
