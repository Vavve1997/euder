package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.feature.eurder.api.dto.EurderDto;
import com.vavedin.euder.feature.eurder.api.dto.EurderReportDto;
import com.vavedin.euder.feature.eurder.domain.Eurder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Component
public class EurderMapper {

    private final ItemGroupMapper itemGroupMapper;

    public EurderMapper(ItemGroupMapper itemGroupMapper) {
        this.itemGroupMapper = itemGroupMapper;
    }

    public EurderDto toDto(Eurder eurder) {
        return new EurderDto(eurder.getId(), eurder.getCustomerId(), itemGroupMapper.toDto(eurder.getItemGroups()), eurder.calculateTotalPrice());
    }

    public List<EurderDto> toDto(Collection<Eurder> eurders) {
        return eurders.stream().map(this::toDto).toList();
    }

    public EurderReportDto toReportDto(List<Eurder> eurders) {
        return new EurderReportDto(toDto(eurders),
                eurders.stream()
                .map(Eurder::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
