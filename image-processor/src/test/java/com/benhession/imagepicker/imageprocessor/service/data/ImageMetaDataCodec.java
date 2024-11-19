package com.benhession.imagepicker.imageprocessor.service.data;

import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.data.model.ImageMetadata;
import com.benhession.imagepicker.data.model.ImageProcessingStage;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import java.time.Instant;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

public class ImageMetaDataCodec implements Codec<ImageMetadata> {

    @Override
    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistanceCheck")
    public ImageMetadata decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readStartDocument();
        ObjectId objectId = bsonReader.readObjectId("_id");
        String parentKey = bsonReader.readString("parentKey");
        String filename = bsonReader.readString("filename");
        ImageType imageType = ImageType.valueOf(bsonReader.readString("imageType"));
        bsonReader.readName("status");
        bsonReader.readStartDocument();
        Instant changedAt = Instant.ofEpochSecond(bsonReader.readDateTime("changedAt"));
        ImageProcessingStatus imageProcessingStatus = new ImageProcessingStatus(changedAt,
            ImageProcessingStage.valueOf(bsonReader.readString("stage")));
        bsonReader.readEndDocument();
        bsonReader.readEndDocument();

        return ImageMetadata.builder()
            .id(objectId)
            .parentKey(parentKey)
            .filename(filename)
            .type(imageType)
            .status(imageProcessingStatus)
            .build();
    }

    @Override
    public void encode(BsonWriter bsonWriter, ImageMetadata imageMetadata, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeObjectId("_id", imageMetadata.getId() != null ? imageMetadata.getId() : new ObjectId());
        bsonWriter.writeString("parentKey", imageMetadata.getParentKey());
        bsonWriter.writeString("filename", imageMetadata.getFilename());
        bsonWriter.writeString("type", imageMetadata.getFilename());
        bsonWriter.writeStartDocument("status");
        bsonWriter.writeDateTime("statusChangedAt", imageMetadata.getStatus().statusChangedAt().getEpochSecond());
        bsonWriter.writeString("stage", imageMetadata.getStatus().stage().toString());
        bsonWriter.writeEndDocument();
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<ImageMetadata> getEncoderClass() {
        return ImageMetadata.class;
    }
}
