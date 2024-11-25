package com.benhession.imagepicker.common.sqs;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageCreationMessage extends BaseSqsMessage {
    private String metaDataId;
    private String fileDataKey;
}
