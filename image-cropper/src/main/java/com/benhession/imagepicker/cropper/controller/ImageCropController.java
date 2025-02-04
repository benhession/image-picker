package com.benhession.imagepicker.cropper.controller;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;

import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException.ErrorMessage;
import com.benhession.imagepicker.common.exception.BadRequestException;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.exception.MetaDataRetrievalException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.sqs.ImageCropMessage;
import com.benhession.imagepicker.cropper.service.ImageCropService;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.bson.types.ObjectId;

@ApplicationScoped
@RequiredArgsConstructor
@JBossLog
public class ImageCropController {

    private final ImageCropService imageCropService;
    private final ImageMetaDataService imageMetaDataService;
    private final ObjectStorageService objectStorageService;

    public void handleCropQueueRequest(ImageCropMessage imageCropMessage) {

        var imageMetadata = imageMetaDataService.getImageMetaData(new ObjectId(imageCropMessage.getMetaDataId()))
            .orElseThrow(() -> new MetaDataRetrievalException(
                "Unable to find metadata for id: " + imageCropMessage.getMetaDataId()));

        try {
            if (!imageMetadata.getStatus().stage().equals(ImageProcessingStage.SENT_TO_CROP)) {
                throw new BadRequestException(List.of(ErrorMessage.builder()
                    .message("Expected image to have a status of ORIGINAL_UPLOADED but was "
                        + imageMetadata.getStatus().stage())
                    .build()));
            }

            FileData fileData = objectStorageService.getOriginalFileData(imageMetadata.getParentKey());
            imageCropService.cropOriginalImage(fileData, imageMetadata, imageCropMessage.getImageCropProperties());

        } catch (ImageProcessingException e) {
            log.error(e.getMessage(), e);
            imageMetaDataService.setImageProcessingStage(imageMetadata, PROCESSING_FAILED);
        } catch (BadRequestException e) {
            String errorMessages = e.getErrorMessages().stream()
                .map(ErrorMessage::message)
                .collect(Collectors.joining(", "));
            log.error(errorMessages, e);
            imageMetaDataService.setImageProcessingStage(imageMetadata, PROCESSING_FAILED);
        }
    }
}
