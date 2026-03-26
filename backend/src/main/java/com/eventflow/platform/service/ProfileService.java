package com.eventflow.platform.service;

import com.eventflow.platform.dto.auth.CurrentUserDto;
import com.eventflow.platform.dto.auth.UpdateProfileRequest;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.enums.Language;
import com.eventflow.platform.exception.ResourceNotFoundException;
import com.eventflow.platform.mapper.UserMapper;
import com.eventflow.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public CurrentUserDto getProfile(Long userId) {
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toCurrentUserDto(user);
    }

    @Transactional
    public CurrentUserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFullName(request.fullName());
        user.setPreferredLanguage(Language.fromCode(request.preferredLanguage()));
        return userMapper.toCurrentUserDto(user);
    }
}
