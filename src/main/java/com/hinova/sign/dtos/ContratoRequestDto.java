package com.hinova.sign.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContratoRequestDto {
    @NotNull(message = "ID da proposta é obrigatório")
    private Long propostaId;

    @NotBlank(message = "Nome do cliente é obrigatório")
    private String clienteNome;

    @NotBlank(message = "Email do cliente é obrigatório")
    private String clienteEmail;

    @NotBlank(message = "Empresa é obrigatória")
    private String clienteEmpresa;

    private List<ItemContratoDto> itens;
}
