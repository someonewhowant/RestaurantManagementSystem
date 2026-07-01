package com.vanilla.crm.tables;

import com.vanilla.crm.tables.dto.TableDto;
import com.vanilla.crm.tables.dto.TableStatusRequest;
import com.vanilla.crm.tables.dto.WaiterAssignRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableDto>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TableDto> changeStatus(@PathVariable UUID id, @RequestBody TableStatusRequest request) {
        return ResponseEntity.ok(tableService.changeStatus(id, request.getStatus(), request.getWaiterId()));
    }

    @PatchMapping("/{id}/waiter")
    public ResponseEntity<TableDto> assignWaiter(@PathVariable UUID id, @RequestBody WaiterAssignRequest request) {
        return ResponseEntity.ok(tableService.assignWaiter(id, request.getWaiterId()));
    }
}
