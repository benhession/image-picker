package com.benhession.imagepicker.common.model;

import lombok.Builder;

@Builder
public record FileData(byte[] data, String filename, String mimeType, String imageType) {
}
