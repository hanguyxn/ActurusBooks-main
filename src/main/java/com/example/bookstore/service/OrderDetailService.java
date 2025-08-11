package com.example.bookstore.service;

import com.example.bookstore.entity.OrderDetail;
import com.example.bookstore.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public List<OrderDetail> getAll() {
        return orderDetailRepository.findAll();
    }

    public OrderDetail save(OrderDetail orderDetail) {
        return orderDetailRepository.save(orderDetail);
    }

    public void deleteById(Integer id) {
        orderDetailRepository.deleteById(id);
    }

    public List<OrderDetail> getByOrderId(Integer orderId) {
        return orderDetailRepository.findByOrderOrderId(orderId);
    }

    public List<OrderDetail> getByBookId(Integer bookId) {
        return orderDetailRepository.findByBookId(bookId);
    }

    public void deleteByOrderId(Integer orderId) {
        orderDetailRepository.deleteByOrderOrderId(orderId);
    }
}
