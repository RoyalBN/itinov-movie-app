package com.itinov.movie_api.controller;

import com.itinov.movie_api.service.UserFilmService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
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
}