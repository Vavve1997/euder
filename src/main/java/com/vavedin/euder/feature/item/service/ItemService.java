package com.vavedin.euder.feature.item.service;

import com.vavedin.euder.feature.item.api.ItemMapper;
import com.vavedin.euder.feature.item.api.dto.CreateItemDto;
import com.vavedin.euder.feature.item.api.dto.ItemDto;
import com.vavedin.euder.feature.item.api.dto.UpdateItemDto;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import com.vavedin.euder.feature.item.domain.ItemRepositoryOutOfUse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemService {

    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;


    public ItemService(ItemMapper itemMapper, ItemRepository itemRepository) {
        this.itemMapper = itemMapper;
        this.itemRepository = itemRepository;
    }

    public ItemDto addItem(CreateItemDto createItemDto) {
        return itemMapper.toDto(itemRepository.save(itemMapper.toEntity(createItemDto)));
    }

    @Transactional
    public ItemDto updateItem(String id, UpdateItemDto dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new ItemNotFoundException("Item not found for id " + id));

        item.updateDetails(
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getAmount()
        );

        return itemMapper.toDto(item);
    }
}
