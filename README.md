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
- ```Testcontainers``` para realizar ```testes de integração``` e ```testes ponta a ponta``` em containers ```Docker```;
- ```PostgreSQL``` como banco de dados relacional;
- ```Ktlint``` para as verificações de padrões de escrita;

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
Na raíz do projeto existe um ```Makefile``` com agrupamentos de comandos para funcionar como um ```CI``` local. Segue a lista dos comandos com suas descrições:

1. ```clean``` para remover artefatos gerando anteriormente;
2. ```jar``` para gerar o executável;
3. ```test``` para executar todos os testes da aplicação;
4. ```lint``` para realizar o lint da aplicação;
5. ```build``` para realizar todo o processo de build;
6. ```down``` para derrubar os containers ```Docker```;
7. ```up``` para realizar o processo de build e subir todos os containers;
8. ```restart``` que derruba todos os containers ```Docker``` e realiza novamente todo o processo de ```up```;

### Executar de forma automatizada usando ```make```
Se estiver em ambiente ```Windows``` e ainda assim deseja usar o ```make```, siga os passos ```1``` e ```2```, caso contrário, pule para os passos seguintes:
1. Baixe o ```Chocolatey``` por [aqui](https://chocolatey.org/install);
2. Em seguida instale o ```make``` com o comando: ```choco install make```;
3. Use o comando ```make up```;
4. A aplicação ficará disponível em ```http:127.0.0.1:8080```;

### Executar manualmente via Intellij Community 
1. Na raíz da aplicação execute ```./gradlew bootJar``` para gerar o executável;
2. Em seguida execute ```docker compose up --build -d``` para construir a imagem da aplicação, subir seu container e os containers dependentes;
3. A aplicação ficará disponível em ```http:127.0.0.1:8080```;


## API
Na raís do projeto existe uma coleção do ```Postman``` no [arquivo](PpSimple.postman_collection.json) para testar os endpoints. Caso deseje, pode copiar o ```curl``` dos endpoints abaixo.

### Criar uma nova carteira
```
curl --location 'http://127.0.0.1:8080/api/wallet' \
--header 'Content-Type: application/json' \
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
--header 'Content-Type: application/json' \
--data '{
  "value": 0.01,
  "payer": "bb69e149-5cb4-482d-b68c-034e853783b5",
  "payee": "ca438f08-f480-4df4-bdfb-d0a8524af127"
}'
```
