package com.Cinetime.payload.dto.request;

import com.Cinetime.payload.business.SeatInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketPurchaseRequest {


    private String movieName;
    private Long showTimeId;
    //private Long cinemaId; //Showtime'in icindeki hallda zaten cinema bilgisi var.
    //private Long hall // showTime icindeki hallda zaten hall bilgisi var.
    private List<SeatInfo> seatInfos;
    private Double ticketPrice;


}
