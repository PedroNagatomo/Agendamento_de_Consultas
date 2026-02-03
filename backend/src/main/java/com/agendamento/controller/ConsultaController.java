// ConsultaController.java
package com.agendamento.controller;

import com.agendamento.model.Consulta;
import com.agendamento.security.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/consultas")
@CrossOrigin(origins = "*") // Permite todas as origens para desenvolvimento
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    // GET: Listar todas as consultas
    @GetMapping
    public ResponseEntity<List<Consulta>> listarTodas() {
        List<Consulta> consultas = consultaService.findAll();
        return ResponseEntity.ok(consultas);
    }

    // GET: Buscar consulta por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Consulta> consulta = consultaService.findById(id);

        if (consulta.isPresent()) {
            return ResponseEntity.ok(consulta.get());
        } else {
            Map<String, String> erro = new HashMap<>();
            erro.put("error", "Consulta não encontrada com ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        }
    }

    // GET: Consultas por paciente
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<Consulta>> buscarPorPaciente(@PathVariable Long pacienteId) {
        List<Consulta> consultas = consultaService.findByPacienteId(pacienteId);
        return ResponseEntity.ok(consultas);
    }

    // GET: Consultas por médico
    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<Consulta>> buscarPorMedico(@PathVariable Long medicoId) {
        List<Consulta> consultas = consultaService.findByMedicoId(medicoId);
        return ResponseEntity.ok(consultas);
    }

    // PUT: Atualizar consulta completa
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarConsulta(@PathVariable Long id, @RequestBody Consulta consultaAtualizada) {
        try {
            Consulta consulta = consultaService.atualizarConsulta(id, consultaAtualizada);
            return ResponseEntity.ok(consulta);
        } catch (RuntimeException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        }
    }

    // PATCH: Atualizar apenas o status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> statusRequest) {
        try {
            String novoStatus = statusRequest.get("status");

            if (novoStatus == null || novoStatus.isEmpty()) {
                Map<String, String> erro = new HashMap<>();
                erro.put("error", "Campo 'status' é obrigatório");
                return ResponseEntity.badRequest().body(erro);
            }

            Consulta consulta = consultaService.atualizarStatus(id, novoStatus);
            return ResponseEntity.ok(consulta);

        } catch (RuntimeException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
        }
    }

    // POST: Confirmar consulta
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarConsulta(@PathVariable Long id) {
        try {
            Consulta consulta = consultaService.atualizarStatus(id, "CONFIRMADA");

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("message", "Consulta confirmada com sucesso");
            resposta.put("consulta", consulta);

            return ResponseEntity.ok(resposta);

        } catch (RuntimeException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
        }
    }

    // POST: Cancelar consulta
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarConsulta(@PathVariable Long id) {
        try {
            Consulta consulta = consultaService.atualizarStatus(id, "CANCELADA");

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("message", "Consulta cancelada com sucesso");
            resposta.put("consulta", consulta);

            return ResponseEntity.ok(resposta);

        } catch (RuntimeException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
        }
    }

    // POST: Criar nova consulta
    @PostMapping
    public ResponseEntity<?> criarConsulta(@RequestBody Consulta consulta) {
        try {
            // Validar dados obrigatórios
            if (consulta.getPaciente() == null || consulta.getMedico() == null ||
                    consulta.getDataHora() == null || consulta.getStatus() == null) {
                Map<String, String> erro = new HashMap<>();
                erro.put("error", "Dados incompletos. Paciente, médico, data/hora e status são obrigatórios");
                return ResponseEntity.badRequest().body(erro);
            }

            Consulta novaConsulta = consultaService.save(consulta);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaConsulta);

        } catch (Exception e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("error", "Erro ao criar consulta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
        }
    }

    // DELETE: Excluir consulta
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirConsulta(@PathVariable Long id) {
        try {
            consultaService.deleteById(id);

            Map<String, String> resposta = new HashMap<>();
            resposta.put("message", "Consulta excluída com sucesso");
            return ResponseEntity.ok(resposta);

        } catch (RuntimeException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        }
    }
}