package com.example.bookstore.service;

import com.example.bookstore.entity.Wishlist;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.WishlistRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    public List<Wishlist> getByUserId(Integer userId) {
        return wishlistRepository.findByUser_UserIdOrderByAddedDateDesc(userId);
    }

    public boolean existsByUserIdAndBookId(Integer userId, Integer bookId) {
        return wishlistRepository.existsByUser_UserIdAndBook_BookId(userId, bookId);
    }

    public void addToWishlist(Integer userId, Integer bookId) {
        // Kiểm tra xem đã tồn tại chưa
        if (existsByUserIdAndBookId(userId, bookId)) {
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        Book book = bookRepository.findById(bookId).orElse(null);

        if (user != null && book != null) {
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .book(book)
                    .addedDate(LocalDateTime.now())
                    .build();
            wishlistRepository.save(wishlist);
        }
    }

    public void removeFromWishlist(Integer userId, Integer bookId) {
        wishlistRepository.deleteByUser_UserIdAndBook_BookId(userId, bookId);
    }

    public void clearWishlist(Integer userId) {
        wishlistRepository.deleteByUser_UserId(userId);
    }

    public long countByUserId(Integer userId) {
        return wishlistRepository.countByUser_UserId(userId);
    }
}
