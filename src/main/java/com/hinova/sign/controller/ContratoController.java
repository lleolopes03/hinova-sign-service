package com.hinova.sign.controller;

import com.hinova.sign.dtos.ContratoRequestDto;
import com.hinova.sign.dtos.ContratoResponseDto;
import com.hinova.sign.service.ContratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contratos")
@RequiredArgsConstructor
@Tag(name = "Contratos", description = "Gerenciamento de contratos e assinaturas")
public class ContratoController {
    private final ContratoService service;

    @PostMapping
    @Operation(summary = "Receber contrato do CRM e registrar")
    public ResponseEntity<ContratoResponseDto> criar(@RequestBody @Valid ContratoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarContrato(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar contrato por ID")
    public ResponseEntity<ContratoResponseDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/proposta/{propostaId}/status")
    @Operation(summary = "Consultar status do contrato por ID da proposta")
    public ResponseEntity<ContratoResponseDto> buscarStatusPorProposta(@PathVariable Long propostaId) {
        return ResponseEntity.ok(service.buscarStatusPorPropostaId(propostaId));
    }

    @PostMapping("/{id}/assinar")
    @Operation(summary = "Assinar contrato e notificar o CRM")
    public ResponseEntity<ContratoResponseDto> assinar(@PathVariable Long id) {
        return ResponseEntity.ok(service.assinar(id));
    }
}
