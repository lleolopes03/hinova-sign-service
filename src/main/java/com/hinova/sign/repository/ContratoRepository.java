package com.hinova.sign.repository;

import com.hinova.sign.models.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato,Long> {
    Optional<Contrato> findByPropostaId(Long propostaId);
    boolean existsByPropostaId(Long propostaId);
}
