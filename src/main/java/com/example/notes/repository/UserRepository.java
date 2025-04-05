package com.example.notes.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.notes.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
}
