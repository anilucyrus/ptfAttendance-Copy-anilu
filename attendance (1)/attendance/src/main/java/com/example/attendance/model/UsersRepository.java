package com.example.attendance.model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersModel,Integer> {
    Optional<UsersModel> findByEmail(String email);
    Optional<UsersModel> findById(Long id);
    Optional<UsersModel> findByEmailAndPassword(String email, String password);
    Optional<UsersModel> findByToken(String token);

}
