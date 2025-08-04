
package com.altradar.service;

import com.altradar.util.TechnicalAnalysisUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TechnicalAnalysisService {

    public Map<String, Object> analyze(List<Double> prices, List<Double> volumes) {
        Map<String, Object> result = new HashMap<>();

        // 이동평균
        result.put("ma5", TechnicalAnalysisUtil.movingAverage(prices, 5));
        result.put("ma20", TechnicalAnalysisUtil.movingAverage(prices, 20));
        result.put("ma60", TechnicalAnalysisUtil.movingAverage(prices, 60));

        // RSI
        result.put("rsi", TechnicalAnalysisUtil.rsi(prices, 14));

        // 볼린저 밴드
        double[] bb = TechnicalAnalysisUtil.bollingerBands(prices, 20, 2);
        result.put("bbUpper", bb[0]);
        result.put("bbMiddle", bb[1]);
        result.put("bbLower", bb[2]);

        // MACD
        double[] macd = TechnicalAnalysisUtil.macd(prices, 12, 26, 9);
        result.put("macdLine", macd[0]);
        result.put("signalLine", macd[1]);

        // 거래량 급등 여부
        result.put("volumeSpike", TechnicalAnalysisUtil.isVolumeSpike(volumes, 2.0));

        // Pump Score 계산
        double score = calculatePumpScore(result);
        result.put("pumpScore", score);

        return result;
    }

    private double calculatePumpScore(Map<String, Object> indicators) {
        double score = 0;

        // 예시 점수 계산
        Double rsi = (Double) indicators.get("rsi");
        if (rsi != null && rsi < 70 && rsi > 50) score += 20;

        Boolean volumeSpike = (Boolean) indicators.get("volumeSpike");
        if (volumeSpike != null && volumeSpike) score += 30;

        Double ma5 = (Double) indicators.get("ma5");
        Double ma20 = (Double) indicators.get("ma20");
        if (ma5 != null && ma20 != null && ma5 > ma20) score += 30;

        Double macdLine = (Double) indicators.get("macdLine");
        Double signalLine = (Double) indicators.get("signalLine");
        if (macdLine != null && signalLine != null && macdLine > signalLine) score += 20;

        return Math.min(score, 100);
    }
    
    
}
