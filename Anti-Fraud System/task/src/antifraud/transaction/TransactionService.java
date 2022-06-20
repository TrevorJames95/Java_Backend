package antifraud.transaction;

import antifraud.cardlimit.CardLimit;
import antifraud.cardlimit.CardLimitService;
import antifraud.creditcard.CreditCard;
import antifraud.creditcard.CreditCardService;
import antifraud.ip.IPService;
import antifraud.ip.Ip;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@AllArgsConstructor
@Service
public class TransactionService {
    IPService ipService;
    CreditCardService creditCardService;
    TransactionRepository transactionRepository;

    CardLimitService cardLimitService;
    public TransactionResponse process(Transaction transaction) {
        Optional<Ip> ip = ipService.findByNumber(transaction.getIp());
        Optional<CreditCard> cc = creditCardService.findCreditCard(transaction.getNumber());
        Optional<CardLimit> cl = cardLimitService.getCardLimit(transaction.getNumber());

        StringJoiner info = new StringJoiner(", ");
        info.setEmptyValue("none");
        String result = "";

        long amount = transaction.getAmount();
        long allowed = 200;
        long manual = 1500;

        if (cl.isPresent()) {
            allowed = cl.get().getAllowedLimit();
            manual = cl.get().getManualLimit();
        }

        Map<String, String> tRegionAndIp = verifyByIpAndRegion(transaction);

        if (amount <= allowed && !cc.isPresent() && !ip.isPresent()) {
            result = "ALLOWED";
        } else if (amount > allowed && amount <= manual && !cc.isPresent() && !ip.isPresent()) {
            result = "MANUAL_PROCESSING";
            info.add("amount");
        } else if (amount > manual || cc.isPresent() || ip.isPresent()) {
            if (amount > manual) {
                info.add("amount");
            }
            if (cc.isPresent()) {
                info.add("card-number");
            }
            if (ip.isPresent()) {
                info.add("ip");
            }
            result = "PROHIBITED";
        }

        if (tRegionAndIp.containsKey("MANUAL_PROCESSING")) {
            result = "MANUAL_PROCESSING";
            info.add(tRegionAndIp.get("MANUAL_PROCESSING"));
        } else if (tRegionAndIp.containsKey("PROHIBITED")) {
            result = "PROHIBITED";
            info.add(tRegionAndIp.get("PROHIBITED"));
        }

        transaction.setResult(result);

        //Saves a copy of all checked transactions to improve verification of future ones.
        transactionRepository.save(transaction);

        return new TransactionResponse(result, info.toString());
    }
    public List<Transaction> getHistory(String cardNumber) {
        if (creditCardService.validateCardNumber(cardNumber)) {
            List<Transaction> history = transactionRepository.findByNumber(cardNumber);
            if (history.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            } else {
                return history;
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public List<Transaction> getFullHistory() {
        return transactionRepository.findAll(
                Sort.sort(Transaction.class).by(Transaction::getTransactionId).ascending());
    }
    public boolean validateIp(String ip) {
        return ipService.validateIpAddress(ip);
    }
    public boolean validateCard(String num) {
        return creditCardService.validateCardNumber(num);
    }
    public Map<String, String> verifyByIpAndRegion(Transaction t) {
        //Gets all transactions from the db that match the card number and
        //that are 1 hour earlier than our current time.
        List<Transaction> transactionList = transactionRepository.
                findByNumberAndTime(t.getNumber(), t.getDate().minusHours(1L) ,t.getDate());
        Set<String> uniqueIP = new HashSet<>();
        Set<String> uniqueRegion = new HashSet<>();



        for (Transaction transaction: transactionList) {
            //Ips need to be checked for uniqueness to validate transactions
            //Sets aren't allowed to have duplicate elements.
            //Checking for unique ips other than the provided transaction ip.
            if (!uniqueIP.contains(transaction.getIp()) && !transaction.getIp().equals(t.getIp())) {
                uniqueIP.add(transaction.getIp());
            }

            //Regions need to be checked for uniqueness to validate transactions
            //Checking for unique regions other than the provided transaction region.
            if (!uniqueRegion.contains(transaction.getRegion()) && !transaction.getRegion().equals(t.getRegion())) {
                uniqueRegion.add(transaction.getRegion());
            }
        }

        int regionCount = uniqueRegion.size();
        int ipCount = uniqueIP.size();


        //Filter conditions for determining if a transaction requires manual_processing or blocking.
        if (regionCount > 2 && ipCount > 2) {
            return Map.of("PROHIBITED", "ip-correlation, region-correlation");
        } else if (ipCount >2) {
            return Map.of("PROHIBITED", "ip-correlation");
        } else if (regionCount >2) {
            return Map.of("PROHIBITED", "region-correlation");
        }else if (regionCount == 2 && ipCount == 2) {
            return Map.of("MANUAL_PROCESSING", "ip-correlation, region-correlation");
        } else if (ipCount == 2) {
            return Map.of("MANUAL_PROCESSING", "ip-correlation");
        } else if (regionCount == 2) {
            return Map.of("MANUAL_PROCESSING", "region-correlation");
        }

        //If none of the flags were triggered, we just return an empty map.
        return Map.of();
    }
    public Transaction updateFeedBack(TransactionFeedBack feedBack) {
        Optional<Transaction> transaction = transactionRepository.findById(feedBack.getTransactionId());
        if (transaction.isPresent()) {
            if (transaction.get().getResult().equals(feedBack.getFeedback())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Result = Feedback");
            } else if (transaction.get().getFeedback().isEmpty()) {
                transaction.get().setFeedback(feedBack.getFeedback());
                applyFeedBack(transaction.get());
                return transactionRepository.save(transaction.get());
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Feedback already exists");
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find transaction");
    }

    //Updates card limit, returns an updated result for the transaction.
    public void applyFeedBack(Transaction t) {
        Optional<CardLimit> cl = cardLimitService.getCardLimit(t.getNumber());
        CardLimit card;
        String result = "";
        if (cl.isPresent()) {
            card = cl.get();
        } else {
            card = new CardLimit(t.getNumber());
        }

        if (t.getResult().equals("MANUAL_PROCESSING") && t.getFeedback().equals("ALLOWED")) {
            //increases allowed limit if result = manual and feedback = allowed
            card.setAllowedLimit(cardLimitService.increaseLimit(card.getAllowedLimit(),
                    (int) t.getAmount()));
        } else if (t.getResult().equals("PROHIBITED") && t.getFeedback().equals("ALLOWED")) {
            //increase allowed & manual limit
            card.setAllowedLimit(cardLimitService.increaseLimit(card.getAllowedLimit(),
                    (int) t.getAmount()));
            card.setManualLimit(cardLimitService.increaseLimit(card.getManualLimit(),
                    (int) t.getAmount()));
        } else if (t.getResult().equals("PROHIBITED") && t.getFeedback().equals("MANUAL_PROCESSING")) {
            //increase manual limit
            card.setManualLimit(cardLimitService.increaseLimit(card.getManualLimit(),
                    (int) t.getAmount()));
        } else if (t.getResult().equals("ALLOWED") && t.getFeedback().equals("MANUAL_PROCESSING")) {
            //decrease allowed limit
            card.setAllowedLimit(cardLimitService.decreaseLimit(card.getAllowedLimit(),
                    (int) t.getAmount()));
        } else if (t.getResult().equals("ALLOWED") && t.getFeedback().equals("PROHIBITED")) {
            //decrease allowed & manual limit
            card.setAllowedLimit(cardLimitService.decreaseLimit(card.getAllowedLimit(),
                    (int) t.getAmount()));
            card.setManualLimit(cardLimitService.decreaseLimit(card.getManualLimit(),
                    (int) t.getAmount()));
        } else if (t.getResult().equals("MANUAL_PROCESSING") && t.getFeedback().equals("PROHIBITED")) {
            //decrease manual limit
            card.setManualLimit(cardLimitService.decreaseLimit(card.getManualLimit(),
                    (int) t.getAmount()));
        }
        cardLimitService.saveCardLimit(card);
    }
}















