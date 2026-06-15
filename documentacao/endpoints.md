# Documentação de Endpoints — E-commerce API

Base URL: `http://localhost:8080/ecommerce`

---

## Produtos

| Método | Endpoint | Descrição | Body (JSON) | Resposta |
|--------|----------|-----------|-------------|----------|
| GET | `/api/produtos` | Lista todos os produtos | — | `200` Array de produtos |
| GET | `/api/produtos/{id}` | Busca produto por ID | — | `200` Produto / `404` Não encontrado |
| GET | `/api/produtos?categoria=X` | Filtra produtos por categoria | — | `200` Array filtrado |
| POST | `/api/produtos` | Cria novo produto | `{ nome, categoria, preco, estoque, ativo }` | `201` Produto criado / `400` Erro de validação |
| PUT | `/api/produtos/{id}` | Atualiza produto existente | `{ nome, categoria, preco, estoque, ativo }` | `200` Produto atualizado / `404` Não encontrado |
| DELETE | `/api/produtos/{id}` | Remove produto | — | `200` Mensagem de sucesso / `404` Não encontrado |

### Exemplo — Criar Produto (POST)

```json
{
  "nome": "Mouse Gamer",
  "categoria": "Acessórios",
  "preco": 150.00,
  "estoque": 30,
  "ativo": true
}
```

### Exemplo — Resposta de Erro

```json
{
  "erro": "O campo 'nome' é obrigatório"
}
```

---

## Pedidos

| Método | Endpoint | Descrição | Body (JSON) | Resposta |
|--------|----------|-----------|-------------|----------|
| GET | `/api/pedidos` | Lista todos os pedidos | — | `200` Array de pedidos |
| GET | `/api/pedidos/{id}` | Busca pedido por ID | — | `200` Pedido / `404` Não encontrado |
| POST | `/api/pedidos` | Cria novo pedido | `{ nomeCliente, idProduto, quantidade }` | `201` Pedido criado / `400` Erro de validação |
| PUT | `/api/pedidos/{id}` | Atualiza pedido (inclusive status) | `{ nomeCliente, idProduto, quantidade, status }` | `200` Pedido atualizado / `404` Não encontrado |
| DELETE | `/api/pedidos/{id}` | Remove pedido | — | `200` Mensagem de sucesso / `404` Não encontrado |

### Regras do POST /api/pedidos

- O `idProduto` enviado **deve existir** na lista de produtos
- O `total` é calculado automaticamente: `preco do produto × quantidade`
- O `status` inicial é sempre `PENDENTE`

### Exemplo — Criar Pedido (POST)

```json
{
  "nomeCliente": "Ana Costa",
  "idProduto": 1,
  "quantidade": 2
}
```

### Exemplo — Resposta (201 Created)

```json
{
  "id": 4,
  "nomeCliente": "Ana Costa",
  "idProduto": 1,
  "quantidade": 2,
  "total": 7000.00,
  "status": "PENDENTE"
}
```

### Exemplo — Alterar Status (PUT)

```json
{
  "status": "CONFIRMADO"
}
```

---

## Status disponíveis para Pedido

| Status | Descrição |
|--------|-----------|
| `PENDENTE` | Pedido aguardando confirmação |
| `CONFIRMADO` | Pedido confirmado |
| `CANCELADO` | Pedido cancelado |
