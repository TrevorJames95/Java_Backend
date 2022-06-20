package antifraud.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query(value = "SELECT * FROM Transaction WHERE number =?1 AND date BETWEEN ?2 AND ?3",
            nativeQuery = true)
    List<Transaction> findByNumberAndTime(String number, LocalDateTime dt1, LocalDateTime dt2);

    @Query(value = "SELECT * FROM Transaction WHERE number =?1",
            nativeQuery = true)
    List<Transaction> findByNumber(String number);
}
