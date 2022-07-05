package account.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final static List<String> breachedPasswords = List.of("PasswordForJanuary",
            "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username).orElseThrow(() ->
                new UsernameNotFoundException("User " + username + " not found"));
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

    public User changeRole(RoleChange roleChange){
        Optional<User> userFromDB = userRepository.findByUsernameIgnoreCase(roleChange.getUser());
        Role role;
        //Checks for an invalid given role.
        try {
            role = Role.valueOf("ROLE_" + roleChange.getRole());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }

        if (userFromDB.isPresent()) {
            User user = userFromDB.get();
            //User shouldn't have both admin and accountant.
            if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR) && role.equals(Role.ROLE_ACCOUNTANT) ||
                    (user.getRoles().contains(Role.ROLE_ACCOUNTANT) && role.equals(Role.ROLE_ADMINISTRATOR)) ||
                        (user.getRoles().contains(Role.ROLE_ADMINISTRATOR) && role.equals(Role.ROLE_USER))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The user cannot combine administrative and business roles!");
            }

            if (roleChange.getOperation().equals("GRANT")) {
                if (user.getRoles().contains(role)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role exists");
                }
                user.grantAuthority(role);
            } else if (roleChange.getOperation().equals("REMOVE")) {
                if (!user.getRoles().contains(role)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The user does not have a role!");
                } else if (user.getRoles().contains(role) && role.equals(Role.ROLE_ADMINISTRATOR)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Can't remove ADMINISTRATOR role!");
                } else if (user.getRoles().size() == 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The user must have at least one role!");
                }
                user.removeAuthority(role);
            }
            return userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findByOrderByIdAsc();
    }

    public Optional<User> getUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
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

        return Map.of("user", userName.toLowerCase(),
                "status", "Deleted successfully!");
    }

}
