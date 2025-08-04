package com.altradar.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "crypto_coins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoCoin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String coinId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(precision = 20, scale = 8)
    private BigDecimal currentPrice;
    
    @Column(precision = 20, scale = 2)
    private BigDecimal marketCap;
    
    @Column(precision = 20, scale = 2)
    private BigDecimal volume24h;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal priceChange24h;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal priceChangePercentage24h;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal pumpScore;
    
    @Enumerated(EnumType.STRING)
    private TrendType trend;
    
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;
    
    private LocalDateTime lastUpdated;
    
    @Column(columnDefinition = "TEXT")
    private String signals;
    
    public enum TrendType {
        BULLISH, BEARISH, SIDEWAYS
    }
    
    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }
    
    public static CryptoCoin fromAnalysisResult(Map<String, Object> analysis) {
        CryptoCoin coin = new CryptoCoin();

        coin.setCoinId((String) analysis.get("symbol"));
        coin.setSymbol((String) analysis.get("symbol"));
        coin.setName((String) analysis.getOrDefault("name", (String) analysis.get("symbol")));

        // 가격 관련 값 변환 (null 안전 처리)
        coin.setCurrentPrice(toBigDecimal(analysis.get("price")));
        coin.setMarketCap(toBigDecimal(analysis.get("marketCap")));
        coin.setVolume24h(toBigDecimal(analysis.get("volume24h")));
        coin.setPriceChange24h(toBigDecimal(analysis.get("priceChange24h")));
        coin.setPriceChangePercentage24h(toBigDecimal(analysis.get("priceChangePercentage24h")));
        coin.setPumpScore(toBigDecimal(analysis.get("pumpScore")));

        // 트렌드와 리스크는 분석 결과에 있으면 설정
        if (analysis.containsKey("trend") && analysis.get("trend") != null) {
            try {
                coin.setTrend(TrendType.valueOf(((String) analysis.get("trend")).toUpperCase()));
            } catch (IllegalArgumentException e) {
                coin.setTrend(null);
            }
        }

        if (analysis.containsKey("riskLevel") && analysis.get("riskLevel") != null) {
            try {
                coin.setRiskLevel(RiskLevel.valueOf(((String) analysis.get("riskLevel")).toUpperCase()));
            } catch (IllegalArgumentException e) {
                coin.setRiskLevel(null);
            }
        }

        coin.setLastUpdated(LocalDateTime.now());
        coin.setSignals((String) analysis.get("signals"));

        return coin;
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
} 