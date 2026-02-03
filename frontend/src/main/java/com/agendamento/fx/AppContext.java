package com.agendamento.fx;

import com.agendamento.fx.service.ApiService;

import java.util.Map;

public class AppContext {
    private static Map<String, Object> usuarioLogado;
    private static Long usuarioId;
    private static String usuarioTipo;
    private static String usuarioNome;

    // Adicione estas variáveis
    private static Boolean isStandalone = null;
    private static String apiBaseUrl = null;

    public static void setUsuarioLogado(Map<String, Object> usuario) {
        usuarioLogado = usuario;
        if (usuario != null) {
            usuarioId = ((Number) usuario.get("id")).longValue();
            usuarioTipo = (String) usuario.get("tipo");
            usuarioNome = (String) usuario.get("nome");
        }
    }

    public static Map<String, Object> getUsuarioLogado() {
        return usuarioLogado;
    }

    public static Long getUsuarioId() {
        return usuarioId;
    }

    public static String getUsuarioTipo() {
        return usuarioTipo;
    }

    public static String getUsuarioNome() {
        return usuarioNome;
    }

    public static void logout() {
        usuarioLogado = null;
        usuarioId = null;
        usuarioTipo = null;
        usuarioNome = null;
        ApiService.clearToken();
    }

    public static boolean isPaciente() {
        return "PACIENTE".equals(usuarioTipo);
    }

    public static boolean isMedico() {
        return "MEDICO".equals(usuarioTipo);
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(usuarioTipo);
    }

    // ===== NOVOS MÉTODOS PARA MODO DESKTOP =====

    public static boolean isStandaloneApp() {
        if (isStandalone == null) {
            // Verificar se estamos em modo standalone
            String jarPath = AppContext.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            isStandalone = jarPath.toLowerCase().endsWith(".jar");
        }
        return isStandalone;
    }

    public static String getApiBaseUrl() {
        if (apiBaseUrl == null) {
            if (isStandaloneApp()) {
                apiBaseUrl = "http://localhost:18083";
            } else {
                apiBaseUrl = System.getProperty("api.base-url", "http://localhost:8083");
            }
        }
        return apiBaseUrl;
    }

    public static String getDatabasePath() {
        if (isStandaloneApp()) {
            String userDir = System.getProperty("user.dir");
            return userDir + "\\database\\medischedule";
        }
        return null;
    }
}