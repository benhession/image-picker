package com.benhession.imagepicker.common.sqs;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public abstract class BaseSqsMessage {
    private String authJwt;
}
