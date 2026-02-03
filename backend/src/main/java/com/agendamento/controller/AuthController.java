package com.agendamento.controller;

import com.agendamento.model.*;
import com.agendamento.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(request.getEmail());

        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuário não encontrado"));
        }

        Usuario usuario = usuarioOptional.get();

        // Verifica senha
        if (!usuario.getSenha().equals(request.getSenha())) {
            return ResponseEntity.status(401).body(Map.of("error", "Senha incorreta"));
        }

        // Remove senha da resposta
        usuario.setSenha(null);

        // Retorna com tipo específico
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login realizado com sucesso");
        response.put("tipo", usuario.getTipo());
        response.put("usuario", usuario);

        return ResponseEntity.ok(response);
    }

    // REGISTRO
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Verifica se email já existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email já cadastrado"));
        }

        Usuario usuario;

        // Cria usuário baseado no tipo
        switch (request.getTipo().toUpperCase()) {
            case "MEDICO":
                Medico medico = new Medico();
                medico.setNome(request.getNome());
                medico.setEmail(request.getEmail());
                medico.setSenha(request.getSenha());
                medico.setTelefone(request.getTelefone());
                medico.setCrm(request.getCrm());
                medico.setEspecialidade(request.getEspecialidade());
                usuario = medico;
                break;

            case "PACIENTE":
                Paciente paciente = new Paciente();
                paciente.setNome(request.getNome());
                paciente.setEmail(request.getEmail());
                paciente.setSenha(request.getSenha());
                paciente.setTelefone(request.getTelefone());
                paciente.setDataNascimento(request.getDataNascimento());
                paciente.setObservacoes(request.getObservacoes());
                usuario = paciente;
                break;

            case "ADMIN":
                Admin admin = new Admin();
                admin.setNome(request.getNome());
                admin.setEmail(request.getEmail());
                admin.setSenha(request.getSenha());
                admin.setTelefone(request.getTelefone());
                usuario = admin;
                break;

            default:
                return ResponseEntity.badRequest().body(Map.of("error", "Tipo de usuário inválido"));
        }

        // Salva no banco
        Usuario savedUsuario = usuarioRepository.save(usuario);
        savedUsuario.setSenha(null); // Remove senha da resposta

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuário registrado com sucesso");
        response.put("tipo", savedUsuario.getTipo());
        response.put("usuario", savedUsuario);

        return ResponseEntity.ok(response);
    }

    // DTOs
    public static class LoginRequest {
        private String email;
        private String senha;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
    }

    public static class RegisterRequest {
        private String nome;
        private String email;
        private String senha;
        private String telefone;
        private String tipo;
        // Campos específicos
        private String crm;
        private String especialidade;
        private String dataNascimento;
        private String observacoes;

        // Getters e Setters
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
        public String getTelefone() { return telefone; }
        public void setTelefone(String telefone) { this.telefone = telefone; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public String getCrm() { return crm; }
        public void setCrm(String crm) { this.crm = crm; }
        public String getEspecialidade() { return especialidade; }
        public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
        public String getDataNascimento() { return dataNascimento; }
        public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
        public String getObservacoes() { return observacoes; }
        public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    }

    @PostMapping("/test-register")
    public ResponseEntity<?> testRegister(@RequestBody Map<String, Object> request) {
        System.out.println("=== DEBUG REGISTER ===");
        System.out.println("Request completo: " + request);
        System.out.println("Tipo recebido: " + request.get("tipo"));
        System.out.println("Tipo classe: " + request.get("tipo").getClass());

        return ResponseEntity.ok(Map.of(
                "debug", request,
                "tipo", request.get("tipo"),
                "tipo_class", request.get("tipo").getClass().getName()
        ));
    }

    @GetMapping("/medicos")
    public ResponseEntity<?> listarMedicos() {
        try {
            // Buscar todos os médicos
            List<Medico> medicos = usuarioRepository.findAll().stream()
                    .filter(u -> u instanceof Medico)
                    .map(u -> (Medico) u)
                    .collect(Collectors.toList());

            // Remover senhas
            medicos.forEach(m -> m.setSenha(null));

            return ResponseEntity.ok(medicos);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
}