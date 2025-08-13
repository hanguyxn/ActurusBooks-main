package com.example.bookstore.service;

import com.example.bookstore.dto.ProvinceDto;
import com.example.bookstore.dto.WardDto;
import com.example.bookstore.dto.SearchResultDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AddressApiService {

    private static final String BASE_URL = "https://34tinhthanh.com";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Lấy danh sách tất cả tỉnh/thành phố
     */
    public List<ProvinceDto> getAllProvinces() {
        try {
            String response = restTemplate.getForObject(BASE_URL + "/api/provinces", String.class);
            return objectMapper.readValue(response, new TypeReference<List<ProvinceDto>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách tỉnh/thành phố: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách phường/xã theo mã tỉnh/thành phố
     */
    public List<WardDto> getWardsByProvinceCode(String provinceCode) {
        try {
            String url = BASE_URL + "/api/wards?province_code=" + provinceCode;
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readValue(response, new TypeReference<List<WardDto>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách phường/xã: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm kiếm tỉnh/thành hoặc phường/xã theo từ khóa
     */
    public List<SearchResultDto> searchAddress(String keyword) {
        try {
            String url = BASE_URL + "/api/search?q=" + keyword;
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readValue(response, new TypeReference<List<SearchResultDto>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm kiếm địa chỉ: " + e.getMessage(), e);
        }
    }
}
