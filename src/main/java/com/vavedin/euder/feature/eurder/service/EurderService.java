package com.vavedin.euder.feature.eurder.service;

import com.vavedin.euder.feature.eurder.api.EurderMapper;
import com.vavedin.euder.feature.eurder.api.dto.CreateEurderDto;
import com.vavedin.euder.feature.eurder.api.dto.CreateItemGroupDto;
import com.vavedin.euder.feature.eurder.api.dto.EurderDto;
import com.vavedin.euder.feature.eurder.domain.Eurder;
import com.vavedin.euder.feature.eurder.domain.EurderRepository;
import com.vavedin.euder.feature.eurder.domain.ItemGroup;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import com.vavedin.euder.feature.user.domain.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EurderService {

    private final EurderRepository eurderRepository;
    private final EurderMapper eurderMapper;
    private final ItemRepository itemRepository;


    public EurderService(EurderRepository eurderRepository, EurderMapper eurderMapper, ItemRepository itemRepository) {
        this.eurderRepository = eurderRepository;
        this.eurderMapper = eurderMapper;
        this.itemRepository = itemRepository;
    }

    public EurderDto addEurder(CreateEurderDto createEurderDto, User customer) {

        List<CreateItemGroupDto> requestedGroups = createEurderDto.getItemGroupDtos();

        if (requestedGroups == null || requestedGroups.isEmpty()) {
            throw new IllegalArgumentException("ItemGroup should contain items");
        }

        LocalDate today = LocalDate.now();

        List<ItemGroup> itemGroups = requestedGroups.stream()
                .map(group -> createItemGroup(group, today))
                .toList();

        Eurder eurder = new Eurder(customer.getId(), itemGroups);
        Eurder savedEurder = eurderRepository.addEurder(eurder);

        return eurderMapper.toDto(savedEurder);
    }

    private ItemGroup createItemGroup(CreateItemGroupDto requestedGroup, LocalDate today) {

        if (requestedGroup == null) {
            throw new IllegalArgumentException("ItemGroup must not be null");
        }

        String itemId = requestedGroup.getItemId();

        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Item id must not be blank");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Item not found")
                );

        boolean enoughStockAvailable = item.getAmount() >= requestedGroup.getAmount();

        LocalDate shippingDate = today.plusDays(enoughStockAvailable ? 1 : 7);

        return new ItemGroup(
                item.getId(),
                item.getName(),
                item.getPrice(),
                requestedGroup.getAmount(),
                shippingDate
        );
    }
}
