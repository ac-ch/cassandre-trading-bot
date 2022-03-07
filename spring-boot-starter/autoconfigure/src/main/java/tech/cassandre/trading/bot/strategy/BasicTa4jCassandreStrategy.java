package tech.cassandre.trading.bot.strategy;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Strategy;
import org.ta4j.core.num.DoubleNum;
import reactor.core.publisher.BaseSubscriber;
import tech.cassandre.trading.bot.dto.market.TickerDTO;
import tech.cassandre.trading.bot.dto.user.AccountDTO;
import tech.cassandre.trading.bot.dto.util.CurrencyPairDTO;
import tech.cassandre.trading.bot.strategy.internal.CassandreStrategy;
import tech.cassandre.trading.bot.util.ta4j.BarAggregator;
import tech.cassandre.trading.bot.util.ta4j.DurationBarAggregator;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * BasicCassandreStrategy - User inherits this class this one to make a strategy with ta4j.
 * <p>
 * These are the classes used to manage a position.
 * - CassandreStrategyInterface list the methods a strategy type must implement to be able to interact with the Cassandre framework.
 * - CassandreStrategyConfiguration contains the configuration of the strategy.
 * - CassandreStrategyDependencies contains all the dependencies required by a strategy and provided by the Cassandre framework.
 * - CassandreStrategyImplementation is the default implementation of CassandreStrategyInterface, this code manages the interaction between Cassandre framework and a strategy.
 * - CassandreStrategy (class) is the class that every strategy used by user ({@link BasicCassandreStrategy} or {@link BasicTa4jCassandreStrategy}) must extend. It contains methods to access data and manage orders, trades, positions.
 * - CassandreStrategy (interface) is the annotation allowing you Cassandre to recognize a user strategy.
 * - BasicCassandreStrategy - User inherits this class this one to make a basic strategy.
 * - BasicCassandreStrategy - User inherits this class this one to make a strategy with ta4j.
 */
@SuppressWarnings("unused")
public abstract class BasicTa4jCassandreStrategy extends CassandreStrategy {

    /** Timestamp of the last added bar. */
    private ZonedDateTime lastAddedBarTimestamp;

    /** Is historical data imported. */
    private boolean isHistoricalImport;

    /** Series. */
    private final BarSeries series;

    /** Ta4j Strategy. */
    private Strategy strategy;

    /** The bar aggregator. */
    private final BarAggregator barAggregator = new DurationBarAggregator(getDelayBetweenTwoBars());

    /**
     * Constructor.
     */
    public BasicTa4jCassandreStrategy() {
        // Build the series.
        series = new BaseBarSeriesBuilder()
                .withNumTypeOf(DoubleNum.class)
                .withName(getRequestedCurrencyPair().toString())
                .build();
        series.setMaximumBarCount(getMaximumBarCount());

        // Build the strategy.
        strategy = getStrategy();

        final AggregatedBarSubscriber barSubscriber = new AggregatedBarSubscriber(this::addBarAndCallStrategy);

        barAggregator.getBarFlux().subscribe(barSubscriber);
        barSubscriber.request(1);
    }

    /**
     * Implements this method to tell the bot which currency pair your strategy will receive.
     *
     * @return the list of currency pairs tickers your want to receive
     */
    public abstract CurrencyPairDTO getRequestedCurrencyPair();

    /**
     * Implements this method to tell the bot how many bars you want to keep in your bar series.
     *
     * @return maximum bar count.
     */
    @SuppressWarnings("SameReturnValue")
    public abstract int getMaximumBarCount();

    /**
     * Implements this method to set the time that should separate two bars.
     *
     * @return temporal amount
     */
    public abstract Duration getDelayBetweenTwoBars();

    /**
     * Implements this method to tell the bot which strategy to apply.
     *
     * @return strategy
     */
    public abstract Strategy getStrategy();

