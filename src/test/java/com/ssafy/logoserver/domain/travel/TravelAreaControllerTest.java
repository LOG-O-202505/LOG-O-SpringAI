package com.ssafy.logoserver.domain.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelAreaDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.travel.repository.TravelAreaRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRootRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TravelAreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TravelAreaRepository travelAreaRepository;

    @Autowired
    private TravelRootRepository travelRootRepository;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Travel testTravel;
    private Area testArea;
    private TravelRoot testTravelRoot;
    private TravelArea testTravelArea;

    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 데이터 정리
        travelAreaRepository.deleteAll();
        travelRootRepository.deleteAll();
        travelRepository.deleteAll();
        userRepository.deleteAll();
        areaRepository.deleteAll();

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

        // 테스트 지역 생성
        testArea = Area.builder()
                .areaName("제주도")
                .build();

        testArea = areaRepository.save(testArea);

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

        // 테스트 여행 루트 생성
        testTravelRoot = TravelRoot.builder()
                .travel(testTravel)
                .area(testArea)
                .day(1)
                .travelDate(LocalDate.of(2025, 5, 1))
                .memo("첫째 날 루트")
                .build();

        testTravelRoot = travelRootRepository.save(testTravelRoot);

        // 테스트 여행 지역 생성
        testTravelArea = TravelArea.builder()
                .user(testUser)
                .travel(testTravel)
                .travelDay(testTravelRoot)
                .area(testArea)
                .seq(1)
                .startTime(LocalDateTime.of(2025, 5, 1, 10, 0))
                .memo("성산일출봉")
                .build();

        testTravelArea = travelAreaRepository.save(testTravelArea);
    }

    @Test
    @DisplayName("모든 여행 지역 조회")
    void getAllTravelAreas() throws Exception {
        mockMvc.perform(get("/api/travel-areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].memo", is("성산일출봉")));
    }

    @Test
    @DisplayName("ID로 여행 지역 조회")
    void getTravelAreaById() throws Exception {
        mockMvc.perform(get("/api/travel-areas/{tauid}", testTravelArea.getTauid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.memo", is("성산일출봉")))
                .andExpect(jsonPath("$.data.seq", is(1)));
    }

    @Test
    @DisplayName("존재하지 않는 여행 지역 조회 시 404")
    void getTravelAreaByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/travel-areas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @DisplayName("여행 ID로 여행 지역 목록 조회")
    void getTravelAreasByTravelId() throws Exception {
        mockMvc.perform(get("/api/travel-areas/travel/{travelId}", testTravel.getTuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].memo", is("성산일출봉")));
    }

    @Test
    @DisplayName("여행 루트 ID로 여행 지역 목록 조회")
    void getTravelAreasByTravelRootId() throws Exception {
        mockMvc.perform(get("/api/travel-areas/travel-root/{travelRootId}", testTravelRoot.getTruid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].memo", is("성산일출봉")));
    }

    @Test
    @DisplayName("새 여행 지역 생성")
    void createTravelArea() throws Exception {
        TravelAreaDto travelAreaDto = TravelAreaDto.builder()
                .userId(testUser.getUuid())
                .travelId(testTravel.getTuid())
                .travelDayId(testTravelRoot.getTruid())
                .areaId(testArea.getAuid())
                .seq(2)
                .startTime(LocalDateTime.of(2025, 5, 1, 14, 0))
                .memo("우도")
                .build();

        mockMvc.perform(post("/api/travel-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelAreaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.memo", is("우도")))
                .andExpect(jsonPath("$.data.seq", is(2)));
    }

    @Test
    @DisplayName("여행 지역 정보 업데이트")
    void updateTravelArea() throws Exception {
        TravelAreaDto travelAreaDto = TravelAreaDto.builder()
                .memo("수정된 성산일출봉")
                .seq(3)
                .build();

        mockMvc.perform(put("/api/travel-areas/{tauid}", testTravelArea.getTauid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelAreaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.memo", is("수정된 성산일출봉")))
                .andExpect(jsonPath("$.data.seq", is(3)));
    }

    @Test
    @DisplayName("여행 지역 삭제")
    void deleteTravelArea() throws Exception {
        mockMvc.perform(delete("/api/travel-areas/{tauid}", testTravelArea.getTauid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        // 실제로 삭제되었는지 확인
        mockMvc.perform(get("/api/travel-areas/{tauid}", testTravelArea.getTauid()))
                .andExpect(status().isNotFound());
    }
}