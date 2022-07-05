package account.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayRollRepository extends JpaRepository<PayRoll, Long> {
    public Optional<PayRoll> findByEmployeeAndPeriod(String employee, String period);

    List<PayRoll> findByEmployeeIgnoreCase(String employee);
}
