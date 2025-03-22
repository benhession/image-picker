package com.benhession.imagepicker.imageprocessor.controller;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.exception.MetaDataRetrievalException;
import com.benhession.imagepicker.common.exception.SecurityException;
import com.benhession.imagepicker.common.security.UserInfo;
import static com.benhession.imagepicker.common.security.UserInfo.EDITOR_ROLE;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.CROPPED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import static com.benhession.imagepicker.data.service.ObjectStorageService.ORIGINAL_FILES_PREFIX;
import com.benhession.imagepicker.imageprocessor.service.FileDataRetrievalService;
import com.benhession.imagepicker.imageprocessor.service.ImageClassifierService;
import com.benhession.imagepicker.imageprocessor.service.ImageCreationService;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageProcessingController {

    private static final List<ImageProcessingStage> VALID_PROCESSING_STAGES = List.of(ORIGINAL_UPLOADED, CROPPED);

    private final UserInfo userInfo;
    private final FileDataRetrievalService fileDataRetrievalService;
    private final ImageMetaDataService imageMetaDataService;
    private final Logger log;
    private final ImageCreationService imageCreationService;
    private final ImageClassifierService imageClassifierService;

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

            if (!VALID_PROCESSING_STAGES.contains(imageMetadata.getStatus().stage())) {
                throw new ImageProcessingException(
                    "Image metadata is not in the correct stage for processing for imageId: "
                        + imageMetadata.getId().toString());
            }
            imageMetadata = imageMetaDataService.setImageProcessingStage(imageMetadata, PROCESSING);

            // start get labels from classifier async
            Future<List<String>> imageClassifiersFuture = imageClassifierService.findClassifiersAsync(
                ORIGINAL_FILES_PREFIX + message.getFileDataKey());

            // create images
            var fileData = fileDataRetrievalService.retrieve(message.getFileDataKey())
                .orElseThrow(() -> new ImageProcessingException("File not found for key: "
                    + message.getFileDataKey()));
            fileData.setImageType(imageMetadata.getType().name());
            imageCreationService.createNewImages(fileData, imageMetadata);

            // this will cancel the process if the timeout is reached
            imageMetadata = imageMetaDataService.getImageMetaData(imageMetadata.getId()).orElseThrow();

            // get labels from classifier
            try {
                List<String> classifiersResult = imageClassifiersFuture.get(2L, TimeUnit.MINUTES);
                imageMetadata.setAiTags(classifiersResult);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                log.error("Unable to get classifiers for image {}", imageMetadata.getParentKey(), e);
            }

            if (imageMetadata.getStatus().stage().equals(PROCESSING)) {
                imageMetaDataService.setImageProcessingStage(imageMetadata, PROCESSING_COMPLETE);
            }

        } catch (SecurityException | ImageProcessingException e) {
            log.error(e.getMessage(), e);
            imageMetaDataService.setImageProcessingStage(imageMetadata, PROCESSING_FAILED);
        }
    }
}
