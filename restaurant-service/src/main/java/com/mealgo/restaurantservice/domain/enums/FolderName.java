package com.mealgo.restaurantservice.domain.enums;

public enum FolderName {
    AVATAR("avatar"),
    POST_IMAGE("postImage"),
    POST_VIDEO("postVideo"),
    MESSAGE_IMAGE("messageImage"),
    MESSAGE_VIDEO("messageVideo"),
    ;

    private final String path;

    FolderName(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
