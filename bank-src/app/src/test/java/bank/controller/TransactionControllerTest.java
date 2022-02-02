package bank.controller;

import bank.TestData;
import bank.data.AccountMapper;
import bank.data.BalanceMapper;
import bank.data.TransactionMapper;
import bank.domain.Account;
import bank.domain.Balance;
import bank.domain.Transaction;
import bank.enums.Country;
import bank.enums.TransactionDirection;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private BalanceMapper balanceMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCreateTransaction() throws Exception {
        var account = new Account()
                .setCustomerId(UUID.randomUUID().toString()).setCountry(Country.EE);
        accountMapper.insertAccount(account);

        var balance = new Balance()
                .setCurrency(Currency.getInstance("EUR")).setAmount(BigDecimal.ZERO);
        balanceMapper.insertBalance(balance, account);

        var transactionRequest = TestData.generateTransactionRequest(account.getId());

        this.mockMvc
                .perform(post(TransactionController.PATH)
                        .content(objectMapper.writeValueAsBytes(transactionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.accountId").value(account.getId()))
                .andExpect(jsonPath("$.amount").value(transactionRequest.getAmount().toString()))
                .andExpect(jsonPath("$.currency").value(transactionRequest.getCurrency().getCurrencyCode()))
                .andExpect(jsonPath("$.direction").value(transactionRequest.getDirection().name()))
                .andExpect(jsonPath("$.description").value(transactionRequest.getDescription()));
    }

    @Test
    void testCreateTransactionThatResultsInNegativeBalance() throws Exception {
        var account = new Account()
                .setCustomerId(UUID.randomUUID().toString()).setCountry(Country.EE);
        accountMapper.insertAccount(account);

        var balance = new Balance()
                .setCurrency(Currency.getInstance("EUR")).setAmount(BigDecimal.ZERO);
        balanceMapper.insertBalance(balance, account);

        var transactionRequest = TestData.generateTransactionRequest(account.getId());
        transactionRequest.setDirection(TransactionDirection.OUT);

        this.mockMvc
                .perform(post(TransactionController.PATH)
                        .content(objectMapper.writeValueAsBytes(transactionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransactionWithNullAmount() throws Exception {
        var account = new Account()
                .setCustomerId(UUID.randomUUID().toString()).setCountry(Country.EE);
        accountMapper.insertAccount(account);

        var balance = new Balance()
                .setCurrency(Currency.getInstance("EUR")).setAmount(BigDecimal.ZERO);
        balanceMapper.insertBalance(balance, account);

        var transactionRequest = TestData.generateTransactionRequest(account.getId());
        transactionRequest.setAmount(null);

        this.mockMvc
                .perform(post(TransactionController.PATH)
                        .content(objectMapper.writeValueAsBytes(transactionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransactionWithNegativeAmount() throws Exception {
        var account = new Account()
                .setCustomerId(UUID.randomUUID().toString()).setCountry(Country.EE);
        accountMapper.insertAccount(account);

        var balance = new Balance()
                .setCurrency(Currency.getInstance("EUR")).setAmount(BigDecimal.ZERO);
        balanceMapper.insertBalance(balance, account);

        var transactionRequest = TestData.generateTransactionRequest(account.getId());
        transactionRequest.setAmount(BigDecimal.valueOf(-1L));

        this.mockMvc
                .perform(post(TransactionController.PATH)
                        .content(objectMapper.writeValueAsBytes(transactionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransactionWithNoCurrency() throws Exception {
        var account = new Account()
                .setCustomerId(UUID.randomUUID().toString()).setCountry(Country.EE);
        accountMapper.insertAccount(account);

        var balance = new Balance()
                .setCurrency(Currency.getInstance("EUR")).setAmount(BigDecimal.ZERO);
        balanceMapper.insertBalance(balance, account);

        var transactionRequest = TestData.generateTransactionRequest(account.getId());
        transactionRequest.setCurrency(null);

        this.mockMvc
                .perform(post(TransactionController.PATH)
                        .content(objectMapper.writeValueAsBytes(transactionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransactionWithUnknownAccountId() throws Exception {

        var transactionRequest = TestData.generateTransactionRequest(new Random().nextLong());

        this.mockMvc
                .perform(post(TransactionController.PATH)
                        .content(objectMapper.writeValueAsBytes(transactionRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUnknownAccountTransactions() throws Exception {
        this.mockMvc
                .perform(get(TransactionController.PATH + "/{accountId}", new Random().nextLong()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAccountTransactions() throws Exception {
        var account = new Account()
                .setCustomerId(UUID.randomUUID().toString()).setCountry(Country.EE);
        accountMapper.insertAccount(account);

        var transaction = new Transaction()
                .setAccountId(account.getId())
                .setDirection(TransactionDirection.IN)
                .setCurrency(Currency.getInstance("EUR"))
                .setBalanceAfter(BigDecimal.valueOf(100L))
                .setAmount(BigDecimal.valueOf(50L))
                .setDescription("Description");
        transactionMapper.insertTransaction(transaction);

        this.mockMvc
                .perform(get(TransactionController.PATH + "/{accountId}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.[0].accountId").value(account.getId()))
                .andExpect(jsonPath("$.[0].transactionId").value(transaction.getId()))
                .andExpect(jsonPath("$.[0].amount").value(transaction.getAmount().toString()))
                .andExpect(jsonPath("$.[0].currency").value(transaction.getCurrency().getCurrencyCode()))
                .andExpect(jsonPath("$.[0].direction").value(transaction.getDirection().name()))
                .andExpect(jsonPath("$.[0].description").value(transaction.getDescription()))
        ;
    }

    @Test
    void testGetAccountTransactionsWhenNoTransactions() throws Exception {
        var account = new Account()
                .setCustomerId(UUID.randomUUID().toString()).setCountry(Country.EE);
        accountMapper.insertAccount(account);

        this.mockMvc
                .perform(get(TransactionController.PATH + "/{accountId}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
