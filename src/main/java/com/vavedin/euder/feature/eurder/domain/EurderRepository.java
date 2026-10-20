package com.vavedin.euder.feature.eurder.domain;

import org.springframework.stereotype.Repository;

@Repository
public class EurderRepository {

    private final EurderDatabase eurderDatabase;

    public EurderRepository(EurderDatabase eurderDatabase) {
        this.eurderDatabase = eurderDatabase;
    }

    public Eurder addEurder(Eurder eurder) {
        eurderDatabase.getEurderDatabase().put(eurder.getId(), eurder);
        return eurder;
    }
}
