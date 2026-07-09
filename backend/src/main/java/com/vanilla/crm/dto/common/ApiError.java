package com.vanilla.crm.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Стандартный формат ответа при ошибке")
public class ApiError {
    @Schema(description = "HTTP-статус код", example = "400")
    private int status;

    @Schema(description = "Краткое описание ошибки", example = "Bad Request")
    private String error;

    @Schema(description = "Подробное сообщение", example = "Блюдо с данным ID не найдено")
    private String message;

    @Schema(description = "Время возникновения ошибки")
    private LocalDateTime timestamp;

    @Schema(description = "Детали валидации (если есть)")
    private Map<String, String> details;
}
