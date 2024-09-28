package com.benhession.imagepicker.data.dto;

public record ImageUploadDto(String filename, String mimetype, byte[] image) {
}
