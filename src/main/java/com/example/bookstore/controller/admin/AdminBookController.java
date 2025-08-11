package com.example.bookstore.controller.admin;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {
    @Autowired
    private BookService bookService;

    @Autowired
    private FileStorageService fileStorageService;

    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(".*\\.(jpg|jpeg|png|gif)(\\?.*)?$",
            Pattern.CASE_INSENSITIVE);

    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("book", new BookDTO());
        model.addAttribute("editMode", false);
        return "admin/books/books-mana";
    }

    @PostMapping("/add")
    public String addBook(@ModelAttribute("book") BookDTO bookDTO,
            @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        try {
            // Xử lý upload ảnh
            if (!imageFile.isEmpty()) {
                String fileName = fileStorageService.storeFile(imageFile);
                bookDTO.setImageUrl("/uploads/" + fileName);
            } else if (bookDTO.getImageUrl() != null && !bookDTO.getImageUrl().isEmpty()) {
                // Kiểm tra URL ảnh nếu không upload file
                if (!IMAGE_URL_PATTERN.matcher(bookDTO.getImageUrl()).matches()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "URL ảnh không hợp lệ. Vui lòng upload file hoặc dùng link trực tiếp (jpg, png, gif).");
                    return "redirect:/admin/books";
                }
            }

            bookService.save(bookDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm sách: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/edit")
    public String editBook(@ModelAttribute("book") BookDTO bookDTO,
            @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        try {
            // Xử lý upload ảnh cho edit
            if (!imageFile.isEmpty()) {
                // Xóa ảnh cũ nếu có
                BookDTO existingBook = bookService.findById(bookDTO.getBookId());
                if (existingBook != null && existingBook.getImageUrl() != null &&
                        existingBook.getImageUrl().startsWith("/uploads/")) {
                    String oldFileName = existingBook.getImageUrl().replace("/uploads/", "");
                    fileStorageService.deleteFile(oldFileName);
                }

                // Upload ảnh mới
                String fileName = fileStorageService.storeFile(imageFile);
                bookDTO.setImageUrl("/uploads/" + fileName);
            } else if (bookDTO.getImageUrl() != null && !bookDTO.getImageUrl().isEmpty()) {
                // Kiểm tra URL ảnh nếu không upload file
                if (!IMAGE_URL_PATTERN.matcher(bookDTO.getImageUrl()).matches()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "URL ảnh không hợp lệ. Vui lòng upload file hoặc dùng link trực tiếp (jpg, png, gif).");
                    return "redirect:/admin/books";
                }
            }

            bookService.save(bookDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật sách: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            // Xóa ảnh trước khi xóa sách
            BookDTO book = bookService.findById(id);
            if (book != null && book.getImageUrl() != null && book.getImageUrl().startsWith("/uploads/")) {
                String fileName = book.getImageUrl().replace("/uploads/", "");
                fileStorageService.deleteFile(fileName);
            }

            bookService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sách: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("book", bookService.findById(id));
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("editMode", true);
        return "admin/books/books-mana";
    }
}