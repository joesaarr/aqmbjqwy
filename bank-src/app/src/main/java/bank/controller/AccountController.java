package bank.controller;

import bank.dto.AccountDto;
import bank.request.CreateAccountRequest;
import bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static bank.controller.AccountController.PATH;

@Validated
@RestController
@RequestMapping(PATH)
public class AccountController {

    public static final String PATH = "/account";

    @Autowired
    private AccountService accountService;

    @PostMapping
    ResponseEntity<AccountDto> create(@RequestBody @Valid CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    @GetMapping("/{id}")
    ResponseEntity<AccountDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.find(id));
    }
}
