package com.ssafy.logoserver.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.domain.user.dto.UserRequestDto;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 데이터 정리
        userRepository.deleteAll();

        // 테스트 유저 생성
        testUser = User.builder()
                .id("testuser")
                .password(passwordEncoder.encode("password123"))
                .name("테스트")
                .nickname("테스터")
                .birthday(LocalDate.of(1990, 1, 1))
                .address("서울특별시 강남구")
                .phone("010-1234-5678")
                .role(User.Role.USER)
                .build();

        userRepository.save(testUser);
    }

    @Test
    @DisplayName("모든 사용자 조회")
    void getAllUsers() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(get("/api/users"));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].id", is(testUser.getId())));
    }

    @Test
    @DisplayName("UUID로 사용자 조회")
    void getUserByUuid() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/users/{uuid}", testUser.getUuid()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is(testUser.getId())));
    }

    @Test
    @DisplayName("새 사용자 생성")
    void createUser() throws Exception {
        // given
        UserRequestDto userRequestDto = UserRequestDto.builder()
                .id("newuser")
                .password("newpassword123")
                .name("새사용자")
                .nickname("뉴비")
                .birthday(LocalDate.of(1995, 5, 5))
                .address("서울특별시 서초구")
                .phone("010-9876-5432")
                .role(User.Role.USER)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is("newuser")));
    }

    @Test
    @DisplayName("사용자 정보 업데이트")
    void updateUser() throws Exception {
        // given
        UserRequestDto userRequestDto = UserRequestDto.builder()
                .nickname("업데이트된닉네임")
                .address("서울특별시 마포구")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/users/{uuid}", testUser.getUuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.nickname", is("업데이트된닉네임")))
                .andExpect(jsonPath("$.data.address", is("서울특별시 마포구")));
    }

    @Test
    @DisplayName("사용자 삭제")
    void deleteUser() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/users/{uuid}", testUser.getUuid()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        // 실제로 삭제되었는지 확인
        mockMvc.perform(get("/api/users/{uuid}", testUser.getUuid()))
                .andExpect(status().isNotFound());
    }
}