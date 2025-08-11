package com.example.bookstore.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer order_id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime order_date;

    private BigDecimal total_amount;

    private String phone;
    private String address;

    private String payment_method;

    private String status;

    private String proof_image;

    private String discount_code;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderDetail> orderItems;

    // Helper camelCase getters for templates expecting order.orderDate /
    // totalAmount
    @Transient
    public LocalDateTime getOrderDate() {
        return order_date;
    }

    @Transient
    public BigDecimal getTotalAmount() {
        return total_amount;
    }
}
