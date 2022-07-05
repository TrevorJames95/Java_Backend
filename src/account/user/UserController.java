package account.user;

import account.security.SecruityLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/api/auth/signup")
    public User registerUser(@Valid  @RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/api/auth/changepass")
    public Map<String, String> changePassword(@RequestBody Map<String,String> password,
                                              @AuthenticationPrincipal UserDetails auth) {
        if (password.get("new_password").length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        } else if (userService.hackedPasswords(password.get("new_password"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        } else {
            User user = (User)userService.loadUserByUsername(auth.getUsername());
            if (bCryptPasswordEncoder.matches(password.get("new_password"), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
            } else {
                user.setPassword(bCryptPasswordEncoder.encode(password.get("new_password")));
                userService.save(user);
            }
        }

        return Map.of("email", auth.getUsername().toLowerCase(),
                "status", "The password has been updated successfully");
    }

    @GetMapping("api/admin/user")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("api/admin/user/role")
    public User changeRole(@Valid @RequestBody RoleChange roleChange) {
        return userService.changeRole(roleChange);
    }

    @DeleteMapping("api/admin/user/{userName}")
    public Map<String, String> deleteUser(@PathVariable String userName) {
        return userService.deleteUser(userName);
    }

    @PutMapping("api/admin/user/access")
    public Map<String, String> changeAccess(@RequestBody Map<String, String> userOperation) {
        System.out.println(userOperation.get("user"));
        System.out.println(userOperation.get("operation"));
        return Map.of();
    }

    @GetMapping("api/security/events")
    public List<SecruityLog> getSecurityLogs() {
        return List.of();
    }

}
