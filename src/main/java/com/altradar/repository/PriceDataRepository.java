package com.altradar.repository;

import com.altradar.model.PriceData;
import com.altradar.model.CryptoCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceDataRepository extends JpaRepository<PriceData, Long> {
    
    List<PriceData> findByCoinOrderByTimestampDesc(CryptoCoin coin);
    
    @Query("SELECT pd FROM PriceData pd WHERE pd.coin = :coin AND pd.timestamp >= :startDate ORDER BY pd.timestamp ASC")
    List<PriceData> findByCoinAndDateRange(@Param("coin") CryptoCoin coin, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT pd FROM PriceData pd WHERE pd.coin = :coin ORDER BY pd.timestamp DESC LIMIT :limit")
    List<PriceData> findRecentByCoin(@Param("coin") CryptoCoin coin, @Param("limit") int limit);
    
    @Query("SELECT pd FROM PriceData pd WHERE pd.coin = :coin AND pd.timestamp >= :startDate AND pd.timestamp <= :endDate ORDER BY pd.timestamp ASC")
    List<PriceData> findByCoinAndDateRange(@Param("coin") CryptoCoin coin, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(pd.price) FROM PriceData pd WHERE pd.coin = :coin AND pd.timestamp >= :startDate")
    Double getAveragePriceByCoinAndDateRange(@Param("coin") CryptoCoin coin, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT MAX(pd.price) FROM PriceData pd WHERE pd.coin = :coin AND pd.timestamp >= :startDate")
    Double getMaxPriceByCoinAndDateRange(@Param("coin") CryptoCoin coin, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT MIN(pd.price) FROM PriceData pd WHERE pd.coin = :coin AND pd.timestamp >= :startDate")
    Double getMinPriceByCoinAndDateRange(@Param("coin") CryptoCoin coin, @Param("startDate") LocalDateTime startDate);
} 