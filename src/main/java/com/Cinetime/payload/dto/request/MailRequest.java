package com.Cinetime.payload.dto.request;

import com.Cinetime.payload.business.SeatInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MailRequest {

    private String to;
    private String subject;
    private String movieName;
    private String total;
    private String cinemaName;
    private String cinemaAddress;
    private String hallName;
    private List<SeatInfo> seatInfos;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private String adress;
    private String retrievalCode;


}
