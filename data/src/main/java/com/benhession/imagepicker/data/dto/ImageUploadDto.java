package com.benhession.imagepicker.data.dto;

import lombok.Builder;

@Builder
public record ImageUploadDto(String filename, String mimetype, byte[] image) {
}
