package com.example.bookstore;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpSession;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Wishlist;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.WishlistService;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");

        // Luôn hiển thị 8 sách của cửa hàng
        List<Book> storeBooks = bookRepository.findTop8ByOrderByBookIdDesc();
        model.addAttribute("storeBooks", storeBooks);

        if (user != null) {
            model.addAttribute("username", user.getUsername());

            // Lấy sách yêu thích của user
            List<Wishlist> wishlistItems = wishlistService.getByUserId(user.getUser_id());

            if (!wishlistItems.isEmpty()) {
                // Có sách yêu thích -> hiển thị section wishlist
                List<Book> wishlistBooks = wishlistItems.stream()
                        .limit(4)
                        .map(Wishlist::getBook)
                        .collect(Collectors.toList());

                model.addAttribute("wishlistBooks", wishlistBooks);
                model.addAttribute("hasWishlist", true);
            } else {
                model.addAttribute("hasWishlist", false);
            }
        } else {
            model.addAttribute("hasWishlist", false);
        }

        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/product-detail")
    public String productDetail(@RequestParam(required = false) Integer bookId, Model model) {
        if (bookId != null) {
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book != null) {
                model.addAttribute("book", book);
            } else {
                return "redirect:/product"; // Chuyển hướng nếu không tìm thấy sách
            }
        }
        return "product-detail";
    }

}