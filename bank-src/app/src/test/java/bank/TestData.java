package bank;

import bank.configuration.BankConf;
import bank.enums.Country;
import bank.enums.TransactionDirection;
import bank.request.CreateAccountRequest;
import bank.request.TransactionRequest;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;
import java.util.UUID;

public class TestData {

    public static CreateAccountRequest generateCreateAccountRequest() {
        return new CreateAccountRequest()
                .setCustomerId(UUID.randomUUID().toString())
                .setCountry(Country.EE)
                .setCurrencies(new HashSet<>(BankConf.ALLOWED_CURRENCIES));
    }

    public static TransactionRequest generateTransactionRequest(Long accountId) {
        return new TransactionRequest()
                .setAccountId(accountId)
                .setAmount(BigDecimal.valueOf(100L))
                .setCurrency(Currency.getInstance("EUR"))
                .setDirection(TransactionDirection.IN)
                .setDescription("Transaction description");
    }

}
