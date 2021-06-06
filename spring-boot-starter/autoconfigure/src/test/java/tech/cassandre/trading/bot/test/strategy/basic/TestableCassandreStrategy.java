package tech.cassandre.trading.bot.test.strategy.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import tech.cassandre.trading.bot.dto.market.TickerDTO;
import tech.cassandre.trading.bot.dto.position.PositionDTO;
import tech.cassandre.trading.bot.dto.trade.OrderDTO;
import tech.cassandre.trading.bot.dto.trade.TradeDTO;
import tech.cassandre.trading.bot.dto.user.AccountDTO;
import tech.cassandre.trading.bot.dto.util.CurrencyPairDTO;
import tech.cassandre.trading.bot.strategy.BasicCassandreStrategy;
import tech.cassandre.trading.bot.strategy.CassandreStrategy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static tech.cassandre.trading.bot.test.strategy.basic.TestableCassandreStrategy.PARAMETER_TESTABLE_STRATEGY_ENABLED;
import static tech.cassandre.trading.bot.test.util.junit.BaseTest.ETH_BTC;
import static tech.cassandre.trading.bot.test.util.junit.BaseTest.ETH_USDT;

/**
 * Testable strategy (used for tests).
 */
@SuppressWarnings("unused")
@CassandreStrategy(
        strategyId = "01",
        strategyName = "Testable strategy")
@ConditionalOnProperty(
        value = PARAMETER_TESTABLE_STRATEGY_ENABLED,
        havingValue = "true")
public class TestableCassandreStrategy extends BasicCassandreStrategy {

    /** Testable strategy enabled parameter. */
    public static final String PARAMETER_TESTABLE_STRATEGY_ENABLED = "testableStrategy.enabled";

    /** Waiting time during each method. */
    public static final int WAITING_TIME_IN_SECONDS = 1;

    /** Logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** Accounts update received. */
    private final List<AccountDTO> accountsUpdateReceived = new LinkedList<>();

    /** Tickers update received. */
    private final List<TickerDTO> tickersUpdateReceived = new LinkedList<>();

    /** Orders update received. */
    private final List<OrderDTO> ordersUpdateReceived = new LinkedList<>();

    /** Trades update received. */
    private final List<TradeDTO> tradesUpdateReceived = new LinkedList<>();

    /** Positions update received. */
    private final List<PositionDTO> positionsUpdateReceived = new LinkedList<>();

    /** Positions status update received. */
    private final List<PositionDTO> positionsStatusUpdateReceived = new LinkedList<>();

    /** Requested currency pairs. */
    Set<CurrencyPairDTO> requestedCurrencyPairs = ConcurrentHashMap.newKeySet();

    /**
     * Constructor.
     */
    public TestableCassandreStrategy() {
        requestedCurrencyPairs.add(ETH_BTC);
        requestedCurrencyPairs.add(ETH_USDT);
    }

    @Override
    public final Set<CurrencyPairDTO> getRequestedCurrencyPairs() {
        return requestedCurrencyPairs;
    }

    /**
     * Updates the requested currency pairs.
     *
     * @param newRequestedCurrencyPairs new list of requested currency pairs
     */
    public final void updateRequestedCurrencyPairs(Set<CurrencyPairDTO> newRequestedCurrencyPairs) {
        requestedCurrencyPairs.clear();
        requestedCurrencyPairs.addAll(newRequestedCurrencyPairs);
    }

    @Override
    public Optional<AccountDTO> getTradeAccount(Set<AccountDTO> accounts) {
        if (accounts.size() == 1) {
            // Used for Gemini integration tests.
            return accounts.stream().findFirst();
        } else {
            return accounts.stream()
                    .filter(a -> "trade".equals(a.getName()))
                    .findFirst();
        }
    }

