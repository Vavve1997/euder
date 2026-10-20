package com.vavedin.euder.feature.item.service;

import com.vavedin.euder.common.exception.NotFoundException;

public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException(String itemId) {
        super("No item was found for id " + itemId);
    }
}
