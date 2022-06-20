package antifraud.transaction;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    TransactionService transactionService;

    @PostMapping("/transaction")
    TransactionResponse saveTransaction(@Valid @RequestBody Transaction request) {
        if (transactionService.validateIp(request.getIp()) &&
                transactionService.validateCard(request.getNumber())) {
            return transactionService.process(request);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/transaction")
    Transaction addFeedBack(@Valid @RequestBody TransactionFeedBack feedBack) {
        String typesOfFeedBack = "ALLOWED, MANUAL_PROCESSING, PROHIBITED";
        if (typesOfFeedBack.contains(feedBack.getFeedback())) {
            return transactionService.updateFeedBack(feedBack);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect feedback type.");
        }
    }

    @GetMapping("/history")
    List<Transaction> getFullHistory() {
        return transactionService.getFullHistory();
    }

    @GetMapping("/history/{cardNumber}")
    List<Transaction> getHistory(@PathVariable String cardNumber) {
        return transactionService.getHistory(cardNumber);
    }


}