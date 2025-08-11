package com.example.bookstore.service;

import com.example.bookstore.entity.Category;
import com.example.bookstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository danhMucRepository;

    public List<Category> getAllRootCategories() {
        return danhMucRepository.findByParentIsNull();
    }

    public List<Category> getChildren(Integer parentId) {
        return danhMucRepository.findByParentId(parentId);
    }

    public Category save(Category danhMuc) {
        return danhMucRepository.save(danhMuc);
    }

    public void delete(Integer id) {
        danhMucRepository.deleteById(id);
    }

    public Category getById(Integer id) {
        return danhMucRepository.findById(id).orElse(null);
    }
}