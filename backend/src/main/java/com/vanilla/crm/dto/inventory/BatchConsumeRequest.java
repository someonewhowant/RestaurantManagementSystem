package com.vanilla.crm.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchConsumeRequest {
    private List<ConsumeItemDto> items;
}
