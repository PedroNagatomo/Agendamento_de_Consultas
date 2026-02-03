package com.agendamento.security;

import com.agendamento.model.Consulta;
import com.agendamento.repository.ConsultaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    // Buscar consulta por ID
    public Optional<Consulta> findById(Long id) {
        return consultaRepository.findById(id);
    }

    // Buscar consultas por paciente
    public List<Consulta> findByPacienteId(Long pacienteId) {
        return consultaRepository.findByPacienteId(pacienteId);
    }

    // Buscar consultas por médico
    public List<Consulta> findByMedicoId(Long medicoId) {
        return consultaRepository.findByMedicoId(medicoId);
    }

    // Atualizar consulta completa
    @Transactional
    public Consulta atualizarConsulta(Long id, Consulta consultaAtualizada) {
        Optional<Consulta> consultaExistente = consultaRepository.findById(id);

        if (consultaExistente.isPresent()) {
            Consulta consulta = consultaExistente.get();

            // Atualizar apenas os campos permitidos
            if (consultaAtualizada.getStatus() != null) {
                consulta.setStatus(consultaAtualizada.getStatus());
            }

            if (consultaAtualizada.getDataHora() != null) {
                consulta.setDataHora(consultaAtualizada.getDataHora());
            }

            if (consultaAtualizada.getMotivo() != null) {
                consulta.setMotivo(consultaAtualizada.getMotivo());
            }

            return consultaRepository.save(consulta);
        }

        throw new RuntimeException("Consulta não encontrada com ID: " + id);
    }

    // Atualizar apenas o status
    @Transactional
    public Consulta atualizarStatus(Long id, String novoStatus) {
        Optional<Consulta> consultaExistente = consultaRepository.findById(id);

        if (consultaExistente.isPresent()) {
            Consulta consulta = consultaExistente.get();

            // Validar status
            if (isStatusValido(novoStatus)) {
                consulta.setStatus(Consulta.StatusConsulta.valueOf(novoStatus));
                return consultaRepository.save(consulta);
            } else {
                throw new RuntimeException("Status inválido: " + novoStatus);
            }
        }

        throw new RuntimeException("Consulta não encontrada com ID: " + id);
    }

    // Validar status
    private boolean isStatusValido(String status) {
        return status.equals("AGENDADA") ||
                status.equals("CONFIRMADA") ||
                status.equals("CANCELADA") ||
                status.equals("REALIZADA");
    }

    // Salvar nova consulta
    public Consulta save(Consulta consulta) {
        return consultaRepository.save(consulta);
    }

    // Deletar consulta
    @Transactional
    public void deleteById(Long id) {
        if (consultaRepository.existsById(id)) {
            consultaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Consulta não encontrada com ID: " + id);
        }
    }

    // Listar todas as consultas
    public List<Consulta> findAll() {
        return consultaRepository.findAll();
    }
}