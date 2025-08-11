package com.example.bookstore.controller.admin;

import com.example.bookstore.repository.*;
import com.example.bookstore.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Controller
@RequestMapping("/phieu-nhap-chi-tiet")
public class PhieuNhapChiTietController {
    @Autowired
    private PhieuNhapChiTietRepository chiTietRepo;
    @Autowired
    private PhieuNhapRepository phieuNhapRepo;

    @GetMapping("/hien-thi")
    public String hienThi(Model model) {
        List<PhieuNhapChiTiet> list = chiTietRepo.findAll();
        List<PhieuNhap> phieuNhapList = phieuNhapRepo.findAll();
        Map<Integer, PhieuNhap> phieuNhapMap = phieuNhapList.stream()
                .collect(Collectors.toMap(PhieuNhap::getId, pn -> pn));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Map<String, Object>> listView = list.stream().map(ct -> {
            Map<String, Object> map = new HashMap<>();
            map.put("ct", ct);
            map.put("ngaySanXuatStr", ct.getNgaySanXuat() != null ? ct.getNgaySanXuat().format(formatter) : "");
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("listChiTietView", listView);
        model.addAttribute("listPhieuNhap", phieuNhapList);
        model.addAttribute("phieuNhapMap", phieuNhapMap);
        return "phieu_nhap_chi_tiet/hien_thi";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("chiTiet", new PhieuNhapChiTiet());
        model.addAttribute("listPhieuNhap", phieuNhapRepo.findAll());
        return "phieu_nhap_chi_tiet/add";
    }

    @PostMapping("/add")
    public String add(@Validated @ModelAttribute("chiTiet") PhieuNhapChiTiet chiTiet,
            BindingResult result,
            Model model) {
        System.out.println("Submit add: " + chiTiet);
        if (result.hasErrors()) {
            System.out.println("Có lỗi validate: " + result);
            model.addAttribute("listPhieuNhap", phieuNhapRepo.findAll());
            return "phieu_nhap_chi_tiet/add";
        }
        chiTietRepo.save(chiTiet);
        System.out.println("Đã lưu vào DB!");
        return "redirect:/phieu-nhap-chi-tiet/hien-thi";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("id") Integer id, Model model) {
        PhieuNhapChiTiet chiTiet = chiTietRepo.findById(id).orElse(null);
        List<PhieuNhapChiTiet> list = chiTietRepo.findAll();
        List<PhieuNhap> phieuNhapList = phieuNhapRepo.findAll();
        Map<Integer, PhieuNhap> phieuNhapMap = phieuNhapList.stream()
                .collect(Collectors.toMap(PhieuNhap::getId, pn -> pn));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Map<String, Object> chiTietView = null;
        if (chiTiet != null) {
            chiTietView = new HashMap<>();
            chiTietView.put("ct", chiTiet);
            chiTietView.put("ngaySanXuatStr",
                    chiTiet.getNgaySanXuat() != null ? chiTiet.getNgaySanXuat().format(formatter) : "");
        }
        List<Map<String, Object>> listView = list.stream().map(ct -> {
            Map<String, Object> map = new HashMap<>();
            map.put("ct", ct);
            map.put("ngaySanXuatStr", ct.getNgaySanXuat() != null ? ct.getNgaySanXuat().format(formatter) : "");
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("chiTietView", chiTietView);
        model.addAttribute("listChiTietView", listView);
        model.addAttribute("listPhieuNhap", phieuNhapList);
        model.addAttribute("phieuNhapMap", phieuNhapMap);
        return "phieu_nhap_chi_tiet/hien_thi";
    }
}