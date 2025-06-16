package com.Cinetime.entity;

import com.Cinetime.converter.MovieStatusConverter;
import com.Cinetime.enums.MovieStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.cglib.core.Local;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table(name = "MOVIE")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 100)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false)
    private String slug;

    @NotBlank
    @Size(min = 3, max = 300)
    @Column(nullable = false)
    private String summary;

    @NotNull
    @Column(nullable = false)
    private LocalDate releaseDate;

    @NotNull
    @Column(nullable = false)
    private Integer duration;

    //Nullable
    private Double rating;


    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore
    private Set<Showtime> showtimes = new HashSet<>();

    @NotBlank
    @Column(nullable = false)
    private String director;

    @JdbcTypeCode(SqlTypes.JSON)
    @NotEmpty
    @Column(name = "cast_list", nullable = false)
    private List<String> cast;

    @NotEmpty
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<String> formats;

    @NotEmpty
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<String> genre;

    @Column(name = "poster_url")
    private String posterUrl;

    @NotNull
    @Convert(converter = MovieStatusConverter.class)
    @Column(name = "status", nullable = false)
    private MovieStatus status = MovieStatus.COMING_SOON;

    // With these:
    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getDurationFormatted() {
        return String.format("%02d:%02d", duration / 60, duration % 60);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return Objects.equals(id, movie.id); // Use only ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Use only ID
    }
    //TODO: Rating sınıfı oluşturup hikaye,görsellik,müzik,oyunculuk eklenebilir.

}
