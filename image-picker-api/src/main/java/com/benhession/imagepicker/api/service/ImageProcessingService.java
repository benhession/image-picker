package com.benhession.imagepicker.api.service;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.CROPPED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.SENT_TO_CROP;

import com.benhession.imagepicker.api.sqs.ImageCroppingQueueService;
import com.benhession.imagepicker.api.sqs.ImageProcessingQueueService;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.ImageCropProperties;
import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.common.sqs.ImageCropMessage;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageProcessingService {

    private static final String CROPPED_SUFFIX = "-cropped";

    private final ImageProcessingQueueService imageProcessingQueueService;
    private final ImageMetaDataService imageMetaDataService;
    private final ObjectStorageService objectStorageService;
    private final ImageValidationService imageValidationService;
    private final CropValidationService cropValidationService;
    private final ImageCroppingQueueService imageCroppingQueueService;
    private final Logger logger;

    public ImageMetadata validateAndProcessUploadedImage(ImageType imageType, ImageMetadata imageMetadata) {
        try {
            String fileDataKey = imageMetadata.getParentKey();
            if (imageMetadata.getStatus().stage().equals(CROPPED)) {
                fileDataKey += CROPPED_SUFFIX;
            }

            var fileData = objectStorageService.getOriginalFileData(fileDataKey);
            imageValidationService.validateInputImage(fileData.getData(), imageType);

            imageMetadata.setType(imageType);
            imageMetadata.setStatus(ImageProcessingStatus.of(ORIGINAL_UPLOADED));
            imageMetadata = persistAndFindMetadata(imageMetadata);

            var imageCreationMessage = ImageCreationMessage.builder()
                .fileDataKey(fileDataKey)
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

    public ImageMetadata validateAndCropOriginalImage(ImageMetadata imageMetadata,
        ImageCropProperties imageCropProperties) {

        try {
            var fileData = objectStorageService.getOriginalFileData(imageMetadata.getParentKey());

            try (var byteArrayInputStream = new ByteArrayInputStream(fileData.getData())) {
                BufferedImage originalImage = ImageIO.read(byteArrayInputStream);
                cropValidationService.validateCrop(imageCropProperties, originalImage);

                imageMetadata.setType(imageCropProperties.imageType());
                imageMetadata.setStatus(ImageProcessingStatus.of(SENT_TO_CROP));
                imageMetadata = persistAndFindMetadata(imageMetadata);

                var imageCropMessage = ImageCropMessage.builder()
                    .imageCropProperties(imageCropProperties)
                    .metaDataId(imageMetadata.getId().toString())
                    .fileDataKey(imageMetadata.getParentKey())
                    .build();

                imageCroppingQueueService.sendMessage(imageCropMessage);
                return imageMetadata;

            } catch (IOException e) {
                throw new ImageProcessingException("Error reading image bytes", e);
            }
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
