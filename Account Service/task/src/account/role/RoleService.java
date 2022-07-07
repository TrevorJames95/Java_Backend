package account.role;

import account.security.SecurityEvents;
import account.security.SecurityLogService;
import account.user.User;
import account.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityLogService securityLogService;

    public User changeRole(RoleChange roleChange){
        Optional<User> userFromDB = userRepository.findByUsernameIgnoreCase(roleChange.getUser());
        Role role = validateRole(roleChange.getRole());

        SecurityEvents securityEvents = SecurityEvents.DEFAULT_VALUE;
        String object = null;

        if (userFromDB.isPresent()) {
            User user = userFromDB.get();
            roleLogicValidator(user.getRoles(), role);

            if (roleChange.getOperation().equals("GRANT")) {
                if (user.getRoles().contains(role)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role exists");
                }
                user.grantAuthority(role);
                //builds the security logging object.
                securityEvents = SecurityEvents.GRANT_ROLE;
                object = "Grant role " + roleChange.getRole() + " to " + user.getEmail();

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

                securityEvents = SecurityEvents.REMOVE_ROLE;
                object = "Remove role " + roleChange.getRole() + " from " + user.getEmail();

            }
            securityLogService.saveEvent(securityEvents, securityLogService.getUserName(),
                    object, "/api/admin/user/role");
            return userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
    }

    private Role validateRole(String roleString) {
        //Checks for an invalid given role.
        try {
            Role role = Role.valueOf("ROLE_" + roleString);
            return role;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }
    }

    private void roleLogicValidator(List<Role> roles, Role role) {
        //Requirement for users to be either in an administrative group or a business group.
        //This is enforced by preventing certain roles from overlapping with each other.
        //Admin can't possess the accountant role.
        if (roles.contains(Role.ROLE_ADMINISTRATOR) && role.equals(Role.ROLE_ACCOUNTANT) ||
                //Accountant can't possess the admin role.
                (roles.contains(Role.ROLE_ACCOUNTANT) && role.equals(Role.ROLE_ADMINISTRATOR)) ||
                //Admin can't possess the user role.
                (roles.contains(Role.ROLE_ADMINISTRATOR) && role.equals(Role.ROLE_USER)) ||
                //Admin can't possess the auditor role.
                (roles.contains(Role.ROLE_ADMINISTRATOR) && role.equals(Role.ROLE_AUDITOR)) ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The user cannot combine administrative and business roles!");
        }
    }
}
