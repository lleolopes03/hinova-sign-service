package com.hinova.sign.service;

import com.hinova.sign.client.CrmClient;
import com.hinova.sign.dtos.ContratoRequestDto;
import com.hinova.sign.dtos.ContratoResponseDto;
import com.hinova.sign.dtos.mapper.ContratoMapper;
import com.hinova.sign.exception.ContratoJaExisteException;
import com.hinova.sign.exception.ContratoNaoEncontradoException;
import com.hinova.sign.exception.StatusInvalidoException;
import com.hinova.sign.models.Contrato;
import com.hinova.sign.models.enums.StatusContrato;
import com.hinova.sign.repository.ContratoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContratoService {
    private final ContratoRepository repository;
    private final ContratoMapper mapper;
    private final CrmClient crmClient;

    @Transactional
    public ContratoResponseDto criarContrato(ContratoRequestDto dto) {
        // idempotência: se já existe contrato para essa proposta, retorna o existente
        if (repository.existsByPropostaId(dto.getPropostaId())) {
            throw new ContratoJaExisteException("Já existe um contrato para a proposta: " + dto.getPropostaId());
        }

        Contrato contrato = mapper.toEntity(dto);
        contrato.setConteudo(gerarConteudoContrato(dto));

        return mapper.toResponseDto(repository.save(contrato));
    }


    public ContratoResponseDto buscarPorId(Long id) {
        return mapper.toResponseDto(buscarOuLancar(id));
    }


    public ContratoResponseDto buscarStatusPorPropostaId(Long propostaId) {
        Contrato contrato = repository.findByPropostaId(propostaId)
                .orElseThrow(() -> new ContratoNaoEncontradoException("Contrato não encontrado para proposta: " + propostaId));
        return mapper.toResponseDto(contrato);
    }

    @Transactional
    public ContratoResponseDto assinar(Long id) {
        Contrato contrato = buscarOuLancar(id);

        if (contrato.getStatus() != StatusContrato.AGUARDANDO_ASSINATURA) {
            throw new StatusInvalidoException("Contrato já foi assinado");
        }

        contrato.setStatus(StatusContrato.ASSINADO);
        contrato.setAssinadoEm(LocalDateTime.now());
        Contrato salvo = repository.save(contrato);

        // notifica o CRM via callback
        crmClient.notificarAssinatura(contrato.getPropostaId());

        return mapper.toResponseDto(salvo);
    }

    private String gerarConteudoContrato(ContratoRequestDto dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("CONTRATO COMERCIAL\n\n");
        sb.append("Cliente: ").append(dto.getClienteNome()).append("\n");
        sb.append("Email: ").append(dto.getClienteEmail()).append("\n");
        sb.append("Empresa: ").append(dto.getClienteEmpresa()).append("\n\n");
        sb.append("ITENS:\n");
        if (dto.getItens() != null) {
            dto.getItens().forEach(item ->
                    sb.append("- ").append(item.getNome())
                            .append(" | Qtd: ").append(item.getQuantidade())
                            .append(" | Preço: R$ ").append(item.getPrecoUnitario()).append("\n")
            );
        }
        sb.append("\nStatus: AGUARDANDO ASSINATURA");
        return sb.toString();
    }

    private Contrato buscarOuLancar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ContratoNaoEncontradoException("Contrato não encontrado: " + id));
    }
}
