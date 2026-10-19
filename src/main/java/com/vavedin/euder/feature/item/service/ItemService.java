package com.vavedin.euder.feature.item.service;

import com.vavedin.euder.feature.item.api.ItemMapper;
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
}
