package com.benhession.imagepicker.imageprocessor.service;

import static com.benhession.imagepicker.common.model.ImageSize.values;
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
import com.benhession.imagepicker.common.util.MimeTypeUtil;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.service.ImageMetaDataService;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageCreationService {

    private final ImageSizeService imageSizeService;
    private final FilenameUtil filenameUtil;
    private final ObjectStorageService objectStorageService;
    private final ImageMetaDataService imageMetaDataService;
    private final MimeTypeUtil mimeTypeUtil;

    public void createNewImages(final FileData fileData, ImageMetadata imageMetadata)
        throws ImageProcessingException {

        if (!imageMetadata.getStatus().stage().equals(ORIGINAL_UPLOADED)) {
            throw new ImageProcessingException("Image metadata is not in the processing stage for imageId: "
                + imageMetadata.getId().toString());
        }

        imageMetadata = imageMetaDataService.setImageProcessingStage(imageMetadata, PROCESSING);

        final ImageType imageType = ImageType.valueOf(fileData.imageType());

        List<ImageUploadDto> images = Arrays.stream(values())
            .map(imageSize -> ImageUploadDto.builder()
                .filename(filenameUtil.getFilename(fileData.filename(), imageType, imageSize))
                .mimetype(fileData.mimeType())
                .image(resizeAsNewImage(fileData, imageSize, fileData.mimeType()))
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
        ImageType imageType = ImageType.valueOf(fileData.imageType());
        var heightWidth = imageSizeService.findImageHeightWidth(imageType, imageSize);

        try {
            if (mimeType.equals("image/gif")) {
                return resizeGif(fileData.data(), heightWidth);
            }

            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileData.data());
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
                fileData.filename(), imageSize, imageType));
        }
    }

    private byte[] resizeGif(byte[] data, ImageHeightWidth imageHeightWidth) throws IOException {

        try (InputStream inputStream = new ByteArrayInputStream(data);
            var outputStream = new ByteArrayOutputStream()) {

            List<Integer> delays = new ArrayList<>();
            List<BufferedImage> frames = new LinkedList<>();
            var gifDecoder = new GifDecoder();
            var gifEncoder = new AnimatedGifEncoder();
            gifDecoder.read(inputStream);

            int frameCount = gifDecoder.getFrameCount();
            for (int i = 0; i < frameCount; i++) {
                var newFrame = Thumbnails.of(gifDecoder.getFrame(i))
                    .width(imageHeightWidth.getWidth())
                    .height(imageHeightWidth.getHeight())
                    .asBufferedImage();
                frames.add(newFrame);
                delays.add(gifDecoder.getDelay(i));
            }

            var averageDelay = delays.stream()
                .mapToInt(a -> a)
                .summaryStatistics()
                .getAverage();
            var roundedDelay = Math.toIntExact(Math.round(averageDelay));

            gifEncoder.start(outputStream);
            gifEncoder.setRepeat(gifDecoder.getLoopCount());
            gifEncoder.setDelay(roundedDelay);
            for (var frame : frames) {
                gifEncoder.addFrame(frame);
            }
            gifEncoder.finish();

            return outputStream.toByteArray();
        }
    }
}
