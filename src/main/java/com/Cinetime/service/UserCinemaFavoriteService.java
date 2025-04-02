package com.Cinetime.service;

import com.Cinetime.entity.User;
import com.Cinetime.helpers.PageableHelper;
import com.Cinetime.payload.mappers.CinemaMapper;
import com.Cinetime.payload.response.CinemaResponse;
import com.Cinetime.repo.UserCinemaFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCinemaFavoriteService {

    private final UserCinemaFavoriteRepository favoriteRepository;
    private final PageableHelper pageableHelper;
    private final CinemaMapper cinemaMapper;

    public Page<CinemaResponse> getUserFavoriteCinemas(User user, int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.pageableSort(page, size, sort, type);

        return favoriteRepository.findByUser(user, pageable)
                .map(fav -> cinemaMapper.mapCinemaToCinemaResponse(fav.getCinema()));
    }
}
