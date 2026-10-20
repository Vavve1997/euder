package com.vavedin.euder.feature.item.domain;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ItemRepository {

    private final ItemDatabase itemDatabase;

    public ItemRepository(ItemDatabase itemDatabase) {
        this.itemDatabase = itemDatabase;
    }

    public Item addItem(Item item) {
        itemDatabase.getItemDatabase().put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(String id) {
        return Optional.ofNullable(itemDatabase.getItemDatabase().get(id));
    }

}
