package com.ssafy.logoserver.utils;

import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtil {

    private static UserRepository userRepository;

    @Autowired
    public SecurityUtil(UserRepository userRepository) {
        SecurityUtil.userRepository = userRepository;
    }

    /**
     * 현재 로그인한 사용자의 아이디를 반환
     * @return 사용자 아이디
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return String.valueOf(principal);
    }

    /**
     * 현재 로그인한 사용자의 UUID 반환
     * @return 사용자 UUID
     */
    public static Long getCurrentUserUuid() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }

        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(User::getUuid).orElse(null);
    }

    /**
     * 현재 로그인한 사용자의 UUID와 주어진 UUID가 일치하는지 확인
     * @param uuid 비교할 UUID
     * @return 일치 여부
     */
    public static boolean isCurrentUserUuid(Long uuid) {
        Long currentUserUuid = getCurrentUserUuid();
        return currentUserUuid != null && currentUserUuid.equals(uuid);
    }

    /**
     * 현재 로그인한 사용자가 관리자인지 확인
     * @return 관리자 여부
     */
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}