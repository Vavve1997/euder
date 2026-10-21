package com.vavedin.euder.feature.eurder.domain;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    public List<Eurder> findByCustomerId(String id) {
        return eurderDatabase.getEurderDatabase().values()
                .stream().filter(eurder -> eurder.getCustomerId().equals(id))
                .toList();
    }

    public Optional<Eurder> findById(String id) {
        return Optional.ofNullable(eurderDatabase.getEurderDatabase().get(id));
    }

    public List<Eurder> findAllEurdersWithItemsShippingToday() {
        return eurderDatabase.getEurderDatabase().values().stream()
                .filter(Eurder::hasItemGroupShippingToday)
                .toList();
    }
}
