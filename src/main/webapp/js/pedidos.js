const API_PEDIDOS = 'http://localhost:8080/ecommerce/api/pedidos';
const API_PRODUTOS = 'http://localhost:8080/ecommerce/api/produtos';

document.addEventListener('DOMContentLoaded', () => {
    carregarPedidos();
    carregarProdutos();
});

async function carregarProdutos() {
    const response = await fetch(API_PRODUTOS);
    const produtos = await response.json();

    const select = document.getElementById('idProduto');
    produtos.forEach(produto => {
        const option = document.createElement('option');
        option.value = produto.id;
        option.textContent = `${produto.nome} - R$ ${produto.preco}`;
        select.appendChild(option);
    });
}

async function carregarPedidos() {
    const response = await fetch(API_PEDIDOS);
    const pedidos = await response.json();

    const tbody = document.getElementById('tbody-pedidos');
    tbody.innerHTML = '';

    pedidos.forEach(pedido => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${pedido.id}</td>
            <td>${pedido.nomeCliente}</td>
            <td>${pedido.idProduto}</td>
            <td>${pedido.quantidade}</td>
            <td>R$ ${pedido.total}</td>
            <td>${pedido.status}</td>
            <td>
                <button onclick="editarStatus(${pedido.id})">✏️ Editar</button>
                <button onclick="excluirPedido(${pedido.id})">🗑️ Excluir</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function criarPedido(event) {
    event.preventDefault();

    const pedido = {
        nomeCliente: document.getElementById('nomeCliente').value,
        idProduto: Number(document.getElementById('idProduto').value),
        quantidade: Number(document.getElementById('quantidade').value)
    };

    await fetch(API_PEDIDOS, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(pedido)
    });

    document.getElementById('form-pedido').reset();
    carregarPedidos();
}

async function editarStatus(id) {
    const novoStatus = prompt('Novo status (PENDENTE, CONFIRMADO, CANCELADO):');
    if (!novoStatus) return;

    await fetch(`${API_PEDIDOS}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: novoStatus.toUpperCase() })
    });

    carregarPedidos();
}

async function excluirPedido(id) {
    if (!confirm('Deseja excluir este pedido?')) return;

    await fetch(`${API_PEDIDOS}/${id}`, {
        method: 'DELETE'
    });

    carregarPedidos();
}

document.getElementById('form-pedido').addEventListener('submit', criarPedido);