# 🚁 Drone Simulator - Desafio Técnico DTI

Este projeto simula a operação de uma frota de drones urbanos para entrega de encomendas, implementando lógica de alocação (prioridade, capacidade, autonomia) e simulação de estados de voo em uma aplicação Spring Boot 3.3.1.

# 1. Pré-requisitos 
Para rodar o simulador localmente, você precisa ter as seguintes ferramentas instaladas:

**Java Development Kit (JDK):** Versão 21 ou superior (o projeto usa java.version 21).

**Apache Maven:** Versão 3.6.0 ou superior (para gerenciamento de dependências e build).

**Cliente REST:** Thunder Client (extensão do VS Code) ou Postman para testar os endpoints.

# 2. Configuração do Ambiente e Compilação
Siga estes passos para preparar o ambiente e construir o projeto.

## 2.1. Clonar e Navegar

Clone o repositório através do git Desktop e navegue até a pasta raiz do projeto no seu terminal:
          
              URL: https://github.com/JoaoPedromourinhaSantos/CaseDTI.git
              

## 2.2. Compilação e Instalação

Use o Maven para limpar, compilar e empacotar a aplicação, garantindo que o Lombok gere todos os métodos de acesso (getters/setters).

### **Primeiro Passo:** crie um novo terminal dentro da pasta CASEDTI 


<img width="1916" height="1028" alt="CriandoTerminal" src="https://github.com/user-attachments/assets/a4b9921f-854b-46ec-a556-24573f293a20" />

### **Segundo Passo**: use os dois comandos abaixo dentro do terminal          
         1.  mvn clean 
         
         2. mvn install

**Resultado Esperado: [INFO] BUILD SUCCESS**

<img width="1328" height="344" alt="mvnclean" src="https://github.com/user-attachments/assets/04847115-0bab-409a-81c0-abb79dabee5c" />
<img width="1327" height="789" alt="mvninstall" src="https://github.com/user-attachments/assets/eb0155f1-0064-46cd-9a7b-92f7ed7195e5" />


# 3. Execução da Aplicação
Inicie o servidor Spring Boot, que ficará ativo na porta 8080.

  Dentro do terminal, cole esse comando abaixo:

    java -jar target/drone-simulator-0.0.1-SNAPSHOT.jar
    
**Resultado Esperado: O console exibirá o logo do Spring Boot e a mensagem: Tomcat started on port 8080 (http)**

<img width="1331" height="400" alt="javar-jar" src="https://github.com/user-attachments/assets/4f0eb550-c8e5-4048-af79-9eed8155c793" />


**O servidor estará ativo em http://localhost:8080.**

# 4. Teste da API com Thunder Client (Ciclo Completo)

**Caso você não tenha a extensão ainda, va em extensões e procure a extensão Thunder CLient, depois clique em instalar.**

<img width="640" height="1012" alt="thunderclient" src="https://github.com/user-attachments/assets/7e9a2335-08fb-4485-b33e-dd3aca1cedd0" />

Depois de instalar, essa logo vai aparecer na barra lateral do VS CODE:

<img width="1914" height="873" alt="OndeAparece" src="https://github.com/user-attachments/assets/fc16ac13-fc74-4fc8-ae0f-85f22220d505" />


 Com o servidor rodando na porta 8080, teste as funcionalidades da API na ordem correta: 

   ### 1. criação de pedidos
   ### 2.  checagem de status
   ### 3.   alocação e rastreamento.

## 4.1. Criar um Pedido (POST /api/pedidos)
Vamos criar três pedidos (ALTA, MEDIA, BAIXA) para testar a priorização.

1. Abra o Thunder Client e clique em "New Request".

2. Defina o Método como POST.

3. Insira a URL: http://localhost:8080/api/pedidos

4. Vá para a aba Query e adicione os seguintes pares Name e Value (Repita para os 3 pedidos, alterando os valores):
* **pesoKg**:	8.0  **prioridade**: BAIXA **x**:	3.0 **y**:	0.0
* **pesoKg**: 1.5  **prioridade**: ALTA  **x**:	0.0 **y**: 2.0
* **pesoKg**: 3.0  **prioridade**: MEDIA  **x**: 1.0 **y**:	1.0

## Ação: Envie as 3 requisições (uma por vez), basta clicar em "SEND" para enviar.

**EXEMPLOS**
URL USADA: http://localhost:8080/api/pedidos?pesoKg=8.0&prioridade=BAIXA&x=3.0&y=0.0
<img width="1917" height="873" alt="criandoRequisição" src="https://github.com/user-attachments/assets/cb2a90c7-c6ac-4002-927c-0344e6e5aa73" />

