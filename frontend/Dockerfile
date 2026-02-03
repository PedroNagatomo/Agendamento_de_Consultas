FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar arquivos do projeto
COPY target/agendamento-frontend-1.0.0.jar app.jar
COPY src/main/resources /app/resources

# Instalar dependências do sistema
RUN apt-get update && apt-get install -y \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libgtk-3-0 \
    && rm -rf /var/lib/apt/lists/*

# Expor porta (se necessário)
EXPOSE 8080

# Comando para executar
CMD ["java", "-jar", "app.jar"]