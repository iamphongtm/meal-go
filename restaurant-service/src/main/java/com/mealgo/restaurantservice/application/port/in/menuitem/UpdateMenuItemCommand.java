package com.mealgo.restaurantservice.application.port.in.menuitem;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateMenuItemCommand(UUID restaurantId, UUID ownerId, UUID menuItemId, UUID categoryId,
                                    String name, String description, BigDecimal basePrice, String imageUrl) {}
