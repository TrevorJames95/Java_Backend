package antifraud.user;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username).orElseThrow(() ->
                new UsernameNotFoundException("User " + username + " not found"));
    }

    @Transactional
    public Optional<User> register(User user) {
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            return Optional.empty();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        //Defaults our first user as the Admin and unlocks their account.
        if (savedUser.getId()== 1L) {
            //Gets the ADMINISTRATOR ROLE.
            savedUser.setRole("ADMINISTRATOR");
            savedUser.setEnabled(true);
        } else {
            //Gets the MERCHANT ROLE.
            savedUser.setRole("MERCHANT");
            savedUser.setEnabled(false);
        }
        return Optional.of(userRepository.save(savedUser));

    }

    public List<User> listUsers() {
        return userRepository.findAll(
                Sort.sort(User.class).by(User::getId).ascending()
        );
    }

    @Transactional
    public boolean delete(String username) {
        return userRepository.deleteByUsernameIgnoreCase(username) == 1;
    }


    @Transactional
    //Checks the role of the user and determines if it can be reassigned to a support role.
    public User changeRole(String userName, String role) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(userName);
        if (user.isPresent()) {
            if (user.get().getRole().equals("SUPPORT") && role.equals("SUPPORT")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has Support Role.");
            } else if (role.equals("SUPPORT") || role.equals("MERCHANT")){
                user.get().setRole(role);
                user.get().setEnabled(true);
                return userRepository.save(user.get());
            } else {
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Role");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No User located");
        }
    }

    @Transactional
    //Locks or unlocks the user account.
    public Map<String, String> changeAccess(String userName, String operation) {
        Optional<User> user = userRepository.findByUsernameIgnoreCase(userName);

        if (user.isPresent()) {
            if (operation.equals("UNLOCK")) {
                user.get().setEnabled(true);
            } else if (operation.equals("LOCK")) {
                user.get().setEnabled(false);
            }
            userRepository.save(user.get());
            return Map.of("status", "User " + userName + " " + operation.toLowerCase() + "ed!");
        } else {
            throw new UsernameNotFoundException("User " + userName + " not found");
        }
    }
}