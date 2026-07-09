package com.vanilla.crm.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchConsumeRequest {
    @NotEmpty(message = "Список ингредиентов не может быть пустым")
    @Valid
    private List<ConsumeItemDto> items;
}
