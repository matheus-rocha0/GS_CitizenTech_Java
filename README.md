# Fila Zero (UPA Agora)

Projeto desenvolvido para a Global Solution 2026.2 da FIAP (Engenharia de Software - Domain Driven Design com Java).

## Integrantes

- Caio Nascimento Battista (RM 561383)
- Lucas Cavalcante (RM 562857)
- Manoah (RM 563713)
- Matheus Rocha (RM 561689)

## Links

- Repositório Back-end Java (GitHub): https://github.com/matheus-rocha0/GS_CitizenTech_Java.git
- Repositório Front-end FilaZero (GitHub, branch matheus-dev): https://github.com/Luqetaa/FilaZero.git
- Deploy (Render): https://gs-citizentech-java.onrender.com

## O que o projeto faz

O Fila Zero (UPA Agora) junta a API REST em Java Spring Boot com o front-end FilaZero para mostrar a lotação e o tempo estimado de espera nas unidades públicas de saúde de São Paulo (UBS, UPAs e hospitais), focado em atendimentos de baixa e média complexidade (classificação verde e azul).

Funcionalidades implementadas:

- Cálculo da fila e do tempo de espera pela Lei de Little (`W = L / u`): a API conta quantas senhas foram emitidas, chamadas e canceladas em cada unidade para calcular quantas pessoas estão aguardando (`L`), a vazão por minuto (`u`) e a faixa estimada de espera, classificando a unidade em `TRANQUILA`, `MODERADA`, `CHEIA`, `DESATUALIZADO` ou `SEM_DADOS`.
- Bloqueio de CPF pela LGPD: os eventos de senha usam apenas uma referência anônima (`refSenha`). Se alguém tentar enviar um CPF formatado ou 11 dígitos seguidos em `POST /api/eventos` ou `PUT /api/eventos/{id}`, a API rejeita com erro HTTP 400.
- Integração com o mapa do FilaZero: o endpoint `GET /api/unidades/catalogo` entrega as 30 unidades carregadas no banco (10 UPAs, 10 Hospitais e 10 UBS de São Paulo) para preencher o mapa e a lista do front-end.
- Ranking comparativo: o endpoint `GET /api/unidades/comparativo` soma o tempo de deslocamento com a espera estimada e ordena as unidades pelo menor tempo total.
- Cadastro, atualização e exclusão de unidades: suporte completo a `POST`, `PUT` e `DELETE` em `/api/unidades`, com ajuste dos limiares de lotação e do status de integração (`ONLINE`, `SEM_DADOS`, `OFFLINE`).
- Emissão, chamada e exclusão de eventos de senha no totem: suporte a `GET`, `POST`, `PUT` e `DELETE` em `/api/eventos`, atualizando o horário da unidade e recalculando a fila na hora.

## Passo a passo para rodar

Requisitos: Java 21 (ou superior) no IntelliJ IDEA e Node.js 22+ para o front-end.

1. Subir o Back-end Java:
   - Abra a pasta `JAVA` no IntelliJ IDEA.
   - Abra a classe `src/main/java/br/com/fiap/upaagora/UpaAgoraApplication.java` e clique em Play (Run).
   - A API Java sobe na porta `8080` e cria automaticamente o banco H2 local na pasta `data/` já preenchido com as 30 unidades e eventos iniciais.
   - Se preferir rodar pelo terminal na pasta `JAVA`, use `mvn spring-boot:run`.
   - Para conectar ao banco Oracle da FIAP em vez do H2 local, rode o arquivo `script.sql` no Oracle SQL Developer e defina as variáveis `DB_USE_ORACLE=true`, `DB_URL`, `DB_USER` e `DB_PASSWORD` antes de iniciar a aplicação.

2. Subir o Front-end FilaZero:
   - Abra um terminal na pasta `FilaZero` (na branch `matheus-dev`).
   - Na primeira vez, rode `npm install` (ou `npm.cmd install` no PowerShell).
   - Rode `npm run dev` (ou `npm.cmd run dev` no PowerShell).
   - Abra `http://127.0.0.1:5173` no navegador. O Vite encaminha automaticamente `/api/unidades` e `/api/eventos` para `http://127.0.0.1:8080`.

