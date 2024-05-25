package com.benhession.imagepicker.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@MongoEntity(collection = "images")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ImageMetadata {

    private ObjectId id;
    private String parentKey;
    private String filename;
    private ImageType type;
    private List<String> tags;
}

