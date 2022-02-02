package bank.controller;

import bank.dto.TransactionDto;
import bank.request.TransactionRequest;
import bank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static bank.controller.TransactionController.PATH;

@Validated
@RestController
@RequestMapping(PATH)
public class TransactionController {

    public static final String PATH = "/transaction";

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    ResponseEntity<TransactionDto> create(@RequestBody @Valid TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(request));
    }

    @GetMapping("/{accountId}")
    ResponseEntity<List<TransactionDto>> get(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.findAllByAccountId(accountId));
    }
}
