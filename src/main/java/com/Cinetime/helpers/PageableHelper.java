package com.Cinetime.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageableHelper {

    public Pageable pageableSort(int page, int size, String sort, String type) {

        Pageable pageable = Pageable.unpaged();
        if (type.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        }
        return pageable;
    }

}
