package com.example.bookstore.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WardDto {
    private String ward_code;
    private String ward_name;
    private String province_code;
    private String province_name;
    private String province_short_name;
    private String province_code_short;
    private String place_type;
    private Boolean has_merger;
    private String[] old_units;
    private Integer old_units_count;
    private String merger_details;
    private Boolean province_is_merged;
    private String[] province_merged_with;
    private String administrative_center;
}
