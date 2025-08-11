package com.example.bookstore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bookstore.entity.Promotion;
import com.example.bookstore.repository.PromotionRepository;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository repo;

    public List<Promotion> findAll() {
        return repo.findAll();
    }

    public List<Promotion> findActivePromotions() {
        return repo.findByActiveTrue();
    }

    public void save(Promotion promotion) {
        repo.save(promotion);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}
