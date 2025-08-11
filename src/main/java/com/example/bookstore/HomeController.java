package com.example.bookstore;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpSession;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());
        }
        List<Book> newBooks = bookRepository.findTop4ByOrderByBookIdDesc();
        model.addAttribute("newBooks", newBooks);
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