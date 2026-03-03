# SIGN Service — Hinova Desafio Técnico

Serviço responsável pela geração e assinatura eletrônica de contratos.
Faz parte de uma plataforma SaaS composta por dois módulos independentes: **CRM** e **SIGN**.

## Arquitetura

```
hinova-crm-service  (porta 8080)
hinova-sign-service (porta 8081)
```

A comunicação entre os serviços é feita via **REST síncrono**.

**Fluxo completo:**
1. CRM cria proposta → status: `RASCUNHO`
2. CRM envia proposta para assinatura → chama SIGN via REST
3. SIGN cria contrato → status: `AGUARDANDO_ASSINATURA`
4. CRM atualiza proposta → status: `ENVIADA_PARA_ASSINATURA`
5. SIGN assina contrato → chama callback do CRM
6. CRM atualiza proposta → status: `ASSINADA`

## Tecnologias

- Java 21
- Spring Boot 4.0.3
- MySQL 8
- MapStruct 1.6.3
- Lombok
- SpringDoc OpenAPI (Swagger)
- JUnit 5 + Mockito

## Pré-requisitos

- Java 21+
- Maven
- MySQL rodando na porta 3306
- CRM Service rodando na porta 8080

## Como rodar

### 1. Configure o banco de dados

```sql
CREATE DATABASE IF NOT EXISTS sign_db;
```

### 2. Configure o `application.properties`

```properties
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

### 3. Suba o CRM Service primeiro

```bash
cd ../hinova-crm-service
./mvnw spring-boot:run
```

### 4. Suba o SIGN Service

```bash
./mvnw spring-boot:run
```

## Endpoints

| Método | URL | Descrição |
|--------|-----|-----------|
| `POST` | `/api/v1/contratos` | Receber contrato do CRM e registrar |
| `GET` | `/api/v1/contratos/{id}` | Buscar contrato por ID |
| `GET` | `/api/v1/contratos/proposta/{propostaId}/status` | Consultar status do contrato por proposta |
| `POST` | `/api/v1/contratos/{id}/assinar` | Assinar contrato e notificar CRM |

## Swagger

Acesse: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

## Testes

```bash
./mvnw test
```

## Decisões Técnicas

- **REST síncrono:** suficiente para o escopo, simples e fácil de validar
- **MapStruct:** mapeamento entre DTOs e entidades em tempo de compilação (type-safe, sem reflexão)
- **Idempotência:** ao receber o mesmo `propostaId` duas vezes, retorna `409 CONFLICT` ao invés de criar duplicata
- **Banco separado por serviço:** `sign_db` independente do CRM — cada serviço é dono dos seus dados
- **Callback ao CRM:** após assinatura, o SIGN notifica o CRM via REST para atualizar o status da proposta
- **Geração de contrato:** conteúdo gerado em texto simples (simulado), conforme escopo do desafio
