# Relatório de Estudo: Projeto E-commerce API

Este documento foi criado para guiar seu estudo sobre o projeto. Ele explica a estrutura, como o código funciona e quais foram as principais decisões técnicas tomadas pela equipe para cumprir a atividade.

---

## 1. Visão Geral da Arquitetura

O projeto segue um modelo **Cliente-Servidor (Client-Server)** usando uma abordagem **RESTful**.

*   **Backend (Servidor):** Construído em **Java** usando a especificação **Jakarta Servlet API**. Ele atua puramente como uma API de dados, expondo endpoints HTTP (URLs) que recebem e retornam JSON. Ele *não* gera as páginas HTML (não estamos usando JSP, por exemplo).
*   **Frontend (Cliente):** Construído com **HTML estático, CSS (Bootstrap 5) e JavaScript (Vanilla)**. O navegador do usuário carrega o HTML/JS, e o JavaScript se comunica ativamente com o servidor via **Fetch API** em segundo plano.

Essa separação clara (Backend só processa dados, Frontend só cuida da interface) é a forma padrão como aplicações web modernas funcionam atualmente.

---

## 2. O Backend (Java / Servlets)

O backend é a "inteligência" e o "banco de dados" temporário da aplicação.

### 2.1. Como os Servlets Funcionam (`ProdutoServlet` e `PedidoServlet`)
Em Java Web tradicional, os **Servlets** são classes que interceptam requisições HTTP (`GET`, `POST`, `PUT`, `DELETE`) e decidem como responder a elas.

No projeto, ambos usam a anotação `@WebServlet(urlPatterns = {"/api/produtos/*"})` e `@WebServlet(urlPatterns = {"/api/pedidos/*"})`. Isso diz ao servidor Tomcat: *"Qualquer requisição que comece com esse caminho deve ser tratada por esta classe"*.

As classes herdam de `HttpServlet` e sobrescrevem métodos específicos:
*   `doGet()`: Chamado para ler dados.
*   `doPost()`: Chamado para criar novos dados (quando recebemos um body no request).
*   `doPut()`: Chamado para atualizar dados existentes.
*   `doDelete()`: Chamado para excluir dados.

**💡 Ponto de Estudo:** Entenda o roteamento. O método `req.getPathInfo()` é a "mágica" para extrair o ID da URL. Por exemplo, se a URL for `/api/produtos/5`, o `getPathInfo()` retorna `"/5"`. A gente usa `substring(1)` para tirar a barra e converter `"5"` para o número `Long 5`.

### 2.2. Armazenamento em Memória (`ServletContext`)
**Por que não usamos Banco de Dados (MySQL, PostgreSQL)?**
Neste projeto acadêmico, o escopo pedia algo funcional e focado no fluxo cliente-servidor, não na persistência em disco.

**A Decisão Técnica:**
Usamos a memória RAM do próprio servidor Tomcat para guardar as listas.
Nos métodos `init()` dos Servlets, fazemos:
```java
getServletContext().setAttribute("produtos", listaDeProdutos);
```
O `ServletContext` é uma área de memória compartilhada global. Ele é o mesmo para *todos* os usuários da aplicação e para *todos* os Servlets. Isso permitiu que o `PedidoServlet` pudesse ler a lista criada pelo `ProdutoServlet` para validar se um produto existia antes de criar um pedido.

### 2.3. Trabalhando com JSON (`Gson` e `JsonUtil`)
Um Servlet nativo não sabe o que é JSON. Se fizéssemos "na mão", teríamos que pegar os objetos Java e colar `"{ \"nome\": \"" + obj.getNome() + "\" }"` um por um, o que seria horrível.

**A Decisão Técnica:**
Adicionamos a biblioteca **Google Gson** no arquivo `pom.xml`.
*   Para enviar: O `Gson` pega a nossa `List<Produto>`, converte magicamente num texto JSON perfeito, e o utilitário `JsonUtil.sendResponse()` joga esse texto pro navegador com o código `200 (OK)` ou `201 (Created)`.
*   Para receber: No `POST` e `PUT`, nós lemos o corpo da requisição linha por linha (com `BufferedReader`) e depois passamos pro Gson: `gson.fromJson(textoJson, Produto.class)`. Ele instancia o objeto Java automaticamente.

---

