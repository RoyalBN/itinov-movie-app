package com.itinov.movie_api.service;

import com.itinov.movie_api.dto.FilmDTO;
import com.itinov.movie_api.mapper.UserFilmMapper;
import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.FilmSortBy;
import com.itinov.movie_api.model.User;
import com.itinov.movie_api.model.UserFilmRelation;
import com.itinov.movie_api.repository.FilmRepository;
import com.itinov.movie_api.repository.UserFilmRelationRepository;
import com.itinov.movie_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserFilmService {

    private UserRepository userRepository;
    private FilmRepository filmRepository;
    private UserFilmRelationRepository relationRepository;
    private final UserFilmMapper mapper;

    public UserFilmService(UserRepository userRepository,
                           FilmRepository filmRepository,
                           UserFilmRelationRepository relationRepository,
                           UserFilmMapper mapper
    ) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.relationRepository = relationRepository;
        this.mapper = mapper;
    }

    public void toggleFavoriteStatus(Long userId, Long filmId) {
        validateIds(userId, filmId);

        User user = getUserOrThrow(userId);
        Film film = getFilmOrThrow(filmId);

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

    private static void validateIds(Long userId, Long filmId) {
        if (userId == null || filmId == null) {
            throw new IllegalArgumentException("User ID and film ID must not be null");
        }
    }

    private Film getFilmOrThrow(Long filmId) {
        return filmRepository.findById(filmId).orElseThrow(() -> new EntityNotFoundException("Film not found with id: " + filmId));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    public void removeFilmFromFavorite(Long userId, Long filmId) {
        validateIds(userId, filmId);

        User user = getUserOrThrow(userId);
        Film film = getFilmOrThrow(filmId);

        UserFilmRelation relation = relationRepository.findByUserAndFilm(user, film)
                .orElseThrow(() -> new EntityNotFoundException("Relation not found for user ID: " + userId + " and film ID: " + filmId));

        relation.setFavorite(false);
        relationRepository.save(relation);
    }

    public List<FilmDTO> getFavoriteFilmsSorted(Long userId, FilmSortBy sortBy, Sort.Direction direction) {
        User user = getUserOrThrow(userId);

        List<UserFilmRelation> relations = switch (sortBy) {
            case RATING -> direction.isAscending()
                    ? relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_RatingAsc(user)
                    : relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_RatingDesc(user);
            default -> direction.isAscending()
                    ? relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateAsc(user)
                    : relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateDesc(user);
        };

        return relations.stream()
                .map(mapper::mapToFilmDTO)
                .collect(Collectors.toList());
    }

    private UserFilmRelation createNewRelation(User user, Film film) {
        return UserFilmRelation.builder()
                .user(user)
                .film(film)
                .hasBeenWatched(false)
                .build();
    }

    public void toggleFilmAsWatched(Long userId, Long filmId) {
        User user = getUserOrThrow(userId);
        Film film = getFilmOrThrow(filmId);

        UserFilmRelation relation = relationRepository.findByUserAndFilm(user, film)
                .orElseGet(() -> createNewRelation(user, film));
                //.orElseThrow(() -> new EntityNotFoundException("Relation not found for user ID: " + userId + " and film ID: " + filmId));

        boolean newWatchStatus = relation.isHasBeenWatched();
        relation.setHasBeenWatched(!newWatchStatus);
        relation.setWatchedAt(newWatchStatus ? null : LocalDateTime.now());
        relationRepository.save(relation);
    }

    public List<FilmDTO> getWatchedFilms(Long userId) {
        User user = getUserOrThrow(userId);
        List<UserFilmRelation> relations = relationRepository.findByUserAndHasBeenWatchedTrue(user);

        return relations.stream()
                .map(mapper::mapToFilmDTO)
                .collect(Collectors.toList());
    }

    public List<FilmDTO> getUnwatchedFilms(Long userId) {
        User user = getUserOrThrow(userId);
        List<UserFilmRelation> relations = relationRepository.findByUserAndHasBeenWatchedFalse(user);

        return relations.stream()
                .map(mapper::mapToFilmDTO)
                .collect(Collectors.toList());
    }
}
