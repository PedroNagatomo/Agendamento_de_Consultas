package com.agendamento.fx.model;

import javafx.beans.property.*;

public class PacienteDTO {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty nome = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty telefone = new SimpleStringProperty();
    private final StringProperty cpf = new SimpleStringProperty();

    public PacienteDTO() {}

    public PacienteDTO(Long id, String nome, String email, String telefone, String cpf) {
        setId(id);
        setNome(nome);
        setEmail(email);
        setTelefone(telefone);
        setCpf(cpf);
    }

    // Getters e Setters
    public long getId() { return id.get(); }
    public LongProperty idProperty() { return id; }
    public void setId(long id) { this.id.set(id); }

    public String getNome() { return nome.get(); }
    public StringProperty nomeProperty() { return nome; }
    public void setNome(String nome) { this.nome.set(nome); }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public void setEmail(String email) { this.email.set(email); }

    public String getTelefone() { return telefone.get(); }
    public StringProperty telefoneProperty() { return telefone; }
    public void setTelefone(String telefone) { this.telefone.set(telefone); }

    public String getCpf() { return cpf.get(); }
    public StringProperty cpfProperty() { return cpf; }
    public void setCpf(String cpf) { this.cpf.set(cpf); }
}