    @Override
    public final void onAccountsUpdates(final Map<String, AccountDTO> accounts) {
        accounts.values()
                .stream()
                .peek(accountDTO -> logger.info("TestableStrategy-onAccountsUpdates n° {} : {} \n ",
                        getCount(accountsUpdateReceived),
                        accountDTO))
                .forEach(accountsUpdateReceived::add);

        try {
            TimeUnit.SECONDS.sleep(WAITING_TIME_IN_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public final void onTickersUpdates(final Map<CurrencyPairDTO, TickerDTO> tickers) {
        tickers.values()
                .stream()
                .peek(tickerDTO -> logger.info("TestableStrategy-onTickersUpdates n° {} : {} \n ",
                        getCount(tickersUpdateReceived),
                        tickerDTO))
                .forEach(tickersUpdateReceived::add);

        try {
            TimeUnit.SECONDS.sleep(WAITING_TIME_IN_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public final void onOrdersUpdates(final Map<String, OrderDTO> orders) {
        orders.values()
                .stream()
                .peek(orderDTO -> logger.info("TestableStrategy-onOrdersUpdates n° {} : {} \n ",
                        getCount(ordersUpdateReceived),
                        orderDTO))
                .forEach(ordersUpdateReceived::add);

        try {
            TimeUnit.SECONDS.sleep(WAITING_TIME_IN_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onTradesUpdates(final Map<String, TradeDTO> trades) {
        trades.values()
                .stream()
                .peek(tradeDTO -> logger.info("TestableStrategy-onTradesUpdates n° {} : {} \n ",
                        getCount(tradesUpdateReceived),
                        tradeDTO))
                .forEach(tradesUpdateReceived::add);

        try {
            TimeUnit.SECONDS.sleep(WAITING_TIME_IN_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onPositionsUpdates(final Map<Long, PositionDTO> positions) {
        positions.values()
                .stream()
                .peek(positionDTO -> logger.info("TestableStrategy-onPositionsUpdates n° {} : {} \n ",
                        getCount(positionsUpdateReceived),
                        positionDTO))
                .forEach(positionsUpdateReceived::add);

        try {
            TimeUnit.SECONDS.sleep(WAITING_TIME_IN_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onPositionsStatusUpdates(final Map<Long, PositionDTO> positions) {
        positions.values()
                .stream()
                .peek(positionDTO -> logger.info("TestableStrategy-onPositionsStatusUpdates n° {} : {} \n ",
                        getCount(positionsStatusUpdateReceived),
                        positionDTO))
                .forEach(positionsStatusUpdateReceived::add);

        try {
            TimeUnit.SECONDS.sleep(WAITING_TIME_IN_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Return formatted list count.
     *
     * @param list list to count
     * @return int value with format
     */
    private String getCount(final List<?> list) {
        return String.format("%03d", list.size() + 1);
    }

    /**
     * Getter lastAccountsReceived.
     *
     * @return last accounts received.
     */
    public final List<AccountDTO> getAccountsUpdatesReceived() {
        return accountsUpdateReceived;
    }

    /**
     * Getter lastTickersReceived.
     *
     * @return lastTickersReceived
     */
    public final List<TickerDTO> getTickersUpdateReceived() {
        return tickersUpdateReceived;
    }

    /**
     * Getter lastOrdersReceived.
     *
     * @return lastOrderReceived
     */
    public final List<OrderDTO> getOrdersUpdateReceived() {
        return ordersUpdateReceived;
    }

    /**
     * Getter tradesUpdateReceived.
     *
     * @return tradesUpdateReceived
     */
    public final List<TradeDTO> getTradesUpdateReceived() {
        return tradesUpdateReceived;
    }

    /**
     * Getter accountsUpdateReceived.
     *
     * @return accountsUpdateReceived
     */
    public final List<AccountDTO> getAccountsUpdateReceived() {
        return accountsUpdateReceived;
    }

    /**
     * Getter positionsUpdateReceived.
     *
     * @return positionsUpdateReceived
     */
    public final List<PositionDTO> getPositionsUpdateReceived() {
        return positionsUpdateReceived;
    }

    /**
     * Getter positionsStatusUpdateReceived.
     *
     * @return positionsStatusUpdateReceived
     */
    public final List<PositionDTO> getPositionsStatusUpdateReceived() {
        return positionsStatusUpdateReceived;
    }

}