package com.mealgo.restaurantservice.domain.model.restaurant;

import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import java.time.Instant;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;
import com.mealgo.restaurantservice.domain.shared.Entity;
import com.mealgo.restaurantservice.domain.shared.IAggregateRoot;
import lombok.Getter;

import java.util.UUID;

@Getter
public final class Restaurant extends Entity<RestaurantId> implements IAggregateRoot {
    private final UUID ownerId;
    private String name;
    private String phone;
    private RestaurantStatus status;
    private Address address;
    private Double latitude;
    private Double longitude;
    private ApprovalStatus approvalStatus;
    private Instant submittedAt;
    private UUID reviewedBy;
    private Instant reviewedAt;
    private String rejectionReason;
    private String pauseReason;
    private String avatarUrl;

    public Restaurant(RestaurantId id, UUID ownerId, String name, String phone,
                      RestaurantStatus status, Address address, Double latitude, Double longitude) {
        super(id);
        this.ownerId = ownerId;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.submittedAt = Instant.now();
    }

    public Restaurant(RestaurantId id, UUID ownerId, String name, String phone,
                      RestaurantStatus status, Address address, Double latitude, Double longitude,
                      ApprovalStatus approvalStatus, Instant submittedAt, UUID reviewedBy,
                      Instant reviewedAt, String rejectionReason, String pauseReason) {
        this(id, ownerId, name, phone, status, address, latitude, longitude);
        this.approvalStatus = approvalStatus;
        this.submittedAt = submittedAt;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
        this.pauseReason = pauseReason;
    }

    public void review(ApprovalStatus decision, UUID adminId, String reason, Instant now) {
        if (approvalStatus != ApprovalStatus.PENDING) {
            throw new BusinessException(RESTAURANT_REVIEW_CONFLICT);
        }
        if (decision != ApprovalStatus.APPROVED && decision != ApprovalStatus.REJECTED) {
            throw new BusinessException(RESTAURANT_REVIEW_INVALID);
        }
        String trimmed = null;
        if (reason != null) {
            trimmed = reason.trim();
        }
        if (decision == ApprovalStatus.REJECTED && (trimmed == null || trimmed.isEmpty() || trimmed.length() > 1000)) {
            throw new BusinessException(RESTAURANT_REJECTION_REASON_INVALID);
        }
        approvalStatus = decision;
        reviewedBy = adminId;
        reviewedAt = now;
        rejectionReason = null;
        if (decision == ApprovalStatus.REJECTED) {
            rejectionReason = trimmed;
        }
    }

    public Restaurant(RestaurantId id, UUID ownerId, String name, String phone,
                      RestaurantStatus status, Address address, Double latitude, Double longitude,
                      ApprovalStatus approvalStatus, Instant submittedAt, UUID reviewedBy,
                      Instant reviewedAt, String rejectionReason, String pauseReason, String avatarUrl) {
        this(id, ownerId, name, phone, status, address, latitude, longitude,
                approvalStatus, submittedAt, reviewedBy, reviewedAt, rejectionReason, pauseReason);
        this.avatarUrl = avatarUrl;
    }

    public boolean updateAvatar(String avatarUrl) {
        requireApproved();
        if (avatarUrl == null || avatarUrl.isBlank() || avatarUrl.length() > 2048) {
            throw new BusinessException(RESTAURANT_AVATAR_INVALID);
        }
        String normalized = avatarUrl.trim();
        try {
            java.net.URI uri = new java.net.URI(normalized);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new BusinessException(RESTAURANT_AVATAR_INVALID);
            }
        } catch (java.net.URISyntaxException exception) {
            throw new BusinessException(RESTAURANT_AVATAR_INVALID);
        }
        if (normalized.equals(this.avatarUrl)) {
            return false;
        }
        this.avatarUrl = normalized;
        return true;
    }

    public void resubmit(String name, String phone, Address address, Double latitude, Double longitude, Instant now) {
        if (approvalStatus != ApprovalStatus.REJECTED) {
            throw new BusinessException(RESTAURANT_REVIEW_CONFLICT);
        }
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        approvalStatus = ApprovalStatus.PENDING;
        submittedAt = now;
        reviewedBy = null;
        reviewedAt = null;
        rejectionReason = null;
        status = RestaurantStatus.CLOSED;
        pauseReason = null;
    }

    public void changeOperatingStatus(RestaurantStatus status, String pauseReason) {
        requireApproved();
        this.status = status;
        this.pauseReason = pauseReason;
    }

    public boolean updateAddress(Address newAddress, Double latitude, Double longitude) {
        requireApproved();

        if (status != RestaurantStatus.CLOSED) {
            throw new BusinessException(RESTAURANT_ADDRESS_REQUIRES_CLOSED);
        }

        if (newAddress == null || newAddress.addressLine() == null
                || newAddress.addressLine().isBlank() || newAddress.addressLine().length() > 255
                || newAddress.wardCode() == null || newAddress.wardCode() <= 0
                || latitude == null || !Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || longitude == null || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new BusinessException(RESTAURANT_ADDRESS_INVALID);
        }

        String addressLine = newAddress.addressLine().trim();
        Address normalizedAddress = new Address(addressLine, newAddress.wardCode());

        if (normalizedAddress.equals(address)
                && java.util.Objects.equals(this.latitude, latitude)
                && java.util.Objects.equals(this.longitude, longitude)) {
            return false;
        }

        this.address = normalizedAddress;
        this.latitude = latitude;
        this.longitude = longitude;

        return true;
    }

    public void requireApproved() {
        if (approvalStatus != ApprovalStatus.APPROVED) {
            throw new BusinessException(RESTAURANT_NOT_APPROVED);
        }
    }

    public static Restaurant register(UUID ownerId, String name, String phone, RestaurantStatus status,
                                      Address address, Double latitude, Double longitude) {
        return new Restaurant(RestaurantId.newId(), ownerId, name, phone, status, address, latitude, longitude);
    }

}
