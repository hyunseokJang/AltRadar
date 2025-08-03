package com.altradar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpbitApiResponse {
    
    @JsonProperty("market")
    private String market; // KRW-BTC
    
    @JsonProperty("trade_date")
    private String tradeDate;
    
    @JsonProperty("trade_time")
    private String tradeTime;
    
    @JsonProperty("trade_price")
    private BigDecimal tradePrice;
    
    @JsonProperty("trade_volume")
    private BigDecimal tradeVolume;
    
    @JsonProperty("prev_closing_price")
    private BigDecimal prevClosingPrice;
    
    @JsonProperty("change")
    private String change; // RISE, EVEN, FALL
    
    @JsonProperty("change_price")
    private BigDecimal changePrice;
    
    @JsonProperty("change_rate")
    private BigDecimal changeRate;
    
    @JsonProperty("high_price")
    private BigDecimal highPrice;
    
    @JsonProperty("low_price")
    private BigDecimal lowPrice;
    
    @JsonProperty("acc_trade_volume_24h")
    private BigDecimal accTradeVolume24h;
    
    @JsonProperty("acc_trade_price_24h")
    private BigDecimal accTradePrice24h;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    // 추가 필드들
    @JsonProperty("market_warning")
    private String marketWarning;
    
    @JsonProperty("market_cap")
    private BigDecimal marketCap;
    
    @JsonProperty("market_cap_rank")
    private Integer marketCapRank;
    
    // 심볼 추출 메서드
    public String getSymbol() {
        if (market != null && market.contains("-")) {
            return market.split("-")[1];
        }
        return market;
    }
    
    // 코인 ID 추출 메서드
    public String getCoinId() {
        return getSymbol().toLowerCase();
    }
    
    // 24시간 가격 변화율
    public BigDecimal getPriceChangePercentage24h() {
        return changeRate != null ? changeRate.multiply(new BigDecimal("100")) : BigDecimal.ZERO;
    }
    
    // 24시간 가격 변화
    public BigDecimal getPriceChange24h() {
        return changePrice != null ? changePrice : BigDecimal.ZERO;
    }
} 