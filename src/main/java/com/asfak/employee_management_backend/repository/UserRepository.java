package com.asfak.employee_management_backend.repository;

import com.asfak.employee_management_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmployeeId(Long employeeId);

    Optional<User> findByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);

    List<User> findByRoleIgnoreCase(String role);



}