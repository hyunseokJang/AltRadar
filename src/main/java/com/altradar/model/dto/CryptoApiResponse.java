package com.altradar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CryptoApiResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("current_price")
    private BigDecimal currentPrice;
    
    @JsonProperty("market_cap")
    private BigDecimal marketCap;
    
    @JsonProperty("total_volume")
    private BigDecimal totalVolume;
    
    @JsonProperty("price_change_24h")
    private BigDecimal priceChange24h;
    
    @JsonProperty("price_change_percentage_24h")
    private BigDecimal priceChangePercentage24h;
    
    @JsonProperty("market_cap_rank")
    private Integer marketCapRank;
    
    @JsonProperty("high_24h")
    private BigDecimal high24h;
    
    @JsonProperty("low_24h")
    private BigDecimal low24h;
} 