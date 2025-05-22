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
                .email("test@example.com")
                .gender("M")
                .nickname("테스터")
                .birthday(LocalDate.of(1990, 1, 1))
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
                get("/api/users/uuid/{uuid}", testUser.getUuid()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is(testUser.getId())));
    }

    @Test
    @DisplayName("사용자 ID로 사용자 조회")
    void getUserById() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/users/id/{id}", testUser.getId()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.nickname", is(testUser.getNickname())));
    }

    @Test
    @DisplayName("닉네임으로 사용자 조회")
    void getUserByNickname() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/users/nickname/{nickname}", testUser.getNickname()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is(testUser.getId())));
    }

    @Test
    @DisplayName("새 사용자 생성")
    void createUser() throws Exception {
        UserRequestDto userRequestDto = UserRequestDto.builder()
                .id("newuser")
                .password("newpassword123")
                .name("새사용자")
                .email("newuser@example.com")
                .gender("F")
                .nickname("뉴비")
                .birthday(LocalDate.of(1995, 5, 5))
                .role(User.Role.USER)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is("newuser")))
                .andExpect(jsonPath("$.data.email", is("newuser@example.com")));
    }

    @Test
    @DisplayName("닉네임 중복으로 사용자 생성 실패")
    void createUserFailDueToNicknameDuplicate() throws Exception {
        // given
        UserRequestDto userRequestDto = UserRequestDto.builder()
                .id("newuser")
                .password("newpassword123")
                .name("새사용자")
                .nickname("테스터") // 이미 존재하는 닉네임
                .birthday(LocalDate.of(1995, 5, 5))
                .role(User.Role.USER)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDto)));

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다: 테스터"));
    }

    @Test
    @DisplayName("사용자 정보 업데이트")
    void updateUser() throws Exception {
        // given
        UserRequestDto userRequestDto = UserRequestDto.builder()
                .nickname("업데이트된닉네임")
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
        mockMvc.perform(get("/api/users/uuid/{uuid}", testUser.getUuid()))
                .andExpect(status().isNotFound());
    }
}