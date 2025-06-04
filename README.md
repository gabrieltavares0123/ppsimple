<h1 align="center"> 
  Desafio backend PicPay
</h1>
Esta é uma versão simlpificada do PicPay que contempla a funcionalidade de transferir valores entre carteiras. Nela, usuários comuns e lojistas podem realizar transferências. Tudo foi desenvolvido baseado nesse desafio https://github.com/PicPay/picpay-desafio-backend.

## Tecnologias envolvidas
- ```Spring Boot``` como framework web;
- ```Spring Data``` para acesso a dados;
- ```Gradle``` como construtor;
- ```Docker compose``` para containizar a aplicação e aplicações dependentes;
- ```Spring Cloud Kafka``` para mensageria;
- ```Testcontainers``` para realizar ```testes de integração``` e ```testes ponta a ponta``` em containers ```Docker```.
- ```PostgreSQL``` como banco de dados relacional.
## Decisões
Tomei a liberdade de usar alguns conceitos usados em aplicações reais na solução;
- Para não expor os identificadores dos usuários, criei identificadores externos baseados em ```UUID```;
- Usei uma simplificação do que seria ```arquitetura hexagonal``` para proteger a lógica de negócio;
- Decidi ir um pouco além dos ```testes unitários``` e escrevi ```testes de integração``` e ```ponta a ponta```;
- Usei ```Testcontainers``` para executar os testes de integração e ponta a ponta;
- Decidi arquitetar a aplicação para ser executada containizada.

## Arquitetura
Quando uma transferência é feita, primeiro verificamos se ela está autorizada através de um serviço externo ```Authorization Service```. Caso verdadeiro, a transferência é salva em um banco de dados ```PostgreSQL```. Em seguida uma notificação é enviada via serviço externo ```Notification Service```. Caso esse serviço esteja indisponível, a notificação é enviada como evento no ```Kafka``` para ser reenviada posteriormente.

Segue um desenho da arquitetura:
![alt text](docs/ppsimple-architecture.png)

## Como executar a aplicação
- Na raíz da aplicação execute ```./gradlew bootJar``` para gerar o executável;
- Em seguida execute ```docker compose up --build -d``` para construir a imagem da aplicação, subir seu container e os containers dependentes;
- A aplicação ficará disponível em ```http:127.0.0.1:8080```;

## API
### Criar uma nova carteira
```
curl --location 'http://127.0.0.1:8080/api/wallet' \
--data-raw '{
  "ownerName": "Gabriel Jorge",
  "document": "000.000.000-00",
  "documentType": "CPF",
  "email": "gabriel.jorge@mail.com",
  "password": "12345678"
}'
```

### Realizar uma transferência
```
curl --location 'http://127.0.0.1:8080/api/transfer' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1YThhODFjZS1hY2I1LTQ0YTMtOGViNi1lNzc2Yzc0MWZhYjUiLCJpYXQiOjE3NDYyMjM1MzcsImV4cCI6MzQ5MjQ1MDY3NH0.Kx_ugzpPpp-halcREQC2qbYCemAOJ_4EjHtKIsCGQAlLZEm7gZQxeq8q188pFaR2ddtMFhP2tahWKWz2ua7uYA' \
--header 'Content-Type: application/json' \
--data '{
  "value": 0.01,
  "payer": "bb69e149-5cb4-482d-b68c-034e853783b5",
  "payee": "ca438f08-f480-4df4-bdfb-d0a8524af127"
}'
```
