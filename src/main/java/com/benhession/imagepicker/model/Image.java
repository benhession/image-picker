package com.benhession.imagepicker.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;

@MongoEntity(collection = "images")
public class Image {

    @BsonId
    private String id;
    private String name;
    private ImageType type;
    private List<String> tags;
}

