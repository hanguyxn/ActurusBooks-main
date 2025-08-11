package com.example.bookstore.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

// Added relation to Book so templates can access detail.bookTitle and other book info
// without changing existing database structure. We keep primitive bookId for easier inserts.

@Entity
@Table(name = "Order_Details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Orders order;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    // Bi-directional association (read-only) to get book title in views
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Book book;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    // Tính tổng tiền cho từng chi tiết
    public BigDecimal getSubTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // Helper for Thymeleaf: detail.bookTitle
    @Transient
    public String getBookTitle() {
        if (book != null && book.getTitle() != null) {
            return book.getTitle();
        }
        return "Book #" + (bookId != null ? bookId : "?");
    }
}
