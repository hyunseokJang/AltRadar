package com.altradar.model.dto;

import com.altradar.model.CryptoCoin;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PumpAnalysisResult {
    
    private String coinId;
    private String name;
    private String symbol;
    private BigDecimal currentPrice;
    private BigDecimal pumpScore;
    private CryptoCoin.TrendType trend;
    private CryptoCoin.RiskLevel riskLevel;
    private List<String> signals;
    private BigDecimal rsi;
    private BigDecimal volumeRatio;
    private BigDecimal volatility;
    private LocalDateTime lastUpdated;
    private String recommendation;
    
    public static PumpAnalysisResult fromCryptoCoin(CryptoCoin coin) {
        return PumpAnalysisResult.builder()
                .coinId(coin.getCoinId())
                .name(coin.getName())
                .symbol(coin.getSymbol())
                .currentPrice(coin.getCurrentPrice())
                .pumpScore(coin.getPumpScore())
                .trend(coin.getTrend())
                .riskLevel(coin.getRiskLevel())
                .signals(coin.getSignals() != null ? 
                    List.of(coin.getSignals().split(",")) : List.of())
                .lastUpdated(coin.getLastUpdated())
                .build();
    }
} 