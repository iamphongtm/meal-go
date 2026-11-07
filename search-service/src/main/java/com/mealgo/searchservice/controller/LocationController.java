package com.mealgo.searchservice.controller;

import lombok.RequiredArgsConstructor;
import com.mealgo.searchservice.dto.ResolvedAddress;
import com.mealgo.searchservice.service.LocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/reverse")
    public ResolvedAddress reverse(
            @RequestParam(name = "latitude") double latitude,
            @RequestParam(name = "longitude") double longitude) {
        return locationService.resolve(latitude, longitude);
    }
}
