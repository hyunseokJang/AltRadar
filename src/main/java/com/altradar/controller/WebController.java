package com.altradar.controller;

import com.altradar.model.CryptoCoin;
import com.altradar.model.dto.PumpAnalysisResult;
import com.altradar.repository.CryptoCoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class WebController {
    
    private final CryptoCoinRepository cryptoCoinRepository;
    
    @GetMapping("/saved-dashboard")
    public String savedDashboard() {
        // templates/saved-dashboard.html 렌더링
        return "saved-dashboard";
    }
    
    /**
     * 메인 대시보드 페이지
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 상위 급등 가능성 코인들
        List<CryptoCoin> topPumpCoins = cryptoCoinRepository.findHighPumpPotential(BigDecimal.valueOf(70));
        List<PumpAnalysisResult> topPumpResults = topPumpCoins.stream()
                .limit(10)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        // 상승 트렌드 고점수 코인들
        List<CryptoCoin> bullishCoins = cryptoCoinRepository.findBullishHighPotential(BigDecimal.valueOf(70));
        List<PumpAnalysisResult> bullishResults = bullishCoins.stream()
                .limit(10)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        // 소형 시가총액 고점수 코인들
        List<CryptoCoin> smallCapCoins = cryptoCoinRepository.findSmallCapHighPotential(BigDecimal.valueOf(1000000000));
        List<PumpAnalysisResult> smallCapResults = smallCapCoins.stream()
                .limit(10)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        model.addAttribute("topPumpCoins", topPumpResults);
        model.addAttribute("bullishCoins", bullishResults);
        model.addAttribute("smallCapCoins", smallCapResults);
        model.addAttribute("ts", System.currentTimeMillis());
        
        return "dashboard";
    }
    
    /**
     * 급등 가능성 코인 목록 페이지
     */
    @GetMapping("/pump-potential")
    public String pumpPotential(@RequestParam(defaultValue = "70") BigDecimal minScore, Model model) {
        List<CryptoCoin> highPotentialCoins = cryptoCoinRepository.findHighPumpPotential(minScore);
        List<PumpAnalysisResult> results = highPotentialCoins.stream()
                .limit(50)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        model.addAttribute("coins", results);
        model.addAttribute("minScore", minScore);
        
        return "pump-potential";
    }
    
    /**
     * 상승 트렌드 코인 목록 페이지
     */
    @GetMapping("/bullish")
    public String bullishCoins(@RequestParam(defaultValue = "70") BigDecimal minScore, Model model) {
        List<CryptoCoin> bullishCoins = cryptoCoinRepository.findBullishHighPotential(minScore);
        List<PumpAnalysisResult> results = bullishCoins.stream()
                .limit(50)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        model.addAttribute("coins", results);
        model.addAttribute("minScore", minScore);
        
        return "bullish";
    }
    
    /**
     * 소형 시가총액 코인 목록 페이지
     */
    @GetMapping("/small-cap")
    public String smallCapCoins(@RequestParam(defaultValue = "1000000000") BigDecimal maxMarketCap, Model model) {
        List<CryptoCoin> smallCapCoins = cryptoCoinRepository.findSmallCapHighPotential(maxMarketCap);
        List<PumpAnalysisResult> results = smallCapCoins.stream()
                .limit(50)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        model.addAttribute("coins", results);
        model.addAttribute("maxMarketCap", maxMarketCap);
        
        return "small-cap";
    }
    
    /**
     * 모든 코인 목록 페이지
     */
    @GetMapping("/all-coins")
    public String allCoins(@RequestParam(defaultValue = "100") int limit, Model model) {
        List<CryptoCoin> allCoins = cryptoCoinRepository.findAll();
        List<PumpAnalysisResult> results = allCoins.stream()
                .limit(limit)
                .map(PumpAnalysisResult::fromCryptoCoin)
                .collect(Collectors.toList());
        
        model.addAttribute("coins", results);
        model.addAttribute("limit", limit);
        
        return "all-coins";
    }
    
    /**
     * 코인 상세 정보 페이지
     */
    @GetMapping("/coin/{coinId}")
    public String coinDetails(@PathVariable String coinId, Model model) {
        return cryptoCoinRepository.findByCoinId(coinId)
                .map(coin -> {
                    PumpAnalysisResult result = PumpAnalysisResult.fromCryptoCoin(coin);
                    model.addAttribute("coin", result);
                    return "coin-details";
                })
                .orElse("redirect:/");
    }
} 