package com.vavedin.euder.feature.item.domain;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ItemDatabase {

    private final ConcurrentHashMap<String, Item> itemDatabase;

    public ItemDatabase() {
        itemDatabase = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, Item> getItemDatabase() {
        return itemDatabase;
    }
}
