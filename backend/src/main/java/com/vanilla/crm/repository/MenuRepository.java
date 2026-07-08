package com.vanilla.crm.repository;

import com.vanilla.crm.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Dish, UUID> {
    List<Dish> findByCategory(String category);
}
