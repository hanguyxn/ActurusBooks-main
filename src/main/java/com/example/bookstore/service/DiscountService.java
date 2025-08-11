package com.example.bookstore.service;

import com.example.bookstore.entity.Discount;
import com.example.bookstore.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {
    @Autowired
    private DiscountRepository repo;

    public List<Discount> findAll() {
        return repo.findAll();
    }

    public String addDiscount(String code, Double value, String expiryDateStr) {
        // Kiểm tra mã giảm giá
        if (code == null || code.trim().isEmpty() || code.length() > 50) {
            return "Mã giảm giá không hợp lệ! Phải từ 1-50 ký tự.";
        }
        if (repo.existsByCode(code.trim())) {
            return "Mã giảm giá đã tồn tại!";
        }
        // Kiểm tra giá trị
        if (value == null || value < 0 || value > 100) {
            return "Giá trị giảm giá phải từ 0% đến 100%!";
        }
        // Kiểm tra ngày hết hạn
        if (expiryDateStr == null || expiryDateStr.isEmpty()) {
            return "Ngày hết hạn không được để trống!";
        }
        try {
            // Chuyển đổi định dạng từ input datetime-local (yyyy-MM-dd'T'HH:mm)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime date = LocalDateTime.parse(expiryDateStr, formatter);
            if (date.isBefore(LocalDateTime.now())) {
                return "Ngày hết hạn phải lớn hơn thời gian hiện tại!";
            }
            Discount d = new Discount(null, code.trim(), value, date, true);
            repo.save(d);
            return null;
        } catch (DateTimeParseException e) {
            return "Định dạng ngày không hợp lệ! Sử dụng định dạng yyyy-MM-dd HH:mm.";
        }
    }

    public Discount getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã giảm giá với ID: " + id));
    }

    public void delete(Integer id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Mã giảm giá không tồn tại!");
        }
        repo.deleteById(id);
    }

    public void toggle(Integer id) {
        repo.findById(id).ifPresent(d -> {
            d.setIsActive(!d.getIsActive());
            repo.save(d);
        });
    }

    public Discount validateDiscount(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        Optional<Discount> discountOpt = repo.findByCodeAndIsActiveTrue(code.trim().toUpperCase());
        if (discountOpt.isPresent()) {
            Discount discount = discountOpt.get();
            if (discount.getExpiryDate().isAfter(LocalDateTime.now())) {
                return discount;
            }
        }
        return null;
    }
}