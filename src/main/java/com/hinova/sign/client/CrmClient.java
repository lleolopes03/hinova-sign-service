package com.hinova.sign.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CrmClient {

    private final RestClient restClient;

    public CrmClient(@Value("${crm.service.url}") String crmServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(crmServiceUrl)
                .build();
    }

    public void notificarAssinatura(Long propostaId) {
        restClient.post()
                .uri("/api/v1/propostas/callback/assinatura/" + propostaId)
                .retrieve()
                .toBodilessEntity();
    }
}
