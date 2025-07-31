package com.altradar.service;

import com.altradar.model.CryptoCoin;
import com.altradar.model.PriceData;
import com.altradar.repository.CryptoCoinRepository;
import com.altradar.repository.PriceDataRepository;
import com.altradar.util.TechnicalAnalysisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicalAnalysisService {
    
    private final PriceDataRepository priceDataRepository;
    private final CryptoCoinRepository cryptoCoinRepository;
    
    /**
     * 코인에 대한 기술적 분석을 수행하고 업데이트합니다
     */
    public void analyzeAndUpdateCoin(CryptoCoin coin) {
        try {
            List<PriceData> priceDataList = priceDataRepository.findRecentByCoin(coin, 50);
            
            if (priceDataList.size() < 30) {
                log.warn("{} 코인의 충분한 가격 데이터가 없습니다. 필요: 30, 현재: {}", 
                        coin.getSymbol(), priceDataList.size());
                return;
            }
            
            // 가격 데이터를 시간순으로 정렬
            priceDataList.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
            
            // 기술적 지표 계산
            List<BigDecimal> prices = priceDataList.stream()
                    .map(PriceData::getPrice)
                    .collect(Collectors.toList());
            
            // RSI 계산
            BigDecimal rsi = TechnicalAnalysisUtil.calculateRSI(prices, 14);
            
            // MACD 계산
            BigDecimal macd = TechnicalAnalysisUtil.calculateMACD(prices);
            BigDecimal macdSignal = TechnicalAnalysisUtil.calculateMACDSignal(prices);
            
            // 이동평균선 계산
            BigDecimal sma20 = TechnicalAnalysisUtil.calculateSMA(prices, 20);
            BigDecimal sma50 = TechnicalAnalysisUtil.calculateSMA(prices, 50);
            
            // 볼린저 밴드 계산
            TechnicalAnalysisUtil.BollingerBands bb = TechnicalAnalysisUtil.calculateBollingerBands(prices, 20, 2.0);
            
            // 변동성 계산
            BigDecimal volatility = TechnicalAnalysisUtil.calculateVolatility(prices, 20);
            
            // 모멘텀 계산
            BigDecimal momentum = TechnicalAnalysisUtil.calculateMomentum(prices, 10);
            
            // 거래량 비율 계산 (최근 거래량 / 평균 거래량)
            BigDecimal avgVolume = priceDataList.stream()
                    .map(PriceData::getVolume)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(priceDataList.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal currentVolume = priceDataList.get(priceDataList.size() - 1).getVolume();
            BigDecimal volumeRatio = currentVolume.divide(avgVolume, 2, RoundingMode.HALF_UP);
            
            // 급등 가능성 점수 계산
            BigDecimal pumpScore = calculatePumpScore(
                    rsi, macd, macdSignal, sma20, sma50, bb, 
                    volatility, momentum, volumeRatio, prices.get(prices.size() - 1)
            );
            
            // 트렌드 분석
            CryptoCoin.TrendType trend = analyzeTrend(prices.get(prices.size() - 1), sma20, sma50);
            
            // 리스크 레벨 분석
            CryptoCoin.RiskLevel riskLevel = analyzeRiskLevel(pumpScore, volatility);
            
            // 신호들 생성
            List<String> signals = generateSignals(rsi, macd, macdSignal, bb, volumeRatio, momentum);
            
            // 코인 정보 업데이트
            coin.setPumpScore(pumpScore);
            coin.setTrend(trend);
            coin.setRiskLevel(riskLevel);
            coin.setSignals(String.join(",", signals));
            coin.setLastUpdated(LocalDateTime.now());
            
            cryptoCoinRepository.save(coin);
            
            log.info("{} 코인 분석 완료 - 점수: {}, 트렌드: {}, 리스크: {}", 
                    coin.getSymbol(), pumpScore, trend, riskLevel);
            
        } catch (Exception e) {
            log.error("{} 코인 분석 중 오류: {}", coin.getSymbol(), e.getMessage());
        }
    }
    
    /**
     * 급등 가능성 점수를 계산합니다
     */
    private BigDecimal calculatePumpScore(BigDecimal rsi, BigDecimal macd, BigDecimal macdSignal,
                                       BigDecimal sma20, BigDecimal sma50, 
                                       TechnicalAnalysisUtil.BollingerBands bb,
                                       BigDecimal volatility, BigDecimal momentum, 
                                       BigDecimal volumeRatio, BigDecimal currentPrice) {
        
        BigDecimal score = BigDecimal.valueOf(50); // 기본 점수
        
        // RSI 분석 (30 이하일 때 매수 신호)
        if (rsi.compareTo(BigDecimal.valueOf(30)) < 0) {
            score = score.add(BigDecimal.valueOf(20));
        } else if (rsi.compareTo(BigDecimal.valueOf(40)) < 0) {
            score = score.add(BigDecimal.valueOf(10));
        } else if (rsi.compareTo(BigDecimal.valueOf(70)) > 0) {
            score = score.subtract(BigDecimal.valueOf(10));
        }
        
        // MACD 분석
        if (macd.compareTo(macdSignal) > 0 && macd.compareTo(BigDecimal.ZERO) > 0) {
            score = score.add(BigDecimal.valueOf(15));
        } else if (macd.compareTo(macdSignal) < 0) {
            score = score.subtract(BigDecimal.valueOf(10));
        }
        
        // 볼린저 밴드 분석 (하단 밴드 근처)
        BigDecimal bbPosition = currentPrice.subtract(bb.getLowerBand())
                .divide(bb.getUpperBand().subtract(bb.getLowerBand()), 4, RoundingMode.HALF_UP);
        
        if (bbPosition.compareTo(BigDecimal.valueOf(0.2)) < 0) {
            score = score.add(BigDecimal.valueOf(15));
        } else if (bbPosition.compareTo(BigDecimal.valueOf(0.8)) > 0) {
            score = score.subtract(BigDecimal.valueOf(10));
        }
        
        // 거래량 분석
        if (volumeRatio.compareTo(BigDecimal.valueOf(1.5)) > 0) {
            score = score.add(BigDecimal.valueOf(10));
        } else if (volumeRatio.compareTo(BigDecimal.valueOf(0.5)) < 0) {
            score = score.subtract(BigDecimal.valueOf(5));
        }
        
        // 가격 모멘텀
        if (momentum.compareTo(BigDecimal.ZERO) > 0) {
            score = score.add(BigDecimal.valueOf(10));
        } else {
            score = score.subtract(BigDecimal.valueOf(5));
        }
        
        // 이동평균선 분석
        if (currentPrice.compareTo(sma20) > 0 && sma20.compareTo(sma50) > 0) {
            score = score.add(BigDecimal.valueOf(10));
        } else if (currentPrice.compareTo(sma20) < 0 && sma20.compareTo(sma50) < 0) {
            score = score.subtract(BigDecimal.valueOf(10));
        }
        
        // 변동성 분석
        if (volatility.compareTo(BigDecimal.valueOf(0.02)) > 0 && 
            volatility.compareTo(BigDecimal.valueOf(0.1)) < 0) {
            score = score.add(BigDecimal.valueOf(5));
        } else if (volatility.compareTo(BigDecimal.valueOf(0.15)) > 0) {
            score = score.subtract(BigDecimal.valueOf(5));
        }
        
        // 점수 정규화 (0-100)
        return score.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
    }
    
    /**
     * 트렌드를 분석합니다
     */
    private CryptoCoin.TrendType analyzeTrend(BigDecimal currentPrice, BigDecimal sma20, BigDecimal sma50) {
        if (currentPrice.compareTo(sma20) > 0 && sma20.compareTo(sma50) > 0) {
            return CryptoCoin.TrendType.BULLISH;
        } else if (currentPrice.compareTo(sma20) < 0 && sma20.compareTo(sma50) < 0) {
            return CryptoCoin.TrendType.BEARISH;
        } else {
            return CryptoCoin.TrendType.SIDEWAYS;
        }
    }
    
    /**
     * 리스크 레벨을 분석합니다
     */
    private CryptoCoin.RiskLevel analyzeRiskLevel(BigDecimal pumpScore, BigDecimal volatility) {
        if (pumpScore.compareTo(BigDecimal.valueOf(80)) > 0) {
            return CryptoCoin.RiskLevel.LOW;
        } else if (pumpScore.compareTo(BigDecimal.valueOf(60)) > 0) {
            return CryptoCoin.RiskLevel.MEDIUM;
        } else {
            return CryptoCoin.RiskLevel.HIGH;
        }
    }
    
    /**
     * 매매 신호들을 생성합니다
     */
    private List<String> generateSignals(BigDecimal rsi, BigDecimal macd, BigDecimal macdSignal,
                                       TechnicalAnalysisUtil.BollingerBands bb,
                                       BigDecimal volumeRatio, BigDecimal momentum) {
        List<String> signals = new ArrayList<>();
        
        if (rsi.compareTo(BigDecimal.valueOf(30)) < 0) {
            signals.add("RSI Oversold");
        }
        
        if (macd.compareTo(macdSignal) > 0) {
            signals.add("MACD Bullish");
        }
        
        if (volumeRatio.compareTo(BigDecimal.valueOf(1.5)) > 0) {
            signals.add("High Volume");
        }
        
        if (momentum.compareTo(BigDecimal.ZERO) > 0) {
            signals.add("Positive Momentum");
        }
        
        return signals;
    }
} 