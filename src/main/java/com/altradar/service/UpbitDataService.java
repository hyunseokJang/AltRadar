
package com.altradar.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitDataService {
	

	// 글로벌 속도 제한 (초당 약 8~9회 요청)
	private long lastRequestTime = 0;
	private final long REQUEST_INTERVAL_MS = 120;

    private final TechnicalAnalysisService technicalAnalysisService;
    private final RestTemplate restTemplate;

    private static final String UPBIT_TICKER_URL = "https://api.upbit.com/v1/ticker?markets=%s";
    private static final String UPBIT_CANDLES_URL = "https://api.upbit.com/v1/candles/minutes/1?market=%s&count=200";

    public Map<String, Object> getMarketAnalysis(String market) {
        try {
            // 가격 데이터 가져오기
            String candleUrl = String.format(UPBIT_CANDLES_URL, market);
            JSONArray candles = new JSONArray(restTemplate.getForObject(candleUrl, String.class));

            List<Double> prices = new ArrayList<>();
            List<Double> volumes = new ArrayList<>();

            for (int i = candles.length() - 1; i >= 0; i--) {
                JSONObject candle = candles.getJSONObject(i);
                prices.add(candle.getDouble("trade_price"));
                volumes.add(candle.getDouble("candle_acc_trade_volume"));
            }

            // 분석 실행
            Map<String, Object> analysis = technicalAnalysisService.analyze(prices, volumes);

            // 현재가 정보
            String tickerUrl = String.format(UPBIT_TICKER_URL, market);
            JSONArray tickerArr = new JSONArray(restTemplate.getForObject(tickerUrl, String.class));
            JSONObject ticker = tickerArr.getJSONObject(0);

            Map<String, Object> result = new HashMap<>();
            result.put("symbol", market);
            result.put("price", ticker.getDouble("trade_price"));
            result.putAll(analysis);

            return result;
        } catch (Exception e) {
            log.error("Error fetching market analysis for {}: {}", market, e.getMessage());
            return null;
        }
    }

    public List<Map<String, Object>> getMultipleMarketAnalysis(List<String> markets) {
        List<Map<String, Object>> results = new ArrayList<>();

        // 한 번에 10개씩 끊어서 처리
        for (int i = 0; i < markets.size(); i += 10) {
            List<String> batch = markets.subList(i, Math.min(i + 10, markets.size()));

            try {
                // 1. 가격 정보는 batch로 한 번에 가져오기
                String tickerUrl = String.format("https://api.upbit.com/v1/ticker?markets=%s", String.join(",", batch));
                JSONArray tickerArr = new JSONArray(restTemplate.getForObject(tickerUrl, String.class));

                // 2. 각 마켓별 캔들 데이터 가져오기
                for (int j = 0; j < batch.size(); j++) {
                    String market = batch.get(j);
                    JSONObject ticker = tickerArr.getJSONObject(j);

                    // 캔들 API 호출 (속도 제한 적용)
                    rateLimit(); // 요청 전 대기
                    String candleUrl = String.format("https://api.upbit.com/v1/candles/minutes/1?market=%s&count=200", market);
                    JSONArray candles = new JSONArray(restTemplate.getForObject(candleUrl, String.class));

                    List<Double> prices = new ArrayList<>();
                    List<Double> volumes = new ArrayList<>();
                    for (int k = candles.length() - 1; k >= 0; k--) {
                        JSONObject candle = candles.getJSONObject(k);
                        prices.add(candle.getDouble("trade_price"));
                        volumes.add(candle.getDouble("candle_acc_trade_volume"));
                    }

                    // 기술 분석 실행
                    Map<String, Object> analysis = technicalAnalysisService.analyze(prices, volumes);
                    analysis.put("symbol", market);
                    analysis.put("price", ticker.getDouble("trade_price"));

                    results.add(analysis);

                    // 캔들 API 호출 간격 유지
                    rateLimit();
                }

            } catch (Exception e) {
                log.error("Error fetching batch market analysis: {}", e.getMessage(), e);
            }
        }

        return results;
    }
    
    public List<String> getAllKrwMarkets() {
        String url = "https://api.upbit.com/v1/market/all?isDetails=false";
        String response = restTemplate.getForObject(url, String.class);

        org.json.JSONArray arr = new org.json.JSONArray(response);
        return java.util.stream.IntStream.range(0, arr.length())
                .mapToObj(i -> arr.getJSONObject(i).getString("market"))
                .filter(m -> m.startsWith("KRW-"))
                .collect(Collectors.toList());
    }
    private synchronized void rateLimit() {
        long now = System.currentTimeMillis();
        long waitTime = REQUEST_INTERVAL_MS - (now - lastRequestTime);
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ignored) {}
        }
        lastRequestTime = System.currentTimeMillis();
    }
    
}
