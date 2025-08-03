package com.altradar.service;

import com.altradar.model.CryptoCoin;
import com.altradar.model.PriceData;
import com.altradar.model.dto.UpbitApiResponse;
import com.altradar.repository.CryptoCoinRepository;
import com.altradar.repository.PriceDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpbitDataService {

    private final WebClient webClient;
    private final CryptoCoinRepository cryptoCoinRepository;
    private final PriceDataRepository priceDataRepository;

    @Value("${upbit.api.base-url:https://api.upbit.com/v1}")
    private String upbitBaseUrl;

    // 주요 알트코인 목록 (KRW 마켓)
    private static final List<String> TARGET_COINS = List.of(
        "KRW-BTC", "KRW-ETH", "KRW-XRP", "KRW-ADA", "KRW-DOGE",
        "KRW-MATIC", "KRW-DOT", "KRW-LTC", "KRW-BCH", "KRW-LINK",
        "KRW-UNI", "KRW-ATOM", "KRW-ETC", "KRW-XLM", "KRW-TRX",
        "KRW-NEO", "KRW-VET", "KRW-ALGO", "KRW-MANA", "KRW-SAND"
    );

    /**
     * Upbit에서 현재 시세 데이터를 수집합니다.
     */
    public void collectCurrentData() {
        log.info("Upbit에서 현재 시세 데이터 수집 시작");
        
        try {
            // 모든 코인 데이터를 한 번에 조회
            List<UpbitApiResponse> marketData = webClient.get()
                .uri(upbitBaseUrl + "/ticker?markets=" + String.join(",", TARGET_COINS))
                .retrieve()
                .bodyToFlux(UpbitApiResponse.class)
                .collectList()
                .block();

            if (marketData != null) {
                for (UpbitApiResponse apiResponse : marketData) {
                    processCoinData(apiResponse);
                }
                log.info("{}개 코인의 데이터 수집 완료", marketData.size());
            }
        } catch (Exception e) {
            log.error("Upbit 데이터 수집 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 개별 코인 데이터를 처리합니다.
     */
    private void processCoinData(UpbitApiResponse apiResponse) {
        try {
            String coinId = apiResponse.getCoinId();
            String symbol = apiResponse.getSymbol();
            
            // 기존 코인 정보 조회 또는 새로 생성
            CryptoCoin coin = cryptoCoinRepository.findByCoinId(coinId)
                .orElse(new CryptoCoin());

            // 코인 정보 업데이트
            coin.setCoinId(coinId);
            coin.setSymbol(symbol);
            coin.setName(symbol); // Upbit는 심볼만 제공하므로 이름도 심볼로 설정
            coin.setCurrentPrice(apiResponse.getTradePrice());
            coin.setMarketCap(apiResponse.getAccTradePrice24h()); // 24시간 거래대금을 시가총액으로 사용
            coin.setVolume24h(apiResponse.getAccTradeVolume24h());
            coin.setPriceChange24h(apiResponse.getPriceChange24h());
            coin.setPriceChangePercentage24h(apiResponse.getPriceChangePercentage24h());
            coin.setLastUpdated(LocalDateTime.now());

            // 데이터베이스에 저장
            coin = cryptoCoinRepository.save(coin);

            // 가격 데이터 저장
            PriceData priceData = new PriceData();
            priceData.setCoin(coin);
            priceData.setPrice(apiResponse.getTradePrice());
            priceData.setVolume(apiResponse.getTradeVolume());
            priceData.setTimestamp(LocalDateTime.now());
            
            priceDataRepository.save(priceData);

            log.debug("코인 데이터 처리 완료: {} - 가격: {}", symbol, apiResponse.getTradePrice());
            
        } catch (Exception e) {
            log.error("코인 데이터 처리 중 오류 발생: {} - {}", apiResponse.getSymbol(), e.getMessage(), e);
        }
    }

    /**
     * 특정 코인의 과거 가격 데이터를 수집합니다.
     */
    public void collectHistoricalData(String market, int count) {
        try {
            String url = String.format("%s/candles/minutes/1?market=%s&count=%d", 
                upbitBaseUrl, market, count);
            
            List<UpbitApiResponse> historicalData = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(UpbitApiResponse.class)
                .collectList()
                .block();

            if (historicalData != null) {
                CryptoCoin coin = cryptoCoinRepository.findByCoinId(market.split("-")[1].toLowerCase())
                    .orElse(null);
                
                if (coin != null) {
                    for (UpbitApiResponse data : historicalData) {
                        PriceData priceData = new PriceData();
                        priceData.setCoin(coin);
                        priceData.setPrice(data.getTradePrice());
                        priceData.setVolume(data.getTradeVolume());
                        priceData.setTimestamp(LocalDateTime.now()); // 실제로는 timestamp를 사용해야 함
                        
                        priceDataRepository.save(priceData);
                    }
                    log.info("{} 코인의 과거 데이터 {}개 수집 완료", market, historicalData.size());
                }
            }
        } catch (Exception e) {
            log.error("과거 데이터 수집 중 오류 발생: {} - {}", market, e.getMessage(), e);
        }
    }

    /**
     * 모든 코인의 과거 데이터를 수집합니다.
     */
    public void collectAllHistoricalData() {
        for (String market : TARGET_COINS) {
            collectHistoricalData(market, 200); // 최근 200개 데이터
            try {
                Thread.sleep(100); // API 호출 제한을 위한 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 스케줄링된 데이터 수집 (5분마다)
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    public void scheduledDataCollection() {
        log.info("스케줄된 데이터 수집 시작");
        collectCurrentData();
    }

    /**
     * 수동으로 데이터 수집을 트리거합니다.
     */
    public void triggerDataCollection() {
        log.info("수동 데이터 수집 트리거");
        collectCurrentData();
    }
} 