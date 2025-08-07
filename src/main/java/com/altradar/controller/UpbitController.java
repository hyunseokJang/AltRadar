package com.altradar.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.altradar.service.UpbitDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upbit")
public class UpbitController {

    private final UpbitDataService upbitDataService;

    // ✅ 현재 시세 수집
    /**
     * 정기적인 데이터 업데이트
     */
    @PostMapping("/collect")
    public Map<String, Object> collectCurrentData(@RequestParam(required = false) List<String> markets) {
        Map<String, Object> res = new HashMap<>();
        try {
            if (markets == null || markets.isEmpty()) {
                markets = upbitDataService.getAllKrwMarkets();
            }
            List<Map<String, Object>> data = upbitDataService.getMultipleMarketAnalysis(markets);

            res.put("status", "success");
            res.put("message", "현재 시세 수집 완료 (" + data.size() + "개 마켓)");
            res.put("timestamp", Instant.now().getEpochSecond());
            res.put("data", data);
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    // ✅ 과거 데이터 수집 (예: 최근 200개 1분봉)
    @PostMapping("/collect/historical")
    public Map<String, Object> collectHistoricalData(@RequestParam(required = false) String market) {
        Map<String, Object> res = new HashMap<>();
        try {
            if (market == null || market.isBlank()) {
                market = "KRW-BTC"; // 기본값: 비트코인
            }
            Map<String, Object> data = upbitDataService.getMarketAnalysis(market);

            res.put("status", "success");
            res.put("timestamp", Instant.now().getEpochSecond());
            res.put("data", data);
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    // ✅ Upbit API 상태 확인
    @GetMapping("/status")
    public Map<String, Object> getUpbitApiStatus() {
        Map<String, Object> res = new HashMap<>();
        try {
            List<String> markets = upbitDataService.getAllKrwMarkets();
            res.put("status", "running");
            res.put("marketCount", markets.size());
            res.put("timestamp", Instant.now().getEpochSecond());
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }
}