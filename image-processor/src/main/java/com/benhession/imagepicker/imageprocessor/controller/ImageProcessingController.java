package com.benhession.imagepicker.imageprocessor.controller;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;
import static com.benhession.imagepicker.imageprocessor.security.UserInfo.EDITOR_ROLE;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.exception.SecurityException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.imageprocessor.exception.MetaDataRetrievalException;
import com.benhession.imagepicker.imageprocessor.security.UserInfo;
import com.benhession.imagepicker.imageprocessor.service.FileDataRetrievalService;
import com.benhession.imagepicker.imageprocessor.service.ImageCreationService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageProcessingController {

    private final UserInfo userInfo;
    private final FileDataRetrievalService fileDataRetrievalService;
    private final ImageMetaDataService imageMetaDataService;
    private final Logger log;
    private final ImageCreationService imageCreationService;

    public void handleProcessingRequest(ImageCreationMessage message) {

        ImageMetadata imageMetadata;

        try {
            imageMetadata = imageMetaDataService.getImageMetaData(new ObjectId(message.getMetaDataId()))
                .orElseThrow(() -> new MetaDataRetrievalException("Image metadata not found for imageId: "
                    + message.getMetaDataId()));
        } catch (MetaDataRetrievalException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        try {
            if (!userInfo.isEditor()) {
                throw new SecurityException(
                    String.format("User: %s must have role %s to process images", userInfo.getUserName(), EDITOR_ROLE));
            }

            var imageUploadDto = fileDataRetrievalService.retrieve(message.getFileDataKey())
                .orElseThrow(() -> new ImageProcessingException("ImageUploadDto not found for key: "
                    + message.getFileDataKey()));

            FileData fileData = FileData.builder()
                .imageType(imageMetadata.getType().name())
                .mimeType(imageUploadDto.mimetype())
                .data(imageUploadDto.image())
                .filename(imageUploadDto.filename())
                .build();

            imageCreationService.createNewImages(fileData, imageMetadata);

        } catch (SecurityException | ImageProcessingException e) {
            log.error(e.getMessage(), e);
            imageMetaDataService.setImageProcessingStage(imageMetadata, PROCESSING_FAILED);
        }
    }
}
