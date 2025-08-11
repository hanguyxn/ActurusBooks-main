package com.example.bookstore.controller.admin;

import com.example.bookstore.dto.DiscountDTO;
import com.example.bookstore.entity.Discount;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.DiscountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/discounts")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @GetMapping("/list")
    public String showDiscounts(Model model, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/login";
        model.addAttribute("discounts", discountService.findAll());
        model.addAttribute("newDiscount", new DiscountDTO());
        return "admin/discount/discount-mana";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("newDiscount") DiscountDTO discountDTO,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/login";

        String error = discountService.addDiscount(discountDTO.getCode(),
                discountDTO.getValue(),
                discountDTO.getExpiryDate());
        if (error != null) {
            redirectAttributes.addFlashAttribute("errorMessage", error);
            return "redirect:/admin/discounts/list";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Thêm mã giảm giá thành công!");
        return "redirect:/admin/discounts/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/login";
        try {
            discountService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa mã giảm giá: " + e.getMessage());
        }
        return "redirect:/admin/discounts/list";
    }

    @GetMapping("/edit/{id}")
    public String editDiscount(@PathVariable Integer id, Model model, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/login";

        Discount discount = discountService.getById(id);

        model.addAttribute("discount", discount);
        model.addAttribute("editMode", true);
        model.addAttribute("discounts", discountService.findAll());
        return "redirect:/admin/discounts/list";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Integer id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/login";
        try {
            discountService.toggle(id);
            redirectAttributes.addFlashAttribute("successMessage", "Chuyển trạng thái mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi chuyển trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/discounts/list";
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "admin".equals(user.getRole());
    }
}