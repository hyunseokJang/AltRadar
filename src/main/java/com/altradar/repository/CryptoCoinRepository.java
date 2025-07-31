package com.altradar.repository;

import com.altradar.model.CryptoCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoCoinRepository extends JpaRepository<CryptoCoin, Long> {
    
    Optional<CryptoCoin> findByCoinId(String coinId);
    
    List<CryptoCoin> findByTrend(CryptoCoin.TrendType trend);
    
    List<CryptoCoin> findByRiskLevel(CryptoCoin.RiskLevel riskLevel);
    
    @Query("SELECT c FROM CryptoCoin c WHERE c.pumpScore >= :minScore ORDER BY c.pumpScore DESC")
    List<CryptoCoin> findHighPumpPotential(@Param("minScore") BigDecimal minScore);
    
    @Query("SELECT c FROM CryptoCoin c WHERE c.pumpScore >= :minScore AND c.trend = 'BULLISH' ORDER BY c.pumpScore DESC")
    List<CryptoCoin> findBullishHighPotential(@Param("minScore") BigDecimal minScore);
    
    @Query("SELECT c FROM CryptoCoin c ORDER BY c.pumpScore DESC LIMIT :limit")
    List<CryptoCoin> findTopPumpPotential(@Param("limit") int limit);
    
    @Query("SELECT c FROM CryptoCoin c WHERE c.marketCap <= :maxMarketCap ORDER BY c.pumpScore DESC")
    List<CryptoCoin> findSmallCapHighPotential(@Param("maxMarketCap") BigDecimal maxMarketCap);
    
    @Query("SELECT c FROM CryptoCoin c WHERE c.volume24h >= :minVolume ORDER BY c.pumpScore DESC")
    List<CryptoCoin> findHighVolumeHighPotential(@Param("minVolume") BigDecimal minVolume);
} 