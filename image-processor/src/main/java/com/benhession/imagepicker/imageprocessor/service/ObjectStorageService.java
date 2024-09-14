package com.benhession.imagepicker.imageprocessor.service;


import com.benhession.imagepicker.imageprocessor.dto.ImageUploadDto;
import java.util.List;

public interface ObjectStorageService {
    String uploadFiles(String originalFilename, List<ImageUploadDto> images);

    String getBaseResourcePath(String parentKey);
}
