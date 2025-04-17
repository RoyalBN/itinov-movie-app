package com.itinov.movie_api.repository;

import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.User;
import com.itinov.movie_api.model.UserFilmRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserFilmRelationRepositoryTest {

    private User user;
    private Film film;
    private UserFilmRelation relation;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserFilmRelationRepository repository;

    private UserFilmRelation persistRelation(boolean isFavorite, boolean hasBeenWatched) {
        UserFilmRelation relation = UserFilmRelation.builder()
                .user(user)
                .film(film)
                .isFavorite(isFavorite)
                .hasBeenWatched(hasBeenWatched)
                .build();
        entityManager.persist(relation);
        entityManager.flush();

        return relation;
    }

    @BeforeEach
    void setUp() {
        user = User.builder().username("Peter").build();
        film = Film.builder().title("Star Wars").rating(8.5).build();

        entityManager.persist(user);
        entityManager.persist(film);
        entityManager.flush();
    }

    @Test
    @DisplayName("[N] Find relation by user and film")
    void should_find_relation_by_user_and_film() {
        // Arrange
        UserFilmRelation relation = persistRelation(true, false);

        // Act
        Optional<UserFilmRelation> foundRelation = repository.findByUserAndFilm(user, film);

        // Assert
        assertThat(foundRelation).isPresent();
        assertThat(foundRelation.get().isFavorite()).isTrue();
        assertThat(foundRelation.get().isHasBeenWatched()).isFalse();
    }

    @Test
    @DisplayName("[N] Return empty when relation does not exist")
    void should_return_empty_when_relation_does_not_exist() {
        // Act
        Optional<UserFilmRelation> foundRelation = repository.findByUserAndFilm(user, film);

        // Assert
        assertThat(foundRelation).isEmpty();
    }

    @Test
    @DisplayName("[N] Remove a film from favorite")
    void should_remove_film_from_favorite() {
        // Arrange
        UserFilmRelation relation = persistRelation(true, false);

        // Act
        Optional<UserFilmRelation> foundRelation = repository.findByUserAndFilm(user, film);
        foundRelation.get().setFavorite(false);
        repository.save(foundRelation.get());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<UserFilmRelation> updatedRelation = repository.findByUserAndFilm(user, film);
        assertThat(updatedRelation.get().isFavorite()).isFalse();
        assertThat(updatedRelation.get().isHasBeenWatched()).isFalse();
    }

}