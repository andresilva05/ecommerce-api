package br.com.ecommerce.servlet;

import br.com.ecommerce.model.Pedido;
import br.com.ecommerce.model.Produto;
import br.com.ecommerce.model.StatusPedido;
import br.com.ecommerce.util.JsonUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PedidoServlet", urlPatterns = {"/api/pedidos/*"})
public class PedidoServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    public void init() {
        ServletContext ctx = getServletContext();

        if (ctx.getAttribute("pedidos") == null) {
            List<Pedido> pedidos = new ArrayList<>();
            pedidos.add(new Pedido(1L, "João Silva", 1L, 2, new BigDecimal("7000.00"), StatusPedido.CONFIRMADO));
            pedidos.add(new Pedido(2L, "Maria Souza", 2L, 3, new BigDecimal("360.00"), StatusPedido.PENDENTE));
            pedidos.add(new Pedido(3L, "Carlos Pereira", 3L, 1, new BigDecimal("450.00"), StatusPedido.CANCELADO));
            ctx.setAttribute("pedidos", pedidos);
            ctx.setAttribute("pedidoIdCounter", 4L);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Pedido> getPedidos() {
        return (List<Pedido>) getServletContext().getAttribute("pedidos");
    }

    @SuppressWarnings("unchecked")
    private List<Produto> getProdutos() {
        return (List<Produto>) getServletContext().getAttribute("produtos");
    }

    private long getNextId() {
        long id = (long) getServletContext().getAttribute("pedidoIdCounter");
        getServletContext().setAttribute("pedidoIdCounter", id + 1);
        return id;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        List<Pedido> pedidos = getPedidos();

        if (pathInfo != null && !pathInfo.equals("/")) {
            try {
                Long id = Long.parseLong(pathInfo.substring(1));
                Pedido pedido = pedidos.stream()
                        .filter(p -> p.getId().equals(id))
                        .findFirst()
                        .orElse(null);

                if (pedido == null) {
                    JsonUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            Map.of("erro", "Pedido não encontrado com id: " + id));
                    return;
                }
                JsonUtil.sendResponse(resp, HttpServletResponse.SC_OK, pedido);
                return;
            } catch (NumberFormatException e) {
                JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Map.of("erro", "ID inválido"));
                return;
            }
        }

        JsonUtil.sendResponse(resp, HttpServletResponse.SC_OK, pedidos);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = getRequestBody(req);
        Pedido novoPedido = gson.fromJson(body, Pedido.class);

        if (novoPedido.getNomeCliente() == null || novoPedido.getNomeCliente().isBlank()) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "O campo 'nomeCliente' é obrigatório"));
            return;
        }
        if (novoPedido.getIdProduto() == null) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "O campo 'idProduto' é obrigatório"));
            return;
        }
        if (novoPedido.getQuantidade() <= 0) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "A quantidade deve ser maior que zero"));
            return;
        }

        List<Produto> produtos = getProdutos();
        if (produtos == null) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Map.of("erro", "Sistema de produtos não inicializado."));
            return;
        }

        Produto produtoRef = produtos.stream()
                .filter(p -> p.getId().equals(novoPedido.getIdProduto()))
                .findFirst()
                .orElse(null);

        if (produtoRef == null) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "Produto não encontrado com id: " + novoPedido.getIdProduto()));
            return;
        }

        BigDecimal total = produtoRef.getPreco().multiply(new BigDecimal(novoPedido.getQuantidade()));
        novoPedido.setTotal(total);
        novoPedido.setStatus(StatusPedido.PENDENTE);
        novoPedido.setId(getNextId());

        getPedidos().add(novoPedido);

        JsonUtil.sendResponse(resp, HttpServletResponse.SC_CREATED, novoPedido);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "ID do pedido é obrigatório na URL"));
            return;
        }

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            Pedido pedidoExistente = getPedidos().stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (pedidoExistente == null) {
                JsonUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        Map.of("erro", "Pedido não encontrado com id: " + id));
                return;
            }

            String body = getRequestBody(req);
            Pedido dadosAtualizados = gson.fromJson(body, Pedido.class);

            if (dadosAtualizados.getNomeCliente() != null && !dadosAtualizados.getNomeCliente().isBlank()) {
                pedidoExistente.setNomeCliente(dadosAtualizados.getNomeCliente());
            }
            if (dadosAtualizados.getIdProduto() != null) {
                pedidoExistente.setIdProduto(dadosAtualizados.getIdProduto());
            }
            if (dadosAtualizados.getQuantidade() > 0) {
                pedidoExistente.setQuantidade(dadosAtualizados.getQuantidade());
            }
            if (dadosAtualizados.getStatus() != null) {
                pedidoExistente.setStatus(dadosAtualizados.getStatus());
            }

            List<Produto> produtos = getProdutos();
            Produto produtoRef = produtos != null ? produtos.stream()
                    .filter(p -> p.getId().equals(pedidoExistente.getIdProduto()))
                    .findFirst()
                    .orElse(null) : null;

            if (produtoRef != null) {
                BigDecimal total = produtoRef.getPreco().multiply(new BigDecimal(pedidoExistente.getQuantidade()));
                pedidoExistente.setTotal(total);
            }

            JsonUtil.sendResponse(resp, HttpServletResponse.SC_OK, pedidoExistente);

        } catch (NumberFormatException e) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "ID inválido"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "ID do pedido é obrigatório na URL"));
            return;
        }

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            boolean removido = getPedidos().removeIf(p -> p.getId().equals(id));

            if (!removido) {
                JsonUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        Map.of("erro", "Pedido não encontrado com id: " + id));
                return;
            }

            JsonUtil.sendResponse(resp, HttpServletResponse.SC_OK,
                    Map.of("mensagem", "Pedido removido com sucesso"));

        } catch (NumberFormatException e) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "ID inválido"));
        }
    }

    private String getRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
