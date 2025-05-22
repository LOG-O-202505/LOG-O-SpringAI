package com.ssafy.logoserver.domain.area;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.domain.area.dto.AreaDto;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Area testArea;

    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 데이터 정리
        areaRepository.deleteAll();

        // 테스트 지역 생성
        testArea = Area.builder()
                .build();

        testArea = areaRepository.save(testArea);
    }

    @Test
    @DisplayName("모든 지역 조회")
    void getAllAreas() throws Exception {
        mockMvc.perform(get("/api/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data[0].areaName", is("제주도")));
    }

    @Test
    @DisplayName("ID로 지역 조회")
    void getAreaById() throws Exception {
        mockMvc.perform(get("/api/areas/{auid}", testArea.getAuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.areaName", is("제주도")));
    }

    @Test
    @DisplayName("존재하지 않는 지역 조회 시 404")
    void getAreaByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/areas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @DisplayName("이름으로 지역 조회")
    void getAreaByName() throws Exception {
        mockMvc.perform(get("/api/areas/name/{areaName}", "제주도"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.areaName", is("제주도")));
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 지역 조회 시 404")
    void getAreaByNameNotFound() throws Exception {
        mockMvc.perform(get("/api/areas/name/존재하지않는지역"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")));
    }

    @Test
    @DisplayName("새 지역 생성")
    void createArea() throws Exception {
        AreaDto areaDto = AreaDto.builder()
                .build();

    }

    @Test
    @DisplayName("중복된 이름으로 지역 생성 시 400")
    void createAreaDuplicateName() throws Exception {
        AreaDto areaDto = AreaDto.builder()
                .build();

        mockMvc.perform(post("/api/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(areaDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message").value("이미 존재하는 지역 이름입니다: 제주도"));
    }

    @Test
    @DisplayName("지역 정보 업데이트")
    void updateArea() throws Exception {
        AreaDto areaDto = AreaDto.builder()
                .build();

    }

    @Test
    @DisplayName("지역 삭제")
    void deleteArea() throws Exception {
        mockMvc.perform(delete("/api/areas/{auid}", testArea.getAuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));

        // 실제로 삭제되었는지 확인
        mockMvc.perform(get("/api/areas/{auid}", testArea.getAuid()))
                .andExpect(status().isNotFound());
    }
}