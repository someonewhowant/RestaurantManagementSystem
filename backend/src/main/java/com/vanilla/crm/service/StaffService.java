package com.vanilla.crm.service;

import com.vanilla.crm.dto.staff.EmployeeDto;
import com.vanilla.crm.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface StaffService {
    List<EmployeeDto> getAllEmployees();
    EmployeeDto createEmployee(EmployeeDto dto);
    EmployeeDto updateEmployee(UUID id, EmployeeDto dto);
    EmployeeDto changeStatus(UUID id, String newStatus);
    EmployeeDto toggleShift(UUID id);
    void deleteEmployee(UUID id);
}
