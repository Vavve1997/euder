package com.vavedin.euder.feature.item.domain;

import org.springframework.stereotype.Repository;

@Repository
public class ItemRepository {

    private final ItemDatabase itemDatabase;

    public ItemRepository(ItemDatabase itemDatabase) {
        this.itemDatabase = itemDatabase;
    }
}
