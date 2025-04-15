package com.Cinetime.controller;

import com.Cinetime.payload.dto.response.HallResponse;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.service.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    //C05
    @GetMapping("/special-halls")
    public ResponseMessage<List<HallResponse>> getAllSpecialHalls() {
        return hallService.getAllSpecialHalls();
    }


}
