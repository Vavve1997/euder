package com.vavedin.euder.feature.eurder.service;

import com.vavedin.euder.common.exception.ForbiddenException;
import com.vavedin.euder.common.exception.NotFoundException;
import com.vavedin.euder.feature.eurder.api.EurderMapper;
import com.vavedin.euder.feature.eurder.api.ItemGroupMapper;
import com.vavedin.euder.feature.eurder.api.dto.*;
import com.vavedin.euder.feature.eurder.domain.Eurder;
import com.vavedin.euder.feature.eurder.domain.EurderRepository;
import com.vavedin.euder.feature.eurder.domain.ItemGroup;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import com.vavedin.euder.feature.user.api.AddressMapper;
import com.vavedin.euder.feature.user.api.dto.AddressDto;
import com.vavedin.euder.feature.user.domain.User;
import org.springframework.stereotype.Service;
import tools.jackson.databind.cfg.MapperBuilder;

import java.time.LocalDate;
import java.util.List;

@Service
public class EurderService {

    private final EurderRepository eurderRepository;
    private final EurderMapper eurderMapper;
    private final ItemRepository itemRepository;
    private final ItemGroupMapper itemGroupMapper;
    private final AddressMapper addressMapper;



    public EurderService(EurderRepository eurderRepository, EurderMapper eurderMapper, ItemRepository itemRepository, ItemGroupMapper itemGroupMapper, AddressMapper addressMapper) {
        this.eurderRepository = eurderRepository;
        this.eurderMapper = eurderMapper;
        this.itemRepository = itemRepository;
        this.itemGroupMapper = itemGroupMapper;
        this.addressMapper = addressMapper;
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

        Eurder eurder = new Eurder(customer.getId(), itemGroups, customer.getAddress());
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

        if (enoughStockAvailable) {
            item.takeAmountFromStock(requestedGroup.getAmount());
        }

        return new ItemGroup(
                item.getId(),
                item.getName(),
                item.getPrice(),
                requestedGroup.getAmount(),
                shippingDate
        );
    }

    public EurderReportDto getReportOfEurders(User user) {
        return eurderMapper.toReportDto(eurderRepository.findByCustomerId(user.getId()));
    }

    public EurderDto reorderExistingEurder(String id, User user) {
        Eurder originalEurder = eurderRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("eurder not found for id " + id)
                );

        if (!originalEurder.getCustomerId().equals(user.getId())) {
            throw new ForbiddenException(
                    "You can only reorder your own eurders"
            );
        }

        List<CreateItemGroupDto> requestedGroups =
                originalEurder.getItemGroups().stream()
                        .map(group -> new CreateItemGroupDto(
                                group.getItemId(),
                                group.getAmount()
                        ))
                        .toList();

        return addEurder(new CreateEurderDto(requestedGroups), user);
    }

    public List<EurderShipmentDto> getAllEurdersShippingToday() {
        return eurderRepository.findAllEurdersWithItemsShippingToday().stream()
                .map(eurder -> new EurderShipmentDto(eurder.getId(),
                        addressMapper.toDto(eurder.getShippingAddress()),
                        itemGroupMapper.toDto(eurder.getAllItemGroupsThatShipToday())))
                .toList();
    }
}
