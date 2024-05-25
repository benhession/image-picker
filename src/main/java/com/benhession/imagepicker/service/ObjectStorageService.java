package com.benhession.imagepicker.service;

import com.benhession.imagepicker.dto.ImageUploadDto;

import java.util.List;

public interface ObjectStorageService {
    String uploadFiles(String originalFilename, List<ImageUploadDto> images);
    String getBaseResourcePath(String parentKey);
}
