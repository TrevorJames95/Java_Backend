package account.config;

import account.role.Role;
import account.security.SecurityEvents;
import account.security.SecurityLogService;
import account.user.User;
import account.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class AuthenticationFailureListener implements
        ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserService userService;
    @Autowired
    private SecurityLogService securityLogService;


    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        //todo
        Optional<User> userOptional = userService.getUser(e.getAuthentication().getName());
        SecurityEvents securityEvents = null;
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getFailedAttempt() < 5) {
                securityLogService.saveEvent(
                        SecurityEvents.LOGIN_FAILED, e.getAuthentication().getName(),
                        request.getRequestURI(), request.getRequestURI());
                if (user.getFailedAttempt() == 4 && !user.getRoles().contains(Role.ROLE_ADMINISTRATOR)) {
                    securityLogService.saveEvent(
                            SecurityEvents.BRUTE_FORCE, e.getAuthentication().getName(),
                            request.getRequestURI(), request.getRequestURI());
                    userService.lock(user);
                    securityLogService.saveEvent(SecurityEvents.LOCK_USER, e.getAuthentication().getName(),
                            "Lock user " + user.getUsername(), "/api/admin/user/access");
                }
            }
            userService.increaseFailedAttempts(user);
        } else {
            securityLogService.saveEvent(
                    SecurityEvents.LOGIN_FAILED, e.getAuthentication().getName(),
                    request.getRequestURI(), request.getRequestURI());
        }


    }
}
