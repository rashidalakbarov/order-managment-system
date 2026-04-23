package com.jm.order_managment_system.repository;

import com.jm.order_managment_system.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findFirstByOrderByIdAsc();
}
