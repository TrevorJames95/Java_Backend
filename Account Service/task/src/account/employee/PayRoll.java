package account.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "employee", "period" }) })
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PayRoll{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employee;

    private String period;

    private Long salary;

}
