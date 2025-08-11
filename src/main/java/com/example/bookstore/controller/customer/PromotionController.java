package com.example.bookstore.controller.customer;

import com.example.bookstore.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/promotions")
    public String promotions(Model model) {
        model.addAttribute("promotions", promotionService.findActivePromotions());
        return "customer/promotion/promotions";
    }
}
