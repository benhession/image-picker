package com.benhession.imagepicker.dto;

public record ImageUploadDto(String filename, String mimetype, byte[] image) {
}
