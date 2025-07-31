package com.altradar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "coin_id", nullable = false)
    private CryptoCoin coin;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal price;
    
    @Column(precision = 20, scale = 2)
    private BigDecimal volume;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal rsi;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal macd;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal macdSignal;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal sma20;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal sma50;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal bbUpper;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal bbLower;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal bbMiddle;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal volumeRatio;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal volatility;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal momentum;
} 