package com.microlend.service;

import com.microlend.entity.User;
import com.microlend.enums.UserRole;
import com.microlend.enums.UserStatus;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public List<User> getByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public User updateStatus(Long id, UserStatus status) {
        User user = getById(id);
        user.setStatus(status);
        return userRepository.save(user);
    }

    public void delete(Long id) {
        getById(id);
        userRepository.deleteById(id);
    }
}
