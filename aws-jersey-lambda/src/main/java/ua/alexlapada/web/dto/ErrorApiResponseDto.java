package ua.alexlapada.web.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ErrorApiResponseDto {
    private String message;
    private String description;
}
