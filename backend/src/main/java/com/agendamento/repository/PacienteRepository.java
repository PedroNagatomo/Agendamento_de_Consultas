package com.agendamento.repository;

import com.agendamento.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    // Remova os métodos findByCpf e existsByCpf
    // pois o CPF agora está na entidade Usuario
}