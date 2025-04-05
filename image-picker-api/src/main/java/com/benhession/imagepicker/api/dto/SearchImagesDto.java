package com.benhession.imagepicker.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchImagesDto {

    @NotNull
    private String searchTerm;
    @NotNull
    private Integer page;
    @NotNull
    private Integer size;
}
