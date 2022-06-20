package antifraud.cardlimit;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CardLimitService {
    private final CardLimitRepository cardLimitRepository;

    public Optional<CardLimit> getCardLimit(String number) {
        return cardLimitRepository.getCardByNumber(number);
    }

    public CardLimit saveCardLimit(CardLimit cl) {
        return cardLimitRepository.save(cl);
    }

    public void deleteCardLimit(CardLimit cl) {
        cardLimitRepository.delete(cl);
    }

    public CardLimit updateLimit(CardLimit cl) {
        CardLimit cardLimit = cardLimitRepository.getCardByNumber(cl.getNumber()).orElseThrow();
        cardLimit.setAllowedLimit(cl.getAllowedLimit());
        cardLimit.setManualLimit(cl.getManualLimit());
        return cardLimit;
    }

    public int increaseLimit(int current_Limit, int value_from_transaction) {
        return (int)Math.ceil(0.8 * current_Limit + 0.2 * value_from_transaction);
    }

    public int decreaseLimit(int current_Limit, int value_from_transaction) {
        return (int)Math.ceil(0.8 * current_Limit - 0.2 * value_from_transaction);
    }
}
