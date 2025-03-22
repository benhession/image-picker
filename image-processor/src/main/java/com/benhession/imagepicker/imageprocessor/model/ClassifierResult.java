package com.benhession.imagepicker.imageprocessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RegisterForReflection
public class ClassifierResult {

    private List<String> categories;
    private String label;
    @JsonProperty("label_id")
    private int labelId;
    private double score;
}
