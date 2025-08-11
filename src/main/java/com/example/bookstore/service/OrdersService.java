package com.example.bookstore.service;

import com.example.bookstore.entity.Orders;
import com.example.bookstore.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrdersService {
    @Autowired
    private OrdersRepository ordersRepository;

    public List<Orders> getAll() {
        return ordersRepository.findAllByOrderByOrderDateDesc();
    }

    public Orders save(Orders order) {
        return ordersRepository.save(order);
    }

    public Optional<Orders> getById(Integer id) {
        return ordersRepository.findById(id);
    }

    public void delete(Integer id) {
        ordersRepository.deleteById(id);
    }

    public List<Orders> getByUserId(Integer userId) {
        return ordersRepository.findByUser_UserIdOrderByOrder_dateDesc(userId);
    }

    public List<Orders> getByStatus(String status) {
        return ordersRepository.findByStatusOrderByOrder_dateDesc(status);
    }

    // Thống kê doanh thu theo tháng
    public List<Object[]> getMonthlyRevenue() {
        return ordersRepository.findMonthlyRevenue();
    }

    // Đếm số đơn hàng theo trạng thái
    public long countByStatus(String status) {
        return ordersRepository.countByStatus(status);
    }

    // Lấy đơn hàng gần đây
    public List<Orders> getRecentOrders(int limit) {
        return ordersRepository.findTop10ByOrderByOrder_dateDesc();
    }
}
