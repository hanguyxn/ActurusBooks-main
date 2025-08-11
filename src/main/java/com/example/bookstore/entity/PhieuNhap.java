package com.example.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "phieu_nhap")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuNhap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phieu_nhap")
    private String maPhieuNhap;

    @Column(name = "ngay_nhap")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayNhap;

    @Column(name = "nha_cung_cap")
    private String nhaCungCap;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "ghi_chu")
    private String ghiChu;

}