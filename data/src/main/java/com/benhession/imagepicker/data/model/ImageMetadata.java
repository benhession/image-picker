package com.benhession.imagepicker.data.model;

import com.benhession.imagepicker.common.model.ImageType;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

@MongoEntity(collection = "images")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ImageMetadata {

    @BsonId
    @EqualsAndHashCode.Exclude
    private ObjectId id;
    private String parentKey;
    private String filename;
    private ImageType type;
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<String> aiTags = new ArrayList<>();
    @EqualsAndHashCode.Exclude
    private ImageProcessingStatus status;
}