## 3. O Frontend (HTML / CSS / JS)

O front-end precisava ser interativo, buscando e enviando dados sem recarregar a página inteira.

### 3.1. Estilização com Bootstrap 5
**A Decisão Técnica:**
Em vez de escrever milhares de linhas de CSS do zero para deixar a interface bonita, optamos pelo **Bootstrap 5 via CDN** (link direto no `<head>`).
*   **Por quê?** Produtividade e padronização. Ele nos deu de graça: a responsividade (a grid com `row` e `col`), a `navbar` principal, as tabelas bem formatadas (`table-hover`), botões (`btn-success`, `btn-primary`), e os painéis de layout (`card`).
*   A página de Pedidos feita pelo colega já usava esse padrão. Para entregar uma experiência coesa, reescrevemos o arquivo `produtos.html` que havíamos feito para adotar exatamente as mesmas classes do Bootstrap, parecendo ser o mesmo sistema.

### 3.2. A Comunicação com o Backend (Fetch API)
Em arquivos como o `pedidos.js` (ou no `<script>` dentro de produtos), as requisições pro Java são feitas usando a **Fetch API** assíncrona (`async/await`).

**Por que Fetch API?**
Antigamente usava-se `XMLHttpRequest` ou jQuery `$.ajax`. A Fetch API é o padrão moderno e nativo do JavaScript. Ela retorna `Promises`, tornando o código muito mais limpo com o uso de `async/await`.

**💡 Ponto de Estudo:** Entenda o fluxo de criação.
Quando o usuário clica em "Salvar" no formulário:
1. O evento `onsubmit` é disparado e chamamos `e.preventDefault()` para a página não piscar/recarregar.
2. Lemos os valores dos inputs via `document.getElementById('...').value`.
3. Montamos um objeto JavaScript.
4. Transformamos ele em string JSON com `JSON.stringify()`.
5. Enviamos pro Java via `fetch(url, { method: 'POST', body: ... })`.
6. O Java recebe, valida, processa e retorna o JSON.
7. O JavaScript lê o retorno, avisa o usuário (via `alert`) e chama a função de listar para atualizar a tabela em tempo real.

---

## 4. Resumo de Respostas para a Banca/Avaliação

Se o professor perguntar:

1.  **"Onde os dados ficam salvos se eu fechar o servidor?"**
    *Resposta:* "Eles são perdidos. Optamos por usar o `ServletContext` como um repositório em memória RAM para focar na arquitetura REST e na comunicação HTTP, simulando um banco de dados temporário. Toda vez que o Tomcat sobe, o método `init()` dos Servlets é chamado uma única vez e pré-popula as tabelas com dados de teste."

2.  **"Por que vocês não usaram formulários HTML tradicionais (com `<form action=".../servlet" method="post">`)?"**
    *Resposta:* "Porque queríamos construir uma verdadeira SPA (Single Page Application) consumindo uma API REST. Se usássemos o `form action`, a página iria redirecionar para o servidor e perderíamos o contexto do HTML. Usando JavaScript e `Fetch API`, a página se atualiza dinamicamente e trafega apenas dados em formato JSON, que é a prática recomendada do mercado."

3.  **"Como vocês resolvem a dependência entre Pedidos e Produtos, já que eles não estão no banco?"**
    *Resposta:* "Ambos salvam suas listas no escopo global da aplicação (`getServletContext()`). Dentro do método `doPost` do `PedidoServlet`, nós acessamos a lista de produtos (chave `'produtos'`), procuramos se existe algum produto com o ID enviado, e só permitimos a criação do pedido se o produto de fato existir. Inclusive, usamos o preço desse objeto resgatado para calcular o valor total do pedido no backend, impedindo que o front-end fraude os valores."

4.  **"Como as bibliotecas externas foram gerenciadas?"**
    *Resposta:* "No backend, utilizamos o **Maven** (`pom.xml`). Ele garante que qualquer pessoa da equipe baixe a biblioteca do Gson ou do Jakarta Servlet automaticamente, sem precisar ficar enviando `.jar` pelo WhatsApp. No frontend, usamos **CDN (Content Delivery Network)** para o Bootstrap, de modo que os arquivos vêm diretamente de servidores em nuvem ultrarrápidos em vez de pesar nosso repositório."
