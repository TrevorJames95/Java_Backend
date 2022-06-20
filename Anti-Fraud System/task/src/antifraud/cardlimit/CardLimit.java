package antifraud.cardlimit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CARDLIMIT")
public class CardLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    private int manualLimit = 1500;

    private int allowedLimit = 200;

    public CardLimit(String number) {
        this.number = number;
    }

}
