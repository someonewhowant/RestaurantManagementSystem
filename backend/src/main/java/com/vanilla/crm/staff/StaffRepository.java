package com.vanilla.crm.staff;

import com.vanilla.crm.staff.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Employee, UUID> {
}
