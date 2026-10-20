package com.vavedin.euder.feature.eurder.service;

import com.vavedin.euder.feature.eurder.api.EurderMapper;
import com.vavedin.euder.feature.eurder.api.dto.CreateEurderDto;
import com.vavedin.euder.feature.eurder.api.dto.EurderDto;
import com.vavedin.euder.feature.eurder.domain.EurderRepository;
import com.vavedin.euder.feature.item.domain.ItemRepository;
import com.vavedin.euder.feature.user.domain.User;
import org.springframework.stereotype.Service;

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
        return eurderMapper.toDto(eurderRepository.addEurder(eurderMapper.toEntity(createEurderDto, customer)));
    }
}
