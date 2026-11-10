package com.mealgo.searchservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class PublicDiscoveryRoutesTest {
    @Test
    void reverseLocationUsesPublicRoute() throws NoSuchMethodException {
        assertArrayEquals(new String[]{"/api/public/locations"},
                LocationController.class.getAnnotation(RequestMapping.class).value());
        assertArrayEquals(new String[]{"/reverse"},
                LocationController.class.getMethod("reverse", double.class, double.class)
                        .getAnnotation(GetMapping.class).value());
    }

    @Test
    void nearbyIsPublicAndDishSearchKeepsItsRoute() throws NoSuchMethodException {
        assertArrayEquals(new String[]{"/api"},
                RestaurantSearchController.class.getAnnotation(RequestMapping.class).value());
        assertArrayEquals(new String[]{"/public/restaurants/nearby"},
                RestaurantSearchController.class.getMethod("nearby", double.class, double.class,
                                double.class, int.class, int.class)
                        .getAnnotation(GetMapping.class).value());
        assertArrayEquals(new String[]{"/search/restaurants/by-dish"},
                RestaurantSearchController.class.getMethod("byDish", String.class, int.class, int.class)
                        .getAnnotation(GetMapping.class).value());
    }
}
