package com.itinov.movie_api.controller;

import com.itinov.movie_api.service.UserFilmService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Positive;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}