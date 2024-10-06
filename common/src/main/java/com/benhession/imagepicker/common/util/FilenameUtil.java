package com.benhession.imagepicker.common.util;

import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.common.model.ImageType;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class FilenameUtil {
    public String getFilename(String originalFilename, ImageType imageType, ImageSize imageSize) {
        return String.format("%s-%s-%s", imageSize, imageType, originalFilename);
    }

    public String generateParentKey(String originalFilename) {
        return String.format("%s-%s", originalFilename, UUID.randomUUID());
    }
}
