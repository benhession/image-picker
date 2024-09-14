package com.benhession.imagepicker.api.dto;

public record ImageUploadDto(String filename, String mimetype, byte[] image) {
}
