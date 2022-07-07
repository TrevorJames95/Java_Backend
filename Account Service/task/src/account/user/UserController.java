package account.user;

import account.role.RoleChange;
import account.role.RoleService;
import account.security.SecurityLog;
import account.security.SecurityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private SecurityLogService securityLogService;

    @PostMapping("/api/auth/signup")
    public User registerUser(@Valid  @RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/api/auth/changepass")
    public Map<String, String> changePassword(@RequestBody Map<String,String> password,
                                              @AuthenticationPrincipal UserDetails auth) {
        return userService.changePassword(password, auth);
    }

    @GetMapping("api/admin/user")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("api/admin/user/role")
    public User changeRole(@Valid @RequestBody RoleChange roleChange) {
        return roleService.changeRole(roleChange);
    }

    @DeleteMapping("api/admin/user/{userName}")
    public Map<String, String> deleteUser(@PathVariable String userName) {
        return userService.deleteUser(userName);
    }

    @PutMapping("api/admin/user/access")
    public Map<String, String> changeAccess(@RequestBody Map<String, String> userOperation) {
        return userService.changeAccess(userOperation.get("user"), userOperation.get("operation"));
    }

    @GetMapping("api/security/events")
    public List<SecurityLog> getSecurityLogs() {
        return securityLogService.getSecurityEvents();
    }

}
