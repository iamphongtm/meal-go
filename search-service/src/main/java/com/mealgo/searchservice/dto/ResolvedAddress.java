package com.mealgo.searchservice.dto;

/**
 * Suggested address resolved from device coordinates.
 * Coordinates retain the original request location; unavailable address fields are null.
 */
public record ResolvedAddress(
        double latitude,
        double longitude,
        String name,
        String ward,
        String city,
        String formattedAddress
) {
}
