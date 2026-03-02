package com.hinova.sign.dtos;

import com.hinova.sign.models.enums.StatusContrato;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContratoResponseDto {
    private Long id;
    private Long propostaId;
    private String clienteNome;
    private String clienteEmpresa;
    private StatusContrato status;
    private String conteudo;
    private LocalDateTime criadoEm;
    private LocalDateTime assinadoEm;
}
