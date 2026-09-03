package com.acme.accountservice.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JdbcUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JdbcUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountUser accountUser;
        try {
            accountUser = userRepository.findByEmailIgnoreCase(username);
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            throw new UsernameNotFoundException("User not found");
        }
        return User.withUsername(accountUser.email())
                .password(accountUser.password())
                .authorities(accountUser.roles().toArray(String[]::new))
                .accountLocked(accountUser.locked())
                .build();
    }
}
