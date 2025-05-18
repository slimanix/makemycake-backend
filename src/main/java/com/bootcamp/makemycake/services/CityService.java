package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.models.City;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityService {
    
    private final ObjectMapper objectMapper;
    
    public List<City> getAllCities() {
        try {
            ClassPathResource resource = new ClassPathResource("static/data/morocco_cities.json");
            // Read the root node
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(resource.getInputStream());
            // Navigate to the data array
            com.fasterxml.jackson.databind.JsonNode dataNode = root.path("cities").path("data");
            // Map the data array to List<City>
            return objectMapper.readValue(
                dataNode.traverse(),
                new com.fasterxml.jackson.core.type.TypeReference<List<City>>() {}
            );
        } catch (IOException e) {
            log.error("Error reading cities data: {}", e.getMessage());
            throw new RuntimeException("Failed to read cities data", e);
        }
    }

    public City getCityByVilleId(String villeId) {
        List<City> cities = getAllCities();
        return cities.stream()
                .filter(city -> String.valueOf(city.getVilleId()).equals(villeId))
                .findFirst()
                .orElse(null);
    }

    public City getCityByName(String cityName) {
        List<City> cities = getAllCities();
        return cities.stream()
                .filter(city -> city.getNames().values().stream()
                        .anyMatch(name -> name.equalsIgnoreCase(cityName)))
                .findFirst()
                .orElse(null);
    }
} 