package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.api.dto.ImageUploadDto;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class S3StorageService implements ObjectStorageService {

    private final S3Client s3Client;

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

        return parentKey;
    }

    @Override
    public String getBaseResourcePath(String parentKey) {
        URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
          .bucket(bucketName)
          .key(parentKey)
          .build());

        return url.toString();
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
