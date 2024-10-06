package com.benhession.imagepicker.common.sqs;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public class ImageCreationMessage extends BaseSqsMessage {
    private String metaDataId;
    private String fileDataKey;
}
