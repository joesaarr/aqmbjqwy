package bank.dto;

import bank.domain.Transaction;
import bank.enums.TransactionDirection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Currency;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionDto {

    Long accountId;
    Long transactionId;
    BigDecimal amount;
    Currency currency;
    TransactionDirection direction;
    String description;
    BigDecimal balanceAfter;

    public static TransactionDto from(Transaction transaction) {
        return new TransactionDto(
                transaction.getAccountId(),
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDirection(),
                transaction.getDescription(),
                transaction.getBalanceAfter()
        );
    }

}
