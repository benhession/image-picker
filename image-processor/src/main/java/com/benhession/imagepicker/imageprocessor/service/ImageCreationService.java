package com.benhession.imagepicker.imageprocessor.service;

import static com.benhession.imagepicker.common.model.ImageSize.values;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.CROPPED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.ORIGINAL_UPLOADED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.PROCESSING_COMPLETE;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.model.ImageHeightWidth;
import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.common.service.ImageSizeService;
import com.benhession.imagepicker.common.util.FilenameUtil;
import com.benhession.imagepicker.common.util.GifUtil;
import com.benhession.imagepicker.common.util.MimeTypeUtil;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageCreationService {

    private static final List<ImageProcessingStage> VALID_PROCESSING_STAGES = List.of(ORIGINAL_UPLOADED, CROPPED);

    private final ImageSizeService imageSizeService;
    private final FilenameUtil filenameUtil;
    private final ObjectStorageService objectStorageService;
    private final ImageMetaDataService imageMetaDataService;
    private final MimeTypeUtil mimeTypeUtil;
    private final GifUtil gifUtil;

    public void createNewImages(final FileData fileData, ImageMetadata imageMetadata)
        throws ImageProcessingException {

        if (!VALID_PROCESSING_STAGES.contains(imageMetadata.getStatus().stage())) {
            throw new ImageProcessingException("Image metadata is not in the correct stage for processing for imageId: "
                + imageMetadata.getId().toString());
        }

        imageMetadata = imageMetaDataService.setImageProcessingStage(imageMetadata, PROCESSING);

        final ImageType imageType = ImageType.valueOf(fileData.getImageType());

        List<ImageUploadDto> images = Arrays.stream(values())
            .map(imageSize -> ImageUploadDto.builder()
                .filename(filenameUtil.getFilename(fileData.getFilename(), imageType, imageSize))
                .mimetype(fileData.getMimeType())
                .image(resizeAsNewImage(fileData, imageSize, fileData.getMimeType()))
                .build())
            .toList();

        objectStorageService.uploadFiles(images, imageMetadata.getParentKey());

        // this will cancel the process if the timeout is reached
        imageMetadata = imageMetaDataService.getImageMetaData(imageMetadata.getId()).orElseThrow();

        if (imageMetadata.getStatus().stage().equals(PROCESSING)) {
            imageMetaDataService.setImageProcessingStage(imageMetadata, PROCESSING_COMPLETE);
        }
    }

    private byte[] resizeAsNewImage(FileData fileData, ImageSize imageSize,
        String mimeType) {
        ImageType imageType = ImageType.valueOf(fileData.getImageType());
        var heightWidth = imageSizeService.findImageHeightWidth(imageType, imageSize);

        try {
            if (mimeType.equals("image/gif")) {
                return resizeGif(fileData.getData(), heightWidth);
            }

            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileData.getData());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                var bufferedImage = Thumbnails.of(byteArrayInputStream)
                    .width(heightWidth.getWidth())
                    .height(heightWidth.getHeight())
                    .asBufferedImage();

                ImageIO.write(bufferedImage, mimeTypeUtil.mimeTypeToFileFormat(mimeType), outputStream);
                return outputStream.toByteArray();
            }

        } catch (IOException e) {
            throw new ImageProcessingException(String.format("Error resizing file: %s to %s %s",
                fileData.getFilename(), imageSize, imageType));
        }
    }

    private byte[] resizeGif(byte[] data, ImageHeightWidth imageHeightWidth) throws IOException {
        return gifUtil.alterGifFrames(data, frame -> {
            try {
                return Thumbnails.of(frame)
                    .width(imageHeightWidth.getWidth())
                    .height(imageHeightWidth.getHeight())
                    .asBufferedImage();
            } catch (IOException e) {
                throw new ImageProcessingException("Error resizing gif file", e);
            }
        });
    }
}
