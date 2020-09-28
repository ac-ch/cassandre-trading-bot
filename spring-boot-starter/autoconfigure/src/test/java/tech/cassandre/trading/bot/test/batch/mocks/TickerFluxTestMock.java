package tech.cassandre.trading.bot.test.batch.mocks;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tech.cassandre.trading.bot.batch.AccountFlux;
import tech.cassandre.trading.bot.batch.OrderFlux;
import tech.cassandre.trading.bot.batch.TickerFlux;
import tech.cassandre.trading.bot.service.MarketService;
import tech.cassandre.trading.bot.service.TradeService;
import tech.cassandre.trading.bot.service.UserService;
import tech.cassandre.trading.bot.test.util.junit.BaseTest;
import tech.cassandre.trading.bot.dto.util.CurrencyDTO;
import tech.cassandre.trading.bot.dto.util.CurrencyPairDTO;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class TickerFluxTestMock extends BaseTest {

    @Bean
    @Primary
    public TickerFlux tickerFlux() {
        return new TickerFlux(marketService());
    }

    @Bean
    @Primary
    public AccountFlux accountFlux() {
        return new AccountFlux(userService());
    }

    @Bean
    @Primary
    public OrderFlux orderFlux() {
        return new OrderFlux(tradeService());
    }

    @Bean
    @Primary
    public UserService userService() {
        UserService service = mock(UserService.class);
        given(service.getUser()).willReturn(Optional.empty());
        return service;
    }

    @SuppressWarnings("unchecked")
    @Bean
    @Primary
    public MarketService marketService() {
        // Creates the mock.
        MarketService marketService = mock(MarketService.class);

        // Replies for ETH / BTC.
        final CurrencyPairDTO cp1 = new CurrencyPairDTO(CurrencyDTO.ETH, CurrencyDTO.BTC);
        final Date time = Calendar.getInstance().getTime();
        given(marketService
                .getTicker(cp1))
                .willReturn(BaseTest.getFakeTicker(cp1, new BigDecimal("1")),
                        BaseTest.getFakeTicker(cp1, new BigDecimal("2")),
                        BaseTest.getFakeTicker(cp1, new BigDecimal("3")),
                        Optional.empty(),
                        BaseTest.getFakeTicker(time, cp1, new BigDecimal("4")),
                        BaseTest.getFakeTicker(time, cp1, new BigDecimal("4")),
                        BaseTest.getFakeTicker(cp1, new BigDecimal("5")),
                        BaseTest.getFakeTicker(cp1, new BigDecimal("6")),
                        Optional.empty()
                );

        // Replies for ETH / USDT.
        final CurrencyPairDTO cp2 = new CurrencyPairDTO(CurrencyDTO.ETH, CurrencyDTO.USDT);
        given(marketService
                .getTicker(cp2))
                .willReturn(BaseTest.getFakeTicker(cp2, new BigDecimal("10")),
                        BaseTest.getFakeTicker(cp2, new BigDecimal("20")),
                        BaseTest.getFakeTicker(cp2, new BigDecimal("30")),
                        BaseTest.getFakeTicker(cp2, new BigDecimal("40")),
                        BaseTest.getFakeTicker(cp2, new BigDecimal("50")),
                        Optional.empty(),
                        BaseTest.getFakeTicker(cp2, new BigDecimal("60")),
                        Optional.empty(),
                        BaseTest.getFakeTicker(cp2, new BigDecimal("70"))
                );
        return marketService;
    }

    @Bean
    @Primary
    public TradeService tradeService() {
        TradeService service = mock(TradeService.class);
        given(service.getOpenOrders()).willReturn(new LinkedHashSet<>());
        return service;
    }

}
