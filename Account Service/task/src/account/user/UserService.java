package account.user;

import account.role.Role;
import account.security.SecurityEvents;
import account.security.SecurityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SecurityLogService securityLogService;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    private final static List<String> breachedPasswords = List.of("PasswordForJanuary",
            "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(username);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        throw new UsernameNotFoundException("User " + username + " not found");
    }

    @Transactional
    public void increaseFailedAttempts(User user) {
        int newFailAttempts = user.getFailedAttempt() + 1;
        userRepository.updateFailedAttempts(newFailAttempts, user.getEmail());
    }
    @Transactional
    public void resetFailedAttempts(String email) {
        userRepository.updateFailedAttempts(0, email);
    }

    @Transactional
    public void lock(User user) {
        user.setAccountNonLocked(false);
        userRepository.save(user);
    }

    @Transactional
    public void unlock(User user) {
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        userRepository.save(user);
    }

    public Map<String, String> changeAccess(String username, String operation) {
        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (operation.equals("LOCK") && !user.getRoles().contains(Role.ROLE_ADMINISTRATOR)) {
                lock(user);
                securityLogService.saveEvent(SecurityEvents.LOCK_USER, securityLogService.getUserName(),
                        "Lock user " + user.getUsername(), "/api/admin/user/access");
                return Map.of("status", "User " + user.getUsername() + " locked!");
            } else if (operation.equals("UNLOCK")) {
                unlock(user);
                securityLogService.saveEvent(SecurityEvents.UNLOCK_USER, securityLogService.getUserName(),
                        "Unlock user " + user.getUsername(), "/api/admin/user/access");
                return Map.of("status", "User " + user.getUsername() + " unlocked!");
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
    }
    public User register(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        user.setUsername(user.getEmail());
        if (userExists(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
        } else if (!user.getEmail().endsWith("@acme.com")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong email!");
        } else if (user.getPassword().length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password length must be at least 12 chars!");
        } else if (breachedPasswords.contains(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        if (saved.getId()==1L) {
            saved.grantAuthority(Role.ROLE_ADMINISTRATOR);
        }else {
            saved.grantAuthority(Role.ROLE_USER);
        }

        securityLogService.saveEvent(
                SecurityEvents.CREATE_USER,
                "Anonymous",
                user.getEmail(),
                "/api/auth/signup");
        return userRepository.save(saved);
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    public boolean hackedPasswords(String password) {
        return breachedPasswords.contains(password);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findByOrderByIdAsc();
    }

    public Optional<User> getUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    public Map<String, String> changePassword(Map<String, String> password, UserDetails auth) {
        if (password.get("new_password").length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        } else if (hackedPasswords(password.get("new_password"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        } else {
            User user = userRepository.findByUsernameIgnoreCase(auth.getUsername()).orElseThrow();
            if (bCryptPasswordEncoder.matches(password.get("new_password"), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
            } else {
                user.setPassword(bCryptPasswordEncoder.encode(password.get("new_password")));
                userRepository.save(user);
            }
        }
        securityLogService.saveEvent(SecurityEvents.CHANGE_PASSWORD, auth.getUsername(),
                auth.getUsername(), "/api/auth/changepass");

        return Map.of("email", auth.getUsername().toLowerCase(),
                "status", "The password has been updated successfully");
    }
    public Map<String, String> deleteUser(String userName) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(userName);
        if (user.isPresent()) {
            if (user.get().getRoles().contains(Role.ROLE_ADMINISTRATOR)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Can't remove ADMINISTRATOR role!");
            }
            userRepository.delete(user.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

        securityLogService.saveEvent(SecurityEvents.DELETE_USER, securityLogService.getUserName(),
                user.get().getUsername(), "/api/admin/user");

        return Map.of("user", userName.toLowerCase(),
                "status", "Deleted successfully!");
    }

}
