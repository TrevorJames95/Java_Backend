package antifraud.user;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class UserController {

    UserService userService;

    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    User createUser(@Valid @RequestBody User user) {
        return userService.register(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT));
    }

    @GetMapping("/list")
    List<User> listUsers() {
        return userService.listUsers();
    }

    @DeleteMapping("/user/{username}")
    Map<String, String> delete(@PathVariable String username) {
        if (userService.delete(username)) {
            return Map.of(
                    "username", username,
                    "status", "Deleted successfully!"
            );
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/role")
    User changeRole(@RequestBody Map<String, String> nameAndRole) {
        //Sends over the username and role to the service.
        return userService.changeRole(nameAndRole.get("username"), nameAndRole.get("role"));
    }

    @PutMapping("/access")
    Map<String, String> changeAccess(@RequestBody Map<String, String> nameAndAccess) {
        //Sends over the username and access to the service.
        return userService.changeAccess(nameAndAccess.get("username"), nameAndAccess.get("operation"));
    }
}