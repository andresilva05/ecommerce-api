const API_PEDIDOS = 'http://localhost:8080/ecommerce/api/pedidos';
const API_PRODUTOS = 'http://localhost:8080/ecommerce/api/produtos';

let pedidoIdParaEditar = null;

document.addEventListener('DOMContentLoaded', () => {
    carregarPedidos();
    carregarProdutos();
});

// Carrega os produtos no <select>
async function carregarProdutos() {
    try {
        const response = await fetch(API_PRODUTOS);
        const produtos = await response.json();

        const select = document.getElementById('idProduto');
        produtos.forEach(produto => {
            const option = document.createElement('option');
            option.value = produto.id;
            option.textContent = `${produto.nome} — R$ ${Number(produto.preco).toFixed(2)}`;
            select.appendChild(option);
        });
    } catch (err) {
        console.error('Erro ao carregar produtos:', err);
    }
}

// Carrega a listagem de pedidos na tabela
async function carregarPedidos() {
    try {
        const response = await fetch(API_PEDIDOS);
        const pedidos = await response.json();

        const tbody = document.getElementById('tbody-pedidos');
        tbody.innerHTML = '';

        if (pedidos.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-secondary py-4">Nenhum pedido encontrado.</td>
                </tr>`;
            return;
        }

        pedidos.forEach(pedido => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${pedido.id}</td>
                <td>${pedido.nomeCliente}</td>
                <td>${pedido.idProduto}</td>
                <td>${pedido.quantidade}</td>
                <td>R$ ${Number(pedido.total).toFixed(2)}</td>
                <td>${getBadgeStatus(pedido.status)}</td>
                <td class="text-center">
                    <button class="btn btn-outline-primary btn-action me-1" onclick="abrirModalStatus(${pedido.id}, '${pedido.status}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-outline-danger btn-action" onclick="excluirPedido(${pedido.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error('Erro ao carregar pedidos:', err);
    }
}

// Retorna o badge colorido de acordo com o status
function getBadgeStatus(status) {
    const map = {
        'PENDENTE':   'bg-warning text-dark',
        'CONFIRMADO': 'bg-success',
        'CANCELADO':  'bg-danger'
    };
    const cls = map[status] || 'bg-secondary';
    return `<span class="badge badge-status ${cls}">${status}</span>`;
}

// Criar pedido
async function criarPedido(event) {
    event.preventDefault();

    const pedido = {
        nomeCliente: document.getElementById('nomeCliente').value,
        idProduto: Number(document.getElementById('idProduto').value),
        quantidade: Number(document.getElementById('quantidade').value)
    };

    try {
        await fetch(API_PEDIDOS, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(pedido)
        });
        document.getElementById('form-pedido').reset();
        carregarPedidos();
    } catch (err) {
        console.error('Erro ao criar pedido:', err);
    }
}

// Abre o modal de edição de status
function abrirModalStatus(id, statusAtual) {
    pedidoIdParaEditar = id;
    document.getElementById('selectNovoStatus').value = statusAtual;
    const modal = new bootstrap.Modal(document.getElementById('modalStatus'));
    modal.show();
}

// Salvar novo status via modal
document.getElementById('btnSalvarStatus').addEventListener('click', async () => {
    if (pedidoIdParaEditar === null) return;

    const novoStatus = document.getElementById('selectNovoStatus').value;

    try {
        await fetch(`${API_PEDIDOS}/${pedidoIdParaEditar}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: novoStatus })
        });

        const modalEl = document.getElementById('modalStatus');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal.hide();

        pedidoIdParaEditar = null;
        carregarPedidos();
    } catch (err) {
        console.error('Erro ao atualizar status:', err);
    }
});

// Excluir pedido
async function excluirPedido(id) {
    if (!confirm('Deseja excluir este pedido?')) return;

    try {
        await fetch(`${API_PEDIDOS}/${id}`, {
            method: 'DELETE'
        });
        carregarPedidos();
    } catch (err) {
        console.error('Erro ao excluir pedido:', err);
    }
}

document.getElementById('form-pedido').addEventListener('submit', criarPedido);