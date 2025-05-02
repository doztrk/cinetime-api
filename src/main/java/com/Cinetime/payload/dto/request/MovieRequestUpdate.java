package com.Cinetime.payload.dto.request;

import com.Cinetime.enums.MovieStatus;
import com.Cinetime.payload.dto.request.abstracts.MovieRequestWithShowtime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class MovieRequestUpdate extends MovieRequestWithShowtime {

}
