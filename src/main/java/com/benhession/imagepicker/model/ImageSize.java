package com.benhession.imagepicker.model;

public enum ImageSize {
    THUMBNAIL("thumbnail"),
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    private final String value;

    ImageSize(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
