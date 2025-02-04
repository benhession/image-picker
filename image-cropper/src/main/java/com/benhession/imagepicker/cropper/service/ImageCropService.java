package com.benhession.imagepicker.cropper.service;

import static com.benhession.imagepicker.data.model.ImageProcessingStage.CROPPED;
import static com.benhession.imagepicker.data.model.ImageProcessingStage.CROPPING;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.model.ImageCropProperties;
import com.benhession.imagepicker.common.service.ImageSizeService;
import com.benhession.imagepicker.common.util.GifUtil;
import com.benhession.imagepicker.common.util.MimeTypeUtil;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageCropService {

    private static final String CROPPED_SUFFIX = "-cropped";

    private final ImageSizeService imageSizeService;
    private final MimeTypeUtil mimeTypeUtil;
    private final GifUtil gifUtil;
    private final ObjectStorageService objectStorageService;
    private final ImageMetaDataService imageMetaDataService;

    public void cropOriginalImage(FileData fileData, ImageMetadata imageMetadata,
        ImageCropProperties imageCropProperties) {

        imageMetadata = imageMetaDataService.setImageProcessingStage(imageMetadata, CROPPING);

        try (var byteArrayInputStream = new ByteArrayInputStream(fileData.getData())) {
            BufferedImage originalImage = ImageIO.read(byteArrayInputStream);

            byte[] croppedImage;
            if (fileData.getMimeType().equals("image/gif")) {
                croppedImage = cropGif(fileData.getData(), imageCropProperties);
            } else {
                croppedImage = cropStandardImage(originalImage, imageCropProperties, fileData.getMimeType());
            }

            ImageUploadDto imageUploadDto = ImageUploadDto.builder()
                .image(croppedImage)
                .filename(fileData.getFilename())
                .mimetype(fileData.getMimeType())
                .build();

            objectStorageService.uploadOriginalFileData(imageUploadDto, imageMetadata.getParentKey() + CROPPED_SUFFIX);
            imageMetaDataService.setImageProcessingStage(imageMetadata, CROPPED);

        } catch (IOException e) {
            throw new ImageProcessingException("Failed to process original image data", e);
        }
    }

    private byte[] cropStandardImage(BufferedImage originalImage, ImageCropProperties imageCropProperties,
        String mimeType) {

        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
            BufferedImage croppedImage = originalImage.getSubimage(
                imageCropProperties.baseCoordinate().x(),
                imageCropProperties.baseCoordinate().y(),
                imageCropProperties.width(),
                imageSizeService.calculateImageHeight(imageCropProperties.width(), imageCropProperties.imageType())
            );

            ImageIO.write(croppedImage, mimeTypeUtil.mimeTypeToFileFormat(mimeType), byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            throw new ImageProcessingException("Failed to process standard image data", e);
        } catch (RasterFormatException e) {
            throw new ImageProcessingException("Failed to crop standard image", e);
        }
    }

    private byte[] cropGif(byte[] data, ImageCropProperties imageCropProperties) throws IOException {
        return gifUtil.alterGifFrames(data, frame -> frame.getSubimage(
            imageCropProperties.baseCoordinate().x(),
            imageCropProperties.baseCoordinate().y(),
            imageCropProperties.width(),
            imageSizeService.calculateImageHeight(imageCropProperties.width(), imageCropProperties.imageType())
        ));
    }
}
