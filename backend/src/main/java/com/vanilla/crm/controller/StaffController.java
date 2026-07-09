package com.vanilla.crm.controller;

import com.vanilla.crm.service.StaffService;

import com.vanilla.crm.dto.staff.EmployeeDto;
import com.vanilla.crm.dto.staff.StatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vanilla.crm.util.CsvExportUtil;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
@Tag(name = "Персонал", description = "Управление сотрудниками ресторана: графики, статусы, смены")
public class StaffController {

    private final StaffService staffService;

    @Operation(summary = "Все сотрудники", description = "Возвращает полный список сотрудников ресторана.")
    @ApiResponse(responseCode = "200", description = "Список сотрудников")
    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(staffService.getAllEmployees());
    }

    @Operation(summary = "Добавить сотрудника", description = "Создаёт нового сотрудника.")
    @ApiResponse(responseCode = "200", description = "Созданный сотрудник")
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(staffService.createEmployee(dto));
    }

    @Operation(summary = "Обновить сотрудника", description = "Полное обновление данных сотрудника по ID.")
    @ApiResponse(responseCode = "200", description = "Обновлённый сотрудник")
    @ApiResponse(responseCode = "404", description = "Сотрудник не найден")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable UUID id, @Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(staffService.updateEmployee(id, dto));
    }

    @Operation(summary = "Изменить статус", description = "Смена статуса сотрудника (active / on_leave / fired).")
    @ApiResponse(responseCode = "200", description = "Обновлённый сотрудник")
    @ApiResponse(responseCode = "404", description = "Сотрудник не найден")
    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeDto> changeStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return ResponseEntity.ok(staffService.changeStatus(id, request.getStatus()));
    }

    @Operation(summary = "Переключить смену", description = "Отметка начала или окончания рабочей смены.")
    @ApiResponse(responseCode = "200", description = "Обновлённый сотрудник")
    @ApiResponse(responseCode = "404", description = "Сотрудник не найден")
    @PatchMapping("/{id}/shift")
    public ResponseEntity<EmployeeDto> toggleShift(@PathVariable UUID id) {
        return ResponseEntity.ok(staffService.toggleShift(id));
    }

    @Operation(summary = "Удалить сотрудника", description = "Полностью удаляет запись о сотруднике.")
    @ApiResponse(responseCode = "204", description = "Сотрудник удалён")
    @ApiResponse(responseCode = "404", description = "Сотрудник не найден")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        staffService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Экспорт в CSV", description = "Скачать список персонала в формате CSV.")
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        return CsvExportUtil.buildCsvResponse(staffService.exportCsv(), "staff_report.csv");
    }
}
