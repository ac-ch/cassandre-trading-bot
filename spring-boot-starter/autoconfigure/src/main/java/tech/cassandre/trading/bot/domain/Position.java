package tech.cassandre.trading.bot.domain;

import lombok.Data;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import tech.cassandre.trading.bot.dto.position.PositionStatusDTO;
import tech.cassandre.trading.bot.util.base.BaseDomain;
import tech.cassandre.trading.bot.util.java.EqualsBuilder;
import tech.cassandre.trading.bot.util.jpa.CurrencyAmount;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * Position (used to save data between restarts).
 */
@Data
@Entity
@Table(name = "POSITIONS")
public class Position extends BaseDomain {

    /** An identifier that uniquely identifies the position. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /** Position . */
    @Enumerated(STRING)
    @Column(name = "STATUS")
    private PositionStatusDTO status;

    /** The currency-pair. */
    @Column(name = "CURRENCY_PAIR")
    private String currencyPair;

    /** Amount to be ordered / amount that was ordered. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "AMOUNT_VALUE")),
            @AttributeOverride(name = "currency", column = @Column(name = "AMOUNT_CURRENCY"))
    })
    private CurrencyAmount amount;

    /** Position rules - stop gain percentage. */
    @Column(name = "RULES_STOP_GAIN_PERCENTAGE")
    private Float stopGainPercentageRule;

    /** Position rules - stop loss percentage. */
    @Column(name = "RULES_STOP_LOSS_PERCENTAGE")
    private Float stopLossPercentageRule;

    /** The order that opened the position. */
    @OneToOne(fetch = EAGER, cascade = ALL)
    @JoinColumn(name = "FK_OPENING_ORDER_ID")
    private Order openingOrder;

    /** The order that closed the position. */
    @OneToOne(fetch = EAGER, cascade = ALL)
    @JoinColumn(name = "FK_CLOSING_ORDER_ID")
    private Order closingOrder;

    /** Lowest price. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "LOWEST_PRICE_VALUE")),
            @AttributeOverride(name = "currency", column = @Column(name = "LOWEST_PRICE_CURRENCY"))
    })
    private CurrencyAmount lowestPrice;

    /** Highest price. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "HIGHEST_PRICE_VALUE")),
            @AttributeOverride(name = "currency", column = @Column(name = "HIGHEST_PRICE_CURRENCY"))
    })
    private CurrencyAmount highestPrice;

    /** Latest price. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "LATEST_PRICE_VALUE")),
            @AttributeOverride(name = "currency", column = @Column(name = "LATEST_PRICE_CURRENCY"))
    })
    private CurrencyAmount latestPrice;

    /** Strategy. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_STRATEGY_ID", updatable = false)
    private Strategy strategy;

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Position that = (Position) o;
        return new EqualsBuilder()
                .append(this.id, that.id)
                .append(this.status, that.status)
                .append(this.currencyPair, that.currencyPair)
                .append(this.amount, that.amount)
                .append(this.stopGainPercentageRule, that.stopGainPercentageRule)
                .append(this.stopLossPercentageRule, that.stopLossPercentageRule)
                .append(this.openingOrder, that.closingOrder)
                .append(this.lowestPrice, that.lowestPrice)
                .append(this.highestPrice, that.highestPrice)
                .append(this.latestPrice, that.latestPrice)
                .append(this.strategy.getId(), that.strategy.getId())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(strategy.getId())
                .toHashCode();
    }

}
