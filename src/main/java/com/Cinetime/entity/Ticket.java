package com.Cinetime.entity;

import com.Cinetime.converter.MovieStatusConverter;
import com.Cinetime.converter.TicketStatusConverter;
import com.Cinetime.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TICKET")
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
