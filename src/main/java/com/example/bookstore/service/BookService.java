package com.example.bookstore.service;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository repo;

    public List<BookDTO> findAll() {
        return repo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BookDTO save(BookDTO bookDTO) {
        Book book = convertToEntity(bookDTO);
        Book savedBook = repo.save(book);
        return convertToDTO(savedBook);
    }

    public void deleteById(Integer id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Sách với ID " + id + " không tồn tại");
        }
        repo.deleteById(id);
    }

    public BookDTO findById(Integer id) {
        Book book = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sách với ID " + id + " không tồn tại"));
        return convertToDTO(book);
    }

    private BookDTO convertToDTO(Book book) {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setBookId(book.getBookId());
        bookDTO.setTitle(book.getTitle());
        bookDTO.setDescription(book.getDescription());
        bookDTO.setPrice(book.getPrice());
        bookDTO.setStock(book.getStock());
        bookDTO.setCategory(book.getCategory());
        bookDTO.setImageUrl(book.getImageUrl());
        return bookDTO;
    }

    private Book convertToEntity(BookDTO bookDTO) {
        Book book = new Book();
        book.setBookId(bookDTO.getBookId());
        book.setTitle(bookDTO.getTitle());
        book.setDescription(bookDTO.getDescription());
        book.setPrice(bookDTO.getPrice());
        book.setStock(bookDTO.getStock());
        book.setCategory(bookDTO.getCategory());
        book.setImageUrl(bookDTO.getImageUrl());
        return book;
    }
}