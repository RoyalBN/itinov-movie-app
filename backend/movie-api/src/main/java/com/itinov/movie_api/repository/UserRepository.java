package com.itinov.movie_api.repository;

import com.itinov.movie_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
