package com.example.bookstore.controller.admin;

import com.example.bookstore.entity.PhieuNhap;
import com.example.bookstore.entity.PhieuNhapChiTiet;
import com.example.bookstore.repository.PhieuNhapChiTietRepository;
import com.example.bookstore.repository.PhieuNhapRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phieu-nhap")
public class PhieuNhapController {
    @Autowired
    private PhieuNhapRepository phieuNhapRepo;
    @Autowired
    private PhieuNhapChiTietRepository chiTietRepo;

    @GetMapping
    public List<PhieuNhap> getAllPhieuNhap() {
        return phieuNhapRepo.findAll();
    }

    @PostMapping
    public PhieuNhap createPhieuNhap(@RequestBody PhieuNhap phieuNhap) {
        return phieuNhapRepo.save(phieuNhap);
    }

    @GetMapping("/{id}/chi-tiet")
    public List<PhieuNhapChiTiet> getChiTietByPhieuNhap(@PathVariable Integer id) {
        PhieuNhap phieuNhap = phieuNhapRepo.findById(id).orElse(null);
        if (phieuNhap == null)
            return List.of();
        return chiTietRepo.findByPhieuNhap(phieuNhap);
    }

    @PostMapping("/{id}/chi-tiet")
    public PhieuNhapChiTiet createChiTiet(@PathVariable Integer id, @RequestBody PhieuNhapChiTiet chiTiet) {
        PhieuNhap phieuNhap = phieuNhapRepo.findById(id).orElse(null);
        chiTiet.setPhieuNhap(phieuNhap);
        return chiTietRepo.save(chiTiet);
    }
}