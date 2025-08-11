package com.example.bookstore.controller.admin;

import com.example.bookstore.entity.Promotion;
import com.example.bookstore.service.PromotionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public String adminPromotions(Model model) {
        model.addAttribute("promotions", promotionService.findAll());
        model.addAttribute("promotion", new Promotion());
        return "admin/promotion/promotion-mana";
    }

    @PostMapping("/add")
    public String addPromotion(@ModelAttribute Promotion promotion) {
        promotionService.save(promotion);
        return "redirect:/admin/promotions";
    }

    @GetMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Integer id) {
        promotionService.delete(id);
        return "redirect:/admin/promotions";
    }
}
