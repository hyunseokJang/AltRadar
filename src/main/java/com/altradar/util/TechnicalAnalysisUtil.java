package com.altradar.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;

@Component
public class TechnicalAnalysisUtil {
    
    /**
     * Simple Moving Average 계산
     */
    public static BigDecimal calculateSMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum = sum.add(prices.get(i));
        }
        
        return sum.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
    }
    
    /**
     * Exponential Moving Average 계산
     */
    public static BigDecimal calculateEMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = calculateSMA(prices.subList(0, period), period);
        
        for (int i = period; i < prices.size(); i++) {
            ema = prices.get(i).multiply(multiplier)
                    .add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
        }
        
        return ema.setScale(8, RoundingMode.HALF_UP);
    }
    
    /**
     * RSI (Relative Strength Index) 계산
     */
    public static BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
        if (prices.size() < period + 1) {
            return BigDecimal.ZERO;
        }
        
        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();
        
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }
        
        BigDecimal avgGain = calculateSMA(gains, period);
        BigDecimal avgLoss = calculateSMA(losses, period);
        
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal rs = avgGain.divide(avgLoss, 8, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(100)
                        .divide(BigDecimal.ONE.add(rs), 8, RoundingMode.HALF_UP));
        
        return rsi.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * MACD 계산
     */
    public static BigDecimal calculateMACD(List<BigDecimal> prices) {
        BigDecimal ema12 = calculateEMA(prices, 12);
        BigDecimal ema26 = calculateEMA(prices, 26);
        return ema12.subtract(ema26).setScale(8, RoundingMode.HALF_UP);
    }
    
    /**
     * MACD Signal Line 계산
     */
    public static BigDecimal calculateMACDSignal(List<BigDecimal> prices) {
        // MACD 값들의 EMA를 계산 (9일)
        List<BigDecimal> macdValues = new ArrayList<>();
        for (int i = 26; i < prices.size(); i++) {
            List<BigDecimal> subPrices = prices.subList(0, i + 1);
            macdValues.add(calculateMACD(subPrices));
        }
        
        return calculateEMA(macdValues, 9);
    }
    
    /**
     * 볼린저 밴드 계산
     */
    public static BollingerBands calculateBollingerBands(List<BigDecimal> prices, int period, double stdDev) {
        BigDecimal sma = calculateSMA(prices, period);
        
        // 표준편차 계산
        BigDecimal sumSquaredDiff = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            BigDecimal diff = prices.get(i).subtract(sma);
            sumSquaredDiff = sumSquaredDiff.add(diff.multiply(diff));
        }
        
        BigDecimal variance = sumSquaredDiff.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        BigDecimal standardDeviation = sqrt(variance);
        
        BigDecimal upperBand = sma.add(standardDeviation.multiply(BigDecimal.valueOf(stdDev)));
        BigDecimal lowerBand = sma.subtract(standardDeviation.multiply(BigDecimal.valueOf(stdDev)));
        
        return new BollingerBands(upperBand, sma, lowerBand);
    }
    
    /**
     * 변동성 계산
     */
    public static BigDecimal calculateVolatility(List<BigDecimal> prices, int period) {
        if (prices.size() < period + 1) {
            return BigDecimal.ZERO;
        }
        
        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal returnValue = prices.get(i).subtract(prices.get(i - 1))
                    .divide(prices.get(i - 1), 8, RoundingMode.HALF_UP);
            returns.add(returnValue);
        }
        
        BigDecimal meanReturn = calculateSMA(returns, period);
        BigDecimal sumSquaredDiff = BigDecimal.ZERO;
        
        for (int i = returns.size() - period; i < returns.size(); i++) {
            BigDecimal diff = returns.get(i).subtract(meanReturn);
            sumSquaredDiff = sumSquaredDiff.add(diff.multiply(diff));
        }
        
        BigDecimal variance = sumSquaredDiff.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        return sqrt(variance).setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * 모멘텀 계산 (Rate of Change)
     */
    public static BigDecimal calculateMomentum(List<BigDecimal> prices, int period) {
        if (prices.size() < period + 1) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal currentPrice = prices.get(prices.size() - 1);
        BigDecimal pastPrice = prices.get(prices.size() - period - 1);
        
        return currentPrice.subtract(pastPrice)
                .divide(pastPrice, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 제곱근 계산 (간단한 구현)
     */
    private static BigDecimal sqrt(BigDecimal value) {
        BigDecimal x = value;
        BigDecimal y = BigDecimal.ZERO;
        BigDecimal epsilon = new BigDecimal("0.00000001");
        
        while (x.subtract(y).abs().compareTo(epsilon) > 0) {
            y = x;
            x = value.divide(x, 8, RoundingMode.HALF_UP).add(x)
                    .divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
        }
        
        return x;
    }
    
    /**
     * 볼린저 밴드 결과를 담는 클래스
     */
    public static class BollingerBands {
        private final BigDecimal upperBand;
        private final BigDecimal middleBand;
        private final BigDecimal lowerBand;
        
        public BollingerBands(BigDecimal upperBand, BigDecimal middleBand, BigDecimal lowerBand) {
            this.upperBand = upperBand;
            this.middleBand = middleBand;
            this.lowerBand = lowerBand;
        }
        
        public BigDecimal getUpperBand() { return upperBand; }
        public BigDecimal getMiddleBand() { return middleBand; }
        public BigDecimal getLowerBand() { return lowerBand; }
    }
} 