package com.benhession.imagepicker.data.service;

import com.benhession.imagepicker.data.dto.ImageUploadDto;
import com.benhession.imagepicker.data.dto.PreSignedUploadDto;
import java.util.List;

public interface ObjectStorageService {
    void uploadFiles(List<ImageUploadDto> images, String parentKey);

    String getBaseResourcePath(String parentKey);

    ImageUploadDto getOriginalFileData(String parentKey);

    void uploadOriginalFileData(ImageUploadDto imageUploadDto, String fileDataKey);

    void deleteImagesByParentKey(String parentKey);

    PreSignedUploadDto getPreSignedUrl(ImageUploadDto imageUploadDto, String fileDataKey);
}
