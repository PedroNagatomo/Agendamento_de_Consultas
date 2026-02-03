package com.agendamento.fx.service;

import com.agendamento.fx.AppContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class ApiService {
    private static final String BASE_URL = AppContext.getApiBaseUrl(); // http://localhost:18083
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static String token;

    // ========== TOKEN MANAGEMENT ==========
    public static void setToken(String authToken) {
        token = authToken;
    }

    public static String getToken() {
        return token;
    }

    public static void clearToken() {
        token = null;
    }

    // ========== TEST METHODS ==========
    public static void testarEndpoints() {
        System.out.println("\n🔍 TESTANDO ENDPOINTS DO BACKEND:");
        System.out.println("URL Base: " + BASE_URL);

        String[] endpoints = {
                "/auth/login",
                "/auth/register",
                "/consultas",
                "/medicos",
                "/api/auth/login",
                "/api/auth/register"
        };

        for (String endpoint : endpoints) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + endpoint))
                        .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(3))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println((response.statusCode() != 0 ? "✅ " : "❌ ") + endpoint +
                        " - Status: " + response.statusCode());
            } catch (Exception e) {
                System.out.println("❌ " + endpoint + " - Erro: " + e.getMessage());
            }
        }
    }

    public static boolean testarConexao() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean conectado = response.statusCode() != 0;
            System.out.println(conectado ? "✅ Backend conectado" : "❌ Backend offline");
            return conectado;
        } catch (Exception e) {
            System.out.println("❌ Falha na conexão: " + e.getMessage());
            return false;
        }
    }

    // ========== AUTH METHODS ==========
    public static Map<String, Object> login(String email, String senha) throws Exception {
        System.out.println("🔐 Tentando login para: " + email);

        Map<String, Object> loginData = new HashMap<>();
        loginData.put("email", email);
        loginData.put("senha", senha);

        try {
            // Tentar primeiro endpoint
            Map<String, Object> response = post("/auth/login", loginData, Map.class);

            if (response != null) {
                System.out.println("✅ Login bem-sucedido");

                // Extrair token se existir
                if (response.containsKey("token")) {
                    token = (String) response.get("token");
                    System.out.println("📝 Token JWT recebido");
                }

                // Extrair usuário
                if (response.containsKey("usuario")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> usuario = (Map<String, Object>) response.get("usuario");
                    return usuario;
                }

                return response;
            }

            throw new Exception("Resposta vazia do servidor");

        } catch (Exception e) {
            System.out.println("❌ Falha no login: " + e.getMessage());
            // Tentar endpoint alternativo
            try {
                System.out.println("🔄 Tentando endpoint alternativo /api/auth/login");
                return post("/api/auth/login", loginData, Map.class);
            } catch (Exception e2) {
                throw new Exception("Login falhou em todos os endpoints: " + e2.getMessage());
            }
        }
    }

    public static Map<String, Object> cadastrar(Map<String, Object> dados) throws Exception {
        System.out.println("📝 Cadastrando usuário: " + dados.get("email"));

        try {
            return post("/auth/register", dados, Map.class);
        } catch (Exception e) {
            System.out.println("🔄 Tentando endpoint alternativo /api/auth/register");
            return post("/api/auth/register", dados, Map.class);
        }
    }

    // ========== CONSULTAS METHODS ==========
    public static List<Map<String, Object>> getConsultasPaciente(Long pacienteId) throws Exception {
        System.out.println("📋 Buscando consultas do paciente ID: " + pacienteId);

        try {
            return get("/consultas/paciente/" + pacienteId, List.class);
        } catch (Exception e) {
            System.out.println("🔄 Tentando endpoint alternativo");
            return get("/api/consultas/paciente/" + pacienteId, List.class);
        }
    }

    public static List<Map<String, Object>> getConsultasMedico(Long medicoId) throws Exception {
        System.out.println("📋 Buscando consultas do médico ID: " + medicoId);

        try {
            return get("/consultas/medico/" + medicoId, List.class);
        } catch (Exception e) {
            System.out.println("🔄 Tentando endpoint alternativo");
            return get("/api/consultas/medico/" + medicoId, List.class);
        }
    }

    public static Map<String, Object> agendarConsulta(Map<String, Object> consultaData) throws Exception {
        System.out.println("📅 Agendando consulta");

        try {
            return post("/consultas/agendar", consultaData, Map.class);
        } catch (Exception e) {
            System.out.println("🔄 Tentando endpoint alternativo");
            return post("/api/consultas/agendar", consultaData, Map.class);
        }
    }

    public static boolean cancelarConsulta(Long consultaId) {
        System.out.println("❌ Cancelando consulta ID: " + consultaId);

        String[] endpoints = {
                "/consultas/" + consultaId + "/cancelar",
                "/api/consultas/" + consultaId + "/cancelar",
                "/consultas/cancelar/" + consultaId
        };

        for (String endpoint : endpoints) {
            try {
                System.out.println("🔄 Tentando: " + endpoint);
                post(endpoint, new HashMap<>(), Map.class);
                System.out.println("✅ Consulta cancelada com sucesso");
                return true;
            } catch (Exception e) {
                System.out.println("❌ Falha no endpoint " + endpoint + ": " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean confirmarConsulta(Long consultaId) {
        System.out.println("✅ Confirmando consulta ID: " + consultaId);

        String[] endpoints = {
                "/consultas/" + consultaId + "/confirmar",
                "/api/consultas/" + consultaId + "/confirmar",
                "/consultas/confirmar/" + consultaId
        };

        for (String endpoint : endpoints) {
            try {
                System.out.println("🔄 Tentando: " + endpoint);
                post(endpoint, new HashMap<>(), Map.class);
                System.out.println("✅ Consulta confirmada com sucesso");
                return true;
            } catch (Exception e) {
                System.out.println("❌ Falha no endpoint " + endpoint + ": " + e.getMessage());
            }
        }
        return false;
    }

    // ========== MÉDICOS METHODS ==========
    public static List<Map<String, Object>> listarMedicos() throws Exception {
        System.out.println("👨‍⚕️ Listando médicos");

        try {
            return get("/medicos", List.class);
        } catch (Exception e) {
            System.out.println("🔄 Tentando endpoint alternativo");
            return get("/api/medicos", List.class);
        }
    }

    // ========== GENERIC HTTP METHODS ==========
    private static HttpRequest.Builder createRequestBuilder(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10));

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder;
    }

    public static <T> T get(String endpoint, Class<T> responseType) throws Exception {
        System.out.println("🌐 GET: " + BASE_URL + endpoint);

        try {
            HttpRequest request = createRequestBuilder(endpoint).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return processResponse(response, responseType);

        } catch (Exception e) {
            System.out.println("❌ GET falhou: " + e.getMessage());
            throw e;
        }
    }

    public static <T> T post(String endpoint, Object body, Class<T> responseType) throws Exception {
        System.out.println("🌐 POST: " + BASE_URL + endpoint);

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            System.out.println("📦 Body: " + jsonBody);

            HttpRequest request = createRequestBuilder(endpoint)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return processResponse(response, responseType);

        } catch (Exception e) {
            System.out.println("❌ POST falhou: " + e.getMessage());
            throw e;
        }
    }

    public static <T> T put(String endpoint, Object body, Class<T> responseType) throws Exception {
        System.out.println("🌐 PUT: " + BASE_URL + endpoint);

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            System.out.println("📦 Body: " + jsonBody);

            HttpRequest request = createRequestBuilder(endpoint)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return processResponse(response, responseType);

        } catch (Exception e) {
            System.out.println("❌ PUT falhou: " + e.getMessage());
            throw e;
        }
    }

    public static <T> T delete(String endpoint, Class<T> responseType) throws Exception {
        System.out.println("🌐 DELETE: " + BASE_URL + endpoint);

        try {
            HttpRequest request = createRequestBuilder(endpoint).DELETE().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return processResponse(response, responseType);

        } catch (Exception e) {
            System.out.println("❌ DELETE falhou: " + e.getMessage());
            throw e;
        }
    }

    private static <T> T processResponse(HttpResponse<String> response, Class<T> responseType) throws Exception {
        int status = response.statusCode();
        String body = response.body();

        System.out.println("📊 Status: " + status);

        if (status >= 200 && status < 300) {
            if (body == null || body.isEmpty() || responseType == Void.class) {
                return null;
            }

            if (responseType == String.class) {
                return responseType.cast(body);
            }

            return objectMapper.readValue(body, responseType);

        } else {
            String errorMsg = "Erro " + status;
            if (body != null && !body.isEmpty()) {
                try {
                    Map<?, ?> error = objectMapper.readValue(body, Map.class);
                    if (error.containsKey("message")) {
                        errorMsg += ": " + error.get("message");
                    } else if (error.containsKey("error")) {
                        errorMsg += ": " + error.get("error");
                    } else {
                        errorMsg += ": " + body;
                    }
                } catch (Exception e) {
                    errorMsg += ": " + body;
                }
            }
            throw new RuntimeException(errorMsg);
        }
    }
}