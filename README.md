🚁 Drone Simulator - Desafio Técnico DTI
Este projeto simula a operação de uma frota de drones urbanos para entrega de encomendas, implementando lógica de alocação (prioridade, capacidade, autonomia) e simulação de estados de voo em uma aplicação Spring Boot 3.3.1.

1. Pré-requisitos
Para rodar o simulador localmente, você precisa ter as seguintes ferramentas instaladas:

Java Development Kit (JDK): Versão 21 ou superior (o projeto usa java.version 21).

Apache Maven: Versão 3.6.0 ou superior (para gerenciamento de dependências e build).

Cliente REST: Thunder Client (extensão do VS Code) ou Postman para testar os endpoints.

2. Configuração do Ambiente e Compilação
Siga estes passos para preparar o ambiente e construir o projeto.

2.1. Clonar e Navegar
Clone o repositório e navegue até a pasta raiz do projeto no seu terminal:

Bash

git clone https://www.youtube.com/watch?v=X49Wz3icO3E
cd drone-simulator
2.2. Compilação e Instalação
Use o Maven para limpar, compilar e empacotar a aplicação, garantindo que o Lombok gere todos os métodos de acesso (getters/setters).

Bash

# Limpa, compila e gera o arquivo .jar na pasta /target
mvn clean install
Resultado Esperado: [INFO] BUILD SUCCESS

3. Execução da Aplicação
Inicie o servidor Spring Boot, que ficará ativo na porta 8080.

Bash

# Executa o arquivo JAR gerado na pasta 'target'
java -jar target/drone-simulator-0.0.1-SNAPSHOT.jar
Resultado Esperado: O console exibirá o logo do Spring Boot e a mensagem: Tomcat started on port 8080 (http)

O servidor estará ativo em http://localhost:8080.

4. Teste da API com Thunder Client (Ciclo Completo)
Com o servidor rodando na porta 8080, teste as funcionalidades da API na ordem correta: criação de pedidos, checagem de status, alocação e rastreamento.

4.1. Criar um Pedido (POST /api/pedidos)
Vamos criar três pedidos (ALTA, MEDIA, BAIXA) para testar a priorização.

Abra o Thunder Client e clique em "New Request".

Defina o Método como POST.

Insira a URL: http://localhost:8080/api/pedidos

Vá para a aba Query e adicione os seguintes pares Name e Value (Repita para os 3 pedidos, alterando os valores):

Nome	Valor (Pedido 1)	Valor (Pedido 2 - ALTA)	Valor (Pedido 3)
pesoKg	8.0	1.5	3.0
prioridade	BAIXA	ALTA	MEDIA
x	3.0	0.0	1.0
y	0.0	2.0	1.0

Exportar para as Planilhas
Ação: Envie as 3 requisições (uma por vez).

Resultado Esperado:

Status: 201 Created

Corpo da Resposta: Um objeto Pedido com um id. Anote o ID do pedido de ALTA prioridade para o rastreamento (Etapa 4.4).

4.2. Verificar Status Inicial dos Drones (GET /api/drones/status)
Confirme se os drones estão disponíveis para a alocação.

Crie uma nova requisição.

Defina o Método como GET.

Insira a URL: http://localhost:8080/api/drones/status

Clique em Send.

Resultado Esperado:

Status: 200 OK

Corpo da Resposta: Uma lista (array) contendo 3 objetos Drone. Todos devem ter:

estado: IDLE

localizacao: { "x": 0.0, "y": 0.0 }

4.3. Iniciar a Alocação (POST /api/simulacao/alocar)
Este é o gatilho que inicia o algoritmo de roteirização e a simulação de voo em segundo plano.

Crie uma nova requisição.

Defina o Método como POST.

Insira a URL: http://localhost:8080/api/simulacao/alocar

Não utilize corpo nem parâmetros.

Clique em Send.

Resultados Esperados:

Status: 200 OK

Corpo da Resposta: "Processo de alocação de drones iniciado. Verifique o console para a simulação de voo."

Terminal: O console do seu servidor Spring Boot deve começar a imprimir o log de simulação em tempo real, priorizando o pedido de ALTA: [SIMULAÇÃO] Drone X: Iniciando carregamento....

4.4. Rastrear a Entrega em Tempo Real (GET /api/entregas/status/{id})
Use o ID do pedido de ALTA prioridade anotado na Etapa 4.1 para monitorar o voo.

Crie uma nova requisição.

Defina o Método como GET.

Insira a URL, substituindo {id} pelo ID do pedido (ex: http://localhost:8080/api/entregas/status/1001).

Clique em Send.

Ação: Repita esta requisição a cada poucos segundos.

Progresso Esperado:

Status: 200 OK

Corpo da Resposta (Em Voo): Uma mensagem dinâmica que mostra a distância atual do drone para o destino. Ex: "O pacote está alocado no Drone #... e está em voo. Distância atual do drone para o destino: X.XX km."

Corpo da Resposta (Concluído): Após o drone entregar todos os pacotes alocados e retornar à base, a mensagem final será: "O pedido #... foi entregue e finalizado."

4.5. Limpeza de Fila e Segunda Alocação
Se houverem pedidos restantes (BAIXA e MEDIA), repita a alocação.

Confirme no endpoint GET /api/drones/status que o drone que voou está novamente no estado IDLE.

Envie novamente o POST /api/simulacao/alocar para iniciar o próximo ciclo de entregas.