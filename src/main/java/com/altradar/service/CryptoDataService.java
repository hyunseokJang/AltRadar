package com.altradar.service;

import com.altradar.model.CryptoCoin;
import com.altradar.model.PriceData;
import com.altradar.model.dto.CryptoApiResponse;
import com.altradar.repository.CryptoCoinRepository;
import com.altradar.repository.PriceDataRepository;
import com.altradar.util.TechnicalAnalysisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoDataService {
    
    private final WebClient webClient;
    private final CryptoCoinRepository cryptoCoinRepository;
    private final PriceDataRepository priceDataRepository;
    private final TechnicalAnalysisService technicalAnalysisService;
    
    private static final String COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3";
    
    /**
     * 상위 알트코인 목록을 가져옵니다
     */
    public List<CryptoApiResponse> getTopCoins(int limit) {
        try {
            return webClient.get()
                    .uri(COINGECKO_BASE_URL + "/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=" + limit + "&page=1&sparkline=false")
                    .retrieve()
                    .bodyToMono(CryptoApiResponse[].class)
                    .map(List::of)
                    .block();
        } catch (Exception e) {
            log.error("상위 코인 데이터 수집 중 오류: {}", e.getMessage());
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
            
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(this::parsePriceData)
                    .block();
        } catch (Exception e) {
            log.error("{} 코인 가격 데이터 수집 중 오류: {}", coinId, e.getMessage());
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
     * 모든 상위 코인을 업데이트합니다
     */
    public void updateAllTopCoins(int limit) {
        List<CryptoApiResponse> topCoins = getTopCoins(limit);
        
        for (CryptoApiResponse coinData : topCoins) {
            try {
                CryptoCoin savedCoin = saveOrUpdateCoin(coinData);
                
                // 가격 데이터 수집 및 저장
                List<PriceData> priceDataList = getCoinPriceData(coinData.getId(), 30);
                if (!priceDataList.isEmpty()) {
                    savePriceData(savedCoin, priceDataList);
                    
                    // 기술적 분석 수행
                    technicalAnalysisService.analyzeAndUpdateCoin(savedCoin);
                }
                
                // API 호출 제한을 위한 지연
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("{} 코인 업데이트 중 오류: {}", coinData.getId(), e.getMessage());
            }
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
} 