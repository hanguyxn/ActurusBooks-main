package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.User;
import com.example.bookstore.entity.Wishlist;
import com.example.bookstore.service.WishlistService;
import com.example.bookstore.service.BookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // API để toggle wishlist (thêm/xóa)
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestParam Integer bookId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Bạn cần đăng nhập để thêm vào danh sách yêu thích");
            return ResponseEntity.ok(response);
        }

        try {
            boolean exists = wishlistService.existsByUserIdAndBookId(user.getUser_id(), bookId);

            if (exists) {
                wishlistService.removeFromWishlist(user.getUser_id(), bookId);
                response.put("success", true);
                response.put("action", "removed");
                response.put("message", "Đã xóa khỏi danh sách yêu thích");
            } else {
                wishlistService.addToWishlist(user.getUser_id(), bookId);
                response.put("success", true);
                response.put("action", "added");
                response.put("message", "Đã thêm vào danh sách yêu thích");
            }

            response.put("wishlistCount", wishlistService.countByUserId(user.getUser_id()));
            response.put("inWishlist", !exists);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
