package com.acme.accountservice.auth;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        return userRepository.findById(saved.id());
    }

    public void changePassword(String email, String newPassword) {
        validatePassword(newPassword);
        AccountUser user = findByEmail(email);
        if (passwordEncoder.matches(newPassword, user.password())) {
            throw new PasswordSameException();
        }
        userRepository.updatePassword(email, passwordEncoder.encode(newPassword));
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
