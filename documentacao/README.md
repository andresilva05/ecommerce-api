# E-commerce API

API REST de e-commerce desenvolvida com **Java Servlets** e **Jakarta EE**, utilizando **Gson** para serialização JSON e **Bootstrap 5** no front-end.

## Integrantes

- André
- Davi
- Gustavo

## Tecnologias Utilizadas

- Java 21
- Jakarta Servlet 6.1
- Gson 2.11.0
- Bootstrap 5.3.3
- Apache Tomcat 10+
- Maven

## Como Executar

### Pré-requisitos

- JDK 21 instalado
- Apache Tomcat 10+ instalado
- Maven instalado

### Passo a passo

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/andresilva05/ecommerce-api.git
   cd ecommerce-api
   ```

2. **Compile o projeto com Maven:**
   ```bash
   mvn clean package
   ```

3. **Copie o WAR gerado para o Tomcat:**
   ```bash
   cp target/ecommerce-1.0-SNAPSHOT.war <TOMCAT_HOME>/webapps/ecommerce.war
   ```

4. **Inicie o Tomcat:**
   ```bash
   <TOMCAT_HOME>/bin/startup.sh     # Linux/Mac
   <TOMCAT_HOME>/bin/startup.bat    # Windows
   ```

5. **Acesse no navegador:**
   - Página inicial: `http://localhost:8080/ecommerce/index.html`
   - Produtos: `http://localhost:8080/ecommerce/produtos.html`
   - Pedidos: `http://localhost:8080/ecommerce/pedidos.html`

## Estrutura do Projeto

```
ecommerce-api/
├── src/main/java/br/com/ecommerce/
│   ├── model/
│   │   ├── Produto.java
│   │   ├── Pedido.java
│   │   └── StatusPedido.java
│   ├── servlet/
│   │   ├── ProdutoServlet.java
│   │   └── PedidoServlet.java
│   └── util/
│       └── JsonUtil.java
├── src/main/webapp/
│   ├── index.html
│   ├── produtos.html
│   ├── pedidos.html
│   ├── css/style.css
│   └── js/pedidos.js
├── documentacao/
│   ├── README.md
│   └── endpoints.md
└── pom.xml
```

## Dados Iniciais

A aplicação já inicia com dados pré-populados via `init()` dos Servlets:

- **3 Produtos:** Notebook Dell, Camiseta Nike, Cafeteira Expresso
- **3 Pedidos:** vinculados aos produtos acima com status variados
