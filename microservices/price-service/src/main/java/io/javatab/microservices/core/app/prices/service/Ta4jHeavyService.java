package io.javatab.microservices.core.app.prices.service;

import io.javatab.microservices.core.app.prices.model.IndicatorResult;
import io.javatab.microservices.core.app.prices.model.Price;
import io.javatab.microservices.core.app.prices.model.SMAResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Ta4jHeavyService {

    private final PriceService priceService;

    public Map<String, IndicatorResult> computeForTickers(
            List<String> tickers, LocalDate from, LocalDate to
    ) {
        Map<String, IndicatorResult> results = new HashMap<>();

        for (String ticker : tickers) {
            List<Price> prices = priceService.getEntitiesByTickerAndDateRange(ticker, from, to);
            if (prices.size() < 30) continue;

            BarSeries series = new BaseBarSeriesBuilder()
                    .withName(ticker)
                    .build();

            // Build the time series
            for (Price p : prices) {
                try {
                    ZonedDateTime endTime = p.getDate()
                            .atStartOfDay(ZoneId.systemDefault());
                    Bar bar = BaseBar.builder()
                            .timePeriod(Duration.ofDays(1))
                            .endTime(endTime)
                            .openPrice(DecimalNum.valueOf(p.getOpen()))
                            .highPrice(DecimalNum.valueOf(p.getHigh()))
                            .lowPrice(DecimalNum.valueOf(p.getLow()))
                            .closePrice(DecimalNum.valueOf(p.getClose()))
                            .build();
                    series.addBar(bar);
                } catch (Exception ignored) {
                }
            }

            // Indicators
            ClosePriceIndicator close = new ClosePriceIndicator(series);
            ATRIndicator atr = new ATRIndicator(series, 14);
            RSIIndicator rsi = new RSIIndicator(close, 14);

            BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(close);
            StandardDeviationIndicator stdDev = new StandardDeviationIndicator(close, 20);
            Num k = DecimalNum.valueOf(2);
            BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, stdDev, k);
            BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, stdDev, k);

            // Drawdown and recovery time
            List<Num> closeValues = new ArrayList<>();
            for (int i = 0; i < series.getBarCount(); i++) {
                closeValues.add(series.getBar(i).getClosePrice());
            }

            double runningPeak = closeValues.get(0).doubleValue();
            double maxDrawdown = 0;
            int peakIndex = 0;
            int troughIndex = 0;

            for (int i = 1; i < closeValues.size(); i++) {
                double price = closeValues.get(i).doubleValue();
                if (price > runningPeak) {
                    runningPeak = price;
                    peakIndex = i;
                }
                double dd = (price - runningPeak) / runningPeak;
                if (dd < maxDrawdown) {
                    maxDrawdown = dd;
                    troughIndex = i;
                }
            }

            long recoveryTimeBars = 0;
            if (maxDrawdown < 0) {
                double peakBeforeTrough = closeValues.get(peakIndex).doubleValue();
                for (int j = troughIndex + 1; j < closeValues.size(); j++) {
                    if (closeValues.get(j).doubleValue() >= peakBeforeTrough) {
                        recoveryTimeBars = j - troughIndex;
                        break;
                    }
                }
            }

            // Last values of other indicators
            int lastIndex = series.getBarCount() - 1;
            double lastAtr = atr.getValue(lastIndex).doubleValue();
            double lastRsi = rsi.getValue(lastIndex).doubleValue();
            double lastBbu = bbu.getValue(lastIndex).doubleValue();
            double lastBbl = bbl.getValue(lastIndex).doubleValue();

            // Put result
            results.put(ticker, new IndicatorResult(
                    ticker,
                    lastAtr,
                    lastRsi,
                    lastBbu,
                    lastBbl,
                    maxDrawdown,
                    recoveryTimeBars
            ));
        }
        return results;
    }

    /**
     * Fetches the full price history for `ticker` and computes
     * a simple moving average (SMA) with the given window size.
     *
     * @param ticker the symbol to compute over
     * @param window the look-back period (e.g. 14, 50, 200)
     * @return a List of (date, ticker, sma) for each bar where i >= window−1
     */
    public List<SMAResult> computeSimpleMovingAverage(String ticker, int window) {
        List<Price> prices = priceService.findAllByTickerOrderByDate(ticker);

        List<SMAResult> results = new ArrayList<>();
        // we need at least `window` bars to compute the first SMA
        for (int i = window - 1; i < prices.size(); i++) {
            BigDecimal sum = BigDecimal.ZERO;
            // sum the close prices over [i-window+1 .. i]
            for (int j = i - window + 1; j <= i; j++) {
                sum = sum.add(prices.get(j).getClose());
            }
            BigDecimal sma = sum.divide(BigDecimal.valueOf(window), RoundingMode.HALF_UP);

            // record the SMA for the bar at index i
            results.add(new SMAResult(
                    ticker,
                    prices.get(i).getDate(),
                    sma
            ));
        }

        return results;
    }
}
