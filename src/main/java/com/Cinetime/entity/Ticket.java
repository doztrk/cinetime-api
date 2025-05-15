package com.Cinetime.entity;

import com.Cinetime.converter.MovieStatusConverter;
import com.Cinetime.converter.TicketStatusConverter;
import com.Cinetime.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TICKET")
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String seatLetter;

    @NotNull
    @Column(nullable = false)
    private Integer seatNumber;

    @NotNull
    @Column(nullable = false)
    private Double price;

    @NotNull
    @Convert(converter = TicketStatusConverter.class)
    @Column(name = "status", nullable = false)
    private TicketStatus status = TicketStatus.RESERVED;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;


    @ManyToOne
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;


    @ManyToOne
    @JoinColumn(name = "user_id") //TODO:Nullable true, hardcoded user anonym
    private User user;


    @ManyToOne
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "anonymous_user_id")
    private AnonymousUser anonymousUser;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    private void validateUserOrAnonymous() {
        if (user == null && anonymousUser == null) {
            throw new IllegalStateException("Payments must have either a User or AnonymousUser");
        }
        if (user != null && anonymousUser != null) {
            throw new IllegalStateException("Payments cannot have both User and AnonymousUser");
        }
    }

    @PrePersist
    public void prePersist() {
        validateUserOrAnonymous();
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
        validateUserOrAnonymous();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
