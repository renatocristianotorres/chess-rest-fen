# Esse é um projeto da disciplina de desenvolvimento de sistemas, implementado em Spring Boot

# Professor Renato Cristiano Torres

O projeto consiste em um backend Spring Boot para um jogo de xadrez via REST.

## A implementação será gradual em sala de aula abaixo as sequências :

Criação as classes base utilizando técnicas e obras práticas SOLID, REST, ORM e Clean Code.


- Fase 1:

- Estado do tabuleiro em **FEN** (string).
- Endpoints REST:
    - `POST /api/games` cria partida
    - `GET /api/games/{id}` detalhes (inclui FEN e board 8x8 derivado)
    - `GET /api/games` lista partidas
    - `POST /api/games/{id}/moves` registra jogada
- Regras (implementação progressiva):
    - **Somente peões (P/p) e torres (R/r)** podem se mover nessa fase
    - Peão: 1 passo, 2 do inicial, captura diagonal
    - Torre: linha reta, sem pular peças
    - Turno (WHITE/BLACK)

- Fase 2:
    - **Promoção automática de peão** ao chegar na última fileira (vira **rainha** Q/q)
    - **Fim simples**: se um rei for capturado, a partida fica `FINALIZADO` e preenche a variável `winner` com o lado vitorioso (WHITE ou BLACK)

- Fase 3: a implementar movimentação das outras peças
- Fase 4: implementação da caracterização dos jogadores
- Fase 5: Implementação de Multijogo

## Requisitos

- Java 17+
- Maven 3.8+

## Como rodar

Na pasta do projeto:

```bash
mvn spring-boot:run
```

Servidor sobe em `http://localhost:8080`.

H2 console: `http://localhost:8080/h2`  
JDBC URL: `jdbc:h2:mem:chessdb` (user `sa`, senha vazia)

---

## Testes rápidos (cURL)

### 1) Criar partida

```bash
curl -X POST http://localhost:8080/api/games
```

### 2) Ver partida (id 1)

```bash
curl http://localhost:8080/api/games/1
```

### 3) Jogada branca

```bash
curl -X POST http://localhost:8080/api/games/1/moves   -H "Content-Type: application/json"   -d '{"fromSquare":"e2","toSquare":"e4","playerColor":"WHITE"}'
```

### 4) Jogada preta

```bash
curl -X POST http://localhost:8080/api/games/1/moves   -H "Content-Type: application/json"   -d '{"fromSquare":"e7","toSquare":"e5","playerColor":"BLACK"}'
```

### 5) Exemplo de erro (torre bloqueada)

```bash
curl -X POST http://localhost:8080/api/games/1/moves   -H "Content-Type: application/json"   -d '{"fromSquare":"a1","toSquare":"a3","playerColor":"WHITE"}'
```

---

## Próximas fases sugeridas

1. Promoção do peão
2. Cavalo e bispo
3. Rainha
4. Rei + xeque (e xeque-mate)
5. Roque (usar campo KQkq no FEN)
6. En passant (usar campo en-passant no FEN)

---

## Endpoint de tabuleiro (amigável para frontend)

`GET /api/games/{id}/board`

Retorna um JSON com:

- `files`: ["a","b","c","d","e","f","g","h"]
- `ranks`: [8,7,6,5,4,3,2,1]
- `grid`: matriz 8x8 (strings de 1 caractere: "P","p","R","." etc.)
- `fen`, `turn`, `winner`, `status`

Exemplo:

```bash
curl http://localhost:8080/api/games/1/board
```
