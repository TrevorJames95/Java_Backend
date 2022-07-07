package account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SecurityLogService {
    @Autowired
    private SecurityLogRepo securityLogRepo;

    public void saveEvent(SecurityEvents securityEvents, String subject, String object, String path) {
        SecurityLog securityLog = securityLogRepo.save(new SecurityLog());
        securityLog.setAction(securityEvents);
        securityLog.setDate(LocalDate.now());
        securityLog.setSubject(subject);
        securityLog.setObject(object);
        securityLog.setPath(path);
        securityLogRepo.save(securityLog);
    }

    public List<SecurityLog> getSecurityEvents() {
        return securityLogRepo.findByOrderByIdAsc();
    }

    //Returns the username for the user that is currently logged in.
    public String getUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }
}
