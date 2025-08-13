package com.example.bookstore.service;

import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }

    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User register(User user) throws DataIntegrityViolationException {
        // Check if email already exists
        if (isEmailExists(user.getEmail())) {
            throw new DataIntegrityViolationException("Email đã được sử dụng");
        }

        // Check if username already exists
        if (isUsernameExists(user.getUsername())) {
            throw new DataIntegrityViolationException("Tên đăng nhập đã được sử dụng");
        }

        return userRepository.save(user);
    }
}
