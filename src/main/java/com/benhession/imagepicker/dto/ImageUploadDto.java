package com.benhession.imagepicker.dto;

import java.awt.image.BufferedImage;

public record ImageUploadDto(String filename, String mimetype, BufferedImage image) {
}
