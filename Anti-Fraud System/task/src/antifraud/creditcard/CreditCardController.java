package antifraud.creditcard;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/api/antifraud")
public class CreditCardController {

    CreditCardService creditCardService;

    @PostMapping("/stolencard")
    CreditCard registerStolenCard(@Valid @RequestBody CreditCard cc) {
        if (creditCardService.validateCardNumber(cc.getNumber())) {
            Optional<CreditCard> dbCard = creditCardService.findCreditCard(cc.getNumber());
            if (dbCard.isPresent()) {
                throw  new ResponseStatusException(HttpStatus.CONFLICT);
            } else {
                return creditCardService.saveCreditCard(cc);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("stolencard/{number}")
    Map<String, String> deleteCard(@PathVariable String number) {
        if (!creditCardService.validateCardNumber(number)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else {
            if (creditCardService.deleteCreditCard(number)) {
                return Map.of("status", "Card " + number + " successfully removed!");
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
    }

    @GetMapping("/stolencard")
    List<CreditCard> getCards() {
        return creditCardService.listCards();
    }
}
