package com.altradar.controller;

import com.altradar.service.UpbitDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upbit")
@RequiredArgsConstructor
@Slf4j
public class UpbitController {

    private final UpbitDataService upbitDataService;

    /**
     * 현재 시세 데이터 수집을 트리거합니다.
     */
    @PostMapping("/collect")
    public ResponseEntity<Map<String, Object>> collectData() {
        try {
            upbitDataService.triggerDataCollection();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "데이터 수집이 시작되었습니다.");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("데이터 수집 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "데이터 수집 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 과거 데이터 수집을 트리거합니다.
     */
    @PostMapping("/collect/historical")
    public ResponseEntity<Map<String, Object>> collectHistoricalData() {
        try {
            upbitDataService.collectAllHistoricalData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "과거 데이터 수집이 완료되었습니다.");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("과거 데이터 수집 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "과거 데이터 수집 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 특정 코인의 과거 데이터를 수집합니다.
     */
    @PostMapping("/collect/{market}")
    public ResponseEntity<Map<String, Object>> collectCoinData(
            @PathVariable String market,
            @RequestParam(defaultValue = "200") int count) {
        try {
            upbitDataService.collectHistoricalData(market, count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", market + " 코인의 데이터 수집이 완료되었습니다.");
            response.put("market", market);
            response.put("count", count);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("{} 코인 데이터 수집 중 오류 발생: {}", market, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "데이터 수집 중 오류가 발생했습니다: " + e.getMessage());
            response.put("market", market);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API 상태를 확인합니다.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "running");
        response.put("service", "Upbit Data Service");
        response.put("timestamp", System.currentTimeMillis());
        response.put("endpoints", Map.of(
            "collect", "POST /api/upbit/collect",
            "historical", "POST /api/upbit/collect/historical",
            "coin", "POST /api/upbit/collect/{market}"
        ));
        
        return ResponseEntity.ok(response);
    }
} 