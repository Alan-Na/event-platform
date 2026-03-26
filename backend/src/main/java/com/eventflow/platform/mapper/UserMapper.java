package com.eventflow.platform.mapper;

import com.eventflow.platform.dto.admin.AdminUserOverviewDto;
import com.eventflow.platform.dto.auth.CurrentUserDto;
import com.eventflow.platform.entity.User;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public CurrentUserDto toCurrentUserDto(User user) {
        return new CurrentUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPreferredLanguage().getCode(),
                roleCodes(user),
                user.isEnabled());
    }

    public AdminUserOverviewDto toAdminUserOverviewDto(User user, long confirmed, long waiting, long cancelled) {
        return new AdminUserOverviewDto(user.getId(), user.getFullName(), user.getEmail(), roleCodes(user), confirmed, waiting, cancelled);
    }

    private Set<String> roleCodes(User user) {
        return user.getRoles().stream().map(role -> role.getCode()).collect(java.util.stream.Collectors.toSet());
    }
}
