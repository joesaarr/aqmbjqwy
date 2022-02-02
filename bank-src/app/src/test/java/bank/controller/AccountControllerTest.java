package bank.controller;

import bank.TestData;
import bank.data.AccountMapper;
import bank.domain.Account;
import bank.enums.Country;
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

import java.util.Currency;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AccountMapper accountMapper;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDoesNotGetAccount() throws Exception {
        this.mockMvc
                .perform(get(AccountController.PATH + "/{id}", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAccount() throws Exception {
        var customerId = UUID.randomUUID().toString();
        var country = Country.EE;
        var account = new Account().setCustomerId(customerId).setCountry(country);
        accountMapper.insertAccount(account);

        this.mockMvc
                .perform(get(AccountController.PATH + "/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.country").value(country.name()));
    }

    @Test
    void testCreateAccount() throws Exception {
        var accountRequest = TestData.generateCreateAccountRequest();

        this.mockMvc
                .perform(post(AccountController.PATH)
                        .content(objectMapper.writeValueAsBytes(accountRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.customerId").value(accountRequest.getCustomerId()))
                .andExpect(jsonPath("$.country").value(accountRequest.getCountry().name()))
                .andExpect(jsonPath("$.balances", hasSize(accountRequest.getCurrencies().size())));
    }

    @Test
    void testCreateAccountWithNoCurrencies() throws Exception {
        var accountRequest = TestData.generateCreateAccountRequest();
        accountRequest.setCurrencies(Set.of());

        this.mockMvc
                .perform(post(AccountController.PATH)
                        .content(objectMapper.writeValueAsBytes(accountRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateAccountWithNoCountry() throws Exception {
        var accountRequest = TestData.generateCreateAccountRequest();
        accountRequest.setCountry(null);

        this.mockMvc
                .perform(post(AccountController.PATH)
                        .content(objectMapper.writeValueAsBytes(accountRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateAccountWithInvalidCurrency() throws Exception {
        var accountRequest = TestData.generateCreateAccountRequest();
        accountRequest.setCurrencies(Set.of(Currency.getInstance("RUB")));

        this.mockMvc
                .perform(post(AccountController.PATH)
                        .content(objectMapper.writeValueAsBytes(accountRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateAccountWithNoCustomerId() throws Exception {
        var accountRequest = TestData.generateCreateAccountRequest();
        accountRequest.setCustomerId(null);

        this.mockMvc
                .perform(post(AccountController.PATH)
                        .content(objectMapper.writeValueAsBytes(accountRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateAccountWithEmptyCustomerId() throws Exception {
        var accountRequest = TestData.generateCreateAccountRequest();
        accountRequest.setCustomerId("");

        this.mockMvc
                .perform(post(AccountController.PATH)
                        .content(objectMapper.writeValueAsBytes(accountRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
