package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.feature.eurder.api.dto.CreateEurderDto;
import com.vavedin.euder.feature.eurder.api.dto.EurderDto;
import com.vavedin.euder.feature.eurder.domain.Eurder;
import com.vavedin.euder.feature.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class EurderMapper {

    private final ItemGroupMapper itemGroupMapper;

    public EurderMapper(ItemGroupMapper itemGroupMapper) {
        this.itemGroupMapper = itemGroupMapper;
    }

    public EurderDto toDto(Eurder eurder) {
        return new EurderDto(eurder.getId(), eurder.getCustomerId(), itemGroupMapper.toDto(eurder.getItemGroups()), eurder.calculateTotalPrice());
    }

}
