package com.unispace.service;

import com.unispace.domain.user.Role;
import com.unispace.domain.user.User;
import com.unispace.domain.user.UserRepository;
import com.unispace.dto.request.LoginRequest;
import com.unispace.dto.request.SignUpRequest;
import com.unispace.dto.response.TokenResponse;
import com.unispace.dto.response.UserResponse;
import com.unispace.exception.BusinessException;
import com.unispace.exception.ErrorCode;
import com.unispace.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public UserResponse signUp(SignUpRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
        User user = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .name(req.name())
                .email(req.email())
                .affiliation(req.affiliation())
                .role(Role.USER)
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        String token = tokenProvider.createToken(user.getUsername(), user.getRole().name());
        return TokenResponse.bearer(token, tokenProvider.getValidityMs());
    }
}
