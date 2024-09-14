package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.api.dto.ImageUploadDto;

import java.util.List;

public interface ObjectStorageService {
    String uploadFiles(String originalFilename, List<ImageUploadDto> images);

    String getBaseResourcePath(String parentKey);
}
