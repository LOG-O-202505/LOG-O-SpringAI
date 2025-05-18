package com.ssafy.logoserver.security;

import com.ssafy.logoserver.utils.SecurityUtil;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    public boolean isCurrentUser(Long uuid) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        // 로그인한 사용자의 UUID와 요청된 UUID가 일치하는지 확인
        // 실제 애플리케이션에서는 사용자 서비스를 통해 ID에서 UUID를 조회해야 함
        return SecurityUtil.isCurrentUserUuid(uuid);
    }
}