package com.altradar.controller;

import com.altradar.model.CryptoCoin;
import com.altradar.model.dto.PumpAnalysisResult;
import com.altradar.repository.CryptoCoinRepository;
import com.altradar.service.CryptoDataService;
import com.altradar.service.TechnicalAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
@Slf4j
public class CryptoController {
    
    private final CryptoCoinRepository cryptoCoinRepository;
    private final CryptoDataService cryptoDataService;
    private final TechnicalAnalysisService technicalAnalysisService;
    
    /**
     * 상위 급등 가능성 코인들을 조회합니다
     */
    @GetMapping("/pump-potential")
    public ResponseEntity<List<PumpAnalysisResult>> getPumpPotential(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "70") BigDecimal minScore) {
        
        List<CryptoCoin> highPotentialCoins = cryptoCoinRepository.findHighPumpPotential(minScore);
        
        List<PumpAnalysisResult> results = highPotentialCoins.stream()
                .limit(limit)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * 상승 트렌드의 고점수 코인들을 조회합니다
     */
    @GetMapping("/bullish-high-potential")
    public ResponseEntity<List<PumpAnalysisResult>> getBullishHighPotential(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "70") BigDecimal minScore) {
        
        List<CryptoCoin> bullishCoins = cryptoCoinRepository.findBullishHighPotential(minScore);
        
        List<PumpAnalysisResult> results = bullishCoins.stream()
                .limit(limit)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * 소형 시가총액 고점수 코인들을 조회합니다
     */
    @GetMapping("/small-cap-high-potential")
    public ResponseEntity<List<PumpAnalysisResult>> getSmallCapHighPotential(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "1000000000") BigDecimal maxMarketCap) {
        
        List<CryptoCoin> smallCapCoins = cryptoCoinRepository.findSmallCapHighPotential(maxMarketCap);
        
        List<PumpAnalysisResult> results = smallCapCoins.stream()
                .limit(limit)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * 고거래량 고점수 코인들을 조회합니다
     */
    @GetMapping("/high-volume-high-potential")
    public ResponseEntity<List<PumpAnalysisResult>> getHighVolumeHighPotential(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "10000000") BigDecimal minVolume) {
        
        List<CryptoCoin> highVolumeCoins = cryptoCoinRepository.findHighVolumeHighPotential(minVolume);
        
        List<PumpAnalysisResult> results = highVolumeCoins.stream()
                .limit(limit)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * 특정 코인의 상세 정보를 조회합니다
     */
    @GetMapping("/{coinId}")
    public ResponseEntity<PumpAnalysisResult> getCoinDetails(@PathVariable String coinId) {
        return cryptoCoinRepository.findByCoinId(coinId)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 모든 코인 목록을 조회합니다
     */
    @GetMapping("/all")
    public ResponseEntity<List<PumpAnalysisResult>> getAllCoins(
            @RequestParam(defaultValue = "100") int limit) {
        
        List<CryptoCoin> allCoins = cryptoCoinRepository.findAll();
        
        List<PumpAnalysisResult> results = allCoins.stream()
                .limit(limit)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * 트렌드별 코인들을 조회합니다
     */
    @GetMapping("/trend/{trend}")
    public ResponseEntity<List<PumpAnalysisResult>> getCoinsByTrend(@PathVariable String trend) {
        try {
            CryptoCoin.TrendType trendType = CryptoCoin.TrendType.valueOf(trend.toUpperCase());
            List<CryptoCoin> coins = cryptoCoinRepository.findByTrend(trendType);
            
            List<PumpAnalysisResult> results = coins.stream()
                    .map(PumpAnalysisResult::fromCryptoCoin)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 리스크 레벨별 코인들을 조회합니다
     */
    @GetMapping("/risk/{riskLevel}")
    public ResponseEntity<List<PumpAnalysisResult>> getCoinsByRiskLevel(@PathVariable String riskLevel) {
        try {
            CryptoCoin.RiskLevel risk = CryptoCoin.RiskLevel.valueOf(riskLevel.toUpperCase());
            List<CryptoCoin> coins = cryptoCoinRepository.findByRiskLevel(risk);
            
            List<PumpAnalysisResult> results = coins.stream()
                    .map(PumpAnalysisResult::fromCryptoCoin)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 데이터 업데이트를 수동으로 트리거합니다
     */
    @PostMapping("/update")
    public ResponseEntity<String> updateData(@RequestParam(defaultValue = "50") int limit) {
        try {
            cryptoDataService.updateAllTopCoins(limit);
            return ResponseEntity.ok("데이터 업데이트가 완료되었습니다.");
        } catch (Exception e) {
            log.error("데이터 업데이트 중 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("데이터 업데이트 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 특정 코인의 분석을 다시 수행합니다
     */
    @PostMapping("/{coinId}/analyze")
    public ResponseEntity<String> reanalyzeCoin(@PathVariable String coinId) {
        try {
            return cryptoCoinRepository.findByCoinId(coinId)
                    .map(coin -> {
                        technicalAnalysisService.analyzeAndUpdateCoin(coin);
                        return ResponseEntity.ok(coin.getSymbol() + " 코인 분석이 완료되었습니다.");
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("{} 코인 분석 중 오류: {}", coinId, e.getMessage());
            return ResponseEntity.internalServerError().body("코인 분석 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 정기적인 데이터 업데이트 (매시간)
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다
    public void scheduledDataUpdate() {
        log.info("정기 데이터 업데이트 시작");
        try {
            cryptoDataService.updateAllTopCoins(50);
            log.info("정기 데이터 업데이트 완료");
        } catch (Exception e) {
            log.error("정기 데이터 업데이트 중 오류: {}", e.getMessage());
        }
    }
} 