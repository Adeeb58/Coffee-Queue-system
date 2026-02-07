package com.beanbrewcafe.barista.controller;

import com.beanbrewcafe.barista.model.Drink;
import com.beanbrewcafe.barista.repository.DrinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drinks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DrinkController {

    private final DrinkRepository drinkRepository;

    /**
     * Get all drinks
     * GET /api/drinks
     */
    @GetMapping
    public ResponseEntity<List<Drink>> getAllDrinks() {
        return ResponseEntity.ok(drinkRepository.findAll());
    }
}