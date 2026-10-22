package com.spendwise.service;

import com.spendwise.dto.RegisterRequestDto;
import com.spendwise.dto.UserResponseDto;
import com.spendwise.model.User;
import com.spendwise.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be empty");
        }
        return userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User registerUser(RegisterRequestDto dto) {
        String username = dto.getUsername().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword().trim());
        User user = new User(
                username,
                dto.getFullName().trim(),
                encodedPassword,
                "ROLE_USER"
        );
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(auth.getName())) {
            throw new IllegalStateException("No authenticated user found");
        }
        return userRepository.findByUsername(auth.getName().trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in database: " + auth.getName()));
    }

    public UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public User seedDefaultDemoUserIfEmpty() {
        return userRepository.findByUsername("demo").orElseGet(() -> {
            User demo = new User(
                    "demo",
                    "Demo User",
                    passwordEncoder.encode("password123"),
                    "ROLE_USER"
            );
            return userRepository.save(demo);
        });
    }
}
