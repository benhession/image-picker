package com.benhession.imagepicker.imageprocessor.service.data;

import com.benhession.imagepicker.common.model.FileData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

@UtilityClass
public class S3Helper {
    private static final String ORIGINAL_FILES_PREFIX = "originalFileData/";
    private static final String BUCKET_NAME = "test-bucket";
    private static final URI LOCALSTACK_ENDPOINT = URI.create(System.getenv("LOCALSTACK_S3_ENDPOINT"));

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
        try (var byteArrayOutputStream = new ByteArrayOutputStream();
            var objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(fileData);
            objectOutputStream.flush();

            String key = ORIGINAL_FILES_PREFIX + fileDataKey;

            S3_CLIENT.putObject(PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
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
