package com.itinov.movie_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_film_relations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFilmRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Film film;

    private boolean isFavorite;
    private boolean hasBeenWatched;
    private LocalDateTime watchedAt;
}
