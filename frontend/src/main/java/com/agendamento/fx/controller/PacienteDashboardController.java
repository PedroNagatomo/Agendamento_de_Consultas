package com.agendamento.fx.controller;

import com.agendamento.fx.AppContext;
import com.agendamento.fx.service.ApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PacienteDashboardController implements Initializable {

    @FXML private TableView<ConsultaTableModel> consultasTable;
    @FXML private TableColumn<ConsultaTableModel, String> colMedico;
    @FXML private TableColumn<ConsultaTableModel, String> colEspecialidade;
    @FXML private TableColumn<ConsultaTableModel, String> colData;
    @FXML private TableColumn<ConsultaTableModel, String> colStatus;
    @FXML private TableColumn<ConsultaTableModel, Long> colId; // Coluna para ID

    @FXML private Button btnNovaConsulta;
    @FXML private Button btnCancelarConsulta;
    @FXML private Button btnAtualizar;
    @FXML private Button btnSair;

    @FXML private ComboBox<String> comboEspecialidade;
    @FXML private ComboBox<String> comboMedico;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboHorario;

    @FXML private TextArea txtObservacoes;
    @FXML private Label lblNomePaciente;
    @FXML private ProgressIndicator progressIndicator;

    private ObservableList<ConsultaTableModel> consultas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PacienteDashboardController inicializado!");

        // Configurar tabela
        configurarTabela();

        // Configurar combos
        configurarCombos();

        // Configurar eventos
        configurarEventos();

        // Carregar dados iniciais
        carregarDadosUsuario();
        carregarConsultasDoBackend();
        carregarMedicosDoBackend();
    }

    private void configurarTabela() {
        // Configurar colunas
        colMedico.setCellValueFactory(new PropertyValueFactory<>("medico"));
        colEspecialidade.setCellValueFactory(new PropertyValueFactory<>("especialidade"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Configurar formatação de status
        colStatus.setCellFactory(column -> new TableCell<ConsultaTableModel, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toUpperCase()) {
                        case "AGENDADA":
                        case "AGENDADO":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "CONFIRMADA":
                        case "CONFIRMADO":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "CANCELADA":
                        case "CANCELADO":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case "REALIZADA":
                        case "REALIZADO":
                            setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        consultasTable.setItems(consultas);
    }

    private void configurarCombos() {
        // Especialidades
        comboEspecialidade.getItems().addAll(
                "Cardiologia", "Dermatologia", "Ortopedia",
                "Pediatria", "Ginecologia", "Clínico Geral"
        );

        // Médicos (exemplo - você vai carregar do backend)
        comboMedico.getItems().addAll(
                "1 - Dr. João Silva (Cardiologia)",
                "2 - Dra. Maria Santos (Dermatologia)",
                "3 - Dr. Pedro Oliveira (Ortopedia)"
        );

        // Horários
        comboHorario.getItems().addAll(
                "08:00", "09:00", "10:00", "11:00",
                "14:00", "15:00", "16:00", "17:00"
        );

        // Configurar data mínima para hoje
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(java.time.LocalDate.now()));
            }
        });
    }

    private void configurarEventos() {
        btnNovaConsulta.setOnAction(e -> agendarConsulta());
        btnCancelarConsulta.setOnAction(e -> cancelarConsultaNoBackend());
        btnAtualizar.setOnAction(e -> carregarConsultasDoBackend());
        btnSair.setOnAction(e -> handleSair());
    }

    private void carregarDadosUsuario() {
        if (AppContext.getUsuarioNome() != null) {
            lblNomePaciente.setText("Paciente: " + AppContext.getUsuarioNome());
        }
    }

    private void carregarConsultasDoBackend() {
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }

        new Thread(() -> {
            try {
                Long pacienteId = AppContext.getUsuarioId();
                System.out.println("Carregando consultas do paciente ID: " + pacienteId);

                if (pacienteId != null) {
                    List<Map<String, Object>> consultasApi = ApiService.getConsultasPaciente(pacienteId);

                    Platform.runLater(() -> {
                        consultas.clear();

                        if (consultasApi != null && !consultasApi.isEmpty()) {
                            for (Map<String, Object> consulta : consultasApi) {
                                ConsultaTableModel model = converterParaTableModel(consulta);
                                if (model != null) {
                                    consultas.add(model);
                                }
                            }
                            System.out.println("✓ " + consultas.size() + " consultas carregadas do backend");
                        } else {
                            System.out.println("Nenhuma consulta encontrada no backend");
                            carregarConsultasMock();
                        }

                        if (progressIndicator != null) {
                            progressIndicator.setVisible(false);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        System.out.println("ID do paciente não encontrado");
                        carregarConsultasMock();
                        if (progressIndicator != null) {
                            progressIndicator.setVisible(false);
                        }
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.out.println("✗ Erro ao carregar consultas: " + e.getMessage());
                    carregarConsultasMock();
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                });
            }
        }).start();
    }

    private ConsultaTableModel converterParaTableModel(Map<String, Object> consulta) {
        try {
            Long id = null;
            if (consulta.containsKey("id")) {
                Object idObj = consulta.get("id");
                if (idObj instanceof Number) {
                    id = ((Number) idObj).longValue();
                }
            }

            String medicoNome = "Médico não especificado";
            if (consulta.containsKey("medico")) {
                Object medicoObj = consulta.get("medico");
                if (medicoObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> medico = (Map<String, Object>) medicoObj;
                    medicoNome = (String) medico.getOrDefault("nome", medicoNome);
                }
            }

            String dataHora = (String) consulta.getOrDefault("dataHora", "");
            String dataFormatada = dataHora;
            try {
                LocalDateTime dt = LocalDateTime.parse(dataHora);
                dataFormatada = dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            } catch (Exception e) {
                // Mantém o formato original
            }

            String status = (String) consulta.getOrDefault("status", "AGENDADA");
            String especialidade = "Especialidade"; // Você pode extrair do médico se tiver

            return new ConsultaTableModel(id, medicoNome, especialidade, dataFormatada, status);
        } catch (Exception e) {
            System.err.println("Erro ao converter consulta: " + e.getMessage());
            return null;
        }
    }

    private void cancelarConsultaNoBackend() {
        ConsultaTableModel selecionada = consultasTable.getSelectionModel().getSelectedItem();
        if (selecionada != null && selecionada.getId() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Cancelamento");
            alert.setHeaderText("Deseja cancelar esta consulta?");
            alert.setContentText("Consulta com " + selecionada.getMedico() + " em " + selecionada.getData());

            if (alert.showAndWait().get() == ButtonType.OK) {
                // Desabilitar botão durante operação
                btnCancelarConsulta.setDisable(true);

                new Thread(() -> {
                    try {
                        // Preparar dados para atualização
                        Map<String, Object> dadosAtualizacao = Map.of(
                                "status", "CANCELADA"
                        );

                        // Enviar para backend
                        Map<String, Object> response = ApiService.atualizarConsulta(
                                selecionada.getId(), dadosAtualizacao
                        );

                        Platform.runLater(() -> {
                            btnCancelarConsulta.setDisable(false);

                            if (response != null) {
                                System.out.println("✓ Consulta cancelada no backend: " + response);

                                // Atualizar localmente
                                selecionada.setStatus("CANCELADA");
                                consultasTable.refresh();

                                Notifications.create()
                                        .title("Consulta Cancelada")
                                        .text("Consulta cancelada com sucesso no sistema!")
                                        .showInformation();
                            } else {
                                mostrarErro("Erro ao cancelar consulta no backend");
                            }
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            btnCancelarConsulta.setDisable(false);
                            System.out.println("✗ Erro ao cancelar consulta: " + e.getMessage());

                            // Fallback: cancelar apenas localmente
                            selecionada.setStatus("CANCELADA");
                            consultasTable.refresh();

                            Notifications.create()
                                    .title("Cancelamento Local")
                                    .text("Consulta cancelada apenas localmente (erro no backend)")
                                    .showWarning();
                        });
                    }
                }).start();
            }
        } else {
            mostrarAlerta("Atenção", "Selecione uma consulta para cancelar!");
        }
    }

    @FXML
    private void agendarConsulta() {
        if (!validarAgendamento()) {
            return;
        }

        try {
            // Extrair ID do médico
            String medicoSelecionado = comboMedico.getValue();
            Long medicoId = extrairIdMedico(medicoSelecionado);

            if (medicoId == null) {
                mostrarErro("ID do médico não encontrado");
                return;
            }

            // Preparar dados da consulta
            Map<String, Object> consultaData = new java.util.HashMap<>();
            consultaData.put("pacienteId", AppContext.getUsuarioId());
            consultaData.put("medicoId", medicoId);
            consultaData.put("dataHora", datePicker.getValue() + "T" + comboHorario.getValue() + ":00");
            consultaData.put("motivo", txtObservacoes.getText());
            consultaData.put("status", "AGENDADA");

            // Desabilitar botão durante operação
            btnNovaConsulta.setDisable(true);

            new Thread(() -> {
                try {
                    Map<String, Object> response = ApiService.agendarConsulta(consultaData);

                    Platform.runLater(() -> {
                        btnNovaConsulta.setDisable(false);

                        if (response != null && response.containsKey("id")) {
                            // Adicionar à tabela local
                            ConsultaTableModel novaConsulta = criarModeloDeResposta(response, medicoSelecionado);
                            if (novaConsulta != null) {
                                consultas.add(novaConsulta);
                            }

                            Notifications.create()
                                    .title("Consulta Agendada!")
                                    .text("Consulta agendada com sucesso no sistema")
                                    .showInformation();

                            limparFormulario();
                            consultasTable.refresh();
                        } else {
                            mostrarErro("Erro ao agendar consulta no backend");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        btnNovaConsulta.setDisable(false);
                        mostrarErro("Erro: " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            mostrarErro("Erro ao processar agendamento: " + e.getMessage());
        }
    }

    private Long extrairIdMedico(String medicoSelecionado) {
        try {
            if (medicoSelecionado != null && medicoSelecionado.contains(" - ")) {
                String[] partes = medicoSelecionado.split(" - ");
                return Long.parseLong(partes[0]);
            }
        } catch (Exception e) {
            System.err.println("Erro ao extrair ID do médico: " + e.getMessage());
        }
        return null;
    }

    private ConsultaTableModel criarModeloDeResposta(Map<String, Object> response, String medicoSelecionado) {
        try {
            Long id = null;
            if (response.containsKey("id")) {
                Object idObj = response.get("id");
                if (idObj instanceof Number) {
                    id = ((Number) idObj).longValue();
                }
            }

            // Extrair nome do médico da string selecionada
            String medicoNome = "Médico";
            if (medicoSelecionado != null && medicoSelecionado.contains(" - ")) {
                String[] partes = medicoSelecionado.split(" - ");
                if (partes.length > 1) {
                    medicoNome = partes[1];
                }
            }

            String dataFormatada = datePicker.getValue() + " " + comboHorario.getValue();

            return new ConsultaTableModel(id, medicoNome, comboEspecialidade.getValue(),
                    dataFormatada, "AGENDADA");
        } catch (Exception e) {
            System.err.println("Erro ao criar modelo: " + e.getMessage());
            return null;
        }
    }

    private boolean validarAgendamento() {
        StringBuilder erros = new StringBuilder();

        if (comboEspecialidade.getValue() == null) {
            erros.append("• Selecione uma especialidade!\n");
        }
        if (comboMedico.getValue() == null) {
            erros.append("• Selecione um médico!\n");
        }
        if (datePicker.getValue() == null) {
            erros.append("• Selecione uma data!\n");
        }
        if (comboHorario.getValue() == null) {
            erros.append("• Selecione um horário!\n");
        }

        if (erros.length() > 0) {
            mostrarAlerta("Corrija os seguintes erros:\n" + erros.toString(), "Selecione uma consulta para cancelar!");
            return false;
        }
        return true;
    }

    private void limparFormulario() {
        comboEspecialidade.setValue(null);
        comboMedico.setValue(null);
        datePicker.setValue(null);
        comboHorario.setValue(null);
        txtObservacoes.clear();
    }

    private void carregarConsultasMock() {
        consultas.clear();
        consultas.add(new ConsultaTableModel(1L, "Dr. João Silva", "Cardiologia",
                LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                "AGENDADA"));

        consultas.add(new ConsultaTableModel(2L, "Dra. Maria Santos", "Dermatologia",
                LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                "CONFIRMADA"));

        System.out.println("✓ " + consultas.size() + " consultas mock carregadas");
    }

    @FXML
    private void handleSair() {
        AppContext.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("MediSchedule - Sistema de Agendamento de Consultas");

            Scene scene = new Scene(root);

            // Carregar CSS
            try {
                URL cssUrl = getClass().getResource("/styles/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠ CSS não encontrado para login");
            }

            stage.setScene(scene);

            // CONFIGURAR PARA TELA CHEIA
            stage.setMaximized(true);
            stage.setMinWidth(1024);
            stage.setMinHeight(768);

            Stage currentStage = (Stage) btnSair.getScene().getWindow();
            currentStage.close();

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao voltar para login: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensagem, String s) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void setUsuario(Map<String, Object> usuarioData) {
        System.out.println("PacienteDashboardController: setUsuario chamado");
        System.out.println("Dados recebidos: " + usuarioData);

        // Salvar dados no contexto
        if (usuarioData != null) {
            AppContext.setUsuarioLogado(usuarioData);

            // Atualizar label com nome do paciente
            String nome = (String) usuarioData.get("nome");
            if (nome != null) {
                Platform.runLater(() -> {
                    if (lblNomePaciente != null) {
                        lblNomePaciente.setText("Paciente: " + nome);
                    }
                });
            }

            // Carregar consultas após configurar usuário
            Platform.runLater(() -> {
                carregarDadosUsuario();
                carregarConsultasDoBackend();
                carregarMedicosDoBackend();
            });
        }
    }

    // Classe interna para modelo da tabela
    public static class ConsultaTableModel {
        private Long id;
        private String medico;
        private String especialidade;
        private String data;
        private String status;

        public ConsultaTableModel(Long id, String medico, String especialidade, String data, String status) {
            this.id = id;
            this.medico = medico;
            this.especialidade = especialidade;
            this.data = data;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getMedico() { return medico; }
        public String getEspecialidade() { return especialidade; }
        public String getData() { return data; }
        public String getStatus() { return status; }

        public void setStatus(String status) { this.status = status; }
    }

    private void carregarMedicosDoBackend() {
        new Thread(() -> {
            try {
                System.out.println("Carregando médicos do backend...");
                List<Map<String, Object>> medicos = ApiService.listarMedicos();

                Platform.runLater(() -> {
                    comboMedico.getItems().clear();

                    if (medicos != null && !medicos.isEmpty()) {
                        for (Map<String, Object> medico : medicos) {
                            Long id = ((Number) medico.get("id")).longValue();
                            String nome = (String) medico.get("nome");
                            String especialidade = (String) medico.get("especialidade");
                            String crm = (String) medico.get("crm");

                            // Adicionar ao ComboBox no formato: "ID - Nome (Especialidade) - CRM"
                            String display = String.format("%d - %s (%s) - %s",
                                    id, nome, especialidade, crm);
                            comboMedico.getItems().add(display);
                        }
                        System.out.println("✓ " + medicos.size() + " médicos carregados");
                    } else {
                        System.out.println("Nenhum médico encontrado no backend");
                        carregarMedicosMock();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.out.println("✗ Erro ao carregar médicos: " + e.getMessage());
                    carregarMedicosMock();
                });
            }
        }).start();
    }

    private void carregarMedicosMock() {
        comboMedico.getItems().clear();
        comboMedico.getItems().addAll(
                "1 - Dr. João Silva (Cardiologia) - CRM/SP 123456",
                "2 - Dra. Maria Santos (Dermatologia) - CRM/SP 789012",
                "3 - Dr. Pedro Oliveira (Ortopedia) - CRM/SP 345678"
        );
        System.out.println("Médicos mock carregados");
    }
}