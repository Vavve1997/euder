package com.vavedin.euder.feature.item.api;

import com.vavedin.euder.feature.item.api.dto.CreateItemDto;
import com.vavedin.euder.feature.item.api.dto.ItemDto;
import com.vavedin.euder.feature.item.api.dto.UpdateItemDto;
import com.vavedin.euder.feature.item.service.ItemService;
import com.vavedin.euder.feature.security.SecurityService;
import com.vavedin.euder.feature.security.enums.Feature;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/items")
public class ItemController {

    private final ItemService itemService;
    private final SecurityService securityService;

    public ItemController(ItemService itemService, SecurityService securityService) {
        this.itemService = itemService;
        this.securityService = securityService;
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "basicAuth")
    public ItemDto addItem(@Parameter(hidden = true)
                           @RequestHeader(value = "Authorization", required = false) String authorization,
                           @RequestBody CreateItemDto createItemDto) {
        securityService.validateAuthorization(authorization, Feature.ADD_ITEM);
        return itemService.addItem(createItemDto);
    }

    @PutMapping(path = "/{id}", produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "basicAuth")
    public ItemDto updateItem(@Parameter(hidden = true)
                           @RequestHeader(value = "Authorization", required = false) String authorization,
                           @PathVariable String id,
                           @RequestBody UpdateItemDto updateItemDto) {
        securityService.validateAuthorization(authorization, Feature.UPDATE_ITEM);
        return itemService.updateItem(id, updateItemDto);
    }


}