URL USADA: http://localhost:8080/api/pedidos?pesoKg=1.5&prioridade=ALTA&x=0.0&y=2.0
<img width="1921" height="872" alt="CriandoRequisição2" src="https://github.com/user-attachments/assets/edc8215e-b0ed-4212-9d9c-dab8a8f6029b" />

URL USADA: http://localhost:8080/api/pedidos?pesoKg=3.0&prioridade=MEDIA&x=1.0&y=1.0
<img width="1926" height="875" alt="criandoRequisição3" src="https://github.com/user-attachments/assets/4bdced8b-17c2-4eb4-9217-45e9173317e1" />

Resultado Esperado:

Status: 201 Created

Corpo da Resposta: Um objeto Pedido com um id (EXEMPLO DAS IMAGENS: 1000, 1001, 1002). 

Anote o ID do pedido de ALTA prioridade para o rastreamento (Etapa 4.4).



### 4.2. Verificar Status Inicial dos Drones (GET /api/drones/status)

Confirme se os drones estão disponíveis para a alocação.

1. Crie uma nova requisição.

2. Defina o Método como GET.

3. Insira a URL: http://localhost:8080/api/drones/status

4. Clique em Send.

Resultado Esperado:

Status: 200 OK

Corpo da Resposta: Uma lista (array) contendo 3 objetos Drone. Todos devem ter:

estado: IDLE

localizacao: { "x": 0.0, "y": 0.0 }

IMAGEM EXEMPLO: 
<img width="1911" height="1009" alt="VerificarStatusInicialDosDrones" src="https://github.com/user-attachments/assets/81e5fcb5-d92d-4a22-a735-fc5880829387" />


### 4.3. Iniciar a Alocação (POST /api/simulacao/alocar)
Este é o gatilho que inicia o algoritmo de roteirização e a simulação de voo em segundo plano.

1. Crie uma nova requisição.

2. Defina o Método como POST.

3. Insira a URL: http://localhost:8080/api/simulacao/alocar

**Não utilize corpo nem parâmetros.**

4. Clique em Send.

Resultados Esperados:

Status: 200 OK

Corpo da Resposta: "Processo de alocação de drones iniciado. Verifique o console para a simulação de voo."

IMAGEM EXEMPLO:
<img width="1922" height="1014" alt="iniciarAlocação" src="https://github.com/user-attachments/assets/500525ad-71b4-492f-a96f-cc0ba4644d4b" />



Terminal: O console do seu servidor Spring Boot deve começar a imprimir o log de simulação em tempo real, priorizando o pedido de ALTA: [SIMULAÇÃO] Drone X: Iniciando carregamento....

<img width="1919" height="1032" alt="Resposta Terminal" src="https://github.com/user-attachments/assets/dd7c9ad7-2776-47e8-bddf-277b290511f6" />

4.4. Rastrear a Entrega em Tempo Real (GET /api/entregas/status/{id})
Use o ID do pedido de ALTA prioridade anotado na Etapa 4.1 para monitorar o voo.

1. Crie uma nova requisição.

2. Defina o Método como GET.

3. Insira a URL, substituindo {id} pelo ID do pedido (ex: http://localhost:8080/api/entregas/status/1001).

4. Clique em Send.

**Ação: Repita esta requisição a cada poucos segundos.**

Progresso Esperado:

Status: 200 OK

Corpo da Resposta (Em Voo): Uma mensagem dinâmica que mostra a distância atual do drone para o destino. Ex: "O pacote está alocado no Drone #... e está em voo. Distância atual do drone para o destino: X.XX km."

<img width="1916" height="642" alt="processosfinais" src="https://github.com/user-attachments/assets/f479ba83-3617-4362-b66f-3be825559a57" />

Corpo da Resposta (Concluído): Após o drone entregar todos os pacotes alocados e retornar à base, a mensagem final será: "O pedido #... foi entregue e finalizado."
<img width="1926" height="651" alt="processosfinais2" src="https://github.com/user-attachments/assets/fa80c65d-d152-4d90-b870-2b630a81d577" />

4.5. Limpeza de Fila e Segunda Alocação
Se houverem pedidos restantes (BAIXA e MEDIA), repita a alocação.

Confirme no endpoint GET /api/drones/status que o drone que voou está novamente no estado IDLE.

<img width="1916" height="1015" alt="processosfinais3" src="https://github.com/user-attachments/assets/659eaf9d-c8e3-4f7e-adce-a5cc249e8cf6" />

Envie novamente o POST /api/simulacao/alocar para iniciar o próximo ciclo de entregas.

caso ão haja mais nenhuma entrega disponivel vai aparecer essa mensagem: 

<img width="1919" height="1029" alt="processosfinais4" src="https://github.com/user-attachments/assets/cddd6d49-fd59-4d80-a1e1-c028b52eff40" />
