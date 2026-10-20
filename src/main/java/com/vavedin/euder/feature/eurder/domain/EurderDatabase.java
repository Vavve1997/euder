package com.vavedin.euder.feature.eurder.domain;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class EurderDatabase {

    private final ConcurrentHashMap<String, Eurder> eurderDatabase;

    public EurderDatabase() {
        eurderDatabase = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, Eurder> getEurderDatabase() {
        return eurderDatabase;
    }
}
