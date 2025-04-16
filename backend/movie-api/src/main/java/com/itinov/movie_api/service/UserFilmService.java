package com.itinov.movie_api.service;

import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.User;
import com.itinov.movie_api.model.UserFilmRelation;
import com.itinov.movie_api.repository.FilmRepository;
import com.itinov.movie_api.repository.UserFilmRelationRepository;
import com.itinov.movie_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

public class UserFilmService {

    private UserRepository userRepository;
    private FilmRepository filmRepository;
    private UserFilmRelationRepository relationRepository;

    public UserFilmService(UserRepository userRepository,
                           FilmRepository filmRepository,
                           UserFilmRelationRepository relationRepository
    ) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.relationRepository = relationRepository;
    }

    public void addFilmToFavorite(Long userId, Long filmId) {
        if (userId == null || filmId == null) {
            throw new IllegalArgumentException("User ID and film ID must not be null");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Film film = filmRepository.findById(filmId).orElseThrow(() -> new EntityNotFoundException("Film not found with id: " + filmId));

        UserFilmRelation relation = relationRepository.findByUserAndFilm(user, film)
                .map(existing -> {
                    existing.setFavorite(!existing.isFavorite()); // Toggle
                    return existing;
                })
                .orElseGet(() -> UserFilmRelation.builder()
                        .user(user)
                        .film(film)
                        .isFavorite(true)
                        .hasBeenWatched(false)
                        .build()
                );

        relationRepository.save(relation);
    }
}
