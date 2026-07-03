package com.vanilla.crm.budget;

import com.vanilla.crm.budget.dto.BudgetSummaryDto;
import com.vanilla.crm.budget.dto.TransactionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/budget")
@RequiredArgsConstructor
@Tag(name = "Бюджет", description = "Финансовые операции: доходы, расходы, аналитика")
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "Все транзакции", description = "Возвращает историю финансовых операций постранично.")
    @ApiResponse(responseCode = "200", description = "Страница транзакций")
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionDto>> getTransactions(
            @PageableDefault(size = 20, sort = "date", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(budgetService.getTransactions(pageable));
    }

    @Operation(summary = "Финансовая сводка", description = "Возвращает агрегированные данные: общий доход, расход и баланс.")
    @ApiResponse(responseCode = "200", description = "Финансовая сводка")
    @GetMapping("/summary")
    public ResponseEntity<BudgetSummaryDto> getSummary() {
        return ResponseEntity.ok(budgetService.getSummary());
    }

    @Operation(summary = "Создать транзакцию", description = "Ручное добавление финансовой операции (доход или расход).")
    @ApiResponse(responseCode = "200", description = "Созданная транзакция")
    @PostMapping("/transactions")
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.ok(budgetService.createTransaction(dto));
    }
}
