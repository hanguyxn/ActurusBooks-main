package com.example.bookstore.controller.api;

import com.example.bookstore.dto.ProvinceDto;
import com.example.bookstore.dto.WardDto;
import com.example.bookstore.dto.SearchResultDto;
import com.example.bookstore.entity.UserAddress;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.AddressApiService;
import com.example.bookstore.service.UserAddressService;
import com.example.bookstore.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/address")
public class AddressApiController {

    @Autowired
    private AddressApiService addressApiService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy danh sách tất cả tỉnh/thành phố
     */
    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceDto>> getAllProvinces() {
        try {
            List<ProvinceDto> provinces = addressApiService.getAllProvinces();
            return ResponseEntity.ok(provinces);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy danh sách phường/xã theo mã tỉnh
     */
    @GetMapping("/wards")
    public ResponseEntity<List<WardDto>> getWardsByProvince(@RequestParam String provinceCode) {
        try {
            List<WardDto> wards = addressApiService.getWardsByProvinceCode(provinceCode);
            return ResponseEntity.ok(wards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Tìm kiếm địa chỉ
     */
    @GetMapping("/search")
    public ResponseEntity<List<SearchResultDto>> searchAddress(@RequestParam String q) {
        try {
            List<SearchResultDto> results = addressApiService.searchAddress(q);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy danh sách địa chỉ của user hiện tại
     */
    @GetMapping("/user-addresses")
    public ResponseEntity<List<UserAddress>> getUserAddresses(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<UserAddress> addresses = userAddressService.getAddressesByUserId(currentUser.getUser_id());
        return ResponseEntity.ok(addresses);
    }

    /**
     * Thêm địa chỉ mới cho user
     */
    @PostMapping("/user-addresses")
    public ResponseEntity<Map<String, Object>> addUserAddress(
            @RequestBody UserAddress address,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }

            // Set user cho address
            address.setUser(currentUser);

            UserAddress savedAddress = userAddressService.save(address);

            response.put("success", true);
            response.put("message", "Thêm địa chỉ thành công");
            response.put("address", savedAddress);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi thêm địa chỉ: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật địa chỉ
     */
    @PutMapping("/user-addresses/{id}")
    public ResponseEntity<Map<String, Object>> updateUserAddress(
            @PathVariable Integer id,
            @RequestBody UserAddress address,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }

            Optional<UserAddress> existingAddress = userAddressService.getById(id);
            if (existingAddress.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy địa chỉ");
                return ResponseEntity.notFound().build();
            }

            address.setAddress_id(id);
            address.setUser(currentUser);

            UserAddress savedAddress = userAddressService.save(address);

            response.put("success", true);
            response.put("message", "Cập nhật địa chỉ thành công");
            response.put("address", savedAddress);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật địa chỉ: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa địa chỉ
     */
    @DeleteMapping("/user-addresses/{id}")
    public ResponseEntity<Map<String, Object>> deleteUserAddress(
            @PathVariable Integer id,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }

            userAddressService.delete(id);

            response.put("success", true);
            response.put("message", "Xóa địa chỉ thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa địa chỉ: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Set địa chỉ mặc định
     */
    @PostMapping("/user-addresses/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(
            @PathVariable Integer id,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.status(401).body(response);
            }

            userAddressService.setAsDefault(id, currentUser.getUser_id());

            response.put("success", true);
            response.put("message", "Đặt làm địa chỉ mặc định thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi đặt địa chỉ mặc định: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
