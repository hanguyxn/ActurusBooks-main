package com.example.bookstore.dto;

import lombok.Data;

@Data
public class BookDTO {
    private Integer bookId;
    private String title;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
    private String imageUrl;
}