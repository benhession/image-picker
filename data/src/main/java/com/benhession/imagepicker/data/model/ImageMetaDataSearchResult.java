package com.benhession.imagepicker.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageMetaDataSearchResult extends ImageMetadata implements MongoSearchResult {

    private String pointOfReference;
}
