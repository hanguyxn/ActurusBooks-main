package com.example.bookstore.dto;

import lombok.Data;

@Data
public class PromotionDTO {
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private Boolean isActive;
}
