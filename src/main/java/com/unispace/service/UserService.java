package com.unispace.service;

import com.unispace.domain.user.Role;
import com.unispace.domain.user.User;
import com.unispace.domain.user.UserRepository;
import com.unispace.dto.request.ProfileUpdateRequest;
import com.unispace.dto.response.UserResponse;
import com.unispace.exception.BusinessException;
import com.unispace.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        return UserResponse.from(getByUsername(username));
    }

    @Transactional
    public UserResponse updateProfile(String username, ProfileUpdateRequest req) {
        User user = getByUsername(username);
        if (req.name() != null) user.setName(req.name());
        if (req.email() != null) user.setEmail(req.email());
        if (req.affiliation() != null) user.setAffiliation(req.affiliation());
        return UserResponse.from(user);
    }

    // ----- Admin -----
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse changeRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setRole(role);
        return UserResponse.from(user);
    }
}
