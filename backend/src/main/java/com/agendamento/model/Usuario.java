package com.agendamento.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String telefone;

    // Enum para tipo de usuário (usado apenas para referência)
    public enum TipoUsuario {
        PACIENTE, MEDICO, ADMIN
    }

    public String getTipo() {
        if (this instanceof Paciente) {
            return "PACIENTE";
        } else if (this instanceof Medico) {
            return "MEDICO";
        } else if (this instanceof Admin) {
            return "ADMIN";
        }
        return "DESCONHECIDO";
    }
}