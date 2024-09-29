package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.api.util.FilenameUtil;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.common.model.ImageHeightWidth;
import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.common.util.MimeTypeUtil;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageType;
import com.benhession.imagepicker.data.repository.ImageMetaDataRepository;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.benhession.imagepicker.common.model.ImageSize.values;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageCreationService {

    private final ImageSizeService imageSizeService;
    private final FilenameUtil filenameUtil;
    private final ObjectStorageService objectStorageService;
    private final ImageMetaDataRepository imageMetadataRepository;
    private final MimeTypeUtil mimeTypeUtil;

    public ImageMetadata createNewImages(final FileData fileData) {
        final ImageType imageType = ImageType.valueOf(fileData.imageType());

        List<ImageUploadDto> images = Arrays.stream(values())
            .map(imageSize -> new ImageUploadDto(
                filenameUtil.getFilename(fileData.filename(), imageType, imageSize),
                fileData.mimeType(),
                resizeAsNewImage(fileData, imageSize, fileData.mimeType())))
            .toList();

        String parentKey = objectStorageService.uploadFiles(fileData.filename(), images);

        var imageMetaData = ImageMetadata.builder()
            .parentKey(parentKey)
            .filename(fileData.filename())
            .type(imageType)
            .build();

        imageMetadataRepository.persist(imageMetaData);

        return imageMetadataRepository.findByParentKey(parentKey)
            .orElseThrow(
                () -> new ImageProcessingException("Error retrieving metadata for key: " + parentKey));
    }

    private byte[] resizeAsNewImage(FileData fileData, ImageSize imageSize,
        String mimeType) {
        File file = fileData.data();
        ImageType imageType = ImageType.valueOf(fileData.imageType());
        var heightWidth = imageSizeService.findImageHeightWidth(imageType, imageSize);

        try {
            if (mimeType.equals("image/gif")) {
                return resizeGif(file, heightWidth);
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                var bufferedImage = Thumbnails.of(file)
                    .width(heightWidth.getWidth())
                    .height(heightWidth.getHeight())
                    .asBufferedImage();

                ImageIO.write(bufferedImage, mimeTypeUtil.mimeTypeToFileFormat(mimeType), outputStream);
                return outputStream.toByteArray();
            }

        } catch (IOException e) {
            throw new ImageProcessingException(String.format("Error resizing file: %s to %s %s",
                file.getName(), imageSize, imageType));
        }
    }

    private byte[] resizeGif(File file, ImageHeightWidth imageHeightWidth) throws IOException {

        try (InputStream inputStream = new FileInputStream(file)) {
            try (var outputStream = new ByteArrayOutputStream()) {
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
}
