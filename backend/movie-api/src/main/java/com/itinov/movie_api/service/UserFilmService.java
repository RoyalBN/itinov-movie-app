package com.itinov.movie_api.service;

import com.itinov.movie_api.dto.FilmDTO;
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

    public void removeFilmFromFavorite(Long userId, Long filmId) {
        if (userId == null || filmId == null) {
            throw new IllegalArgumentException("User ID and film ID must not be null");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Film film = filmRepository.findById(filmId).orElseThrow(() -> new EntityNotFoundException("Film not found with id: " + filmId));

        UserFilmRelation relation = relationRepository.findByUserAndFilm(user, film)
                .orElseThrow(() -> new EntityNotFoundException("Relation not found for user ID: " + userId + " and film ID: " + filmId));

        relation.setFavorite(false);
        relationRepository.save(relation);
    }


    public List<FilmDTO> getFavoriteFilmsSorted(Long userId, FilmSortBy sortBy, Sort.Direction direction) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<UserFilmRelation> relations;

        if (sortBy == FilmSortBy.RATING && direction == Sort.Direction.DESC) {
            relations = relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_RatingDesc(user);
        } else if (sortBy == FilmSortBy.RATING && direction == Sort.Direction.ASC) {
            relations = relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_RatingAsc(user);
        } else if (sortBy == FilmSortBy.RELEASE_DATE && direction == Sort.Direction.DESC) {
            relations = relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateDesc(user);
        } else {
            relations = relationRepository.findByUserAndIsFavoriteTrueOrderByFilm_ReleaseDateAsc(user);
        }

        return relations.stream()
                .map(this::mapToFilmDTO)
                .collect(Collectors.toList());
    }

    private FilmDTO mapToFilmDTO(UserFilmRelation relation) {
        Film film = relation.getFilm();

        return FilmDTO.builder()
                .id(film.getId())
                .title(film.getTitle())
                .rating(film.getRating())
                .releaseDate(film.getReleaseDate())
                .posterUrl(film.getPosterUrl())
                .isFavorite(relation.isFavorite())
                .hasBeenWatched(relation.isHasBeenWatched())
                .build();
    }


    public void toggleFilmAsWatched(Long userId, Long filmId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Film film = filmRepository.findById(filmId).orElseThrow(() -> new EntityNotFoundException("Film not found with id: " + filmId));

        UserFilmRelation relation = relationRepository.findByUserAndFilm(user, film)
                .orElseThrow(() -> new EntityNotFoundException("Relation not found for user ID: " + userId + " and film ID: " + filmId));

        boolean newWatchStatus = relation.isHasBeenWatched();
        relation.setHasBeenWatched(!newWatchStatus);
        relation.setWatchedAt(newWatchStatus ? null : LocalDateTime.now());
        relationRepository.save(relation);
    }

    public List<FilmDTO> getWatchedFilms(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        List<UserFilmRelation> relations = relationRepository.findByUserAndHasBeenWatchedTrue(user);

        return relations.stream()
                .map(this::mapToFilmDTO)
                .collect(Collectors.toList());
    }

    public List<FilmDTO> getUnwatchedFilms(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        List<UserFilmRelation> relations = relationRepository.findByUserAndHasBeenWatchedFalse(user);

        return relations.stream()
                .map(this::mapToFilmDTO)
                .collect(Collectors.toList());
    }
}
