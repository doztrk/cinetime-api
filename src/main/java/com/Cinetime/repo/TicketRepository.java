package com.Cinetime.repo;

import com.Cinetime.entity.Showtime;
import com.Cinetime.entity.Ticket;
import com.Cinetime.entity.User;
import com.Cinetime.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t WHERE t.user = :user")
    List<Ticket> findAllTicketsByUser(@Param("user") User user);


    Page<Ticket> findByUserAndStatus(User user, TicketStatus status, Pageable pageable);

    Optional<Ticket> findByShowtimeAndSeatLetterAndSeatNumber(Showtime showtime, String seatLetter, Integer seatNumber);

    List<Ticket> findByShowtime(Showtime showtime);

    List<Ticket> findByShowtimeAndSeatLetterInAndSeatNumberIn(Showtime showtime, List<String> letters, List<Integer> numbers);
    Optional<Ticket> findByShowtimeAndSeatLetterAndSeatNumberAndStatusIn(
            Showtime showtime,
            String seatLetter,
            int seatNumber,
            List<TicketStatus> statusList
    );

}
