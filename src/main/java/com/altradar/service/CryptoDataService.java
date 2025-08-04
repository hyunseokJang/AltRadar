package com.altradar.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.altradar.model.CryptoCoin;
import com.altradar.model.PriceData;
import com.altradar.model.dto.CryptoApiResponse;
import com.altradar.repository.CryptoCoinRepository;
import com.altradar.repository.PriceDataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoDataService {
    
    private final RestTemplate restTemplate;
    private final CryptoCoinRepository cryptoCoinRepository;
    private final PriceDataRepository priceDataRepository;
    private final UpbitDataService upbitDataService;
    
    private static final String COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3";
    
    /**
     * 상위 알트코인 목록을 가져옵니다
     */
    public List<CryptoApiResponse> getTopCoins(int limit) {
        try {
            String url = COINGECKO_BASE_URL + "/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=" 
                         + limit + "&page=1&sparkline=false";

            CryptoApiResponse[] response = restTemplate.getForObject(url, CryptoApiResponse[].class);
            return response != null ? List.of(response) : List.of();
        } catch (Exception e) {
            log.error("상위 코인 데이터 수집 중 오류: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 특정 코인의 가격 데이터를 가져옵니다
     */
    public List<PriceData> getCoinPriceData(String coinId, int days) {
        try {
            String url = String.format("%s/coins/%s/market_chart?vs_currency=usd&days=%d",
                    COINGECKO_BASE_URL, coinId, days);

            String jsonResponse = restTemplate.getForObject(url, String.class);
            return parsePriceData(jsonResponse);
        } catch (Exception e) {
            log.error("{} 코인 가격 데이터 수집 중 오류: {}", coinId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 코인 정보를 저장하거나 업데이트합니다
     */
    public CryptoCoin saveOrUpdateCoin(CryptoApiResponse apiResponse) {
        Optional<CryptoCoin> existingCoin = cryptoCoinRepository.findByCoinId(apiResponse.getId());
        
        CryptoCoin coin;
        if (existingCoin.isPresent()) {
            coin = existingCoin.get();
            coin.setCurrentPrice(apiResponse.getCurrentPrice());
            coin.setMarketCap(apiResponse.getMarketCap());
            coin.setVolume24h(apiResponse.getTotalVolume());
            coin.setPriceChange24h(apiResponse.getPriceChange24h());
            coin.setPriceChangePercentage24h(apiResponse.getPriceChangePercentage24h());
            coin.setLastUpdated(LocalDateTime.now());
        } else {
            coin = new CryptoCoin();
            coin.setCoinId(apiResponse.getId());
            coin.setName(apiResponse.getName());
            coin.setSymbol(apiResponse.getSymbol().toUpperCase());
            coin.setCurrentPrice(apiResponse.getCurrentPrice());
            coin.setMarketCap(apiResponse.getMarketCap());
            coin.setVolume24h(apiResponse.getTotalVolume());
            coin.setPriceChange24h(apiResponse.getPriceChange24h());
            coin.setPriceChangePercentage24h(apiResponse.getPriceChangePercentage24h());
            coin.setLastUpdated(LocalDateTime.now());
        }
        
        return cryptoCoinRepository.save(coin);
    }
    
    /**
     * 가격 데이터를 저장합니다
     */
    public void savePriceData(CryptoCoin coin, List<PriceData> priceDataList) {
        for (PriceData priceData : priceDataList) {
            priceData.setCoin(coin);
            priceDataRepository.save(priceData);
        }
    }
    
    
    
    /**
     * API 응답을 파싱하여 PriceData 리스트로 변환합니다
     */
    private List<PriceData> parsePriceData(String jsonResponse) {
        // 실제 구현에서는 JSON 파싱을 수행합니다
        // 여기서는 간단한 예시로 대체합니다
        return List.of();
    }
    
    /**
     * 업비트 상위 코인 데이터 조회 후 DB 업데이트
     */
    public void updateAllTopCoins(int limit) {
        try {
            // 업비트 마켓 전체 목록 조회
            String marketsUrl = "https://api.upbit.com/v1/market/all?isDetails=false";
            String response = restTemplate.getForObject(marketsUrl, String.class);

            // JSON 파싱
            org.json.JSONArray marketsArr = new org.json.JSONArray(response);

            // KRW 마켓만 필터링
            List<String> markets =
                    java.util.stream.IntStream.range(0, marketsArr.length())
                            .mapToObj(i -> marketsArr.getJSONObject(i).getString("market"))
                            .filter(market -> market.startsWith("KRW-"))
                            .limit(limit) // 상위 limit 개만
                            .collect(Collectors.toList());

            // 분석 실행
            List<Map<String, Object>> analysisResults = upbitDataService.getMultipleMarketAnalysis(markets);

            // DB 저장/업데이트
            for (Map<String, Object> analysis : analysisResults) {
                CryptoCoin coin = CryptoCoin.fromAnalysisResult(analysis);
                cryptoCoinRepository.save(coin);
            }

            log.info("총 {}개 코인 데이터 업데이트 완료", markets.size());
        } catch (Exception e) {
            log.error("코인 데이터 업데이트 중 오류: {}", e.getMessage(), e);
        }
    }
} 