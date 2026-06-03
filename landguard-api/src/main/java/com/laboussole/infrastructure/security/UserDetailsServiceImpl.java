package com.laboussole.infrastructure.security;

import com.laboussole.domain.model.Email;
import com.laboussole.domain.port.out.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Email email;
        try {
            email = Email.of(username);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid email format: " + username);
        }

        return userRepository.findByEmail(email)
                .map(user -> new User(
                        user.email().value(),
                        user.passwordHash().value(),
                        user.canAuthenticate(),
                        true, // accountNonExpired
                        true, // credentialsNonExpired
                        true, // accountNonLocked
                        List.of(new SimpleGrantedAuthority(user.role().authority()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
