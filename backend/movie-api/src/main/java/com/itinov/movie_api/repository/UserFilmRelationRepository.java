package com.itinov.movie_api.repository;

import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.User;
import com.itinov.movie_api.model.UserFilmRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFilmRelationRepository extends JpaRepository<UserFilmRelation, Long> {
    Optional<UserFilmRelation> findByUserAndFilm(User user, Film film);

    List<UserFilmRelation> findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateAsc(User user);
    List<UserFilmRelation> findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateDesc(User user);

    List<UserFilmRelation> findByUserAndIsFavoriteTrueOrderByFilm_RatingAsc(User user);
    List<UserFilmRelation> findByUserAndIsFavoriteTrueOrderByFilm_RatingDesc(User user);

    List<UserFilmRelation> findByUserAndHasBeenWatchedTrue(User user);
    List<UserFilmRelation> findByUserAndHasBeenWatchedFalse(User user);
}
