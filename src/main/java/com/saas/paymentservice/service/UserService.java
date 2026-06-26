package com.saas.paymentservice.service;

import com.saas.paymentservice.dto.RegisterRequest;
import com.saas.paymentservice.entity.User;
import com.saas.paymentservice.exception.UsernameAlreadyExistsException;
import com.saas.paymentservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        String hash = passwordEncoder.encode(request.password());
        User user = new User(request.username(), hash, "USER");
        userRepository.save(user);
    }
}
