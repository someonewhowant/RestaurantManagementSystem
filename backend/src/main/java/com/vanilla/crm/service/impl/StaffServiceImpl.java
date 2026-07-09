package com.vanilla.crm.service.impl;

import com.vanilla.crm.exception.ResourceNotFoundException;
import com.vanilla.crm.util.CsvExportUtil;

import com.vanilla.crm.repository.StaffRepository;

import com.vanilla.crm.dto.staff.EmployeeDto;
import com.vanilla.crm.entity.Employee;
import com.vanilla.crm.mapper.StaffMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import com.vanilla.crm.service.StaffService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;

    @Transactional(readOnly = true)
    @Override
    public List<EmployeeDto> getAllEmployees() {
        return staffRepository.findAll().stream()
                .map(staffMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EmployeeDto createEmployee(EmployeeDto dto) {
        log.info("Creating new employee: {}", dto.getName());
        Employee employee = Employee.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .salary(dto.getSalary())
                .salaryDate(dto.getSalaryDate())
                .role(staffMapper.toRoleEnum(dto.getRole()))
                .status(staffMapper.toStatusEnum(dto.getStatus()))
                .hireDate(dto.getHireDate() != null && !dto.getHireDate().isEmpty() ? LocalDate.parse(dto.getHireDate()) : LocalDate.now())
                .onShift(dto.getOnShift() != null ? dto.getOnShift() : false)
                .build();

        if (dto.getVacationStart() != null && !dto.getVacationStart().isEmpty()) employee.setVacationStart(LocalDate.parse(dto.getVacationStart()));
        if (dto.getVacationEnd() != null && !dto.getVacationEnd().isEmpty()) employee.setVacationEnd(LocalDate.parse(dto.getVacationEnd()));

        return staffMapper.toDto(staffRepository.save(employee));
    }

    @Transactional
    @Override
    public EmployeeDto updateEmployee(UUID id, EmployeeDto dto) {
        log.info("Updating employee {}", id);
        Employee employee = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getPhone() != null) employee.setPhone(dto.getPhone());
        if (dto.getEmail() != null) employee.setEmail(dto.getEmail());
        if (dto.getSalary() != null) employee.setSalary(dto.getSalary());
        if (dto.getSalaryDate() != null) employee.setSalaryDate(dto.getSalaryDate());
        if (dto.getRole() != null) employee.setRole(staffMapper.toRoleEnum(dto.getRole()));
        if (dto.getHireDate() != null && !dto.getHireDate().isEmpty()) {
            employee.setHireDate(LocalDate.parse(dto.getHireDate()));
        }

        if (dto.getStatus() != null) {
            Employee.EmployeeStatus newStatus = staffMapper.toStatusEnum(dto.getStatus());
            employee.setStatus(newStatus);
            if (newStatus == Employee.EmployeeStatus.FIRED) {
                employee.setOnShift(false);
                employee.setShiftStartTime(null);
            } else if (newStatus == Employee.EmployeeStatus.ACTIVE) {
                employee.setFireDate(null);
                employee.setVacationStart(null);
                employee.setVacationEnd(null);
            }
        }

        if (dto.getFireDate() != null) {
            employee.setFireDate(dto.getFireDate().isEmpty() ? null : LocalDate.parse(dto.getFireDate()));
        }
        if (dto.getVacationStart() != null) {
            employee.setVacationStart(dto.getVacationStart().isEmpty() ? null : LocalDate.parse(dto.getVacationStart()));
        }
        if (dto.getVacationEnd() != null) {
            employee.setVacationEnd(dto.getVacationEnd().isEmpty() ? null : LocalDate.parse(dto.getVacationEnd()));
        }

        return staffMapper.toDto(staffRepository.save(employee));
    }

    @Transactional
    @Override
    public EmployeeDto changeStatus(UUID id, String newStatus) {
        log.info("Changing status of employee {} to {}", id, newStatus);
        Employee employee = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employee.setStatus(staffMapper.toStatusEnum(newStatus));

        // If fired, record the date
        if (employee.getStatus() == Employee.EmployeeStatus.FIRED) {
            employee.setFireDate(LocalDate.now());
            employee.setOnShift(false);
            employee.setShiftStartTime(null);
        }

        return staffMapper.toDto(staffRepository.save(employee));
    }

    @Transactional
    @Override
    public EmployeeDto toggleShift(UUID id) {
        log.info("Toggling shift for employee {}", id);
        Employee employee = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        boolean isNowOnShift = !Boolean.TRUE.equals(employee.getOnShift());
        employee.setOnShift(isNowOnShift);
        employee.setShiftStartTime(isNowOnShift ? Instant.now() : null);

        return staffMapper.toDto(staffRepository.save(employee));
    }

    @Transactional
    @Override
    public void deleteEmployee(UUID id) {
        log.info("Deleting employee {}", id);
        staffRepository.deleteById(id);
    }

    @Override
    public byte[] exportCsv() {
        log.info("Exporting staff to CSV");
        List<EmployeeDto> employees = getAllEmployees();
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Имя;Телефон;Email;Зарплата;Дата зарплаты;Роль;Статус;На смене\n");
        for (EmployeeDto emp : employees) {
            sb.append(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s\n",
                    emp.getId(),
                    CsvExportUtil.escapeField(emp.getName()),
                    CsvExportUtil.escapeField(emp.getPhone()),
                    CsvExportUtil.escapeField(emp.getEmail()),
                    emp.getSalary() != null ? emp.getSalary() : "",
                    CsvExportUtil.escapeField(emp.getSalaryDate()),
                    CsvExportUtil.escapeField(emp.getRole()),
                    CsvExportUtil.escapeField(emp.getStatus()),
                    Boolean.TRUE.equals(emp.getOnShift()) ? "Да" : "Нет"));
        }
        return CsvExportUtil.wrapWithBom(sb.toString());
    }
}
