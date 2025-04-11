package com.Cinetime.service;

import com.Cinetime.entity.Ticket;
import com.Cinetime.entity.User;
import com.Cinetime.enums.TicketStatus;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.TicketDto;
import com.Cinetime.payload.mappers.TicketMapper;
import com.Cinetime.repo.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final PageableHelper pageableHelper;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    //T01 Return movies that an authenticated user bought and haven't used yet
    public Page<TicketDto> getCurrentTickets(User user, int page, int size, String sort, String type) {
        Pageable pageable =pageableHelper.pageableSort(page, size, sort, type);
        Page<Ticket> tickets = ticketRepository
                .findByUserAndStatus(user, TicketStatus.RESERVED, pageable);

        return tickets.map(ticketMapper::toDto);
    }

    public Page<TicketDto> getPassedTickets(User user, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);
        Page<Ticket> tickets = ticketRepository
                .findByUserAndStatus(user, TicketStatus.USED, pageable);

        return tickets.map(ticketMapper::toDto);
    }
}
