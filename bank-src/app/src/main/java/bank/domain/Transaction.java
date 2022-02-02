package bank.domain;

import bank.enums.TransactionDirection;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Currency;

@Data
@Accessors(chain = true)
public class Transaction {
    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private Currency currency;
    private TransactionDirection direction;
    private String description;
    private BigDecimal balanceAfter;
}
