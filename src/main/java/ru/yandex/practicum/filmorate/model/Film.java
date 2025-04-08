package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Long> likes;
    private Mpa mpa;
    private List<Genre> genres;

    public Set<Long> getLikes() {
        return likes != null ? new HashSet<>(likes) : new HashSet<>();
    }

    public void setLikes(Set<Long> likes) {
        this.likes = likes != null ? new HashSet<>(likes) : null;
    }

    public void addLike(Long userId) {
        if (likes == null) {
            likes = new HashSet<>();
        }
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        if (likes != null) {
            likes.remove(userId);
        }
    }

    public int getLikesCount() {
        return likes != null ? likes.size() : 0;
    }

    public Mpa getMpa() {
        return mpa;
    }

    public void setMpa(Mpa mpa) {
        this.mpa = mpa;
    }

    public List<Genre> getGenres() {
        return genres != null ? new ArrayList<>(genres) : new ArrayList<>();
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : null;
    }
}