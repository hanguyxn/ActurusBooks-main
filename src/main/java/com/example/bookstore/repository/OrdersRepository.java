package com.example.bookstore.repository;

import com.example.bookstore.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {

        // Lấy tất cả đơn hàng sắp xếp theo ngày đặt hàng giảm dần (mới nhất trước)
        @Query("SELECT o FROM Orders o ORDER BY o.order_date DESC")
        List<Orders> findAllByOrderByOrderDateDesc();

        // Đếm số đơn hàng theo user ID
        @Query("SELECT COUNT(o) FROM Orders o WHERE o.user.user_id = :userId")
        long countByUserId(@Param("userId") Integer userId);

        // Tìm đơn hàng theo user ID
        @Query("SELECT o FROM Orders o WHERE o.user.user_id = :userId ORDER BY o.order_date DESC")
        List<Orders> findByUserId(@Param("userId") Integer userId);

        // Tìm đơn hàng theo user ID, sắp xếp theo ngày đặt hàng giảm dần
        @Query("SELECT o FROM Orders o WHERE o.user.user_id = :userId ORDER BY o.order_date DESC")
        List<Orders> findByUser_UserIdOrderByOrder_dateDesc(@Param("userId") Integer userId);

        // Tìm đơn hàng theo trạng thái, sắp xếp theo ngày đặt hàng giảm dần
        @Query("SELECT o FROM Orders o WHERE o.status = :status ORDER BY o.order_date DESC")
        List<Orders> findByStatusOrderByOrder_dateDesc(@Param("status") String status);

        // Đếm số đơn hàng theo trạng thái
        long countByStatus(String status);

        // Lấy 10 đơn hàng gần đây nhất
        @Query("SELECT o FROM Orders o ORDER BY o.order_date DESC")
        List<Orders> findTop10ByOrderByOrder_dateDesc();

        // Thống kê doanh thu theo tháng
        @Query("SELECT FUNCTION('MONTH', o.order_date) AS month, " +
                        "FUNCTION('YEAR', o.order_date) AS year, " +
                        "SUM(o.total_amount) AS revenue " +
                        "FROM Orders o " +
                        "WHERE o.status != 'Đã hủy' " +
                        "GROUP BY FUNCTION('YEAR', o.order_date), FUNCTION('MONTH', o.order_date) " +
                        "ORDER BY year DESC, month DESC")
        List<Object[]> findMonthlyRevenue();

        // Tìm đơn hàng theo khoảng thời gian
        @Query("SELECT o FROM Orders o WHERE o.order_date BETWEEN :startDate AND :endDate ORDER BY o.order_date DESC")
        List<Orders> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Tổng doanh thu
        @Query("SELECT SUM(o.total_amount) FROM Orders o WHERE o.status != 'Đã hủy'")
        Double getTotalRevenue();
}
