package com.benhession.imagepicker.data.service;

import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import java.util.List;

public interface ObjectStorageService {
    void uploadFiles(List<ImageUploadDto> images, String parentKey);

    String getBaseResourcePath(String parentKey);

    ImageUploadDto getOriginalFileData(String parentKey);

    void uploadOriginalFileData(ImageUploadDto imageUploadDto, String fileDataKey);
}
