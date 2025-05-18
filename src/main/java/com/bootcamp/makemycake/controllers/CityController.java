package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.models.City;
import com.bootcamp.makemycake.services.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping("/cities")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @GetMapping("/cities/{ville_id}")
    public ResponseEntity<?> getCityByVilleId(@PathVariable("ville_id") String villeId) {
        City city = cityService.getCityByVilleId(villeId);
        if (city == null) {
            return ResponseEntity.status(404).body(
                java.util.Map.of(
                    "status", 404,
                    "error", "City not found",
                    "message", "No city found with ville_id: " + villeId
                )
            );
        }
        return ResponseEntity.ok(city);
    }

    @GetMapping("/cities/name/{name}")
    public ResponseEntity<?> getCityByName(@PathVariable("name") String name) {
        City city = cityService.getCityByName(name);
        if (city == null) {
            return ResponseEntity.status(404).body(
                java.util.Map.of(
                    "status", 404,
                    "error", "City not found",
                    "message", "No city found with name: " + name
                )
            );
        }
        return ResponseEntity.ok(city);
    }
} 