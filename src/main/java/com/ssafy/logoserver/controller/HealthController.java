package com.ssafy.logoserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Health Check API", description = "서버 상태 확인 API")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 동작하는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정상 동작",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HealthResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "서버가 정상적으로 동작 중입니다.");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @Getter
    private static class HealthResponse {
        private final String status = "UP";
        private final String message = "서버가 정상적으로 동작 중입니다.";
        private final long timestamp = System.currentTimeMillis();
    }
}