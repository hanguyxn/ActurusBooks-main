package com.example.bookstore.service;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import com.example.bookstore.entity.CartItem;

@Service
public class CartService {
    @SuppressWarnings("unchecked")
    public List<CartItem> getCartItems(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        return cart != null ? cart : new ArrayList<>();
    }
}
