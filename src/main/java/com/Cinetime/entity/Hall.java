package com.Cinetime.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private Integer seatCapacity;

    private Boolean isSpecial = false;

    @ManyToOne
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;


    @OneToMany(mappedBy = "hall", fetch = FetchType.LAZY)
    private Set<Showtime> showtimes = new HashSet<>();


    @NotNull
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;


/*    @ManyToMany(mappedBy = "halls", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private Set<Movie> movies = new HashSet<>();*/


    //To prevent circular reference
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hall hall = (Hall) o;
        return Objects.equals(id, hall.id) &&
                Objects.equals(name, hall.name) &&
                Objects.equals(seatCapacity, hall.seatCapacity) &&
                Objects.equals(isSpecial, hall.isSpecial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, seatCapacity, isSpecial);
    }


    //Eğer name bu değerleri içerirse, specialHall true olacak
    public void setName(String name) {
        this.name = name;
        if (name != null && (
                name.contains("IMAX") ||
                        name.contains("4DX") ||
                        name.contains("GOLD CLASS") ||
                        name.contains("VIP") ||
                        name.contains("SCREENX") ||
                        name.contains("STARIUM") ||
                        name.contains("TEMPUR CINEMA") ||
                        name.contains("D BOX") ||
                        name.contains("SKYBOX") ||
                        name.contains("SKY AUDITORIUM") ||
                        name.contains("PREMIUM CINEMA") ||
                        name.contains("MPX")

        )) {
            this.isSpecial = true;
        }
    }

  /*  @Override
    public String toString() {
        return "Hall{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", seatCapacity=" + seatCapacity +
                ", isSpecial=" + isSpecial +
                ", cinema=" + (cinema != null ? cinema.getId() : null) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }*/
}
