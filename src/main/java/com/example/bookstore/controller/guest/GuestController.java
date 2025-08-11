package com.example.bookstore.controller.guest;

import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/guest")
public class GuestController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/checkout")
    public String guestCheckout(Model model) {
        // Cho phép khách vãng lai checkout
        model.addAttribute("isGuest", true);
        return "customer/checkout/guest-checkout";
    }

    @GetMapping("/products")
    public String guestProducts(Model model) {
        // Khách vãng lai có thể xem sản phẩm
        List<Book> books = bookRepository.findAll();
        model.addAttribute("books", books);
        model.addAttribute("isGuest", true);
        return "customer/product/product";
    }
}
