package com.hinova.sign.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CrmClient {
    private final RestClient restClient;

    @Value("${crm.service.url}")
    private String crmServiceUrl;

    public void notificarAssinatura(Long propostaId) {
        restClient.post()
                .uri(crmServiceUrl + "/api/v1/propostas/callback/assinatura/" + propostaId)
                .retrieve()
                .toBodilessEntity();
    }
}
