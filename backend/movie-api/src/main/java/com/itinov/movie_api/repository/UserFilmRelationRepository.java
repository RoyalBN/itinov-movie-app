package com.itinov.movie_api.repository;

import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.User;
import com.itinov.movie_api.model.UserFilmRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFilmRelationRepository extends JpaRepository<UserFilmRelation, Long> {
    Optional<UserFilmRelation> findByUserAndFilm(User user, Film film);
}
