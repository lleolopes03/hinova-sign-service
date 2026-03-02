package com.hinova.sign.dtos.mapper;

import com.hinova.sign.dtos.ContratoRequestDto;
import com.hinova.sign.dtos.ContratoResponseDto;
import com.hinova.sign.models.Contrato;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContratoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "conteudo", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "assinadoEm", ignore = true)
    Contrato toEntity(ContratoRequestDto dto);

    ContratoResponseDto toResponseDto(Contrato contrato);

}
