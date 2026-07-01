package com.vanilla.crm.staff;

import com.vanilla.crm.staff.dto.EmployeeDto;
import com.vanilla.crm.staff.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        return staffRepository.findAll().stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        Employee employee = Employee.builder()
                .name(dto.getName())
                .role(EmployeeDto.toRoleEnum(dto.getRole()))
                .status(EmployeeDto.toStatusEnum(dto.getStatus()))
                .hireDate(dto.getHireDate() != null ? LocalDate.parse(dto.getHireDate()) : LocalDate.now())
                .onShift(dto.getOnShift() != null ? dto.getOnShift() : false)
                .build();

        if (dto.getVacationStart() != null) employee.setVacationStart(LocalDate.parse(dto.getVacationStart()));
        if (dto.getVacationEnd() != null) employee.setVacationEnd(LocalDate.parse(dto.getVacationEnd()));

        return EmployeeDto.fromEntity(staffRepository.save(employee));
    }

    @Transactional
    public EmployeeDto updateEmployee(UUID id, EmployeeDto dto) {
        Employee employee = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getRole() != null) employee.setRole(EmployeeDto.toRoleEnum(dto.getRole()));
        if (dto.getHireDate() != null) employee.setHireDate(LocalDate.parse(dto.getHireDate()));
        if (dto.getVacationStart() != null) employee.setVacationStart(LocalDate.parse(dto.getVacationStart()));
        if (dto.getVacationEnd() != null) employee.setVacationEnd(LocalDate.parse(dto.getVacationEnd()));

        return EmployeeDto.fromEntity(staffRepository.save(employee));
    }

    @Transactional
    public EmployeeDto changeStatus(UUID id, String newStatus) {
        Employee employee = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setStatus(EmployeeDto.toStatusEnum(newStatus));

        // If fired, record the date
        if (employee.getStatus() == Employee.EmployeeStatus.FIRED) {
            employee.setFireDate(LocalDate.now());
            employee.setOnShift(false);
            employee.setShiftStartTime(null);
        }

        return EmployeeDto.fromEntity(staffRepository.save(employee));
    }

    @Transactional
    public EmployeeDto toggleShift(UUID id) {
        Employee employee = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        boolean isNowOnShift = !Boolean.TRUE.equals(employee.getOnShift());
        employee.setOnShift(isNowOnShift);
        employee.setShiftStartTime(isNowOnShift ? Instant.now() : null);

        return EmployeeDto.fromEntity(staffRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        staffRepository.deleteById(id);
    }
}
