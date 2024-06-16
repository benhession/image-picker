package com.benhession.imagepicker.model;

public enum ImageType {
    SQUARE("square"),
    PANORAMIC("panoramic"),
    RECTANGULAR("rectangular"),
    LANDSCAPE("landscape");

    private final String value;

    ImageType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
