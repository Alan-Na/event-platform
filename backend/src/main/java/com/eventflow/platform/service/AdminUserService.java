package com.eventflow.platform.service;

import com.eventflow.platform.dto.admin.AdminUserOverviewDto;
import com.eventflow.platform.dto.booking.MyBookingItemDto;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.enums.WaitlistStatus;
import com.eventflow.platform.mapper.UserMapper;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.UserRepository;
import com.eventflow.platform.repository.WaitlistEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final UserMapper userMapper;
    private final BookingService bookingService;

    public PageResponse<AdminUserOverviewDto> getUsers(String keyword, int page, int size) {
        Page<User> users = (keyword == null || keyword.isBlank())
                ? userRepository.findAll(PageRequest.of(page, size))
                : userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, PageRequest.of(page, size));

        Page<AdminUserOverviewDto> result = users.map(user -> userMapper.toAdminUserOverviewDto(
                user,
                bookingRepository.countByUserIdAndStatus(user.getId(), BookingStatus.CONFIRMED),
                waitlistEntryRepository.countByUserIdAndStatus(user.getId(), WaitlistStatus.WAITING),
                bookingRepository.countByUserIdAndStatus(user.getId(), BookingStatus.CANCELLED)
                        + waitlistEntryRepository.countByUserIdAndStatus(user.getId(), WaitlistStatus.CANCELLED)));
        return PageResponse.from(result);
    }

    public PageResponse<MyBookingItemDto> getUserBookings(Long userId, int page, int size) {
        return bookingService.getMyBookings(userId, "ALL", page, size);
    }
}
