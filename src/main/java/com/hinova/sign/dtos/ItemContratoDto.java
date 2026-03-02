package com.hinova.sign.dtos;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemContratoDto {
    private String nome;
    private Integer quantidade;
    private BigDecimal precoUnitario;
}
