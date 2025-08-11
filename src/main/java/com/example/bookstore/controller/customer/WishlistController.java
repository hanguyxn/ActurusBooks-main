package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.User;
import com.example.bookstore.entity.Wishlist;
import com.example.bookstore.service.WishlistService;
import com.example.bookstore.service.BookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private BookService bookService;

    @GetMapping
    public String viewWishlist(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Wishlist> wishlistItems = wishlistService.getByUserId(currentUser.getUser_id());
        model.addAttribute("wishlistItems", wishlistItems);
        return "customer/wishlist/wishlist";
    }

    @PostMapping("/add/{bookId}")
    public String addToWishlist(@PathVariable Integer bookId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm vào danh sách yêu thích!");
            return "redirect:/login";
        }

        // Kiểm tra xem đã có trong wishlist chưa
        if (wishlistService.existsByUserIdAndBookId(currentUser.getUser_id(), bookId)) {
            redirectAttributes.addFlashAttribute("info", "Sách đã có trong danh sách yêu thích!");
        } else {
            wishlistService.addToWishlist(currentUser.getUser_id(), bookId);
            redirectAttributes.addFlashAttribute("success", "Đã thêm vào danh sách yêu thích!");
        }

        return "redirect:/product-detail/" + bookId;
    }

    @GetMapping("/remove/{bookId}")
    public String removeFromWishlist(@PathVariable Integer bookId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        wishlistService.removeFromWishlist(currentUser.getUser_id(), bookId);
        redirectAttributes.addFlashAttribute("success", "Đã xóa khỏi danh sách yêu thích!");
        return "redirect:/wishlist";
    }

    @GetMapping("/clear")
    public String clearWishlist(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        wishlistService.clearWishlist(currentUser.getUser_id());
        redirectAttributes.addFlashAttribute("success", "Đã xóa toàn bộ danh sách yêu thích!");
        return "redirect:/wishlist";
    }

    // API để kiểm tra sách có trong wishlist không (cho AJAX)
    @GetMapping("/check/{bookId}")
    @ResponseBody
    public boolean isInWishlist(@PathVariable Integer bookId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return false;
        }
        return wishlistService.existsByUserIdAndBookId(currentUser.getUser_id(), bookId);
    }
}
