package com.vavedin.euder.feature.eurder.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EurderRepository extends JpaRepository<Eurder, String> {
    List<Eurder> findByCustomerId(String customerId);

}
