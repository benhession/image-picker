package com.benhession.imagepicker.data.service;

import com.benhession.imagepicker.data.dto.ImageUploadDto;
import java.util.List;

public interface ObjectStorageService {
    String uploadFiles(String originalFilename, List<ImageUploadDto> images);

    String getBaseResourcePath(String parentKey);
}
