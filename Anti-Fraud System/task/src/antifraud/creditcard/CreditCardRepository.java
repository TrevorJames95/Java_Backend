package antifraud.creditcard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    @Query(value = "SELECT * FROM CREDITCARD  WHERE number = ?1",
            nativeQuery = true)
    Optional<CreditCard> findByNumber(String number);

}
