package com.itinov.movie_api.repository;

import com.itinov.movie_api.model.Film;
import com.itinov.movie_api.model.User;
import com.itinov.movie_api.model.UserFilmRelation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserFilmRelationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserFilmRelationRepository repository;

    @Test
    void should_find_relation_by_user_and_film() {
        // Arrange
        User user = User.builder()
                .username("Peter")
                .build();
        entityManager.persist(user);

        Film film = Film.builder()
                .title("Star Wars")
                .rating(8.5)
                .build();
        entityManager.persist(film);

        UserFilmRelation relation = UserFilmRelation.builder()
                .user(user)
                .film(film)
                .isFavorite(true)
                .hasBeenWatched(false)
                .build();
        entityManager.persist(relation);
        entityManager.flush();

        // Act
        var found = repository.findByUserAndFilm(user, film);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().isFavorite()).isTrue();
        assertThat(found.get().isHasBeenWatched()).isFalse();
    }

    @Test
    void should_return_empty_when_relation_does_not_exist() {
        // Arrange
        User user = User.builder()
                .username("Peter")
                .build();
        entityManager.persist(user);

        Film film = Film.builder()
                .title("Star Wars")
                .rating(8.5)
                .build();
        entityManager.persist(film);
        entityManager.flush();

        // Act
        var found = repository.findByUserAndFilm(user, film);

        // Assert
        assertThat(found).isEmpty();
    }
}