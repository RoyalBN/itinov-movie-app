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
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "film_id")
    private Film film;

    @Column(name = "is_favorite")
    private boolean isFavorite;

    @Column(name = "has_been_watched")
    private boolean hasBeenWatched;

    @Column(name = "watched_at")
    private LocalDateTime watchedAt;
}
