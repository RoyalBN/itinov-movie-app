package com.itinov.movie_api.controller;

import com.itinov.movie_api.dto.FilmDTO;
import com.itinov.movie_api.model.FilmSortBy;
import com.itinov.movie_api.service.UserFilmService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/users")
public class UserFilmController {

    private final UserFilmService userFilmService;

    public UserFilmController(UserFilmService userFilmService) {
        this.userFilmService = userFilmService;
    }

    @PostMapping("/{userId}/favorites/{filmId}")
    public ResponseEntity<Void> addFilmToFavorite(@PathVariable Long userId, @PathVariable Long filmId) {
        try {
            userFilmService.addFilmToFavorite(userId, filmId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}/favorites/{filmId}")
    public ResponseEntity<Void> removeFilmFromFavorite(
            @PathVariable @Positive(message = "ID must be positive") Long userId,
            @PathVariable @Positive(message = "ID must be positive") Long filmId) {
        try {
            userFilmService.removeFilmFromFavorite(userId, filmId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}/favorites")
    public ResponseEntity<List<FilmDTO>> getFavoriteFilms(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "RELEASE_DATE") FilmSortBy sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        List<FilmDTO> result = userFilmService.getFavoriteFilmsSorted(userId, sortBy, direction);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{userId}/films/{filmId}/watched")
    public ResponseEntity<Void> toggleFilmAsWatched(
            @PathVariable @Positive(message = "ID must be positive") Long userId,
            @PathVariable @Positive(message = "ID must be positive") Long filmId
    ) {
        userFilmService.toggleFilmAsWatched(userId, filmId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/films/watched")
    public ResponseEntity<List<FilmDTO>> getWatchedFilms(
            @PathVariable @Positive(message = "ID must be positive") Long userId
    ) {
        List<FilmDTO> result = userFilmService.getWatchedFilms(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/films/unwatched")
    public ResponseEntity<List<FilmDTO>> getUnwatchedFilms(
            @PathVariable @Positive(message = "ID must be positive") Long userId
    ) {
        List<FilmDTO> result = userFilmService.getUnwatchedFilms(userId);
        return ResponseEntity.ok(result);
    }

}