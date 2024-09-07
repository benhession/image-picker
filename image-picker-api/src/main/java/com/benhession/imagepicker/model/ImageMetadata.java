package com.benhession.imagepicker.model;

import io.quarkus.mongodb.panache.common.MongoEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.util.List;

@MongoEntity(collection = "images")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ImageMetadata {

    @BsonId
    @EqualsAndHashCode.Exclude
    private ObjectId id;
    private String parentKey;
    private String filename;
    private ImageType type;
    @EqualsAndHashCode.Exclude
    private List<String> tags;
}
