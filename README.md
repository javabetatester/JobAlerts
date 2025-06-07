# JobAlerts API

Sistema de alertas de emprego que permite aos usuários criar alertas personalizados e receber notificações por email quando novas vagas que correspondem aos seus critérios são encontradas.

## 🚀 Funcionalidades

- **Gestão de Usuários**: Criação e gerenciamento de contas de usuário
- **Alertas Personalizados**: Criação de alertas com tags, localização e critérios específicos
- **Busca Automática**: Sistema de busca automática de vagas usando a API JSearch
- **Notificações por Email**: Envio de emails com vagas encontradas
- **Sistema de Tags**: Matching inteligente baseado em tags obrigatórias e opcionais
- **Histórico de Vagas**: Controle de duplicatas para não enviar vagas já enviadas
- **Scheduler**: Execução automática de buscas a cada hora

## 🛠️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Mail**
- **H2 Database** (desenvolvimento)
- **PostgreSQL** (produção)
- **Maven**
- **Lombok**
- **SpringDoc OpenAPI** (documentação)

## 📋 Pré-requisitos

- Java 17 ou superior
- Maven 3.6 ou superior
- Conta no RapidAPI (para JSearch)
- Conta Gmail (para envio de emails)

## ⚙️ Configuração

### 1. Clone o Repositório

```bash
git clone <url-do-repositorio>
cd JobAlerts
```

### 2. Configuração da API JSearch

#### Criando Conta no RapidAPI:

1. Acesse [RapidAPI](https://rapidapi.com/)
2. Clique em "Sign Up" e crie sua conta
3. Após fazer login, busque por "JSearch"
4. Selecione a API "JSearch - Job Search API"
5. Clique em "Subscribe to Test"
6. Escolha o plano (há opções gratuitas disponíveis)
7. Após a assinatura, vá para a aba "Endpoints"
8. Copie sua API Key (X-RapidAPI-Key)

#### Configurando no Projeto:

No arquivo `src/main/resources/application.properties`, substitua:

```properties
jsearch.api.key=SUA_API_KEY_AQUI
```

### 3. Configuração do Gmail para Envio de Emails

#### Configurando Gmail:

1. **Ative a Verificação em 2 Etapas:**
   - Acesse [Conta Google](https://myaccount.google.com/)
   - Vá em "Segurança"
   - Ative "Verificação em duas etapas"

2. **Gere uma Senha de App:**
   - Ainda em "Segurança", procure por "Senhas de app"
   - Selecione "Email" e "Outro (nome personalizado)"
   - Digite "JobAlerts API" como nome
   - Copie a senha gerada (16 caracteres)

3. **Configure no Projeto:**

No arquivo `src/main/resources/application.properties`, substitua:

```properties
spring.mail.username=seu-email@gmail.com
spring.mail.password=sua-senha-de-app-aqui
```

### 4. Configuração do Banco de Dados

#### Para Desenvolvimento (H2 - já configurado):
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

#### Para Produção (PostgreSQL):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jobalerts
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=seu-usuario
spring.datasource.password=sua-senha
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## 🚀 Executando o Projeto

### Usando Maven:

```bash
# Limpar e compilar
./mvnw clean compile

# Executar
./mvnw spring-boot:run
```

### Usando Java diretamente:

```bash
# Compilar
./mvnw clean package

# Executar
java -jar target/job-alerts-api-0.0.1-SNAPSHOT.jar
```

## 📚 Documentação da API

Após iniciar a aplicação, acesse:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Console H2**: http://localhost:8080/h2-console (desenvolvimento)

## 🔗 Endpoints Principais

### Usuários
- `POST /api/users` - Criar usuário
- `GET /api/users/{id}` - Buscar usuário por ID
- `GET /api/users/email/{email}` - Buscar usuário por email
- `DELETE /api/users/{id}` - Desativar usuário

### Alertas de Emprego
- `POST /api/job-alerts/user/{userId}` - Criar alerta
- `GET /api/job-alerts/user/{userId}` - Listar alertas do usuário
- `PUT /api/job-alerts/{alertId}` - Atualizar alerta
- `DELETE /api/job-alerts/{alertId}` - Desativar alerta

### Busca de Empregos
- `GET /api/job-search/search` - Buscar vagas
- `GET /api/job-search/search/advanced` - Busca com filtros

### Scheduler
- `POST /api/scheduler/run-now` - Executar busca manual
- `GET /api/scheduler/status` - Status do scheduler

### Email
- `POST /api/email/test/{userId}` - Enviar email de teste
- `POST /api/email/welcome/{userId}` - Enviar email de boas-vindas

## 📝 Exemplo de Uso

### 1. Criar um Usuário

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@example.com"
  }'
```

### 2. Criar um Alerta

```bash
curl -X POST http://localhost:8080/api/job-alerts/user/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Desenvolvedor Java",
    "searchQuery": "Java Developer",
    "location": "São Paulo",
    "locationType": "QUALQUER",
    "experienceLevel": "SENIOR",
    "minimumMatchingTags": 2,
    "tags": [
      {"tag": "java", "isRequired": true},
      {"tag": "spring", "isRequired": false},
      {"tag": "microservices", "isRequired": false}
    ]
  }'
```

### 3. Executar Busca Manual

```bash
curl -X POST http://localhost:8080/api/scheduler/run-now
```

## ⚡ Configurações Avançadas

### Scheduler

```properties
# Habilitar/desabilitar scheduler
job.scheduler.enabled=true

# Intervalo de execução (em milissegundos)
# 3600000 = 1 hora
job.scheduler.fixed-rate=3600000
```

### Email

```properties
# Configurações SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 🏗️ Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/jobsearch/
│   │   ├── controller/          # Controllers REST
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── entity/              # Entidades JPA
│   │   ├── exception/           # Tratamento de exceções
│   │   ├── repository/          # Repositórios JPA
│   │   ├── scheduler/           # Jobs agendados
│   │   ├── service/             # Lógica de negócio
│   │   └── config/              # Configurações
│   └── resources/
│       ├── application.properties
│       └── template/email/      # Templates de email
└── test/                        # Testes unitários
```

## 🔧 Troubleshooting

### Problemas Comuns:

1. **Erro de API Key JSearch:**
   - Verifique se a chave está correta
   - Confirme se tem créditos na conta RapidAPI
   - Verifique se a API JSearch está ativa

2. **Erro no envio de emails:**
   - Confirme se a verificação em 2 etapas está ativa
   - Verifique se a senha de app foi gerada corretamente
   - Teste com outro provedor de email se necessário

3. **Banco de dados:**
   - Para H2: acesse o console em /h2-console
   - Para PostgreSQL: verifique se o serviço está rodando

4. **Scheduler não executa:**
   - Verifique se `job.scheduler.enabled=true`
   - Confirme se há alertas ativos no sistema

## 📋 Logs

Para monitorar a aplicação, verifique os logs:

```bash
# Logs do scheduler
tail -f logs/application.log | grep "JobSearchScheduler"

# Logs de email
tail -f logs/application.log | grep "EmailService"
```

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.

## 🆘 Suporte

Para dúvidas ou problemas, abra uma issue no repositório do projeto.

---

**Desenvolvido com ❤️ para facilitar a busca por empregos!**
