package com.benhession.imagepicker.api.service;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;

import com.benhession.imagepicker.api.sqs.ImageProcessingQueueService;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageProcessingService {

    private final ImageProcessingQueueService imageProcessingQueueService;
    private final ImageMetaDataService imageMetaDataService;
    private final ObjectStorageService objectStorageService;
    private final ImageValidationService imageValidationService;
    private final Logger logger;

    public ImageMetadata validateAndProcessUploadedImage(ImageType imageType, ImageMetadata imageMetadata) {
        try {
            var fileData = objectStorageService.getOriginalFileData(imageMetadata.getParentKey());
            imageValidationService.validateInputImage(fileData.getData(), imageType);

            imageMetadata.setType(imageType);
            imageMetadata.setStatus(ImageProcessingStatus.of(ORIGINAL_UPLOADED));
            imageMetadata = persistAndFindMetadata(imageMetadata);

            var imageCreationMessage = ImageCreationMessage.builder()
                .fileDataKey(imageMetadata.getParentKey())
                .metaDataId(imageMetadata.getId().toString())
                .build();

            imageProcessingQueueService.sendMessage(imageCreationMessage);
            return imageMetadata;

        } catch (ImageProcessingException e) {
            logger.error("Unable to process image " + imageMetadata.getFilename(), e);
            imageMetadata.setStatus(ImageProcessingStatus.of(PROCESSING_FAILED));
            return persistAndFindMetadata(imageMetadata);
        }
    }

    private ImageMetadata persistAndFindMetadata(ImageMetadata imageMetadata) throws ImageProcessingException {
        imageMetaDataService.persist(imageMetadata);
        return imageMetaDataService.findByParentKey(imageMetadata.getParentKey())
            .orElseThrow(() -> new ImageProcessingException("Unable to save image metadata: parentKey = "
                + imageMetadata.getParentKey()));
    }
}
