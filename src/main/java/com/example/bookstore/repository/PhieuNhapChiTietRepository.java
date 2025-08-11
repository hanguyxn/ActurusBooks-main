package com.example.bookstore.repository;

import com.example.bookstore.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhieuNhapChiTietRepository extends JpaRepository<PhieuNhapChiTiet, Integer> {
    List<PhieuNhapChiTiet> findByPhieuNhap(PhieuNhap phieuNhap);
}