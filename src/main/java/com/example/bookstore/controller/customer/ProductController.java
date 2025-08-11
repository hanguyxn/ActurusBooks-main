package com.example.bookstore.controller.customer;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {

    @Autowired
    private BookService bookService;

    @GetMapping("/product")
    public String product(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "customer/product/product";
    }

    @GetMapping("/product-detail/{id}")
    public String productDetail(@PathVariable("id") Integer id, Model model) {
        BookDTO book = bookService.findById(id);
        model.addAttribute("book", book);
        return "customer/product/product-detail";
    }
}