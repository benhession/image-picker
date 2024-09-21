package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.api.dto.ImageUploadDto;
import com.benhession.imagepicker.api.dto.ObjectUploadForm;
import com.benhession.imagepicker.api.util.FilenameUtil;
import com.benhession.imagepicker.api.util.MimeTypeUtil;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.ImageHeightWidth;
import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageType;
import com.benhession.imagepicker.data.repository.ImageMetaDataRepository;
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

    public ImageMetadata createNewImages(final ObjectUploadForm objectUploadForm) {
        final ImageType imageType = ImageType.valueOf(objectUploadForm.getImageType());

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
          .orElseThrow(
            () -> new ImageProcessingException("Error retrieving metadata for key: " + parentKey));
    }

    private byte[] resizeAsNewImage(ObjectUploadForm objectUploadForm, ImageSize imageSize,
                                    String mimeType) {
        File file = objectUploadForm.getData();
        ImageType imageType = ImageType.valueOf(objectUploadForm.getImageType());
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
