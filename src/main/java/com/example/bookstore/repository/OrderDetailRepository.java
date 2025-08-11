package com.example.bookstore.repository;

import com.example.bookstore.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("SELECT od FROM OrderDetail od WHERE od.order.order_id = :orderId")
    List<OrderDetail> findByOrderOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT od FROM OrderDetail od WHERE od.bookId = :bookId")
    List<OrderDetail> findByBookId(@Param("bookId") Integer bookId);

    @Modifying
    @Query("DELETE FROM OrderDetail od WHERE od.order.order_id = :orderId")
    void deleteByOrderOrderId(@Param("orderId") Integer orderId);
}
