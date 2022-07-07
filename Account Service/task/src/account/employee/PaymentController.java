package account.employee;

import account.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@RestController
public class PaymentController {
    @Autowired
    private PayRollService payRollService;

    @Autowired
    private UserService userService;


    @PostMapping("api/acct/payments")
    public ResponseEntity<Map<String,String>> uploadPayroll(
            @RequestBody
            @NotEmpty(message = "List cant be empty.") List<@Valid PayRoll> payRollList) {
        if (payRollService.savePayRoll(payRollList)) {
            return new ResponseEntity<>(Map.of("status", "Added successfully!"), HttpStatus.OK);
        }
        throw  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something broke, lets go find it.");
    }

    @GetMapping(value = "/api/empl/payment", params = "period")
    public EmployeeResponse paymentHistoryByPeriod(@AuthenticationPrincipal UserDetails auth,
                                   @RequestParam String period) {
        try {
            String username = auth.getUsername().toLowerCase();
            payRollService.validatePeriod(period);
            return payRollService.getPartialPaymentHistory(username, period);
        } catch (NullPointerException npe) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "This api only for authenticated user");
        }
    }

    @GetMapping("api/empl/payment")
    public List<EmployeeResponse> paymentHistory(@AuthenticationPrincipal UserDetails auth ){
        try {
            String username = auth.getUsername().toLowerCase();
            return payRollService.getPaymentHistory(username);
        } catch (NullPointerException npe) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "This api only for authenticated user");
        }
    }

    @PutMapping("api/acct/payments")
    public ResponseEntity<Map<String, String>>updatePayment(@Valid @RequestBody PayRoll pr) {
        if (payRollService.updatePayRoll(pr)) {
            return new ResponseEntity<>(Map.of("status", "Updated successfully!"), HttpStatus.OK);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid entry");
    }

}
