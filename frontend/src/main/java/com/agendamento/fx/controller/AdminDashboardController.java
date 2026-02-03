package com.agendamento.fx.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private TableView<?> tabelaPacientes;
    @FXML private TableView<?> tabelaMedicos;
    @FXML private TableView<?> tabelaConsultas;

    @FXML private Button btnNovoPaciente;
    @FXML private Button btnEditarPaciente;
    @FXML private Button btnExcluirPaciente;

    @FXML private Button btnNovoMedico;
    @FXML private Button btnEditarMedico;
    @FXML private Button btnExcluirMedico;

    @FXML private Button btnNovaConsulta;
    @FXML private Button btnCancelarConsulta;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarEventos();
    }

    private void configurarEventos() {
        btnNovoPaciente.setOnAction(e -> cadastrarPaciente());
        btnEditarPaciente.setOnAction(e -> editarPaciente());
        btnExcluirPaciente.setOnAction(e -> excluirPaciente());

        btnNovoMedico.setOnAction(e -> cadastrarMedico());
        btnEditarMedico.setOnAction(e -> editarMedico());
        btnExcluirMedico.setOnAction(e -> excluirMedico());

        btnNovaConsulta.setOnAction(e -> criarConsulta());
        btnCancelarConsulta.setOnAction(e -> cancelarConsultaAdmin());
    }

    private void cadastrarPaciente() {
        mostrarAlerta("Funcionalidade", "Cadastrar Paciente - Em desenvolvimento");
    }

    private void editarPaciente() {
        mostrarAlerta("Funcionalidade", "Editar Paciente - Em desenvolvimento");
    }

    private void excluirPaciente() {
        mostrarAlerta("Funcionalidade", "Excluir Paciente - Em desenvolvimento");
    }

    private void cadastrarMedico() {
        mostrarAlerta("Funcionalidade", "Cadastrar Médico - Em desenvolvimento");
    }

    private void editarMedico() {
        mostrarAlerta("Funcionalidade", "Editar Médico - Em desenvolvimento");
    }

    private void excluirMedico() {
        mostrarAlerta("Funcionalidade", "Excluir Médico - Em desenvolvimento");
    }

    private void criarConsulta() {
        mostrarAlerta("Funcionalidade", "Criar Consulta - Em desenvolvimento");
    }

    private void cancelarConsultaAdmin() {
        mostrarAlerta("Funcionalidade", "Cancelar Consulta - Em desenvolvimento");
    }

    @FXML
    private void handleSair() {
        System.exit(0);
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}