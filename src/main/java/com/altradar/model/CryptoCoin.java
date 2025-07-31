package com.altradar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
} 