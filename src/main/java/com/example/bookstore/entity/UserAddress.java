package com.example.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "User_Addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer address_id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "province_code", nullable = false)
    private String provinceCode;

    @Column(name = "province_name", nullable = false)
    private String provinceName;

    @Column(name = "ward_code", nullable = false)
    private String wardCode;

    @Column(name = "ward_name", nullable = false)
    private String wardName;

    @Column(name = "street_address", nullable = false)
    private String streetAddress; // Số nhà, tên đường

    @Column(name = "is_default")
    private Boolean isDefault = false;

    // Phương thức helper để lấy địa chỉ đầy đủ
    @Transient
    public String getFullAddress() {
        return streetAddress + ", " + wardName + ", " + provinceName;
    }
}