Observação: o build do front-end também está em `src/main/resources/static`, então também dá para abrir direto em `http://localhost:8080` apenas com o Java rodando.

## Como testar as funcionalidades e requisições (GET, POST, PUT e DELETE)

1. Na tela principal (`http://127.0.0.1:5173`):
   - Teste de `GET`: assim que a página abre, o front-end busca as 30 unidades em `GET /api/unidades/catalogo`. No topo da lista aparece o banner verde confirmando a conexão com a API Java, e nos cartões aparece a marcação `GET API JAVA SPRING BOOT / ORACLE FIAP`. Clique no botão `Ver retorno do GET (Java)` no banner para abrir um alerta mostrando quantas unidades vieram e o resumo do JSON.
   - Use a barra de busca por nome ou bairro e os filtros `Todas`, `UBS`, `UPA` e `Hospitais` para navegar pelas unidades no mapa.

2. Na tela de Simular trajetos (`http://127.0.0.1:5173/simulador.html`):
   - Clique em `Simular trajetos` no menu superior da tela principal. A seção `Ajustar cenário` já abre exibindo o painel `API Java - Unidades e Senhas`.
   - Teste de `GET`: clique em `Sincronizar` para consultar `GET /api/unidades/comparativo` e `GET /api/eventos?idUnidade=...`, vendo a lotação atual, os eventos recentes e o alerta na tela.
   - Teste de `POST` de senha e bloqueio LGPD: em `Totem da UPA (POST /api/eventos)`, escolha `Etapa`, `Risco`, preencha `Referência da senha` (por exemplo `FZ-001`) e clique em `Emitir senha` ou `Chamar senha`. A fila e a lista `Comparar unidades` são recalculadas na hora. Para testar a regra da LGPD, digite um CPF (como `123.456.789-00`) no campo `Referência da senha` e clique em `Emitir senha` para ver o alerta vermelho com o erro HTTP 400 retornado pelo Java.
   - Teste de `POST` de unidade: abra `Cadastrar unidade Java (POST)`, preencha nome, endereço, bairro, distância e tempo de deslocamento e clique em `Cadastrar unidade`.
   - Teste de `PUT` de unidade: escolha uma unidade em `Unidade na API Java`, altere `Limiar moderada (min)`, `Limiar cheia (min)` ou `Status de integração` (`ONLINE`, `SEM_DADOS`, `OFFLINE`) e clique em `Atualizar unidade Java`.
   - Teste de `DELETE`: em `Eventos recentes (GET / DELETE)`, clique no botão de excluir de um evento para chamar `DELETE /api/eventos/{id}`, ou clique em `Excluir unidade selecionada (DELETE)` para chamar `DELETE /api/unidades/{id}`.

## Endpoints da API

Unidades (`/api/unidades`):

- `GET /api/unidades`: lista todas as unidades cadastradas
- `GET /api/unidades/catalogo`: retorna as unidades no formato consumido pelo front-end FilaZero
- `GET /api/unidades/comparativo`: retorna as unidades ordenadas pelo menor tempo total (deslocamento + espera)
- `GET /api/unidades/{id}`: busca uma unidade pelo ID
- `GET /api/unidades/{id}/status`: calcula a lotação e o tempo estimado de espera da unidade
- `POST /api/unidades`: cadastra uma nova unidade
- `PUT /api/unidades/{id}`: atualiza dados, limiares e status de integração de uma unidade
- `DELETE /api/unidades/{id}`: remove os eventos da unidade e exclui a unidade pelo ID

Eventos de senha (`/api/eventos`):

- `GET /api/eventos`: lista todos os eventos (aceita `?idUnidade={id}` para filtrar)
- `GET /api/eventos/{id}`: busca um evento pelo ID
- `POST /api/eventos`: registra um evento (`SENHA_EMITIDA`, `SENHA_CHAMADA` ou `SENHA_CANCELADA`) com validação de CPF (LGPD)
- `PUT /api/eventos/{id}`: atualiza um evento pelo ID
- `DELETE /api/eventos/{id}`: exclui um evento pelo ID
