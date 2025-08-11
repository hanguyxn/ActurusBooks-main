package com.example.bookstore.repository;

import com.example.bookstore.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    @Query("SELECT w FROM Wishlist w WHERE w.user.user_id = :userId ORDER BY w.addedDate DESC")
    List<Wishlist> findByUser_UserIdOrderByAddedDateDesc(@Param("userId") Integer userId);

    @Query("SELECT w FROM Wishlist w WHERE w.user.user_id = :userId AND w.book.id = :bookId")
    Optional<Wishlist> findByUser_UserIdAndBook_BookId(@Param("userId") Integer userId,
            @Param("bookId") Integer bookId);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Wishlist w WHERE w.user.user_id = :userId AND w.book.bookId = :bookId")
    boolean existsByUser_UserIdAndBook_BookId(@Param("userId") Integer userId, @Param("bookId") Integer bookId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.user_id = :userId AND w.book.bookId = :bookId")
    void deleteByUser_UserIdAndBook_BookId(@Param("userId") Integer userId, @Param("bookId") Integer bookId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.user_id = :userId")
    void deleteByUser_UserId(@Param("userId") Integer userId);

    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.user_id = :userId")
    long countByUser_UserId(@Param("userId") Integer userId);
}
