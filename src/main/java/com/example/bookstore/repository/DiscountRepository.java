package com.example.bookstore.repository;

import com.example.bookstore.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    boolean existsByCode(String code);

    Optional<Discount> findByCode(String code);

    Optional<Discount> findByCodeAndIsActiveTrue(String code);
}