package com.benhession.imagepicker.api.util;

import com.benhession.imagepicker.api.model.ImageSize;
import com.benhession.imagepicker.api.model.ImageType;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FilenameUtil {
    public String getFilename(String originalFilename, ImageType imageType, ImageSize imageSize) {
        return String.format("%s-%s-%s", imageSize, imageType, originalFilename);
    }
}
