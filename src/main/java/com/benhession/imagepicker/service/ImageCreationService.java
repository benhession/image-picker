package com.benhession.imagepicker.service;

import com.benhession.imagepicker.dto.ImageUploadDto;
import com.benhession.imagepicker.dto.ObjectUploadForm;
import com.benhession.imagepicker.exception.BadRequestException;
import com.benhession.imagepicker.exception.ImageProcessingException;
import com.benhession.imagepicker.model.ImageMetadata;
import com.benhession.imagepicker.model.ImageSize;
import com.benhession.imagepicker.model.ImageType;
import com.benhession.imagepicker.repository.ImageMetadataRepository;
import com.benhession.imagepicker.util.FilenameUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.benhession.imagepicker.model.ImageSize.values;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImageCreationService {
    private final ImageSizeService imageSizeService;
    private final FilenameUtil filenameUtil;
    private final ObjectStorageService objectStorageService;
    private final ImageMetadataRepository imageMetadataRepository;

    public ImageMetadata createNewImages(final ObjectUploadForm objectUploadForm) {
        //TODO: test this implementation
        final File file = objectUploadForm.getData();
        final ImageType imageType = ImageType.valueOf(objectUploadForm.getImageType());

        validateInputImage(file, imageType);

        List<ImageUploadDto> images = Arrays.stream(values())
                .map(imageSize -> new ImageUploadDto(
                        filenameUtil.getFilename(objectUploadForm.getFilename(), imageType, imageSize),
                        objectUploadForm.getMimetype(),
                        resizeAsNewImage(objectUploadForm, imageSize, objectUploadForm.getMimetype())))
                .toList();

        String parentKey = objectStorageService.uploadFiles(objectUploadForm.getFilename(), images);

        var imageMetaData = ImageMetadata.builder()
                .parentKey(parentKey)
                .filename(objectUploadForm.getFilename())
                .type(imageType)
                .build();

        imageMetadataRepository.persist(imageMetaData);

        return imageMetadataRepository.findByParentKey(parentKey)
                .orElseThrow(() -> new ImageProcessingException("Error retrieving metadata for key: " + parentKey));
    }

    private BufferedImage resizeAsNewImage(ObjectUploadForm objectUploadForm, ImageSize imageSize, String mimeType) {
        File file = objectUploadForm.getData();
        ImageType imageType = ImageType.valueOf(objectUploadForm.getImageType());
        var heightWidth = imageSizeService.findImageHeightWidth(imageType, imageSize);

        try {
            return Thumbnails.of(file)
                    .width(heightWidth.getWidth())
                    .height(heightWidth.getHeight())
                    .asBufferedImage();

        } catch (IOException e) {
            throw new ImageProcessingException(String.format("Error resizing file: %s to %s %s",
                    file.getName(), imageSize, imageType));
        }
    }

    private void validateInputImage(File file, ImageType imageType) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            List<BadRequestException.ErrorMessage> errorMessages = new ArrayList<>();
            BigDecimal expectedAspectRatio = imageSizeService.findAspectRatio(imageType);
            BigDecimal actualAspectRatio = calculateAspectRatio(bufferedImage.getWidth(), bufferedImage.getHeight());
            int minWidth = imageSizeService.findMinWidth(imageType);

            if (!actualAspectRatio.equals(expectedAspectRatio)) {
                errorMessages.add(BadRequestException.ErrorMessage.builder()
                        .message(String.format("Expected aspect ratio for image type: %s to be %s, but was %s",
                                imageType, expectedAspectRatio.floatValue(), actualAspectRatio.floatValue()))
                        .build());
            }

            if (bufferedImage.getWidth() < minWidth) {
                errorMessages.add(BadRequestException.ErrorMessage.builder()
                        .message(String.format("Expected width of %s image to be more that %s, but was %s",
                                imageType, minWidth, bufferedImage.getWidth()))
                        .build());
            }

            if (!errorMessages.isEmpty()) {
                throw new BadRequestException(errorMessages);
            }

        } catch (IOException e) {
            throw new BadRequestException(List.of(BadRequestException.ErrorMessage.builder()
                    .message("Unable to read file data")
                    .build()));
        }
    }

    private BigDecimal calculateAspectRatio(int width, int height) {
        var widthBd = new BigDecimal(Integer.toString(width));
        var heightBd = new BigDecimal(Integer.toString(height));

        return widthBd.divide(heightBd, 2, RoundingMode.HALF_UP);
    }

}
