package com.example.bookstore.controller.admin;

import com.example.bookstore.entity.Category;
import com.example.bookstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public String showCategoryList(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("categories", categoryService.getAllRootCategories());
        return "admin/category/category";
    }

    @PostMapping("/add")
    public String addCategory(@ModelAttribute Category category) {
        categoryService.save(category);
        return "redirect:/category/list";
    }

    @GetMapping("/delete")
    public String deleteCategory(@RequestParam("id") Integer id) {
        categoryService.delete(id);
        return "redirect:/category/list";
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Integer id, Model model) {
        model.addAttribute("category", categoryService.getById(id));
        model.addAttribute("categories", categoryService.getAllRootCategories());
        return "admin/category/category"; // dùng lại 1 file
    }
}
