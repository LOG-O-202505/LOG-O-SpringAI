package com.ssafy.logoserver.domain.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.domain.travel.dto.TravelDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TravelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Travel testTravel;

    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 데이터 정리
        travelRepository.deleteAll();
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

        testUser = userRepository.save(testUser);

        // 테스트 여행 생성
        testTravel = Travel.builder()
                .user(testUser)
                .location("제주도")
                .title("제주도 여행")
                .startDate(LocalDate.of(2025, 5, 1))
                .endDate(LocalDate.of(2025, 5, 5))
                .peoples(4)
                .season("봄")
                .build();

        testTravel = travelRepository.save(testTravel);
    }

    @Test
    @DisplayName("모든 여행 조회")
    void getAllTravels() throws Exception {
        mockMvc.perform(get("/api/travels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].location", is("제주도")));
    }

    @Test
    @DisplayName("ID로 여행 조회")
    void getTravelById() throws Exception {
        mockMvc.perform(get("/api/travels/{tuid}", testTravel.getTuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.location", is("제주도")))
                .andExpect(jsonPath("$.data.title", is("제주도 여행")));
    }

    @Test
    @DisplayName("존재하지 않는 여행 조회 시 404")
    void getTravelByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/travels/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @DisplayName("사용자 ID로 여행 목록 조회")
    void getTravelsByUserId() throws Exception {
        mockMvc.perform(get("/api/travels/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].location", is("제주도")));
    }

    @Test
    @DisplayName("위치로 여행 목록 조회")
    void getTravelsByLocation() throws Exception {
        mockMvc.perform(get("/api/travels/location/{location}", "제주도"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].title", is("제주도 여행")));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("새 여행 생성")
    void createTravel() throws Exception {
        TravelDto travelDto = TravelDto.builder()
                .location("부산")
                .title("부산 여행")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 3))
                .peoples(2)
                .season("여름")
                .build();

        mockMvc.perform(post("/api/travels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.location", is("부산")))
                .andExpect(jsonPath("$.data.title", is("부산 여행")));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("여행 정보 업데이트")
    void updateTravel() throws Exception {
        TravelDto travelDto = TravelDto.builder()
                .title("수정된 제주도 여행")
                .peoples(5)
                .build();

        mockMvc.perform(put("/api/travels/{tuid}", testTravel.getTuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.title", is("수정된 제주도 여행")))
                .andExpect(jsonPath("$.data.peoples", is(5)));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("여행 삭제")
    void deleteTravel() throws Exception {
        mockMvc.perform(delete("/api/travels/{tuid}", testTravel.getTuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        // 실제로 삭제되었는지 확인
        mockMvc.perform(get("/api/travels/{tuid}", testTravel.getTuid()))
                .andExpect(status().isNotFound());
    }
}