package com.example.bookstore.repository;

import com.example.bookstore.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {

    @Query("SELECT ua FROM UserAddress ua WHERE ua.user.user_id = :userId")
    List<UserAddress> findByUserUserId(@Param("userId") Integer userId);

    @Query("SELECT ua FROM UserAddress ua WHERE ua.user.user_id = :userId AND ua.isDefault = true")
    Optional<UserAddress> findDefaultByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = false WHERE ua.user.user_id = :userId")
    void clearDefaultByUserId(@Param("userId") Integer userId);
}