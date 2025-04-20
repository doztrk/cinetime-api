package com.Cinetime.entity;

import com.Cinetime.converter.MovieStatusConverter;
import com.Cinetime.enums.MovieStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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
    @Size(min = 3, max = 20)
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


    // Many-to-many relationship with Hall
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "movie_hall",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "hall_id")
    )
    private Set<Hall> halls = new HashSet<>();

    // One-to-many relationship with Showtime
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
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

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "poster_id", nullable = false)
    private PosterImage poster;

    @NotNull
    @Convert(converter = MovieStatusConverter.class)
    @Column(name = "status", nullable = false)
    private MovieStatus status = MovieStatus.COMING_SOON;

    @NotNull
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate createdAt;

    @NotNull
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDate updatedAt;

    public String getDurationFormatted() {
        return String.format("%02d:%02d", duration / 60, duration % 60);
    }

    //TODO: Rating sınıfı oluşturup hikaye,görsellik,müzik,oyunculuk eklenebilir.

}
