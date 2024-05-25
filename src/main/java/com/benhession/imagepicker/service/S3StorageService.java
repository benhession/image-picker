package com.benhession.imagepicker.service;

import com.benhession.imagepicker.dto.ImageUploadDto;
import com.benhession.imagepicker.exception.ImageProcessingException;
import com.benhession.imagepicker.util.MimeTypeUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class S3StorageService implements ObjectStorageService{

    private final S3Client s3Client;
    private final MimeTypeUtil mimeTypeUtil;

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @Override
    public String uploadFiles(String originalFilename, List<ImageUploadDto> images) {

        String parentKey = String.format("%s-%s", originalFilename, UUID.randomUUID());
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

        return parentKey;
    }

    private void uploadImage(PutObjectRequest putObjectRequest, ImageUploadDto imageDto) throws AwsServiceException,
            SdkClientException, IOException {


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
