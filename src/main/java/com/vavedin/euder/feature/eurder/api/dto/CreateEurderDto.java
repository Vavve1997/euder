package com.vavedin.euder.feature.eurder.api.dto;

import java.util.List;

public class CreateEurderDto {

    private List<CreateItemGroupDto> itemGroupDtos;

    public CreateEurderDto(List<CreateItemGroupDto> itemGroupDtos) {
        this.itemGroupDtos = itemGroupDtos;
    }

    public CreateEurderDto() {
    }

    public List<CreateItemGroupDto> getItemGroupDtos() {
        return itemGroupDtos;
    }
}
