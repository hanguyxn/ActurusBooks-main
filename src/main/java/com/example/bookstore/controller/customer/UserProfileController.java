package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.User;
import com.example.bookstore.entity.UserAddress;
import com.example.bookstore.service.UserAddressService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserProfileController {

    @Autowired
    private UserAddressService userAddressService;

    @GetMapping("/addresses")
    public String manageAddresses(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<UserAddress> addresses = userAddressService.getAddressesByUserId(currentUser.getUser_id());
        model.addAttribute("addresses", addresses);
        model.addAttribute("user", currentUser);

        return "customer/profile/addresses";
    }

    @PostMapping("/addresses/{id}/set-default")
    public String setDefaultAddress(@PathVariable Integer id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            userAddressService.setAsDefault(id, currentUser.getUser_id());
            redirectAttributes.addFlashAttribute("message", "Đã đặt làm địa chỉ mặc định");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/user/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@PathVariable Integer id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            userAddressService.delete(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa địa chỉ thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/user/addresses";
    }
}
