package com.agendamento.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "pacientes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Paciente extends Usuario {

    @Column(name = "data_nascimento")
    private String dataNascimento;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // Construtor com todos os campos
    public Paciente(String nome, String email, String senha, String telefone,
                    String dataNascimento, String observacoes) {
        super(null, nome, email, senha, telefone);
        this.dataNascimento = dataNascimento;
        this.observacoes = observacoes;
    }
}