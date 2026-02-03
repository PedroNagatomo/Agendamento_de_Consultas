package com.agendamento.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "medicos")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Medico extends Usuario {

    @Column(unique = true)
    private String crm;

    private String especialidade;

    // Construtor com todos os campos
    public Medico(String nome, String email, String senha, String telefone,
                  String crm, String especialidade) {
        super(null, nome, email, senha, telefone);
        this.crm = crm;
        this.especialidade = especialidade;
    }
}