package account.config;

import account.user.User;
import account.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class AuthenticationSuccessEventListener implements
        ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(final AuthenticationSuccessEvent e) {
        //todo
        Optional<User> userOptional = userService.getUser(e.getAuthentication().getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            userService.resetFailedAttempts(user.getUsername());
        }
    }
}

