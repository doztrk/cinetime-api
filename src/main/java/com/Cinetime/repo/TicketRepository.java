package com.Cinetime.repo;

import com.Cinetime.entity.Showtime;
import com.Cinetime.entity.Ticket;
import com.Cinetime.entity.User;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.payload.business.SeatInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t WHERE t.user = :user")
    List<Ticket> findAllTicketsByUser(@Param("user") User user);


    Page<Ticket> findByUserAndStatus(User user, TicketStatus status, Pageable pageable);

    List<Ticket> findByShowtimeAndStatusIn(
            Showtime showtime,
            List<TicketStatus> statuses
    );


    @Query("SELECT CONCAT(t.seatLetter, t.seatNumber) FROM Ticket t WHERE t.showtime.id = :showtimeId AND t.status IN :statuses")
    List<String> findOccupiedSeatsByShowtimeAndStatus(@Param("showtimeId") Long showtimeId, @Param("statuses") List<TicketStatus> statuses);

    @Query("SELECT new com.Cinetime.payload.business.SeatInfo(t.seatLetter, t.seatNumber) FROM Ticket t WHERE t.showtime.id = :showtimeId AND t.status IN :statuses")
    List<SeatInfo> findOccupiedSeatInfoByShowtimeAndStatus(@Param("showtimeId") Long showtimeId, @Param("statuses") List<TicketStatus> statuses);
}
