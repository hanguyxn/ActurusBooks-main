package com.example.bookstore.controller.customer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.bookstore.entity.CartItem;
import com.example.bookstore.service.CartService;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService; // Service chứa List<CartItem>

    @GetMapping
    public String showCheckoutPage(HttpSession session, Model model) {
        List<CartItem> cartItems = cartService.getCartItems(session);
        double subtotal = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double shipping = 20000; // cố định
        double total = subtotal + shipping;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipping", shipping);
        model.addAttribute("total", total);

        return "checkout/checkout";
    }
}
