package bank.domain;

import bank.enums.Country;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Account {
    private Long id;
    private String customerId;
    private Country country;
    private List<Balance> balances = new ArrayList<>();
}
