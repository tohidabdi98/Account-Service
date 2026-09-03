package com.acme.accountservice.auth;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.acme.accountservice.event.SecurityEventActions;
import com.acme.accountservice.event.SecurityEventService;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityEventService eventService;

    @Autowired
    public UserService(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            SecurityEventService eventService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventService = eventService;
    }

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, null);
    }

    @Transactional
    public AccountUser register(SignupRequest request) {
        validatePassword(request.password());
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new UserExistsException();
        }
        boolean firstUser = userRepository.countUsers() == 0;
        AccountUser saved = userRepository.save(
                request.name(),
                request.lastname(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        userRepository.addRole(
                saved.id(),
                firstUser ? RoleNames.ADMINISTRATOR : RoleNames.USER
        );
        AccountUser user = userRepository.findById(saved.id());
        log(SecurityEventActions.CREATE_USER, "Anonymous", user.email(), "/api/auth/signup");
        return user;
    }

    public void changePassword(String email, String newPassword) {
        validatePassword(newPassword);
        AccountUser user = findByEmail(email);
        if (passwordEncoder.matches(newPassword, user.password())) {
            throw new PasswordSameException();
        }
        userRepository.updatePassword(email, passwordEncoder.encode(newPassword));
        log(SecurityEventActions.CHANGE_PASSWORD, email, email, "/api/auth/changepass");
    }

    public AccountUser findByEmail(String email) {
        try {
            return userRepository.findByEmailIgnoreCase(email);
        } catch (EmptyResultDataAccessException exception) {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public List<AccountUser> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public AccountUser updateRole(RoleRequest request) {
        AccountUser user;
        try {
            user = findByEmail(request.user());
        } catch (UsernameNotFoundException exception) {
            throw new UserNotFoundException();
        }
        String role = RoleNames.normalize(request.role());
        if (!RoleNames.ALL.contains(role)) {
            throw new RoleNotFoundException();
        }
        if (!"GRANT".equalsIgnoreCase(request.operation())
                && !"REMOVE".equalsIgnoreCase(request.operation())) {
            throw new UserManagementException("Operation must be GRANT or REMOVE!");
        }

        if ("GRANT".equalsIgnoreCase(request.operation())) {
            if (user.roles().contains(role)) {
                throw new UserManagementException("The user already has this role!");
            }
            if (combinesRoleGroups(user.roles(), role)) {
                throw new UserManagementException(
                        "The user cannot combine administrative and business roles!"
                );
            }
            userRepository.addRole(user.id(), role);
            log(
                    SecurityEventActions.GRANT_ROLE,
                    currentSubject(),
                    "Grant role " + role.replace("ROLE_", "") + " to " + user.email(),
                    "/api/admin/user/role"
            );
        } else {
            if (!user.roles().contains(role)) {
                throw new UserManagementException("The user does not have a role!");
            }
            if (RoleNames.ADMINISTRATOR.equals(role)) {
                throw new UserManagementException("Can't remove ADMINISTRATOR role!");
            }
            if (user.roles().size() == 1) {
                throw new UserManagementException("The user must have at least one role!");
            }
            userRepository.removeRole(user.id(), role);
            log(
                    SecurityEventActions.REMOVE_ROLE,
                    currentSubject(),
                    "Remove role " + role.replace("ROLE_", "") + " from " + user.email(),
                    "/api/admin/user/role"
            );
        }
        return userRepository.findById(user.id());
    }

    @Transactional
    public void delete(String email) {
        AccountUser user;
        try {
            user = findByEmail(email);
        } catch (UsernameNotFoundException exception) {
            throw new UserNotFoundException();
        }
        if (user.roles().contains(RoleNames.ADMINISTRATOR)) {
            throw new UserManagementException("Can't remove ADMINISTRATOR role!");
        }
        userRepository.deletePayments(user.email());
        userRepository.deleteUser(user.id());
        log(SecurityEventActions.DELETE_USER, currentSubject(), user.email(), "/api/admin/user");
    }

    @Transactional
    public String updateAccess(AccessRequest request, String subject) {
        AccountUser user;
        try {
            user = findByEmail(request.user());
        } catch (UsernameNotFoundException exception) {
            throw new UserNotFoundException();
        }
        String operation = request.operation().toUpperCase();
        if (!"LOCK".equals(operation) && !"UNLOCK".equals(operation)) {
            throw new UserManagementException("Operation must be LOCK or UNLOCK!");
        }
        if ("LOCK".equals(operation)) {
            if (user.roles().contains(RoleNames.ADMINISTRATOR)) {
                throw new UserManagementException("Can't lock the ADMINISTRATOR!");
            }
            userRepository.lockUser(user.email());
            log(
                    SecurityEventActions.LOCK_USER,
                    subject,
                    "Lock user " + user.email(),
                    "/api/admin/user/access"
            );
            return "User " + user.email() + " locked!";
        }
        userRepository.unlockUser(user.email());
        log(
                SecurityEventActions.UNLOCK_USER,
                subject,
                "Unlock user " + user.email(),
                "/api/admin/user/access"
        );
        return "User " + user.email() + " unlocked!";
    }

    public boolean handleFailedLogin(String username, String path) {
        log(SecurityEventActions.LOGIN_FAILED, username, path, path);
        try {
            AccountUser user = findByEmail(username);
            if (user.locked()) {
                return false;
            }
            int attempts = userRepository.incrementFailedAttempts(user.email());
            if (attempts >= 5) {
                userRepository.lockUser(user.email());
                log(SecurityEventActions.BRUTE_FORCE, user.email(), path, path);
                log(
                        SecurityEventActions.LOCK_USER,
                        user.email(),
                        "Lock user " + user.email(),
                        path
                );
                return true;
            }
        } catch (UsernameNotFoundException ignored) {
            // The failed authentication event is still recorded for unknown users.
        }
        return false;
    }

    public void handleSuccessfulLogin(String username) {
        try {
            AccountUser user = findByEmail(username);
            userRepository.resetFailedAttempts(user.email());
        } catch (UsernameNotFoundException ignored) {
            // Authentication will reject the unknown user.
        }
    }

    private String currentSubject() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        return authentication == null || authentication.getName() == null
                ? "Anonymous"
                : authentication.getName();
    }

    private void log(String action, String subject, String object, String path) {
        if (eventService != null) {
            eventService.log(action, subject, object, path);
        }
    }

    private boolean combinesRoleGroups(List<String> currentRoles, String newRole) {
        boolean administrative = currentRoles.stream().anyMatch(RoleNames.ADMINISTRATIVE::contains);
        boolean business = currentRoles.stream().anyMatch(RoleNames.BUSINESS::contains);
        return (administrative && RoleNames.BUSINESS.contains(newRole))
                || (business && RoleNames.ADMINISTRATIVE.contains(newRole));
    }

    private void validatePassword(String password) {
        if (password.length() < 12) {
            throw new PasswordPolicyException("Password length must be 12 chars minimum!");
        }
        if (BreachedPasswords.contains(password)) {
            throw new PasswordPolicyException("The password is in the hacker's database!");
        }
    }
}
