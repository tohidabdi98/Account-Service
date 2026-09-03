package com.acme.accountservice.auth;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountUser register(SignupRequest request) {
        validatePassword(request.password());
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new UserExistsException();
        }
        return userRepository.save(
                request.name(),
                request.lastname(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
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

    private void validatePassword(String password) {
        if (password.length() < 12) {
            throw new PasswordPolicyException("Password length must be 12 chars minimum!");
        }
        if (BreachedPasswords.contains(password)) {
            throw new PasswordPolicyException("The password is in the hacker's database!");
        }
    }
}
