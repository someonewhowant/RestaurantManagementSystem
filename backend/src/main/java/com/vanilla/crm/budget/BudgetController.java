package com.vanilla.crm.budget;

import com.vanilla.crm.budget.dto.BudgetSummaryDto;
import com.vanilla.crm.budget.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        return ResponseEntity.ok(budgetService.getAllTransactions());
    }

    @GetMapping("/summary")
    public ResponseEntity<BudgetSummaryDto> getSummary() {
        return ResponseEntity.ok(budgetService.getSummary());
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionDto dto) {
        return ResponseEntity.ok(budgetService.createTransaction(dto));
    }
}
