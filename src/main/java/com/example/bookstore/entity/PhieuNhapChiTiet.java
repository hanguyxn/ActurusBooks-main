package com.example.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "phieu_nhap_chi_tiet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuNhapChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_phieu_nhap", referencedColumnName = "id")
    private PhieuNhap phieuNhap;

    @Column(name = "ten_san_pham")
    private String tenSanPham;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_nhap")
    private BigDecimal giaNhap;

    @Column(name = "ngay_san_xuat")
    private LocalDate ngaySanXuat;

}