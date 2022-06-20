package antifraud.creditcard;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
@AllArgsConstructor
@Service
public class CreditCardService {
    private final CreditCardRepository creditCardRepository;
    public CreditCard saveCreditCard(CreditCard cc) {
        return creditCardRepository.save(cc);
    }

    public Optional<CreditCard> findCreditCard(String number) {
       return creditCardRepository.findByNumber(number);
    }

    //Searches for the card and deletes it after its found.
    //Boolean helps to format the response request.
    @Transactional
    public boolean deleteCreditCard(String number) {
        Optional<CreditCard> cc = creditCardRepository.findByNumber(number);
        if (cc.isPresent()) {
            creditCardRepository.delete(cc.get());
            return true;
        } else {
            return false;
        }
    }

    public List<CreditCard> listCards() {
        return creditCardRepository.findAll(
                Sort.sort(CreditCard.class).by(CreditCard::getId).ascending()
        );
    }
    //Card number is validated using Luhn Algorithm implementation which was sourced from Geeksforgeeks.org
    public boolean validateCardNumber(String number) {
        int nDigits = number.length();
        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--)
        {

            int d = number.charAt(i) - '0';

            if (isSecond)
                d = d * 2;

            // We add two digits to handle
            // cases that make two digits
            // after doubling
            nSum += d / 10;
            nSum += d % 10;

            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }
}
