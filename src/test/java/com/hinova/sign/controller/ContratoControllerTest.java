package com.hinova.sign.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hinova.sign.dtos.ContratoRequestDto;
import com.hinova.sign.dtos.ContratoResponseDto;
import com.hinova.sign.exception.ContratoJaExisteException;
import com.hinova.sign.exception.ContratoNaoEncontradoException;
import com.hinova.sign.exception.GlobalExceptionHandler;
import com.hinova.sign.exception.StatusInvalidoException;
import com.hinova.sign.models.enums.StatusContrato;
import com.hinova.sign.service.ContratoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContratoControllerTest {

    @Mock
    private ContratoService service;

    @InjectMocks
    private ContratoController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void criar_deveRetornar201_quandoDadosValidos() throws Exception {
        ContratoRequestDto request = buildRequest(1L);
        ContratoResponseDto response = ContratoResponseDto.builder()
                .id(1L).propostaId(1L).status(StatusContrato.AGUARDANDO_ASSINATURA)
                .criadoEm(LocalDateTime.now()).build();

        when(service.criarContrato(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/contratos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_ASSINATURA"));
    }

    @Test
    void criar_deveRetornar409_quandoPropostaJaExiste() throws Exception {
        ContratoRequestDto request = buildRequest(1L);

        when(service.criarContrato(any()))
                .thenThrow(new ContratoJaExisteException("Já existe um contrato para a proposta: 1"));

        mockMvc.perform(post("/api/v1/contratos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }



    @Test
    void buscarPorId_deveRetornar200_quandoContratoExiste() throws Exception {
        ContratoResponseDto response = ContratoResponseDto.builder()
                .id(1L).propostaId(1L).status(StatusContrato.AGUARDANDO_ASSINATURA).build();

        when(service.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/contratos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_ASSINATURA"));
    }

    @Test
    void buscarPorId_deveRetornar404_quandoContratoNaoExiste() throws Exception {
        when(service.buscarPorId(99L))
                .thenThrow(new ContratoNaoEncontradoException("Contrato não encontrado: 99"));

        mockMvc.perform(get("/api/v1/contratos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void buscarStatusPorProposta_deveRetornar200_quandoPropostaExiste() throws Exception {
        ContratoResponseDto response = ContratoResponseDto.builder()
                .propostaId(1L).status(StatusContrato.AGUARDANDO_ASSINATURA).build();

        when(service.buscarStatusPorPropostaId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/contratos/proposta/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_ASSINATURA"));
    }

    @Test
    void buscarStatusPorProposta_deveRetornar404_quandoPropostaNaoExiste() throws Exception {
        when(service.buscarStatusPorPropostaId(99L))
                .thenThrow(new ContratoNaoEncontradoException("Contrato não encontrado para proposta: 99"));

        mockMvc.perform(get("/api/v1/contratos/proposta/99/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void assinar_deveRetornar200_quandoContratoValido() throws Exception {
        ContratoResponseDto response = ContratoResponseDto.builder()
                .id(1L).status(StatusContrato.ASSINADO).build();

        when(service.assinar(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/contratos/1/assinar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSINADO"));
    }

    @Test
    void assinar_deveRetornar422_quandoContratoJaAssinado() throws Exception {
        when(service.assinar(1L))
                .thenThrow(new StatusInvalidoException("Contrato já foi assinado"));

        mockMvc.perform(post("/api/v1/contratos/1/assinar"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void assinar_deveRetornar404_quandoContratoNaoExiste() throws Exception {
        when(service.assinar(99L))
                .thenThrow(new ContratoNaoEncontradoException("Contrato não encontrado: 99"));

        mockMvc.perform(post("/api/v1/contratos/99/assinar"))
                .andExpect(status().isNotFound());
    }

    private ContratoRequestDto buildRequest(Long propostaId) {
        return ContratoRequestDto.builder()
                .propostaId(propostaId)
                .clienteNome("Cliente Teste")
                .clienteEmail("cliente@test.com")
                .clienteEmpresa("Empresa Teste")
                .build();
    }
}
