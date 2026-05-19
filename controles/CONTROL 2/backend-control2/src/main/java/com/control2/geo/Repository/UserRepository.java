package com.control2.geo.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.control2.geo.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);

}
