package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.feature.eurder.api.dto.ItemGroupDto;
import com.vavedin.euder.feature.eurder.domain.ItemGroup;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;


@Component
public class ItemGroupMapper {


    public ItemGroupDto toDto(ItemGroup itemGroup) {
        return new ItemGroupDto(itemGroup.getItemId(), itemGroup.getItemName(), itemGroup.getUnitPrice(), itemGroup.getAmount(), itemGroup.getShippingDate(), itemGroup.calculateTotalPrice());
    }

    public List<ItemGroupDto> toDto(Collection<ItemGroup> itemGroups) {
        return itemGroups.stream().map(this::toDto).toList();
    }


}
