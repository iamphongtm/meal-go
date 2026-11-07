package com.mealgo.searchservice.exception;

public enum ErrorCode {
    INVALID_ARGUMENT(400, "Invalid parameters. Check the coordinates and search criteria."),
    INVALID_REQUEST(400, "The request is invalid or unsupported."),
    ADDRESS_NOT_FOUND(404, "No address was found at this location."),
    GEOCODING_NOT_CONFIGURED(503, "The address lookup service is not ready."),
    GEOCODING_UNAVAILABLE(503, "The address lookup service is temporarily unavailable."),
    GEOCODING_TIMEOUT(504, "The address lookup service did not respond in time."),
    GEOCODING_PROVIDER_ERROR(502, "Unable to retrieve an address from the map provider."),
    INTERNAL_ERROR(500, "An unexpected error occurred. Please try again later.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
