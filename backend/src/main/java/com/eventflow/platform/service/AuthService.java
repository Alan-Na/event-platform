package com.eventflow.platform.service;

import com.eventflow.platform.dto.auth.AuthResponse;
import com.eventflow.platform.dto.auth.CurrentUserDto;
import com.eventflow.platform.dto.auth.LoginRequest;
import com.eventflow.platform.dto.auth.RegisterRequest;
import com.eventflow.platform.entity.Role;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.enums.Language;
import com.eventflow.platform.exception.BusinessException;
import com.eventflow.platform.exception.ErrorCode;
import com.eventflow.platform.mapper.UserMapper;
import com.eventflow.platform.repository.RoleRepository;
import com.eventflow.platform.repository.UserRepository;
import com.eventflow.platform.security.CustomUserDetails;
import com.eventflow.platform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email is already registered");
        }

        Role userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "ROLE_USER not found"));

        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .preferredLanguage(Language.fromCode(request.preferredLanguage()))
                .enabled(true)
                .build();
        user.getRoles().add(userRole);
        User saved = userRepository.save(user);
        return issueAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password"));
        return issueAuthResponse(user);
    }

    public CurrentUserDto getCurrentUser(Long userId) {
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication required"));
        return userMapper.toCurrentUserDto(user);
    }

    private AuthResponse issueAuthResponse(User user) {
        CurrentUserDto currentUser = userMapper.toCurrentUserDto(user);
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.isEnabled(),
                currentUser.roles());
        String token = jwtTokenProvider.generateToken(userDetails);
        return new AuthResponse(token, "Bearer", jwtTokenProvider.getJwtExpirationSeconds(), currentUser);
    }
}
