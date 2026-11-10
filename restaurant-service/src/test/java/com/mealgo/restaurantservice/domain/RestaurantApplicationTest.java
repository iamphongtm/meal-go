package com.mealgo.restaurantservice.domain;

import com.mealgo.restaurantservice.domain.enums.*;
import com.mealgo.restaurantservice.domain.model.restaurant.*;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantApplicationTest {
    private Restaurant restaurant() {
        return Restaurant.register(UUID.randomUUID(), "Test", "0900000000", RestaurantStatus.CLOSED,
                new Address("Test address", 1), 10.0, 106.0);
    }

    @Test
    void newApplicationIsPendingAndCannotOpen() {
        Restaurant restaurant = restaurant();
        assertEquals(ApprovalStatus.PENDING, restaurant.getApprovalStatus());
        assertThrows(BusinessException.class, () -> restaurant.changeOperatingStatus(RestaurantStatus.OPENING, null));
    }

    @Test
    void approvalRecordsReviewerAndAllowsOpeningButNotAnotherReview() {
        Restaurant restaurant = restaurant();
        UUID admin = UUID.randomUUID();
        Instant now = Instant.now();
        restaurant.review(ApprovalStatus.APPROVED, admin, null, now);
        assertEquals(admin, restaurant.getReviewedBy());
        assertEquals(now, restaurant.getReviewedAt());
        restaurant.changeOperatingStatus(RestaurantStatus.OPENING, null);
        assertThrows(BusinessException.class,
                () -> restaurant.review(ApprovalStatus.REJECTED, admin, "reason", now));
    }

    @Test
    void rejectionRequiresReasonAndResubmissionClearsReview() {
        Restaurant restaurant = restaurant();
        UUID admin = UUID.randomUUID();
        Instant now = Instant.now();
        assertThrows(BusinessException.class, () -> restaurant.review(ApprovalStatus.REJECTED, admin, "  ", now));
        restaurant.review(ApprovalStatus.REJECTED, admin, "  Missing information  ", now);
        assertEquals("Missing information", restaurant.getRejectionReason());
        restaurant.resubmit("Updated", "0900000000", new Address("New address", 1), 11.0, 107.0, now);
        assertEquals(ApprovalStatus.PENDING, restaurant.getApprovalStatus());
        assertEquals("Updated", restaurant.getName());
        assertNull(restaurant.getReviewedBy());
        assertNull(restaurant.getReviewedAt());
        assertNull(restaurant.getRejectionReason());
    }

    @Test
    void pendingCannotBeResubmittedOrUsedAsReviewDecision() {
        Restaurant restaurant = restaurant();
        assertThrows(BusinessException.class, () -> restaurant.review(ApprovalStatus.PENDING, UUID.randomUUID(), null, Instant.now()));
        assertThrows(BusinessException.class, () -> restaurant.resubmit("Updated", "0900000000",
                new Address("Address", 1), 10.0, 106.0, Instant.now()));
    }
}
