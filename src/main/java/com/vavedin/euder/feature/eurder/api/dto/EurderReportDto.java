package com.vavedin.euder.feature.eurder.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class EurderReportDto {

    private List<EurderDto> eurders;
    private BigDecimal totalPrice;

    public EurderReportDto(List<EurderDto> eurders, BigDecimal totalPrice) {
        this.eurders = eurders;
        this.totalPrice = totalPrice;
    }

    public EurderReportDto() {
    }

    public List<EurderDto> getEurders() {
        return eurders;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