    /**
     * Returns the executed strategy.
     *
     * @return strategy
     */
    public final Strategy getExecutedStrategy() {
        return this.strategy;
    }

    /**
     * Update the Ta4j strategy used by Cassandre strategy.
     *
     * @param newStrategy strategy
     */
    public void updateTA4JStrategy(final Strategy newStrategy) {
        if (newStrategy != null) {
            strategy = newStrategy;
        }
    }

    /**
     * Update the Ta4j strategy used by Cassandre strategy. Use updateTA4JStrategy().
     *
     * @param newStrategy strategy
     */
    @Deprecated
    public void updateStrategy(final Strategy newStrategy) {
        updateTA4JStrategy(newStrategy);
    }

    @Override
    public final Set<CurrencyPairDTO> getRequestedCurrencyPairs() {
        // We only support one currency pair with BasicTa4jCassandreStrategy.
        return Set.of(getRequestedCurrencyPair());
    }

    @Override
    public final void tickersUpdates(final Set<TickerDTO> tickers) {
        // We only retrieve the ticker requested by the strategy (only one because it's a ta4j strategy).
        final Map<CurrencyPairDTO, TickerDTO> tickersUpdates = tickers.stream()
                .filter(ticker -> getRequestedCurrencyPair().equals(ticker.getCurrencyPair()))
                .collect(Collectors.toMap(TickerDTO::getCurrencyPair, Function.identity()));

        tickersUpdates.values().forEach(ticker -> {
            getLastTickers().put(ticker.getCurrencyPair(), ticker);
            if (series.getEndIndex() > 0 && isHistoricalImport) {
                barAggregator.update(series.getLastBar().getEndTime(), ticker.getLast());
                isHistoricalImport = false;
            } else {
                barAggregator.update(ticker.getTimestamp(), ticker.getLast());
            }
        });

        // We update the positions with tickers.
        updatePositionsWithTickersUpdates(tickersUpdates);

        onTickersUpdates(tickersUpdates);
    }

    private Bar addBarAndCallStrategy(final Bar bar) {
        series.addBar(bar);

        int endIndex = series.getEndIndex();
        if (strategy.shouldEnter(endIndex)) {
            // Our strategy should enter.
            shouldEnter();
        } else if (strategy.shouldExit(endIndex)) {
            // Our strategy should exit.
            shouldExit();
        }
        return bar;
    }

    /**
     * Called when your strategy think you should enter.
     */
    public abstract void shouldEnter();

    /**
     * Called when your strategy think you should exit.
     */
    public abstract void shouldExit();

    /**
     * Getter for series.
     *
     * @return series
     */
    public final BarSeries getSeries() {
        return series;
    }

    /**
     * Getter for historical data import.
     *
     * @return isHistoricalImport
     */
    public boolean isHistoricalImport() {
        return isHistoricalImport;
    }

    /**
     * Setter for historical data import.
     *
     * @param historicalImport historicalImport
     */
    public void setHistoricalImport(final boolean historicalImport) {
        isHistoricalImport = historicalImport;
    }

    /**
     * Subscriber to the Bar series.
     */
    private static class AggregatedBarSubscriber extends BaseSubscriber<Bar> {

        /**
         * The function to be called when the next bar arrives.
         */
        private final Function<Bar, Bar> theNextFunction;

        AggregatedBarSubscriber(final Function<Bar, Bar> onNextFunction) {
            this.theNextFunction = onNextFunction;
        }

        /**
         * Invoke the given function and ask for next bar.
         *
         * @param value the bar value
         */
        @Override
        protected void hookOnNext(final Bar value) {
            super.hookOnNext(value);
            theNextFunction.apply(value);
            request(1);
        }
    }

    // =================================================================================================================
    // CanBuy & canSell methods (specialized as there is only currency pair in BasicTa4jCassandreStrategy.

