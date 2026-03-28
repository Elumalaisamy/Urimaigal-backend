package com.urimaigal.service;

import com.urimaigal.exception.ResourceNotFoundException;
import com.urimaigal.model.User;
import com.urimaigal.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public User updateProfile(String id, String name, String phone, String avatar) {
        User user = getUserById(id);
        if (name != null && !name.isBlank()) user.setName(name);
        if (phone != null) user.setPhone(phone);
        if (avatar != null) user.setAvatar(avatar);
        userRepository.update(user);
        return user;
    }
}
