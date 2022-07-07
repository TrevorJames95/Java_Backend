package account.employee;

import account.user.User;
import account.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class PayRollService {
    @Autowired
    private PayRollRepository payRollRepository;

    @Autowired
    private UserService userService;

    public boolean savePayRoll(List<PayRoll> payRollList) {
        //Validation for bad data.
        Set<String> unique = new HashSet<>();
        for (PayRoll pr : payRollList) {
            String uniqueConstraint = pr.getEmployee()+pr.getPeriod();
            if (!userService.userExists(pr.getEmployee())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in the database.");
            } else if (pr.getSalary() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary must be non negative!");
            } else if (unique.contains(uniqueConstraint)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate payments");
            } else {
                unique.add(uniqueConstraint.toLowerCase());
            }
            try {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
                YearMonth yearMonth = YearMonth.parse(pr.getPeriod(), dateTimeFormatter);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong date!");
            }
        }

        payRollRepository.saveAll(payRollList);
        return true;
    }

    public boolean updatePayRoll(PayRoll pr){
        try {
            PayRoll payRollDB = findByPeriod(pr.getEmployee(), pr.getPeriod());
            payRollDB.setSalary(pr.getSalary());
            payRollRepository.save(payRollDB);
            return true;
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to locate record");
        }
    }

    public PayRoll findByPeriod(String employee, String period) {
        return payRollRepository.findByEmployeeAndPeriod(employee, period).orElseThrow();
    }

    public List<PayRoll> findByEmployee(String employee) {
        return payRollRepository.findByEmployeeIgnoreCase(employee);
    }

    //Gets the payment history for a specific employee for a specified period.
    public EmployeeResponse getPartialPaymentHistory(String username, String period) {
        Optional<User> userFromDB = userService.getUser(username);
        if (userFromDB.isPresent()) {
            User user = userFromDB.get();
            PayRoll pr = findByPeriod(username, period);
            EmployeeResponse er = new EmployeeResponse(user.getName(), user.getLastname()
                    , formatPeriod(pr.getPeriod()),
                    String.format("%d dollar(s) %d cent(s)", pr.getSalary() / 100, pr.getSalary() % 100));
            return er;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Employee not found.");
    }

    //Gets the full payment history for a specific employee.
    public List<EmployeeResponse> getPaymentHistory(String username) {
        Optional<User> userFromDB = userService.getUser(username);
        if (userFromDB.isPresent()) {
            User user = userFromDB.get();
            List<PayRoll> payRollList = findByEmployee(username);
            List<EmployeeResponse> employeeResponseList = new ArrayList<>();

            for (PayRoll pr : payRollList) {
                EmployeeResponse er = new EmployeeResponse(user.getName(), user.getLastname()
                        , formatPeriod(pr.getPeriod()),
                        String.format("%d dollar(s) %d cent(s)", pr.getSalary() / 100, pr.getSalary() % 100));
                employeeResponseList.add(er);
            }
            Collections.reverse(employeeResponseList);
            return employeeResponseList;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee not found.");
    }

    public String formatPeriod(String period) {
        String res;
        try {
            YearMonth yearMonth = YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy"));
            String str = yearMonth.getMonth().toString();
            res = str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase()
                    + "-" + yearMonth.getYear();
            return res;

        } catch (DateTimeParseException e) {
            return "Invalid time format or time";
        }
    }

    public void validatePeriod(String period) {
        try {
            YearMonth yearMonth = YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy"));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period");
        }
    }

}
