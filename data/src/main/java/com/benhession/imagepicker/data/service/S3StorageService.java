package com.benhession.imagepicker.data.service;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.data.dto.ImageUploadDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
@ApplicationScoped
public class S3StorageService implements ObjectStorageService {

    private final String ORIGINAL_FILES_PREFIX = "originalFileData/";

    private final S3Client s3Client;

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
    public FileData getOriginalFileData(String fileDataKey) {
        var byteStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(ORIGINAL_FILES_PREFIX + fileDataKey)
            .build());

        try (var objectInputStream = new ObjectInputStream(byteStream)) {
            return  (FileData) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ImageProcessingException("Error reading original file data from s3", e);
        }
    }

    @Override
    public void uploadOriginalFileData(FileData fileData, String fileDataKey) {

        try (var byteArrayOutputStream = new ByteArrayOutputStream();
            var objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(fileData);
            objectOutputStream.flush();

            s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(ORIGINAL_FILES_PREFIX + fileDataKey)
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
