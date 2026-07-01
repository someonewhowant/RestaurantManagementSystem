package com.vanilla.crm.staff;

import com.vanilla.crm.staff.dto.EmployeeDto;
import com.vanilla.crm.staff.dto.StatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(staffService.getAllEmployees());
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(staffService.createEmployee(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable UUID id, @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(staffService.updateEmployee(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeDto> changeStatus(@PathVariable UUID id, @RequestBody StatusRequest request) {
        return ResponseEntity.ok(staffService.changeStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/shift")
    public ResponseEntity<EmployeeDto> toggleShift(@PathVariable UUID id) {
        return ResponseEntity.ok(staffService.toggleShift(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        staffService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
