package com.example.bookstore.controller.customer;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    @Autowired
    private BookService bookService;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Model model) {

        List<BookDTO> allBooks = bookService.findAll();
        List<BookDTO> results = allBooks;

        // Tìm kiếm theo từ khóa
        if (q != null && !q.trim().isEmpty()) {
            String keyword = q.toLowerCase().trim();
            results = results.stream()
                    .filter(book -> book.getTitle().toLowerCase().contains(keyword) ||
                            book.getDescription().toLowerCase().contains(keyword) ||
                            book.getCategory().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        // Lọc theo danh mục
        if (category != null && !category.trim().isEmpty()) {
            results = results.stream()
                    .filter(book -> book.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        // Lọc theo khoảng giá
        if (minPrice != null) {
            results = results.stream()
                    .filter(book -> book.getPrice() >= minPrice)
                    .collect(Collectors.toList());
        }

        if (maxPrice != null) {
            results = results.stream()
                    .filter(book -> book.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
        }

        // Lấy danh sách categories để hiển thị filter
        List<String> categories = allBooks.stream()
                .map(BookDTO::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("books", results);
        model.addAttribute("categories", categories);
        model.addAttribute("searchQuery", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("resultCount", results.size());

        return "customer/search/search-results";
    }

    @GetMapping("/category")
    public String browseByCategory(@RequestParam String name, Model model) {
        return search(null, name, null, null, model);
    }
}
