package com.Cinetime.service;

import com.Cinetime.entity.Cinema;
import com.Cinetime.entity.User;
import com.Cinetime.entity.UserCinemaFavorite;
import com.Cinetime.exception.ResourceNotFoundException;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.dto.response.ResponseMessage;
import com.Cinetime.repo.UserCinemaFavoriteRepository;
import com.Cinetime.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserCinemaFavoriteService {

    private final UserCinemaFavoriteRepository userCinemaFavoriteRepository;
    private final PageableHelper pageableHelper;
    private final UserRepository userRepository;

    public ResponseMessage<List<Cinema>> getUserFavoriteCinemas(int page, int size, String sort, String type) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String phoneNumber = authentication.getName();

            Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);

            if (userOptional.isEmpty()) {

                return ResponseMessage.<List<Cinema>>builder()
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message("User authentication failed, this indicates a system error.")
                        //Userın olması lazım cunku zaten authenticationdan phoeneNumber ile getiriyoruz zaten. Yoksa baska bir hata var.
                        .build();
            }
            User user = userOptional.get();

            Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

            List<Cinema> favoriteCinemas = userCinemaFavoriteRepository
                    .findByUser(user, pageable)
                    .stream()
                    .map(UserCinemaFavorite::getCinema)
                    .toList();


            return ResponseMessage.<List<Cinema>>builder()
                    .message("Favorite cinemas found successfully") // <-- Listenin ici bos olsa bile 200 gonderiyoruz. REST prensibi.
                    .httpStatus(HttpStatus.OK)                                          //Kontrol frontende kalmis.
                    .object(favoriteCinemas)
                    .build();
        } catch (Exception e) {
            return ResponseMessage.<List<Cinema>>builder()
                    .message("Something went wrong " + e.getMessage())
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();


        }
    }

}
