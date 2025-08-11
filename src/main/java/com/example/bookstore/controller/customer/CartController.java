package com.example.bookstore.controller.customer;

import com.example.bookstore.entity.CartItem;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Discount;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.DiscountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DiscountService discountService;

    @GetMapping("/view")
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = getCartFromSession(session);
        double total = cart.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Integer bookId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        List<CartItem> cart = getCartFromSession(session);

        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();

            // Kiểm tra xem sách đã có trong giỏ hàng chưa
            Optional<CartItem> existingItem = cart.stream()
                    .filter(item -> item.getBookId().equals(bookId))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Cập nhật số lượng
                existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            } else {
                // Thêm mới
                CartItem newItem = new CartItem();
                newItem.setBookId(book.getBookId());
                newItem.setTitle(book.getTitle());
                newItem.setPrice(book.getPrice());
                newItem.setImageUrl(book.getImageUrl());
                newItem.setQuantity(quantity);
                cart.add(newItem);
            }

            session.setAttribute("cart", cart);
            redirectAttributes.addFlashAttribute("message", "Đã thêm sách vào giỏ hàng!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sách!");
        }

        return "redirect:/cart/view";
    }

    @GetMapping("/update")
    public String updateQuantity(@RequestParam Integer bookId,
            @RequestParam Integer quantity,
            HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);

        cart.stream()
                .filter(item -> item.getBookId().equals(bookId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        session.setAttribute("cart", cart);
        return "redirect:/cart/view";
    }

    @GetMapping("/remove/{bookId}")
    public String removeFromCart(@PathVariable Integer bookId, HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        cart.removeIf(item -> item.getBookId().equals(bookId));
        session.setAttribute("cart", cart);
        return "redirect:/cart/view";
    }

    @GetMapping("/clear")
    public String clearCart(HttpSession session) {
        session.removeAttribute("cart");
        return "redirect:/cart/view";
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model) {
        List<CartItem> cartItems = getCartFromSession(session);

        if (cartItems.isEmpty()) {
            return "redirect:/cart/view";
        }

        double subtotal = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        double shippingFee = 20000;
        double total = subtotal + shippingFee;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);
        return "checkout";
    }

    @PostMapping("/apply-discount")
    public String applyDiscount(@RequestParam String voucher,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        System.out.println("=== Apply Discount Debug ===");
        System.out.println("Voucher code: " + voucher);

        List<CartItem> cartItems = getCartFromSession(session);
        double subtotal = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        double shippingFee = 20000;
        double discountValue = 0;

        // Validate voucher từ database
        try {
            Discount discount = discountService.validateDiscount(voucher);
            System.out.println("Discount found: "
                    + (discount != null ? discount.getCode() + " - " + discount.getValue() + "%" : "null"));

            if (discount != null) {
                discountValue = discount.getValue();
            } else {
                System.out.println("Discount validation failed");
                redirectAttributes.addFlashAttribute("errorMessage", "Mã giảm giá không hợp lệ hoặc đã hết hạn!");
                return "redirect:/cart/checkout";
            }
        } catch (Exception e) {
            System.out.println("Exception in discount validation: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi xử lý mã giảm giá!");
            return "redirect:/cart/checkout";
        }

        double discountAmount = subtotal * discountValue / 100;
        double total = subtotal - discountAmount + shippingFee;

        System.out.println(
                "Discount calculation - Subtotal: " + subtotal + ", Discount: " + discountAmount + ", Total: " + total);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("discountValue", discountValue);
        model.addAttribute("voucherCode", voucher);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("total", total);

        return "checkout";
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCartFromSession(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    // API để lấy số lượng items trong cart (cho navbar)
    @GetMapping("/count")
    @ResponseBody
    public int getCartCount(HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        return cart.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
