package bank.service;

import bank.TestData;
import bank.configuration.RabbitMqConf;
import bank.exception.AccountNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class AccountServiceTest {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AccountService accountService;

    @Test
    void testCreatesAccount() {
        var accountRequest = TestData.generateCreateAccountRequest();

        var accountDto = accountService.createAccount(accountRequest);

        var account = accountService.find(accountDto.getId());

        assertNotNull(account);
        assertEquals(accountRequest.getCustomerId(), account.getCustomerId());
        assertEquals(accountRequest.getCountry(), account.getCountry());
    }

    @Test
    void testCreatesBalancesForNewAccount() {
        var accountRequest = TestData.generateCreateAccountRequest();

        var accountDto = accountService.createAccount(accountRequest);

        var account = accountService.find(accountDto.getId());

        assertNotNull(account);
        assertEquals(accountRequest.getCurrencies().size(), account.getBalances().size());

        account.getBalances().forEach(balance -> {
            assertEquals(0, BigDecimal.ZERO.compareTo(balance.getAmount()));
            assertTrue(accountRequest.getCurrencies().contains(balance.getCurrency()));
        });
    }

    @Test
    void testCreatesAccountSendsMessage() {
        var accountRequest = TestData.generateCreateAccountRequest();

        var accountDto = accountService.createAccount(accountRequest);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConf.EXCHANGE_NAME),
                eq("create-account"),
                eq(accountDto)
        );
    }

    @Test
    void testDoesNotCreateAccountWithInvalidCurrency() {
        var accountRequest = TestData.generateCreateAccountRequest();
        accountRequest.getCurrencies().add(Currency.getInstance("RUB"));

        assertThrows(
                InvalidCurrencyException.class,
                () -> accountService.createAccount(accountRequest)
        );
    }

    @Test
    void testFindAccount() {
        var accountDto = accountService.createAccount(TestData.generateCreateAccountRequest());

        var foundAccount = accountService.find(accountDto.getId());

        assertEquals(accountDto, foundAccount);
    }

    @Test
    void testDoesNotFindAccount() {
        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.find(new Random().nextLong())
        );
    }
}