package com.benhession.imagepicker.api.service;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_FAILED;

import com.benhession.imagepicker.api.sqs.ImageProcessingQueueService;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.common.util.FilenameUtil;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageProcessingService {

    private final ImageProcessingQueueService imageProcessingQueueService;
    private final ImageMetaDataService imageMetaDataService;
    private final ObjectStorageService objectStorageService;
    private final FilenameUtil filenameUtil;
    private final Logger logger;

    public ImageMetadata processImage(FileData fileData) {

        String parentKey = filenameUtil.generateParentKey(fileData.filename());
        ImageMetadata imageMetadata = ImageMetadata.builder()
            .filename(fileData.filename())
            .type(ImageType.valueOf(fileData.imageType()))
            .parentKey(parentKey)
            .build();

        try {
            ImageUploadDto imageUploadDto;
            try (var inputStream = new ByteArrayInputStream(fileData.data())) {
                imageUploadDto = ImageUploadDto.builder()
                    .image(inputStream.readAllBytes())
                    .filename(fileData.filename())
                    .mimetype(fileData.mimeType())
                    .build();
            } catch (IOException e) {
                throw new ImageProcessingException("Unable to get image byte array", e);
            }

            objectStorageService.uploadOriginalFileData(imageUploadDto, parentKey);
            imageMetadata.setStatus(ImageProcessingStatus.of(ORIGINAL_UPLOADED));
            imageMetadata = persistAndFindMetadata(imageMetadata);

            var imageCreationMessage = ImageCreationMessage.builder()
                .fileDataKey(parentKey)
                .metaDataId(imageMetadata.getId().toString())
                .build();

            imageProcessingQueueService.sendMessage(imageCreationMessage);

            return imageMetadata;

        } catch (ImageProcessingException e) {
            logger.error("Unable to process image " + fileData.filename(), e);
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
