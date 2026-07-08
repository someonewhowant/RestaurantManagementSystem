package com.vanilla.crm.controller;

import com.vanilla.crm.service.BudgetService;

import com.vanilla.crm.dto.budget.BudgetSummaryDto;
import com.vanilla.crm.dto.budget.TransactionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.io.StringWriter;

@RestController
@RequestMapping("/budget")
@RequiredArgsConstructor
@Tag(name = "Бюджет", description = "Финансовые операции: доходы, расходы, аналитика")
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "Все транзакции", description = "Возвращает историю финансовых операций постранично с фильтрацией по дате.")
    @ApiResponse(responseCode = "200", description = "Страница транзакций")
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionDto>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "date", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        
        Instant start = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant end = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        
        return ResponseEntity.ok(budgetService.getTransactions(start, end, pageable));
    }

    @Operation(summary = "Финансовая сводка", description = "Возвращает агрегированные данные: общий доход, расход и баланс с фильтрацией по дате.")
    @ApiResponse(responseCode = "200", description = "Финансовая сводка")
    @GetMapping("/summary")
    public ResponseEntity<BudgetSummaryDto> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Instant start = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant end = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
                
        return ResponseEntity.ok(budgetService.getSummary(start, end));
    }

    @Operation(summary = "Создать транзакцию", description = "Ручное добавление финансовой операции (доход или расход).")
    @ApiResponse(responseCode = "200", description = "Созданная транзакция")
    @PostMapping("/transactions")
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.ok(budgetService.createTransaction(dto));
    }

    @Operation(summary = "Возврат", description = "Создает сторно-транзакцию и возвращает ингредиенты на склад.")
    @PostMapping("/transactions/{id}/refund")
    public ResponseEntity<TransactionDto> refundTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.refundTransaction(id));
    }

    @Operation(summary = "Экспорт в CSV", description = "Скачать историю транзакций в формате CSV.")
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Instant start = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant end = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        
        // Fetch all matching without pagination
        Page<TransactionDto> transactions = budgetService.getTransactions(start, end, Pageable.unpaged());

        StringWriter writer = new StringWriter();
        writer.append("ID;Дата;Сумма;Тип;Категория;Описание\n");
        for (TransactionDto tx : transactions.getContent()) {
            writer.append(String.format("%s;%s;%s;%s;%s;%s\n",
                    tx.getId(),
                    tx.getDate() != null ? tx.getDate().toString() : "",
                    tx.getAmount(),
                    tx.getType(),
                    tx.getCategory(),
                    tx.getDescription() != null ? tx.getDescription().replace(";", " ") : ""));
        }

        byte[] textBytes = writer.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] csvBytes = new byte[textBytes.length + 3];
        csvBytes[0] = (byte) 0xEF;
        csvBytes[1] = (byte) 0xBB;
        csvBytes[2] = (byte) 0xBF;
        System.arraycopy(textBytes, 0, csvBytes, 3, textBytes.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "budget_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}
