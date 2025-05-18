package com.bootcamp.makemycake.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class City {
    @JsonProperty("ville_id")
    private String villeId;
    
    private Map<String, String> names;
} 