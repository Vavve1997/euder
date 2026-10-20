package com.vavedin.euder.feature.eurder.api;

import com.vavedin.euder.feature.eurder.api.dto.CreateItemGroupDto;
import com.vavedin.euder.feature.eurder.api.dto.ItemGroupDto;
import com.vavedin.euder.feature.eurder.domain.ItemGroup;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@Component
public class ItemGroupMapper {

    private final ItemRepository itemRepository;

    public ItemGroupMapper(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemGroupDto toDto(ItemGroup itemGroup) {
        return new ItemGroupDto(itemGroup.getItemId(), itemGroup.getItemName(), itemGroup.getUnitPrice(), itemGroup.getAmount(), itemGroup.getShippingDate(), itemGroup.calculateTotalPrice());
    }

    public List<ItemGroupDto> toDto(Collection<ItemGroup> itemGroups) {
        return itemGroups.stream().map(this::toDto).toList();
    }

    public ItemGroup toEntity(CreateItemGroupDto itemGroupDto) {
        //Als ik nog tijd vind, deze logica best naar service verplaatsen
        Item item = itemRepository.findById(itemGroupDto.getItemId()).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (item.getAmount() - itemGroupDto.getAmount() >= 0) {
            return new ItemGroup(itemGroupDto.getItemId(), item.getName(), item.getPrice(), itemGroupDto.getAmount(), LocalDate.now().plusDays(1));
        } else {
            return new ItemGroup(itemGroupDto.getItemId(), item.getName(), item.getPrice(), itemGroupDto.getAmount(), LocalDate.now().plusWeeks(1));
        }
    }

    public List<ItemGroup> toEntity(List<CreateItemGroupDto> itemGroupDtos) {
        if (itemGroupDtos == null || itemGroupDtos.isEmpty()) {
            throw new IllegalArgumentException("Should contain items");
        }
        return itemGroupDtos.stream().map(this::toEntity).toList();
    }
}
