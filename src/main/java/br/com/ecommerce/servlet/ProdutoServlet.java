package br.com.ecommerce.servlet;

import br.com.ecommerce.model.Produto;
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

import java.util.stream.Collectors;

@WebServlet(name = "ProdutoServlet", urlPatterns = {"/api/produtos/*"})
public class ProdutoServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    public void init() {
        ServletContext ctx = getServletContext();

        if (ctx.getAttribute("produtos") == null) {
            List<Produto> produtos = new ArrayList<>();
            produtos.add(new Produto(1L, "Notebook Dell", "Eletrônicos", 10, true, new BigDecimal("3500.00")));
            produtos.add(new Produto(2L, "Camiseta Nike", "Roupas", 50, true, new BigDecimal("120.00")));
            produtos.add(new Produto(3L, "Cafeteira Expresso", "Casa", 15, true, new BigDecimal("450.00")));
            ctx.setAttribute("produtos", produtos);
            ctx.setAttribute("produtoIdCounter", 4L);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Produto> getProdutos() {
        return (List<Produto>) getServletContext().getAttribute("produtos");
    }

    private long getNextId() {
        long id = (long) getServletContext().getAttribute("produtoIdCounter");
        getServletContext().setAttribute("produtoIdCounter", id + 1);
        return id;
    }

    // GET /api/produtos → lista todos
    // GET /api/produtos/{id} → busca por ID
    // GET /api/produtos?categoria=X → filtro por categoria
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        List<Produto> produtos = getProdutos();

        // GET /api/produtos/{id}
        if (pathInfo != null && !pathInfo.equals("/")) {
            try {
                Long id = Long.parseLong(pathInfo.substring(1));
                Produto produto = produtos.stream()
                        .filter(p -> p.getId().equals(id))
                        .findFirst()
                        .orElse(null);

                if (produto == null) {
                    JsonUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            Map.of("erro", "Produto não encontrado com id: " + id));
                    return;
                }
                JsonUtil.sendResponse(resp, HttpServletResponse.SC_OK, produto);
                return;
            } catch (NumberFormatException e) {
                JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        Map.of("erro", "ID inválido"));
                return;
            }
        }

        // GET /api/produtos?categoria=X
        String categoria = req.getParameter("categoria");
        if (categoria != null && !categoria.isBlank()) {
            List<Produto> filtrados = produtos.stream()
                    .filter(p -> p.getCategoria().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_OK, filtrados);
            return;
        }

        // GET /api/produtos → lista todos
        JsonUtil.sendResponse(resp, HttpServletResponse.SC_OK, produtos);
    }

    // POST /api/produtos → cria com validação
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = getRequestBody(req);
        Produto novoProduto = gson.fromJson(body, Produto.class);

        // Validação
        if (novoProduto.getNome() == null || novoProduto.getNome().isBlank()) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "O campo 'nome' é obrigatório"));
            return;
        }
        if (novoProduto.getCategoria() == null || novoProduto.getCategoria().isBlank()) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "O campo 'categoria' é obrigatório"));
            return;
        }
        if (novoProduto.getPreco() == null || novoProduto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "O campo 'preco' deve ser maior que zero"));
            return;
        }

        novoProduto.setId(getNextId());
        getProdutos().add(novoProduto);

        JsonUtil.sendResponse(resp, HttpServletResponse.SC_CREATED, novoProduto);
    }

    // PUT /api/produtos/{id} → atualiza
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "ID do produto é obrigatório na URL"));
            return;
        }

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            Produto produtoExistente = getProdutos().stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (produtoExistente == null) {
                JsonUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        Map.of("erro", "Produto não encontrado com id: " + id));
                return;
            }

            String body = getRequestBody(req);
            Produto dadosAtualizados = gson.fromJson(body, Produto.class);

            if (dadosAtualizados.getNome() != null && !dadosAtualizados.getNome().isBlank()) {
                produtoExistente.setNome(dadosAtualizados.getNome());
            }
            if (dadosAtualizados.getCategoria() != null && !dadosAtualizados.getCategoria().isBlank()) {
                produtoExistente.setCategoria(dadosAtualizados.getCategoria());
            }
            if (dadosAtualizados.getPreco() != null) {
                produtoExistente.setPreco(dadosAtualizados.getPreco());
            }
            produtoExistente.setEstoque(dadosAtualizados.getEstoque());
            produtoExistente.setAtivo(dadosAtualizados.isAtivo());

            JsonUtil.sendResponse(resp, HttpServletResponse.SC_OK, produtoExistente);

        } catch (NumberFormatException e) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "ID inválido"));
        }
    }

    // DELETE /api/produtos/{id} → remove
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("erro", "ID do produto é obrigatório na URL"));
            return;
        }

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            boolean removido = getProdutos().removeIf(p -> p.getId().equals(id));

            if (!removido) {
                JsonUtil.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        Map.of("erro", "Produto não encontrado com id: " + id));
                return;
            }

            JsonUtil.sendResponse(resp, HttpServletResponse.SC_OK,
                    Map.of("mensagem", "Produto removido com sucesso"));

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
