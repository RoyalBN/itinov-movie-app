package com.itinov.movie_api.controller;

import com.itinov.movie_api.service.UserFilmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserFilmController.class)
class UserFilmControllerTest {

    private static final Long EXISTING_USER_ID = 42L;
    private static final Long EXISTING_FILM_ID = 123L;
    private static final Long NON_EXISTING_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private UserFilmController userFilmController;

    @MockitoBean
    private UserFilmService userFilmService;

    @Test
    @DisplayName("[N] Add a film to favorite")
    void should_add_film_to_favorite() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/{userId}/favorites/{filmId}", EXISTING_USER_ID, EXISTING_FILM_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userFilmService, times(1)).addFilmToFavorite(EXISTING_USER_ID, EXISTING_FILM_ID);
    }

    @Test
    @DisplayName("[E] Return 404 when user not found")
    void should_return_404_when_user_not_found() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("User not found with id: " + NON_EXISTING_ID))
                .when(userFilmService).addFilmToFavorite(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/users/{userId}/favorites/{filmId}", NON_EXISTING_ID, EXISTING_FILM_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userFilmService, times(1)).addFilmToFavorite(anyLong(), anyLong());
    }

    @Test
    @DisplayName("[E] Return 404 when film not found")
    void should_return_404_when_film_not_found() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Film not found with id: " + NON_EXISTING_ID))
                .when(userFilmService)
                .addFilmToFavorite(EXISTING_USER_ID, NON_EXISTING_ID);

        // Act & Assert
        mockMvc.perform(post("/api/users/{userId}/favorites/{filmId}", EXISTING_USER_ID, NON_EXISTING_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userFilmService, times(1)).addFilmToFavorite(EXISTING_USER_ID, NON_EXISTING_ID);
    }

    @Test
    @DisplayName("[E] Return 400 when invalid request")
    void should_return_400_when_invalid_request() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid request"))
                .when(userFilmService).addFilmToFavorite(NON_EXISTING_ID, NON_EXISTING_ID);

        // Act & Assert
        mockMvc.perform(post("/api/users/{userId}/favorites/{filmId}", NON_EXISTING_ID, NON_EXISTING_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userFilmService, times(1)).addFilmToFavorite(anyLong(), anyLong());
    }

    // [N] Remove a film from favorite
    @Test
    @DisplayName("[N] Remove a film from favorite")
    void should_remove_film_from_favorite() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}/favorites/{filmId}", EXISTING_USER_ID, EXISTING_FILM_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userFilmService).removeFilmFromFavorite(EXISTING_USER_ID, EXISTING_FILM_ID);
    }

    // [E] Throw exception when user is null
    @Test
    @DisplayName("[E] Throw exception when user is null")
    void should_throw_exception_when_user_is_null() throws Exception {
        // Arrange
        //doThrow(new IllegalArgumentException("User ID and film ID must not be null"))
        //        .when(userFilmService).removeFilmFromFavorite(null, null);

        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}/favorites/{filmId}", null, null)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userFilmService, never()).removeFilmFromFavorite(anyLong(), anyLong());
    }

    // [E] Throw exception when film is null
    // [E] Throw exception when no relation found
}