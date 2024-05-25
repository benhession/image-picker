package com.benhession.imagepicker.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.util.List;

@MongoEntity(collection = "images")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImageMetadata {

    @BsonId
    private ObjectId id;
    private String parentKey;
    private String filename;
    private ImageType type;
    private List<String> tags;
}

