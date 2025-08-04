
package com.altradar.util;

import java.util.ArrayList;
import java.util.List;

public class TechnicalAnalysisUtil {

    // 이동평균 계산
    public static double movingAverage(List<Double> prices, int period) {
        if (prices.size() < period) return Double.NaN;
        double sum = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        return sum / period;
    }

    // RSI 계산
    public static double rsi(List<Double> prices, int period) {
        if (prices.size() < period + 1) return Double.NaN;
        double gain = 0, loss = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            double diff = prices.get(i) - prices.get(i - 1);
            if (diff > 0) gain += diff;
            else loss -= diff;
        }
        if (loss == 0) return 100;
        double rs = gain / loss;
        return 100 - (100 / (1 + rs));
    }

    // 볼린저 밴드 계산
    public static double[] bollingerBands(List<Double> prices, int period, double k) {
        if (prices.size() < period) return new double[]{Double.NaN, Double.NaN, Double.NaN};
        double ma = movingAverage(prices, period);
        double sumSq = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sumSq += Math.pow(prices.get(i) - ma, 2);
        }
        double stddev = Math.sqrt(sumSq / period);
        return new double[]{ma + k * stddev, ma, ma - k * stddev};
    }

    // MACD 계산
    public static double[] macd(List<Double> prices, int shortPeriod, int longPeriod, int signalPeriod) {
        if (prices.size() < longPeriod + signalPeriod) return new double[]{Double.NaN, Double.NaN};
        double emaShort = ema(prices, shortPeriod);
        double emaLong = ema(prices, longPeriod);
        double macdLine = emaShort - emaLong;
        double signalLine = emaFromMacd(prices, shortPeriod, longPeriod, signalPeriod);
        return new double[]{macdLine, signalLine};
    }

    private static double ema(List<Double> prices, int period) {
        double k = 2.0 / (period + 1);
        double ema = prices.get(0);
        for (int i = 1; i < prices.size(); i++) {
            ema = prices.get(i) * k + ema * (1 - k);
        }
        return ema;
    }

    private static double emaFromMacd(List<Double> prices, int shortPeriod, int longPeriod, int signalPeriod) {
        List<Double> macdValues = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            if (i >= longPeriod - 1) {
                double emaShort = ema(prices.subList(0, i + 1), shortPeriod);
                double emaLong = ema(prices.subList(0, i + 1), longPeriod);
                macdValues.add(emaShort - emaLong);
            }
        }
        return ema(macdValues, signalPeriod);
    }

    // 거래량 급등 감지
    public static boolean isVolumeSpike(List<Double> volumes, double thresholdMultiplier) {
        if (volumes.size() < 2) return false;
        double latest = volumes.get(volumes.size() - 1);
        double avg = 0;
        for (int i = 0; i < volumes.size() - 1; i++) {
            avg += volumes.get(i);
        }
        avg /= (volumes.size() - 1);
        return latest > avg * thresholdMultiplier;
    }
}
