package com.example.bookstore.dto;

import lombok.Data;

@Data
public class DiscountDTO {
    private String code;
    private Double value;
    private String expiryDate; // định dạng ISO-8601 (yyyy-MM-dd'T'HH:mm)
}
