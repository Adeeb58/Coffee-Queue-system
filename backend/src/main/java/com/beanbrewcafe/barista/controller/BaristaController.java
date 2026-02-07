package com.beanbrewcafe.barista.controller;

import com.beanbrewcafe.barista.model.Barista;
import com.beanbrewcafe.barista.service.BaristaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/baristas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BaristaController {

    private final BaristaService baristaService;

    /**
     * Get all baristas
     * GET /api/baristas
     */
    @GetMapping
    public ResponseEntity<List<Barista>> getAllBaristas() {
        return ResponseEntity.ok(baristaService.getAllBaristas());
    }

    /**
     * Assign next order to a barista
     * POST /api/baristas/{id}/assign-next
     */
    @PostMapping("/{id}/assign-next")
    public ResponseEntity<Void> assignNextOrder(@PathVariable Long id) {
        baristaService.assignNextOrderToBarista(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Update barista status
     * PUT /api/baristas/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Barista.BaristaStatus status) {
        baristaService.setBaristaStatus(id, status);
        return ResponseEntity.ok().build();
    }
}