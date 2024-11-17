package com.benhession.imagepicker.data.service;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

@RequiredArgsConstructor
@ApplicationScoped
public class S3StorageService implements ObjectStorageService {

    private static final String ORIGINAL_FILES_PREFIX = "originalFileData/";
    public static final String FILENAME_TAG = "filename";
    public static final String MIME_TYPE_TAG = "mimeType";

    private final S3Client s3Client;
    private final Logger logger;

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @Override
    public void uploadFiles(List<ImageUploadDto> images, String parentKey) {

        List<String> uploadedKeys = new ArrayList<>();

        for (ImageUploadDto imageDto : images) {
            String key = String.format("%s/%s", parentKey, imageDto.filename());

            var putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(imageDto.mimetype())
                .build();

            try {
                uploadImage(putObjectRequest, imageDto);
                uploadedKeys.add(key);
            } catch (AwsServiceException | SdkClientException e) {
                deleteUploadedFiles(uploadedKeys);
                throw new ImageProcessingException("Error uploading file to S3 for filename: "
                    + imageDto.filename(), e);
            } catch (IOException e) {
                deleteUploadedFiles(uploadedKeys);
                throw new ImageProcessingException("Error converting file to Byte Array Stream for filename: "
                    + imageDto.filename(), e);
            }
        }
    }

    @Override
    public String getBaseResourcePath(String parentKey) {
        URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
            .bucket(bucketName)
            .key(parentKey)
            .build());

        return url.toString();
    }

    @Override
    public ImageUploadDto getOriginalFileData(String fileDataKey) {
        try (var objectByteStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(ORIGINAL_FILES_PREFIX + fileDataKey)
                .build())) {

            var taggingResponse = s3Client.getObjectTagging(GetObjectTaggingRequest.builder()
                    .bucket(bucketName)
                    .key(ORIGINAL_FILES_PREFIX + fileDataKey)
                .build());

            var filename = taggingResponse.tagSet()
                .stream()
                .filter(tag -> tag.key().equals(FILENAME_TAG))
                .findFirst()
                .map(Tag::value)
                .orElseThrow(() -> new ImageProcessingException("Filename tag not found for filename: " + fileDataKey));

            var mimeType = taggingResponse.tagSet()
                .stream()
                .filter(tag -> tag.key().equals(MIME_TYPE_TAG))
                .findFirst()
                .map(Tag::value)
                .orElseThrow(() -> new ImageProcessingException("MimeType tag not found for filename: " + fileDataKey));

           return ImageUploadDto.builder()
               .image(objectByteStream.readAllBytes())
               .filename(filename)
               .mimetype(mimeType)
               .build();

        } catch (S3Exception | IOException e) {
            logger.error(e.getMessage(), e);
            throw new ImageProcessingException("Error reading original file data from s3", e);
        }
    }

    @Override
    public void uploadOriginalFileData(ImageUploadDto imageUploadDto, String fileDataKey) {

        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
            byteArrayOutputStream.write(imageUploadDto.image());
            var tagging = Tagging.builder()
                    .tagSet(
                        Tag.builder()
                            .key(FILENAME_TAG)
                            .value(imageUploadDto.filename())
                            .build(),
                        Tag.builder()
                            .key(MIME_TYPE_TAG)
                            .value(imageUploadDto.mimetype())
                            .build())
                    .build();

            s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(ORIGINAL_FILES_PREFIX + fileDataKey)
                .tagging(tagging)
                .build(),
                RequestBody.fromBytes(byteArrayOutputStream.toByteArray()));

        } catch (IOException e) {
            throw new ImageProcessingException("Error uploading original file data to S3", e);
        }
    }

    private void uploadImage(PutObjectRequest putObjectRequest, ImageUploadDto imageDto)
        throws AwsServiceException, SdkClientException, IOException {

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageDto.image())) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, imageDto.image().length));
        }
    }

    private void deleteUploadedFiles(List<String> fileKeys) {

        for (String key : fileKeys) {
            var deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            s3Client.deleteObject(deleteObjectRequest);
        }
    }

}
