package com.agendamento.fx.service;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private static final Map<String, Usuario> USUARIOS = new HashMap<>();

    static {
        // Usuários de demonstração
        USUARIOS.put("admin", new Usuario("admin", "admin123", "Administrador"));
        USUARIOS.put("medico", new Usuario("medico", "medico123", "Médico"));
        USUARIOS.put("paciente", new Usuario("paciente", "paciente123", "Paciente"));
        USUARIOS.put("carlos", new Usuario("carlos", "123", "Paciente"));
        USUARIOS.put("maria", new Usuario("maria", "123", "Médico"));
        USUARIOS.put("joao", new Usuario("joao", "123", "Administrador"));
    }

    public static Usuario login(String username, String senha) {
        Usuario usuario = USUARIOS.get(username);
        if (usuario != null && usuario.getSenha().equals(senha)) {
            return usuario;
        }
        return null;
    }

    public static class Usuario {
        private String username;
        private String senha;
        private String tipo;

        public Usuario(String username, String senha, String tipo) {
            this.username = username;
            this.senha = senha;
            this.tipo = tipo;
        }

        public String getUsername() { return username; }
        public String getSenha() { return senha; }
        public String getTipo() { return tipo; }
    }
}