    /**
     * Returns true if we have enough assets to buy.
     *
     * @param amount amount
     * @return true if we have enough assets to buy
     */
    public final boolean canBuy(final BigDecimal amount) {
        final Optional<AccountDTO> tradeAccount = getTradeAccount(new LinkedHashSet<>(getAccounts().values()));
        return tradeAccount.filter(accountDTO -> canBuy(accountDTO, getRequestedCurrencyPair(), amount)).isPresent();
    }

    /**
     * Returns true if we have enough assets to buy.
     *
     * @param amount                  amount
     * @param minimumBalanceLeftAfter minimum balance that should be left after buying
     * @return true if we have enough assets to buy
     */
    public final boolean canBuy(final BigDecimal amount,
                                final BigDecimal minimumBalanceLeftAfter) {
        final Optional<AccountDTO> tradeAccount = getTradeAccount(new LinkedHashSet<>(getAccounts().values()));
        return tradeAccount.filter(accountDTO -> canBuy(accountDTO, getRequestedCurrencyPair(), amount, minimumBalanceLeftAfter)).isPresent();
    }

    /**
     * Returns true if we have enough assets to buy.
     *
     * @param account account
     * @param amount  amount
     * @return true if we have enough assets to buy
     */
    public final boolean canBuy(final AccountDTO account,
                                final BigDecimal amount) {
        return canBuy(account, getRequestedCurrencyPair(), amount);
    }

    /**
     * Returns true if we have enough assets to buy and if minimumBalanceAfter is left on the account after.
     *
     * @param account                 account
     * @param amount                  amount
     * @param minimumBalanceLeftAfter minimum balance that should be left after buying
     * @return true if we have enough assets to buy
     */
    public final boolean canBuy(final AccountDTO account,
                                final BigDecimal amount,
                                final BigDecimal minimumBalanceLeftAfter) {
        return canBuy(account, getRequestedCurrencyPair(), amount, minimumBalanceLeftAfter);
    }

    /**
     * Returns true if we have enough assets to sell.
     *
     * @param amount                  amount
     * @param minimumBalanceLeftAfter minimum balance that should be left after buying
     * @return true if we have enough assets to sell
     */
    public final boolean canSell(final BigDecimal amount,
                                 final BigDecimal minimumBalanceLeftAfter) {
        final Optional<AccountDTO> tradeAccount = getTradeAccount(new LinkedHashSet<>(getAccounts().values()));
        return tradeAccount.filter(accountDTO -> canSell(accountDTO, getRequestedCurrencyPair().getBaseCurrency(), amount, minimumBalanceLeftAfter)).isPresent();
    }

    /**
     * Returns true if we have enough assets to sell.
     *
     * @param amount amount
     * @return true if we have enough assets to sell
     */
    public final boolean canSell(final BigDecimal amount) {
        final Optional<AccountDTO> tradeAccount = getTradeAccount(new LinkedHashSet<>(getAccounts().values()));
        return tradeAccount.filter(accountDTO -> canSell(accountDTO, getRequestedCurrencyPair().getBaseCurrency(), amount)).isPresent();
    }

    /**
     * Returns true if we have enough assets to sell.
     *
     * @param account account
     * @param amount  amount
     * @return true if we have enough assets to sell
     */
    public final boolean canSell(final AccountDTO account,
                                 final BigDecimal amount) {
        return canSell(account, getRequestedCurrencyPair().getBaseCurrency(), amount);
    }

    /**
     * Returns true if we have enough assets to sell and if minimumBalanceAfter is left on the account after.
     *
     * @param account                 account
     * @param amount                  amount
     * @param minimumBalanceLeftAfter minimum balance that should be left after selling
     * @return true if we have enough assets to sell
     */
    public final boolean canSell(final AccountDTO account,
                                 final BigDecimal amount,
                                 final BigDecimal minimumBalanceLeftAfter) {
        return canSell(account, getRequestedCurrencyPair().getBaseCurrency(), amount, minimumBalanceLeftAfter);
    }

}
