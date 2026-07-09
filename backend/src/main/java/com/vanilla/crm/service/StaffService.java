package com.vanilla.crm.service;

import com.vanilla.crm.dto.staff.EmployeeDto;

import java.util.List;
import java.util.UUID;

public interface StaffService {
    List<EmployeeDto> getAllEmployees();
    EmployeeDto createEmployee(EmployeeDto dto);
    EmployeeDto updateEmployee(UUID id, EmployeeDto dto);
    EmployeeDto changeStatus(UUID id, String newStatus);
    EmployeeDto toggleShift(UUID id);
    void deleteEmployee(UUID id);
    byte[] exportCsv();
}
