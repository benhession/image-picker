package com.benhession.imagepicker.imageprocessor.util;

import com.benhession.imagepicker.imageprocessor.model.ImageSize;
import com.benhession.imagepicker.imageprocessor.model.ImageType;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FilenameUtil {
    public String getFilename(String originalFilename, ImageType imageType, ImageSize imageSize) {
        return String.format("%s-%s-%s", imageSize, imageType, originalFilename);
    }
}
