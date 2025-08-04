
package com.altradar.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.altradar.service.UpbitDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UpbitController {

    private final UpbitDataService upbitDataService;

    @GetMapping("/api/market-analysis")
    public Map<String, Object> getMarketAnalysis(@RequestParam(required = false) List<String> markets) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (markets == null || markets.isEmpty()) {
                // 업비트 전체 마켓 조회
                markets = upbitDataService.getAllKrwMarkets(); // 새 메서드 필요
            }

            List<Map<String, Object>> data = upbitDataService.getMultipleMarketAnalysis(markets);

            response.put("status", "success");
            response.put("timestamp", Instant.now().getEpochSecond());
            response.put("data", data);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("timestamp", Instant.now().getEpochSecond());
            response.put("message", e.getMessage());
        }
        return response;
    }
}
