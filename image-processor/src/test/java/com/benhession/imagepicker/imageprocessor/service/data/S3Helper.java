package com.benhession.imagepicker.imageprocessor.service.data;

import static com.benhession.imagepicker.data.service.S3StorageService.FILENAME_TAG;
import static com.benhession.imagepicker.data.service.S3StorageService.MIME_TYPE_TAG;

import com.benhession.imagepicker.common.model.FileData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import lombok.experimental.UtilityClass;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

@UtilityClass
public class S3Helper {
    private static final String ORIGINAL_FILES_PREFIX = "originalFileData/";
    private static final String BUCKET_NAME = "test-bucket";
    private static final URI LOCALSTACK_ENDPOINT = URI.create("http://localhost:56100");

    private static final S3Client S3_CLIENT = S3Client.builder()
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.builder()
            .accessKeyId("112233445566")
            .secretAccessKey("test-key")
            .build()))
        .endpointOverride(LOCALSTACK_ENDPOINT)
        .forcePathStyle(true)
        .region(Region.US_EAST_1)
        .build();

    public static void uploadOriginalFile(FileData fileData, String fileDataKey) {
        try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
            byteArrayOutputStream.write(fileData.data());
            var tagging = Tagging.builder()
                .tagSet(
                    Tag.builder()
                        .key(FILENAME_TAG)
                        .value(fileData.filename())
                        .build(),
                    Tag.builder()
                        .key(MIME_TYPE_TAG)
                        .value(fileData.mimeType())
                        .build())
                .build();

            S3_CLIENT.putObject(PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(ORIGINAL_FILES_PREFIX + fileDataKey)
                    .tagging(tagging)
                    .build(),
                RequestBody.fromBytes(byteArrayOutputStream.toByteArray()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ListObjectsV2Response listS3Objects(String parentKey) {
        return S3_CLIENT.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .prefix(parentKey)
            .build());
    }
}
