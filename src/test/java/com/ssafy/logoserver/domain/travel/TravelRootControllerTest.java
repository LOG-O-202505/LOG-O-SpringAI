package com.ssafy.logoserver.domain.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelRootDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TravelRootControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    // TravelRootControllerTest.java 수정본
    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 데이터 정리
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
                .role(User.Role.USER)
                .build();

        testUser = userRepository.save(testUser);

        // 테스트 지역 생성
        testArea = Area.builder()
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
                .build();

        testTravel = travelRepository.save(testTravel);

        // 테스트 여행 루트 생성
        testTravelRoot = TravelRoot.builder()
                .travel(testTravel)
                .area(testArea)
                .day(1)
                .travelDate(LocalDate.of(2025, 5, 1))
                .build();

        testTravelRoot = travelRootRepository.save(testTravelRoot);
    }

    @Test
    @DisplayName("모든 여행 루트 조회")
    void getAllTravelRoots() throws Exception {
        mockMvc.perform(get("/api/travel-roots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].memo", is("첫째 날 루트")));
    }

    @Test
    @DisplayName("ID로 여행 루트 조회")
    void getTravelRootById() throws Exception {
        mockMvc.perform(get("/api/travel-roots/{truid}", testTravelRoot.getTruid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.memo", is("첫째 날 루트")))
                .andExpect(jsonPath("$.data.day", is(1)));
    }

    @Test
    @DisplayName("존재하지 않는 여행 루트 조회 시 404")
    void getTravelRootByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/travel-roots/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @DisplayName("여행 ID로 여행 루트 목록 조회")
    void getTravelRootsByTravelId() throws Exception {
        mockMvc.perform(get("/api/travel-roots/travel/{travelId}", testTravel.getTuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].memo", is("첫째 날 루트")));
    }

    @Test
    @DisplayName("여행 ID와 일자로 여행 루트 목록 조회")
    void getTravelRootsByTravelAndDay() throws Exception {
        mockMvc.perform(get("/api/travel-roots/travel/{travelId}/day/{day}", testTravel.getTuid(), 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].memo", is("첫째 날 루트")));
    }

    @Test
    @DisplayName("날짜로 여행 루트 목록 조회")
    void getTravelRootsByDate() throws Exception {
        mockMvc.perform(get("/api/travel-roots/date/{date}", "2025-05-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].memo", is("첫째 날 루트")));
    }

    @Test
    @DisplayName("새 여행 루트 생성")
    void createTravelRoot() throws Exception {
        TravelRootDto travelRootDto = TravelRootDto.builder()
                .travelId(testTravel.getTuid())
                .areaId(testArea.getAuid())
                .day(2)
                .travelDate(LocalDate.of(2025, 5, 2))
                .build();

        mockMvc.perform(post("/api/travel-roots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelRootDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.memo", is("둘째 날 루트")))
                .andExpect(jsonPath("$.data.day", is(2)));
    }

    @Test
    @DisplayName("여행 루트 정보 업데이트")
    void updateTravelRoot() throws Exception {
        TravelRootDto travelRootDto = TravelRootDto.builder()
                .build();

        mockMvc.perform(put("/api/travel-roots/{truid}", testTravelRoot.getTruid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelRootDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.memo", is("수정된 첫째 날 루트")));
    }

    @Test
    @DisplayName("여행 루트 삭제")
    void deleteTravelRoot() throws Exception {
        mockMvc.perform(delete("/api/travel-roots/{truid}", testTravelRoot.getTruid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        // 실제로 삭제되었는지 확인
        mockMvc.perform(get("/api/travel-roots/{truid}", testTravelRoot.getTruid()))
                .andExpect(status().isNotFound());
    }
}