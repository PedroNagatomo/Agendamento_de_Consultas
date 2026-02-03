# Sistema de Agendamento de Consultas Médicas

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-green)
![JavaFX](https://img.shields.io/badge/JavaFX-20-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-✓-blue)

Sistema completo para agendamento de consultas médicas com backend em Spring Boot e frontend desktop em JavaFX. Permite cadastro de pacientes, médicos e agendamento de consultas com validações de horário e disponibilidade.

## Índice

- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Configuração](#instalação-e-configuração)
- [Banco de Dados](#banco-de-dados)
- [API Endpoints](#api-endpoints)
- [Interface JavaFX](#interface-javafx)
- [Configuração](#configuração)
- [Solução de Problemas](#solução-de-problemas)
- [Próximas Funcionalidades](#próximas-funcionalidades)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

## Funcionalidades

### Gestão de Usuários

- Cadastro de pacientes com dados pessoais (nome, CPF, email, telefone, data de nascimento)
- Cadastro de médicos com especialidade e CRM
- Cadastro de administradores para gestão do sistema
- Autenticação JWT com roles específicas
- Validações de CPF único, CRM único e email único

### Agendamento de Consultas

- Agendamento online com escolha de médico, data e horário
- Validação de horários disponíveis por médico
- Controle de conflitos (paciente não pode ter duas consultas no mesmo horário)
- Status da consulta (Agendada, Confirmada, Realizada, Cancelada, Falta)
- Cancelamento com regras de antecedência (24h)

### Dashboard por Perfil

- **Paciente**: Visualiza consultas agendadas, histórico e agenda nova consulta
- **Médico**: Visualiza agenda diária, confirma consultas, visualiza histórico
- **Administrador**: Gestão completa de usuários e relatórios

## Arquitetura

```
agendamento-consultas/
├── backend/           # API Spring Boot
│   ├── src/main/java/com/agendamento/
│   │   ├── controller/    # Controllers REST
│   │   ├── service/       # Lógica de negócio
│   │   ├── repository/    # Repositórios JPA
│   │   ├── model/         # Entidades JPA
│   │   ├── dto/           # Data Transfer Objects
│   │   ├── config/        # Configurações
│   │   └── security/      # Configuração de segurança
│   └── src/main/resources/
│       └── application.properties
│
├── frontend/          # Aplicação JavaFX
│   ├── src/main/java/com/agendamento/fx/
│   │   ├── controller/    # Controladores JavaFX
│   │   ├── service/       # Serviços e API Client
│   │   └── AppContext.java
│   ├── src/main/resources/
│   │   ├── views/         # Arquivos FXML
│   │   └── styles/        # CSS para JavaFX
│   └── pom.xml
│
├── docker-compose.yml
├── Dockerfile.backend
├── Dockerfile.frontend
└── README.md
```

## Tecnologias

### Backend

- **Java 17** - Linguagem principal
- **Spring Boot 3.1.5** - Framework backend
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **JWT (jjwt)** - Tokens de autenticação
- **PostgreSQL** - Banco de dados
- **Lombok** - Redução de boilerplate
- **Validation API** - Validação de dados

### Frontend

- **JavaFX 20** - Framework desktop
- **FXML** - Layout declarativo
- **CSS** - Estilização
- **ControlsFX** - Componentes extras
- **Jackson** - Serialização JSON
- **HTTP Client** - Comunicação com API

### Infraestrutura

- **Docker** - Containerização
- **Docker Compose** - Orquestração
- **Maven** - Gerenciamento de dependências

## Pré-requisitos

- Java 17 ou superior
- Maven 3.8+
- Docker e Docker Compose
- PostgreSQL (para desenvolvimento local)
- Git

## Instalação e Configuração

### Opção 1: Usando Docker (Recomendado)

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/agendamento-consultas.git
cd agendamento-consultas

# Inicie todos os serviços
docker-compose up -d

# Acesse:
# Backend API: http://localhost:8080/api
# Frontend: Execute manualmente (veja abaixo)
```

### Opção 2: Desenvolvimento Local

```bash
# 1. Banco de dados
docker run --name agendamento-db -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15

# 2. Backend
cd backend
mvn clean spring-boot:run

# 3. Frontend (em outro terminal)
cd frontend
mvn clean javafx:run
```

### Opção 3: Execução Manual

```bash
# Compile o backend
cd backend
mvn clean package
java -jar target/agendamento-backend-1.0.0.jar

# Execute o frontend
cd frontend
mvn clean javafx:run
```

## Banco de Dados

### Estrutura Principal

```sql
-- Usuários (pacientes, médicos, administradores)
usuarios (id, nome, email, senha, cpf, telefone, tipo_usuario, ativo, data_cadastro)

-- Específicos
pacientes (id, data_nascimento, observacoes, usuario_id)
medicos (id, crm, especialidade, usuario_id)

-- Consultas
consultas (id, paciente_id, medico_id, data_hora, observacoes, status, data_criacao)

-- Relacionamentos
usuario_roles (usuario_id, role)
medico_horarios (medico_id, horario)
```

### Dados Iniciais

O sistema cria automaticamente:

- **Admin**: admin@clinica.com / admin123
- **Médico**: medico@clinica.com / medico123
- **Paciente**: paciente@email.com / paciente123

## API Endpoints

### Autenticação

```
POST /api/auth/login           # Login de usuário
POST /api/auth/cadastro        # Cadastro de novo usuário
```

### Pacientes

```
GET    /api/pacientes          # Listar pacientes
POST   /api/pacientes          # Criar paciente
GET    /api/pacientes/{id}     # Buscar paciente
PUT    /api/pacientes/{id}     # Atualizar paciente
DELETE /api/pacientes/{id}     # Excluir paciente
```

### Consultas

```
POST   /api/consultas          # Agendar consulta
GET    /api/consultas/paciente/{id}    # Consultas por paciente
GET    /api/consultas/medico/{id}      # Consultas por médico
PATCH  /api/consultas/{id}/status      # Atualizar status
POST   /api/consultas/{id}/cancelar    # Cancelar consulta
```

### Médicos

```
GET    /api/medicos            # Listar médicos
GET    /api/medicos/{id}/horarios-disponiveis  # Horários disponíveis
```

## Interface JavaFX

### Telas Disponíveis

- **Login.fxml** - Tela de autenticação
- **Cadastro.fxml** - Cadastro de usuários com campos condicionais
- **PacienteDashboard.fxml** - Dashboard do paciente
- **MedicoDashboard.fxml** - Dashboard do médico
- **AdminDashboard.fxml** - Dashboard administrativo

### Fluxo de Navegação

```
Login → [Cadastro] → Dashboard específico → [Agendamento/Consulta]
```

## Configuração

### Backend (application.properties)

```properties
# Servidor
server.port=8080
server.servlet.context-path=/api

# Banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5432/agendamento_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=mySecretKey1234567890123456789012345678901234567890
jwt.expiration=86400000

# CORS (para desenvolvimento)
spring.main.allow-bean-definition-overriding=true
```

### Frontend (ApiService.java)

```java
private static final String BASE_URL = "http://localhost:8080/api";
```

## Solução de Problemas

### Erro: CORS no Backend

```java
// No SecurityConfig.java
.cors(cors -> cors.disable())
```

### Erro: JavaFX não inicia

```bash
# Execute com perfil correto
mvn clean javafx:run -Djavafx.version=20
```

### Erro: Banco de dados não conecta

```bash
# Verifique se o PostgreSQL está rodando
docker ps
# Verifique credenciais em application.properties
```

## Próximas Funcionalidades

- Notificações por email
- Relatórios em PDF
- Integração com calendários
- Telemedicina (vídeo chamadas)
- Aplicativo mobile
- Painel de métricas

## Contribuindo

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## Autor

[Seu Nome] - seu.email@exemplo.com

## Agradecimentos

- Spring Boot Team
- JavaFX Community
- PostgreSQL Team
- Todos os contribuidores

---

**Quick Start**

```bash
# Clone e execute com Docker (mais fácil)
git clone https://github.com/seu-usuario/agendamento-consultas.git
cd agendamento-consultas
docker-compose up -d

# Ou para desenvolvimento
cd backend && mvn spring-boot:run
cd frontend && mvn javafx:run
```

Se você gostou do projeto, deixe uma estrela no GitHub!
