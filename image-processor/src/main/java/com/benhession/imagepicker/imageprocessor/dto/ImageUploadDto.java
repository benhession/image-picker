package com.benhession.imagepicker.imageprocessor.dto;

public record ImageUploadDto(String filename, String mimetype, byte[] image) {
}
