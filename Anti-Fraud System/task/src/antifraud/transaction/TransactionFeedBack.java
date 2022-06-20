package antifraud.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
public class TransactionFeedBack {
    @Positive
    private Long transactionId;
    private String feedback;
}
