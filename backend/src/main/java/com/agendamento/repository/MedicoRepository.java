package com.agendamento.repository;

import com.agendamento.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    // Mantenha apenas métodos relacionados a campos de Medico
    List<Medico> findByEspecialidade(String especialidade);

    // Remova findByCrm e existsByCrm se não forem necessários
    // ou ajuste para buscar através do usuário associado
}