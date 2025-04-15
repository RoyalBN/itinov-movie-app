package com.itinov.movie_api.service;

import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.User;
import com.itinov.movie_api.model.UserFilmRelation;
import com.itinov.movie_api.repository.FilmRepository;
import com.itinov.movie_api.repository.UserFilmRelationRepository;
import com.itinov.movie_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserFilmServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private UserFilmRelationRepository relationRepository;

    private UserFilmService userFilmService;

    private User user;
    private Film film;
    private UserFilmRelation userFilmRelation;

    @BeforeEach
    void setUp() {
        userFilmService = new UserFilmService(userRepository, filmRepository, relationRepository);
        user = User.builder().id(1L).username("Peter").build();
        film = Film.builder().id(1L).title("Star Wars").rating(8.5).build();
        userFilmRelation = UserFilmRelation.builder()
                .id(1L)
                .user(user)
                .film(film)
                .hasBeenWatched(true)
                .isFavorite(false)
                .build();
    }

    @Test
    @DisplayName("[N] Add a film to my favorite for non-existing relation")
    void should_add_a_film_to_my_favorite_for_non_existing_relation() {
        // Arrange
        UserFilmRelation expectedRelation = UserFilmRelation.builder()
                .id(10L)
                .user(user)
                .film(film)
                .isFavorite(true)
                .hasBeenWatched(false)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(filmRepository.findById(film.getId())).thenReturn(Optional.of(film));
        when(relationRepository.save(any(UserFilmRelation.class))).thenReturn(expectedRelation);

        // Act
        userFilmService.addFilmToFavorite(user.getId(), film.getId());

        // Assert
        ArgumentCaptor<UserFilmRelation> captor = ArgumentCaptor.forClass(UserFilmRelation.class);
        verify(relationRepository, times(1)).save(captor.capture());

        UserFilmRelation relationSaved = captor.getValue();
        assertThat(relationSaved.getUser()).isEqualTo(user);
        assertThat(relationSaved.getFilm()).isEqualTo(film);
        assertThat(relationSaved.isFavorite()).isTrue();
        assertThat(relationSaved.isHasBeenWatched()).isFalse();
    }

    @Test
    @DisplayName("[N] Update isFavorite to true for existing relation")
    void should_update_isFavorite_to_true_if_relation_already_exists() {
        // Arrange
        UserFilmRelation existingRelation = UserFilmRelation.builder()
                .id(11L)
                .user(user)
                .film(film)
                .isFavorite(false)
                .hasBeenWatched(true)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(filmRepository.findById(film.getId())).thenReturn(Optional.of(film));
        when(relationRepository.findByUserAndFilm(user, film)).thenReturn(Optional.of(existingRelation));
        when(relationRepository.save(any(UserFilmRelation.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        userFilmService.addFilmToFavorite(user.getId(), film.getId());

        // Assert
        ArgumentCaptor<UserFilmRelation> captor = ArgumentCaptor.forClass(UserFilmRelation.class);
        verify(relationRepository).save(captor.capture());
        UserFilmRelation saved = captor.getValue();

        assertThat(saved.isFavorite()).isTrue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getFilm()).isEqualTo(film);
        assertThat(saved.isHasBeenWatched()).isTrue();
    }

    // [N] Throw IllegalArgumentException if userId or filmId is null
    @Test
    @DisplayName("[N] Throw IllegalArgumentException if userId or filmId is null")
    void should_throw_exception_if_user_id_or_film_id_is_null() throws Exception {
        // Arrange
        Long userId = null;
        Long filmId = null;

        // Act
        Throwable thrown = catchThrowable(() -> userFilmService.addFilmToFavorite(userId, filmId));

        // Assert
        assertThat(thrown)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("User ID and film ID must not be null");
    }

    // [E] Throw Exception if user not found

    // [E] Throw Exception if film not found

    // [E] Throw Exception if fields are not valid


}
