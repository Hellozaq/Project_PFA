package com.example.springnodebackend.repository;

import com.example.springnodebackend.model.Cart;
import com.example.springnodebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByUser(User user);
    Optional<Cart> findByUserId(String userId);
}