package com.ssafy.logoserver.domain.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.domain.area.dto.AreaDto;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelDto;
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
public class AreaTravelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private TravelRepository travelRepository;

    @Autowired
    private TravelRootRepository travelRootRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Area testArea;
    private Travel testTravel;
    private TravelRoot testTravelRoot;

    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 데이터 정리
        travelRootRepository.deleteAll();
        travelRepository.deleteAll();
        areaRepository.deleteAll();
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
    }

    @Test
    @DisplayName("지역을 생성하고 해당 지역으로 여행 루트 생성")
    void createAreaAndTravelRoot() throws Exception {
        // 새 지역 생성
        AreaDto areaDto = AreaDto.builder()
                .areaName("부산")
                .build();

        String areaResult = mockMvc.perform(post("/api/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(areaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.areaName", is("부산")))
                .andReturn().getResponse().getContentAsString();

        // 생성된 지역의 ID 추출
        Long areaId = objectMapper.readTree(areaResult)
                .path("data")
                .path("auid")
                .asLong();

        // 해당 지역을 사용하여 여행 루트 생성
        TravelRootDto travelRootDto = TravelRootDto.builder()
                .travelId(testTravel.getTuid())
                .areaId(areaId)
                .day(2)
                .travelDate(LocalDate.of(2025, 5, 2))
                .memo("부산 여행 둘째 날")
                .build();

        mockMvc.perform(post("/api/travel-roots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelRootDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.memo", is("부산 여행 둘째 날")))
                .andExpect(jsonPath("$.data.areaId", is(areaId.intValue())));
    }

    @Test
    @DisplayName("지역을 삭제할 때 관련 여행 루트가 있으면 삭제 실패")
    void deleteAreaWithTravelRootFail() throws Exception {
        mockMvc.perform(delete("/api/areas/{auid}", testArea.getAuid()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("연관된 여행 정보가 있어 삭제할 수 없습니다")));
    }

    @Test
    @DisplayName("지역 정보를 수정하고 연관된 여행 루트 확인")
    void updateAreaAndCheckTravelRoot() throws Exception {
        // 지역 이름 수정
        AreaDto areaDto = AreaDto.builder()
                .areaName("제주특별자치도")
                .build();

        mockMvc.perform(put("/api/areas/{auid}", testArea.getAuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(areaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.areaName", is("제주특별자치도")));

        // 변경된 지역 이름으로 조회
        mockMvc.perform(get("/api/areas/name/{areaName}", "제주특별자치도"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.areaName", is("제주특별자치도")));

        // 연관된 여행 루트 조회 - 내부적으로는 지역이 변경되었지만 API 응답에서는 areaId만 확인 가능
        mockMvc.perform(get("/api/travel-roots/{truid}", testTravelRoot.getTruid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.areaId", is(testArea.getAuid().intValue())));
    }
}