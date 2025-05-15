package com.ssafy.logoserver.domain.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.domain.image.dto.TravelImageDto;
import com.ssafy.logoserver.domain.image.entity.TravelImage;
import com.ssafy.logoserver.domain.image.repository.TravelImageRepository;
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
public class TravelImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TravelImageRepository travelImageRepository;

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
    private TravelImage testTravelImage;

    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 데이터 정리
        travelImageRepository.deleteAll();
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

        // 테스트 여행 이미지 생성
        testTravelImage = TravelImage.builder()
                .user(testUser)
                .travel(testTravel)
                .name("성산일출봉 사진")
                .url("https://example.com/seongsan.jpg")
                .build();

        testTravelImage = travelImageRepository.save(testTravelImage);
    }

    @Test
    @DisplayName("모든 여행 이미지 조회")
    void getAllTravelImages() throws Exception {
        mockMvc.perform(get("/api/travel-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].name", is("성산일출봉 사진")));
    }

    @Test
    @DisplayName("ID로 여행 이미지 조회")
    void getTravelImageById() throws Exception {
        mockMvc.perform(get("/api/travel-images/{tiuid}", testTravelImage.getTiuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is("성산일출봉 사진")))
                .andExpect(jsonPath("$.data.url", is("https://example.com/seongsan.jpg")));
    }

    @Test
    @DisplayName("존재하지 않는 여행 이미지 조회 시 404")
    void getTravelImageByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/travel-images/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @DisplayName("사용자 ID로 여행 이미지 목록 조회")
    void getTravelImagesByUserId() throws Exception {
        mockMvc.perform(get("/api/travel-images/user/{userId}", testUser.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].name", is("성산일출봉 사진")));
    }

    @Test
    @DisplayName("여행 ID로 여행 이미지 목록 조회")
    void getTravelImagesByTravelId() throws Exception {
        mockMvc.perform(get("/api/travel-images/travel/{travelId}", testTravel.getTuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].name", is("성산일출봉 사진")));
    }

    @Test
    @DisplayName("사용자와 여행 ID로 여행 이미지 목록 조회")
    void getTravelImagesByUserAndTravelId() throws Exception {
        mockMvc.perform(get("/api/travel-images/user/{userId}/travel/{travelId}",
                        testUser.getUuid(), testTravel.getTuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].name", is("성산일출봉 사진")));
    }

    @Test
    @DisplayName("새 여행 이미지 생성")
    void createTravelImage() throws Exception {
        TravelImageDto travelImageDto = TravelImageDto.builder()
                .userId(testUser.getUuid())
                .travelId(testTravel.getTuid())
                .name("우도 사진")
                .url("https://example.com/udo.jpg")
                .build();

        mockMvc.perform(post("/api/travel-images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelImageDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is("우도 사진")))
                .andExpect(jsonPath("$.data.url", is("https://example.com/udo.jpg")));
    }

    @Test
    @DisplayName("여행 이미지 정보 업데이트")
    void updateTravelImage() throws Exception {
        TravelImageDto travelImageDto = TravelImageDto.builder()
                .userId(testUser.getUuid())
                .name("수정된 성산일출봉 사진")
                .url("https://example.com/updated-seongsan.jpg")
                .build();

        mockMvc.perform(put("/api/travel-images/{tiuid}", testTravelImage.getTiuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelImageDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is("수정된 성산일출봉 사진")))
                .andExpect(jsonPath("$.data.url", is("https://example.com/updated-seongsan.jpg")));
    }

    @Test
    @DisplayName("여행 이미지 삭제")
    void deleteTravelImage() throws Exception {
        mockMvc.perform(delete("/api/travel-images/{tiuid}", testTravelImage.getTiuid())
                        .param("userId", testUser.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        // 실제로 삭제되었는지 확인
        mockMvc.perform(get("/api/travel-images/{tiuid}", testTravelImage.getTiuid()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("권한 없는 사용자의 여행 이미지 삭제 시도")
    void deleteTravelImageWithoutPermission() throws Exception {
        // 다른 사용자 생성
        User anotherUser = User.builder()
                .id("anotheruser")
                .password(passwordEncoder.encode("password123"))
                .name("다른유저")
                .nickname("다른테스터")
                .birthday(LocalDate.of(1995, 5, 5))
                .address("서울특별시 서초구")
                .phone("010-9876-5432")
                .role(User.Role.USER)
                .build();

        anotherUser = userRepository.save(anotherUser);

        mockMvc.perform(delete("/api/travel-images/{tiuid}", testTravelImage.getTiuid())
                        .param("userId", anotherUser.getUuid().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message").value("여행 이미지 삭제 권한이 없습니다."));
    }
}