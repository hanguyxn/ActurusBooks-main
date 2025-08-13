package com.example.bookstore.service;

import com.example.bookstore.entity.UserAddress;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserAddressService {

    @Autowired
    private UserAddressRepository userAddressRepository;

    public List<UserAddress> getAddressesByUserId(Integer userId) {
        return userAddressRepository.findByUserUserId(userId);
    }

    public Optional<UserAddress> getDefaultAddress(Integer userId) {
        return userAddressRepository.findDefaultByUserId(userId);
    }

    public Optional<UserAddress> getById(Integer id) {
        return userAddressRepository.findById(id);
    }

    @Transactional
    public UserAddress save(UserAddress address) {
        // Nếu đây là địa chỉ mặc định, clear các địa chỉ mặc định khác
        if (address.getIsDefault()) {
            userAddressRepository.clearDefaultByUserId(address.getUser().getUser_id());
        }
        return userAddressRepository.save(address);
    }

    @Transactional
    public void setAsDefault(Integer addressId, Integer userId) {
        // Clear tất cả địa chỉ mặc định hiện tại
        userAddressRepository.clearDefaultByUserId(userId);

        // Set địa chỉ mới làm mặc định
        Optional<UserAddress> addressOpt = userAddressRepository.findById(addressId);
        if (addressOpt.isPresent()) {
            UserAddress address = addressOpt.get();
            address.setIsDefault(true);
            userAddressRepository.save(address);
        }
    }

    public void delete(Integer id) {
        userAddressRepository.deleteById(id);
    }
}
