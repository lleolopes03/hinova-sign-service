package com.hinova.sign.service;

import com.hinova.sign.client.CrmClient;
import com.hinova.sign.dtos.ContratoRequestDto;
import com.hinova.sign.dtos.ContratoResponseDto;
import com.hinova.sign.dtos.ItemContratoDto;
import com.hinova.sign.dtos.mapper.ContratoMapper;
import com.hinova.sign.exception.ContratoJaExisteException;
import com.hinova.sign.exception.ContratoNaoEncontradoException;
import com.hinova.sign.exception.StatusInvalidoException;
import com.hinova.sign.models.Contrato;
import com.hinova.sign.models.enums.StatusContrato;
import com.hinova.sign.repository.ContratoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @Mock
    private ContratoRepository repository;

    @Mock
    private ContratoMapper mapper;

    @Mock
    private CrmClient crmClient;

    @InjectMocks
    private ContratoService service;

    @Test
    void criarContrato_deveRetornarResponseDto_quandoPropostaInexistente() {
        ContratoRequestDto dto = buildRequest(1L);
        Contrato contrato = buildContrato(1L, StatusContrato.AGUARDANDO_ASSINATURA);
        ContratoResponseDto responseDto = ContratoResponseDto.builder()
                .id(1L).propostaId(1L).status(StatusContrato.AGUARDANDO_ASSINATURA).build();

        when(repository.existsByPropostaId(1L)).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(contrato);
        when(repository.save(contrato)).thenReturn(contrato);
        when(mapper.toResponseDto(contrato)).thenReturn(responseDto);

        ContratoResponseDto result = service.criarContrato(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(StatusContrato.AGUARDANDO_ASSINATURA);
        verify(repository).save(contrato);
    }

    @Test
    void criarContrato_deveLancarContratoJaExisteException_quandoPropostaJaExiste() {
        ContratoRequestDto dto = buildRequest(1L);
        when(repository.existsByPropostaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.criarContrato(dto))
                .isInstanceOf(ContratoJaExisteException.class)
                .hasMessageContaining("1");

        verify(repository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarContrato_quandoIdExiste() {
        Contrato contrato = buildContrato(1L, StatusContrato.AGUARDANDO_ASSINATURA);
        ContratoResponseDto responseDto = ContratoResponseDto.builder().id(1L).build();

        when(repository.findById(1L)).thenReturn(Optional.of(contrato));
        when(mapper.toResponseDto(contrato)).thenReturn(responseDto);

        ContratoResponseDto result = service.buscarPorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_deveLancarContratoNaoEncontradoException_quandoIdNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ContratoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void buscarStatusPorPropostaId_deveRetornarContrato_quandoPropostaExiste() {
        Contrato contrato = buildContrato(1L, StatusContrato.AGUARDANDO_ASSINATURA);
        ContratoResponseDto responseDto = ContratoResponseDto.builder()
                .propostaId(1L).status(StatusContrato.AGUARDANDO_ASSINATURA).build();

        when(repository.findByPropostaId(1L)).thenReturn(Optional.of(contrato));
        when(mapper.toResponseDto(contrato)).thenReturn(responseDto);

        ContratoResponseDto result = service.buscarStatusPorPropostaId(1L);

        assertThat(result.getStatus()).isEqualTo(StatusContrato.AGUARDANDO_ASSINATURA);
    }

    @Test
    void buscarStatusPorPropostaId_deveLancarContratoNaoEncontradoException_quandoPropostaNaoExiste() {
        when(repository.findByPropostaId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarStatusPorPropostaId(99L))
                .isInstanceOf(ContratoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void assinar_deveAssinarContrato_quandoStatusAguardandoAssinatura() {
        Contrato contrato = buildContrato(1L, StatusContrato.AGUARDANDO_ASSINATURA);
        contrato.setPropostaId(1L);
        ContratoResponseDto responseDto = ContratoResponseDto.builder()
                .id(1L).status(StatusContrato.ASSINADO).build();

        when(repository.findById(1L)).thenReturn(Optional.of(contrato));
        when(repository.save(contrato)).thenReturn(contrato);
        when(mapper.toResponseDto(contrato)).thenReturn(responseDto);

        ContratoResponseDto result = service.assinar(1L);

        assertThat(result.getStatus()).isEqualTo(StatusContrato.ASSINADO);
        verify(crmClient).notificarAssinatura(1L);
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ASSINADO);
        assertThat(contrato.getAssinadoEm()).isNotNull();
    }

    @Test
    void assinar_deveLancarStatusInvalidoException_quandoContratoJaAssinado() {
        Contrato contrato = buildContrato(1L, StatusContrato.ASSINADO);
        when(repository.findById(1L)).thenReturn(Optional.of(contrato));

        assertThatThrownBy(() -> service.assinar(1L))
                .isInstanceOf(StatusInvalidoException.class);

        verify(crmClient, never()).notificarAssinatura(any());
        verify(repository, never()).save(any());
    }

    private ContratoRequestDto buildRequest(Long propostaId) {
        return ContratoRequestDto.builder()
                .propostaId(propostaId)
                .clienteNome("Cliente Teste")
                .clienteEmail("cliente@test.com")
                .clienteEmpresa("Empresa Teste")
                .itens(List.of(ItemContratoDto.builder()
                        .nome("Produto A").quantidade(2).precoUnitario(new BigDecimal("100.00")).build()))
                .build();
    }

    private Contrato buildContrato(Long id, StatusContrato status) {
        return Contrato.builder()
                .id(id)
                .propostaId(id)
                .clienteNome("Cliente Teste")
                .clienteEmail("cliente@test.com")
                .clienteEmpresa("Empresa Teste")
                .status(status)
                .criadoEm(LocalDateTime.now())
                .build();
    }
}
