package com.vavedin.euder.feature.item.service;

import com.vavedin.euder.feature.item.api.ItemMapper;
import com.vavedin.euder.feature.item.api.dto.CreateItemDto;
import com.vavedin.euder.feature.item.api.dto.ItemDto;
import com.vavedin.euder.feature.item.api.dto.UpdateItemDto;
import com.vavedin.euder.feature.item.domain.Item;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;


    public ItemService(ItemMapper itemMapper, ItemRepository itemRepository) {
        this.itemMapper = itemMapper;
        this.itemRepository = itemRepository;
    }

    public ItemDto addItem(CreateItemDto createItemDto) {
        return itemMapper.toDto(itemRepository.addItem(itemMapper.toEntity(createItemDto)));
    }

    public ItemDto updateItem(String id, UpdateItemDto updateItemDto) {
        itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("No item for Id " + id));

        Item updatedItem = new Item(updateItemDto.getName(), updateItemDto.getDescription(), updateItemDto.getPrice(), updateItemDto.getAmount(), id);
        return itemMapper.toDto(itemRepository.addItem(updatedItem));

    }
}
