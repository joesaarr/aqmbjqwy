package bank.service;

import bank.TestData;
import bank.configuration.RabbitMqConf;
import bank.enums.TransactionDirection;
import bank.exception.AccountNotFoundException;
import bank.exception.InsufficientFundsException;
import bank.exception.InvalidCurrencyException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
public class TransactionServiceTest {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    TransactionService transactionService;

    @Autowired
    AccountService accountService;

    @Test
    void testCreateTransaction() {
        var accountDto = accountService
                .createAccount(TestData.generateCreateAccountRequest());

        assertEquals(0, transactionService.findAllByAccountId(accountDto.getId()).size());

        var transactionRequest = TestData.generateTransactionRequest(accountDto.getId());

        var transactionDto = transactionService.createTransaction(transactionRequest);

        assertEquals(1, transactionService.findAllByAccountId(accountDto.getId()).size());

        assertEquals(accountDto.getId(), transactionDto.getAccountId());
        assertEquals(transactionRequest.getAmount(), transactionDto.getAmount());
        assertEquals(transactionRequest.getCurrency(), transactionDto.getCurrency());
        assertEquals(transactionRequest.getDirection(), transactionDto.getDirection());
        assertEquals(transactionRequest.getDescription(), transactionDto.getDescription());
    }

    @Test
    void testCreateTransactionChangesAccountBalance() {
        var accountDto = accountService
                .createAccount(TestData.generateCreateAccountRequest());

        var transactionRequest = TestData.generateTransactionRequest(accountDto.getId());

        var balanceBefore = accountDto.getBalances().stream()
                .filter(b -> b.getCurrency().equals(transactionRequest.getCurrency()))
                .findAny().get().getAmount();

        var transactionDto = transactionService.createTransaction(transactionRequest);

        var balanceAfter = accountService.find(accountDto.getId()).getBalances().stream()
                .filter(b -> b.getCurrency().equals(transactionRequest.getCurrency()))
                .findAny().get().getAmount();

        assertEquals(transactionDto.getBalanceAfter(), balanceAfter);
        assertEquals(balanceBefore.add(transactionRequest.getAmount()), transactionDto.getBalanceAfter());
    }

    @Test
    void testCreateTransactionSendsMessage() {
        var accountDto = accountService
                .createAccount(TestData.generateCreateAccountRequest());

        assertEquals(0, transactionService.findAllByAccountId(accountDto.getId()).size());

        var transactionRequest = TestData.generateTransactionRequest(accountDto.getId());

        var transactionDto = transactionService.createTransaction(transactionRequest);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConf.EXCHANGE_NAME),
                eq("create-transaction"),
                eq(transactionDto)
        );
    }

    @Test
    void testCreateTransactionForUnknownAccount() {
        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.createTransaction(TestData.generateTransactionRequest(new Random().nextLong()))
        );
    }

    @Test
    void testCreateTransactionForNonExistingBalance() {
        var accountDto = accountService.createAccount(
                TestData.generateCreateAccountRequest().setCurrencies(Set.of(Currency.getInstance("EUR")))
        );

        var transactionRequest = TestData.generateTransactionRequest(accountDto.getId());
        transactionRequest.setCurrency(Currency.getInstance("USD"));

        assertThrows(
                InvalidCurrencyException.class,
                () -> transactionService.createTransaction(transactionRequest)
        );
    }

    @Test
    void testCreateTransactionThatWouldResultInNegativeBalance() {
        var accountDto = accountService.createAccount(TestData.generateCreateAccountRequest());

        var transactionRequest = TestData.generateTransactionRequest(accountDto.getId());
        transactionRequest.setDirection(TransactionDirection.OUT);
        transactionRequest.setAmount(BigDecimal.valueOf(10000L));

        assertThrows(
                InsufficientFundsException.class,
                () -> transactionService.createTransaction(transactionRequest)
        );
    }

    @Test
    void testGetTransactionsForUnknownAccount() {
        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.findAllByAccountId(new Random().nextLong())
        );
    }

    @Test
    void testGetTransactionsWhenNoTransactions() {
        var accountDto = accountService
                .createAccount(TestData.generateCreateAccountRequest());

        assertEquals(0, transactionService.findAllByAccountId(accountDto.getId()).size());

        var transactionRequest = TestData.generateTransactionRequest(accountDto.getId());

        var transactionDto = transactionService.createTransaction(transactionRequest);

        assertEquals(1, transactionService.findAllByAccountId(accountDto.getId()).size());

        assertEquals(accountDto.getId(), transactionDto.getAccountId());
        assertEquals(transactionRequest.getAmount(), transactionDto.getAmount());
        assertEquals(transactionRequest.getCurrency(), transactionDto.getCurrency());
        assertEquals(transactionRequest.getDirection(), transactionDto.getDirection());
        assertEquals(transactionRequest.getDescription(), transactionDto.getDescription());
    }

    @Test
    void testGetTransaction() {
        var accountDto = accountService
                .createAccount(TestData.generateCreateAccountRequest());

        assertEquals(0, transactionService.findAllByAccountId(accountDto.getId()).size());

        var transactionRequest = TestData.generateTransactionRequest(accountDto.getId());

        var transactionDto = transactionService.createTransaction(transactionRequest);

        var transactions = transactionService.findAllByAccountId(accountDto.getId());
        assertEquals(1, transactions.size());
        assertTrue(transactions.stream().allMatch(t -> transactionDto.getTransactionId().equals(t.getTransactionId())));
    }

    @Test
    void testGetMultipleTransactions() {
        var accountDto = accountService
                .createAccount(TestData.generateCreateAccountRequest());

        assertEquals(0, transactionService.findAllByAccountId(accountDto.getId()).size());

        transactionService.createTransaction(TestData.generateTransactionRequest(accountDto.getId()));
        transactionService.createTransaction(TestData.generateTransactionRequest(accountDto.getId()));
        transactionService.createTransaction(TestData.generateTransactionRequest(accountDto.getId()));

        var transactions = transactionService.findAllByAccountId(accountDto.getId());
        assertEquals(3, transactions.size());
    }

    @Test
    void testOnlyGetAccountTransactions() {
        var accountDto1 = accountService
                .createAccount(TestData.generateCreateAccountRequest());
        var accountDto2 = accountService
                .createAccount(TestData.generateCreateAccountRequest());

        var transactionDto1 = transactionService
                .createTransaction(TestData.generateTransactionRequest(accountDto1.getId()));
        transactionService
                .createTransaction(TestData.generateTransactionRequest(accountDto2.getId()));

        var transactions = transactionService.findAllByAccountId(accountDto1.getId());
        assertEquals(1, transactions.size());
        assertTrue(transactions.stream().allMatch(t -> transactionDto1.getTransactionId().equals(t.getTransactionId())));
    }
}
