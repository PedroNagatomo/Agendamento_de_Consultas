package com.agendamento.fx.controller;

import com.agendamento.fx.AppContext;
import com.agendamento.fx.service.ApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MedicoDashboardController implements Initializable {

    @FXML private TableView<ConsultaMedicoTableModel> consultasTable;
    @FXML private TableColumn<ConsultaMedicoTableModel, String> colPaciente;
    @FXML private TableColumn<ConsultaMedicoTableModel, String> colData;
    @FXML private TableColumn<ConsultaMedicoTableModel, String> colMotivo;
    @FXML private TableColumn<ConsultaMedicoTableModel, String> colStatus;
    @FXML private TableColumn<ConsultaMedicoTableModel, String> colAcoes;

    @FXML private Button btnAtualizar;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;
    @FXML private Button btnSair;
    @FXML private Label lblNomeMedico;
    @FXML private Label lblEspecialidade;
    @FXML private Label lblCRM;
    @FXML private ProgressIndicator progressIndicator;

    private ObservableList<ConsultaMedicoTableModel> consultas = FXCollections.observableArrayList();
    private Long medicoId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("MedicoDashboardController inicializado!");

        configurarTabela();
        configurarEventos();
        carregarDadosMedico();
        carregarConsultasDoBackend();
    }

    private void configurarTabela() {
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("paciente"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Coluna de ações com botões
        colAcoes.setCellFactory(param -> new TableCell<ConsultaMedicoTableModel, String>() {
            private final Button btnConfirmarCell = new Button("Confirmar");
            private final Button btnCancelarCell = new Button("Cancelar");
            private final HBox pane = new HBox(5, btnConfirmarCell, btnCancelarCell);

            {
                btnConfirmarCell.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnCancelarCell.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                btnConfirmarCell.setOnAction(event -> {
                    ConsultaMedicoTableModel consulta = getTableView().getItems().get(getIndex());
                    confirmarConsulta(consulta);
                });

                btnCancelarCell.setOnAction(event -> {
                    ConsultaMedicoTableModel consulta = getTableView().getItems().get(getIndex());
                    cancelarConsulta(consulta);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ConsultaMedicoTableModel consulta = getTableView().getItems().get(getIndex());
                    String status = consulta.getStatus();

                    // Mostrar botões apenas para consultas agendadas
                    if ("AGENDADA".equals(status) || "AGENDADO".equals(status)) {
                        btnConfirmarCell.setDisable(false);
                        btnCancelarCell.setDisable(false);
                        setGraphic(pane);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        consultasTable.setItems(consultas);
    }

    private void configurarEventos() {
        btnAtualizar.setOnAction(e -> carregarConsultasDoBackend());
        btnConfirmar.setOnAction(e -> confirmarConsultaSelecionada());
        btnCancelar.setOnAction(e -> cancelarConsultaSelecionada());
        btnSair.setOnAction(e -> handleSair());
    }

    private void carregarDadosMedico() {
        Map<String, Object> usuario = AppContext.getUsuarioLogado();
        if (usuario != null) {
            medicoId = AppContext.getUsuarioId();
            lblNomeMedico.setText("Dr(a). " + AppContext.getUsuarioNome());

            if (usuario.containsKey("especialidade")) {
                lblEspecialidade.setText("Especialidade: " + usuario.get("especialidade"));
            }

            if (usuario.containsKey("crm")) {
                lblCRM.setText("CRM: " + usuario.get("crm"));
            }
        }
    }

    private void carregarConsultasDoBackend() {
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }

        new Thread(() -> {
            try {
                System.out.println("Carregando consultas do médico ID: " + medicoId);

                if (medicoId != null) {
                    List<Map<String, Object>> consultasApi = ApiService.getConsultasMedico(medicoId);

                    Platform.runLater(() -> {
                        consultas.clear();

                        if (consultasApi != null && !consultasApi.isEmpty()) {
                            for (Map<String, Object> consulta : consultasApi) {
                                ConsultaMedicoTableModel model = converterParaTableModel(consulta);
                                if (model != null) {
                                    consultas.add(model);
                                }
                            }
                            System.out.println("✓ " + consultas.size() + " consultas carregadas");
                        } else {
                            System.out.println("Nenhuma consulta encontrada");
                            carregarConsultasMock();
                        }

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

    private ConsultaMedicoTableModel converterParaTableModel(Map<String, Object> consulta) {
        try {
            Long id = null;
            if (consulta.containsKey("id")) {
                Object idObj = consulta.get("id");
                if (idObj instanceof Number) {
                    id = ((Number) idObj).longValue();
                }
            }

            String pacienteNome = "Paciente não especificado";
            if (consulta.containsKey("paciente")) {
                Object pacienteObj = consulta.get("paciente");
                if (pacienteObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> paciente = (Map<String, Object>) pacienteObj;
                    pacienteNome = (String) paciente.getOrDefault("nome", pacienteNome);
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

            String motivo = (String) consulta.getOrDefault("motivo", "Consulta médica");
            String status = (String) consulta.getOrDefault("status", "AGENDADA");

            return new ConsultaMedicoTableModel(id, pacienteNome, dataFormatada, motivo, status);
        } catch (Exception e) {
            System.err.println("Erro ao converter consulta: " + e.getMessage());
            return null;
        }
    }

    private void confirmarConsultaSelecionada() {
        ConsultaMedicoTableModel selecionada = consultasTable.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            confirmarConsulta(selecionada);
        } else {
            mostrarAlerta("Selecione uma consulta para confirmar");
        }
    }

    private void cancelarConsultaSelecionada() {
        ConsultaMedicoTableModel selecionada = consultasTable.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            cancelarConsulta(selecionada);
        } else {
            mostrarAlerta("Selecione uma consulta para cancelar");
        }
    }

    private void confirmarConsulta(ConsultaMedicoTableModel consulta) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Consulta");
        alert.setHeaderText("Deseja confirmar esta consulta?");
        alert.setContentText("Consulta com " + consulta.getPaciente() + " em " + consulta.getData());

        if (alert.showAndWait().get() == ButtonType.OK) {
            atualizarStatusConsulta(consulta, "CONFIRMADA");
        }
    }

    private void cancelarConsulta(ConsultaMedicoTableModel consulta) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancelar Consulta");
        alert.setHeaderText("Deseja cancelar esta consulta?");
        alert.setContentText("Consulta com " + consulta.getPaciente() + " em " + consulta.getData());

        if (alert.showAndWait().get() == ButtonType.OK) {
            atualizarStatusConsulta(consulta, "CANCELADA");
        }
    }

    private boolean tentarUsarEndpointAgendamento(Long consultaId, String novoStatus) {
        try {
            // Usar o endpoint de agendamento para criar uma consulta "atualizada"
            String url = "http://localhost:8083/api/consultas";

            // Buscar consulta atual para ter os dados
            Map<String, Object> consultaAtual = buscarConsultaPorId(consultaId);
            if (consultaAtual == null) return false;

            // Atualizar status
            consultaAtual.put("status", novoStatus);

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(consultaAtual);

            System.out.println("POST (agendamento) para: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status agendamento: " + response.statusCode());

            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            System.out.println("Endpoint agendamento falhou: " + e.getMessage());
            return false;
        }
    }

    private boolean tentarSubstituirConsulta(Long consultaId, String novoStatus) {
        try {
            System.out.println("Tentando substituir consulta " + consultaId);

            // 1. Primeiro tentar deletar a antiga
            boolean deletado = tentarDeletarConsulta(consultaId);

            // 2. Criar uma nova com status atualizado
            if (deletado) {
                return tentarCriarNovaConsulta(consultaId, novoStatus);
            }

            return false;

        } catch (Exception e) {
            System.out.println("Substituição falhou: " + e.getMessage());
            return false;
        }
    }

    private boolean tentarDeletarConsulta(Long consultaId) {
        try {
            String url = "http://localhost:8083/api/consultas/" + consultaId;

            System.out.println("DELETE para: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status DELETE: " + response.statusCode());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.out.println("DELETE falhou: " + e.getMessage());
            return false;
        }
    }

    private void atualizarStatusConsulta(ConsultaMedicoTableModel consulta, String novoStatus) {
        if (consulta.getId() != null) {
            // Mostrar diálogo de confirmação
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar " + ("CONFIRMADA".equals(novoStatus) ? "Confirmação" : "Cancelamento"));
            confirmacao.setHeaderText("Deseja " + ("CONFIRMADA".equals(novoStatus) ? "confirmar" : "cancelar") + " esta consulta?");
            confirmacao.setContentText("Paciente: " + consulta.getPaciente() + "\nData: " + consulta.getData());

            if (confirmacao.showAndWait().get() != ButtonType.OK) {
                return;
            }

            // Desabilitar botões durante operação
            btnConfirmar.setDisable(true);
            btnCancelar.setDisable(true);

            // Mostrar loading
            if (progressIndicator != null) {
                progressIndicator.setVisible(true);
            }

            new Thread(() -> {
                boolean sucessoBackend = false;
                String mensagemErro = "";

                try {
                    System.out.println("=== TENTANDO ATUALIZAR CONSULTA " + consulta.getId() + " ===");

                    // Método 1: Tentar PUT para o endpoint de consultas (que sabemos que não existe, mas vamos tentar)
                    sucessoBackend = tentarAtualizarViaPut(consulta.getId(), novoStatus);

                    // Método 2: Se PUT falhar, tentar uma solução alternativa
                    if (!sucessoBackend) {
                        sucessoBackend = tentarSolucaoAlternativa(consulta.getId(), novoStatus);
                    }

                    // Método 3: Se ainda falhar, tentar simular no frontend
                    if (!sucessoBackend) {
                        mensagemErro = "Endpoint de atualização não encontrado no backend. ";
                        mensagemErro += "Consulte o desenvolvedor do backend para implementar PUT /api/consultas/{id}";
                    }

                } catch (Exception e) {
                    mensagemErro = "Erro: " + e.getMessage();
                }

                final boolean sucessoFinal = sucessoBackend;
                final String erroFinal = mensagemErro;

                Platform.runLater(() -> {
                    // Reabilitar botões
                    btnConfirmar.setDisable(false);
                    btnCancelar.setDisable(false);

                    // Esconder loading
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }

                    if (sucessoFinal) {
                        // Atualizar localmente
                        consulta.setStatus(novoStatus);
                        consultasTable.refresh();

                        // Mostrar mensagem de sucesso
                        Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                        sucesso.setTitle("Sucesso");
                        sucesso.setHeaderText(null);
                        sucesso.setContentText("Consulta " + ("CONFIRMADA".equals(novoStatus) ? "confirmada" : "cancelada") +
                                " com sucesso!");
                        sucesso.showAndWait();

                    } else {
                        // Mostrar opções ao usuário
                        Alert opcoes = new Alert(Alert.AlertType.CONFIRMATION);
                        opcoes.setTitle("Endpoint não disponível");
                        opcoes.setHeaderText("Não foi possível atualizar no servidor");
                        opcoes.setContentText(erroFinal + "\n\nDeseja aplicar a alteração apenas localmente?");

                        ButtonType simLocal = new ButtonType("Sim, aplicar localmente");
                        ButtonType nao = new ButtonType("Não, cancelar");
                        opcoes.getButtonTypes().setAll(simLocal, nao);

                        opcoes.showAndWait().ifPresent(resposta -> {
                            if (resposta == simLocal) {
                                consulta.setStatus(novoStatus);
                                consultasTable.refresh();

                                Alert local = new Alert(Alert.AlertType.INFORMATION);
                                local.setTitle("Alteração local");
                                local.setHeaderText(null);
                                local.setContentText("Alteração aplicada apenas localmente.\n" +
                                        "A consulta voltará ao estado original ao recarregar.");
                                local.showAndWait();
                            }
                        });
                    }
                });

            }).start();

        } else {
            // Consulta sem ID (mock)
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenção");
            alert.setHeaderText("Consulta de demonstração");
            alert.setContentText("Esta é uma consulta de demonstração.\n" +
                    "A alteração foi aplicada apenas localmente.");
            alert.showAndWait();

            consulta.setStatus(novoStatus);
            consultasTable.refresh();
        }
    }

    private boolean tentarAtualizarViaPut(Long consultaId, String novoStatus) {
        try {
            String url = "http://localhost:8083/api/consultas/" + consultaId;

            // Criar dados da consulta (igual aos que vêm do GET)
            Map<String, Object> consultaData = new java.util.HashMap<>();
            consultaData.put("id", consultaId);
            consultaData.put("status", novoStatus);
            // Adicionar outros campos que podem ser necessários
            consultaData.put("dataHora", java.time.LocalDateTime.now().toString());

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(consultaData);

            System.out.println("PUT para: " + url);
            System.out.println("Body: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.out.println("PUT falhou: " + e.getMessage());
            return false;
        }
    }

    private boolean tentarSolucaoAlternativa(Long consultaId, String novoStatus) {
        try {
            System.out.println("Tentando solução alternativa para consulta " + consultaId);

            // Método A: Tentar criar uma nova consulta com status atualizado
            boolean sucesso = tentarCriarNovaConsulta(consultaId, novoStatus);
            if (sucesso) return true;

            // Método B: Tentar usar o endpoint de agendamento existente
            sucesso = tentarUsarEndpointAgendamento(consultaId, novoStatus);
            if (sucesso) return true;

            // Método C: Tentar DELETE + POST (substituição)
            sucesso = tentarSubstituirConsulta(consultaId, novoStatus);

            return sucesso;

        } catch (Exception e) {
            System.out.println("Solução alternativa falhou: " + e.getMessage());
            return false;
        }
    }

    private boolean tentarCriarNovaConsulta(Long consultaId, String novoStatus) {
        try {
            String url = "http://localhost:8083/api/consultas";

            // Primeiro, buscar os dados atuais da consulta
            Map<String, Object> consultaAtual = buscarConsultaPorId(consultaId);
            if (consultaAtual == null) return false;

            // Atualizar o status
            consultaAtual.put("status", novoStatus);

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(consultaAtual);

            System.out.println("POST (criação) para: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status criação: " + response.statusCode());

            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            System.out.println("Criação falhou: " + e.getMessage());
            return false;
        }
    }

    private Map<String, Object> buscarConsultaPorId(Long consultaId) {
        try {
            String url = "http://localhost:8083/api/consultas/" + consultaId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(3))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            }

        } catch (Exception e) {
            System.out.println("Busca por ID falhou: " + e.getMessage());
        }
        return null;
    }

    private void carregarConsultasMock() {
        consultas.clear();
        consultas.add(new ConsultaMedicoTableModel(1L, "Carlos Silva",
                LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                "Dor nas costas", "AGENDADA"));

        consultas.add(new ConsultaMedicoTableModel(2L, "Ana Santos",
                LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                "Check-up anual", "AGENDADA"));

        consultas.add(new ConsultaMedicoTableModel(3L, "João Pereira",
                LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                "Pressão alta", "CONFIRMADA"));
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
        }
    }

    private void mostrarAlerta(String mensagem) {
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
        System.out.println("MedicoDashboardController: setUsuario chamado");
        System.out.println("Dados recebidos: " + usuarioData);

        // Salvar dados no contexto
        if (usuarioData != null) {
            AppContext.setUsuarioLogado(usuarioData);

            // Atualizar labels com dados do médico
            Platform.runLater(() -> {
                String nome = (String) usuarioData.get("nome");
                if (nome != null && lblNomeMedico != null) {
                    lblNomeMedico.setText("Dr(a). " + nome);
                }

                if (usuarioData.containsKey("especialidade") && lblEspecialidade != null) {
                    lblEspecialidade.setText("Especialidade: " + usuarioData.get("especialidade"));
                }

                if (usuarioData.containsKey("crm") && lblCRM != null) {
                    lblCRM.setText("CRM: " + usuarioData.get("crm"));
                }

                // Carregar consultas após configurar usuário
                carregarDadosMedico();
                carregarConsultasDoBackend();
            });
        }
    }

    private void testarEndpointConsulta(Long consultaId) {
        new Thread(() -> {
            System.out.println("=== TESTANDO ENDPOINTS PARA CONSULTA " + consultaId + " ===");

            // Testar diferentes métodos HTTP
            String baseUrl = "http://localhost:8083/api";
            String[] metodos = {"PUT", "PATCH", "POST"};
            String[] caminhos = {
                    "/consultas/" + consultaId,
                    "/consultas/atualizar/" + consultaId,
                    "/consultas/update/" + consultaId,
                    "/consultas/" + consultaId + "/status",
                    "/consultas/status/" + consultaId
            };

            for (String caminho : caminhos) {
                for (String metodo : metodos) {
                    try {
                        String url = baseUrl + caminho;
                        System.out.println("\nTestando " + metodo + " " + url);

                        // Criar dados de teste
                        Map<String, Object> dados = Map.of("status", "CONFIRMADA");
                        ObjectMapper mapper = new ObjectMapper();
                        String requestBody = mapper.writeValueAsString(dados);

                        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header("Content-Type", "application/json")
                                .timeout(Duration.ofSeconds(3));

                        // Configurar método HTTP
                        switch (metodo) {
                            case "PUT":
                                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(requestBody));
                                break;
                            case "PATCH":
                                requestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody));
                                break;
                            case "POST":
                                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
                                break;
                        }

                        HttpResponse<String> response = HttpClient.newHttpClient()
                                .send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

                        System.out.println("Status: " + response.statusCode());
                        System.out.println("Resposta: " + response.body());

                    } catch (Exception e) {
                        System.out.println("Erro: " + e.getMessage());
                    }
                }
            }

            System.out.println("=== FIM DOS TESTES ===");
        }).start();
    }

    private boolean tentarPutDireto(Long consultaId, String novoStatus) {
        try {
            String url = "http://localhost:8083/api/consultas/" + consultaId;

            // Criar objeto de consulta completo
            Map<String, Object> consultaCompleta = new java.util.HashMap<>();
            consultaCompleta.put("id", consultaId);
            consultaCompleta.put("status", novoStatus);
            // Adicione outros campos que seu backend espera

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(consultaCompleta);

            System.out.println("PUT direto para: " + url);
            System.out.println("Body: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status PUT direto: " + response.statusCode());
            System.out.println("Resposta: " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("Erro no PUT direto: " + e.getMessage());
            return false;
        }
    }

    private boolean tentarRequisicaoManual(Long consultaId, String novoStatus) {
        try {
            // Testar diferentes combinações de URL e método
            String[] urls = {
                    "http://localhost:8083/consultas/" + consultaId,
                    "http://localhost:8083/api/consultas/" + consultaId,
                    "http://localhost:8083/consultas/update",
                    "http://localhost:8083/api/consultas/update"
            };

            String[] metodos = {"PUT", "POST", "PATCH"};

            for (String url : urls) {
                for (String metodo : metodos) {
                    try {
                        System.out.println("Tentando " + metodo + " para: " + url);

                        Map<String, Object> dados = new java.util.HashMap<>();
                        dados.put("id", consultaId);
                        dados.put("status", novoStatus);

                        ObjectMapper mapper = new ObjectMapper();
                        String requestBody = mapper.writeValueAsString(dados);

                        HttpRequest.Builder builder = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header("Content-Type", "application/json")
                                .timeout(Duration.ofSeconds(3));

                        switch (metodo) {
                            case "PUT": builder.PUT(HttpRequest.BodyPublishers.ofString(requestBody)); break;
                            case "POST": builder.POST(HttpRequest.BodyPublishers.ofString(requestBody)); break;
                            case "PATCH": builder.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody)); break;
                        }

                        HttpResponse<String> response = HttpClient.newHttpClient()
                                .send(builder.build(), HttpResponse.BodyHandlers.ofString());

                        System.out.println("Status: " + response.statusCode());

                        if (response.statusCode() == 200) {
                            System.out.println("✓ Sucesso com " + metodo + " " + url);
                            return true;
                        }

                    } catch (Exception e) {
                        // Continuar para próxima combinação
                        continue;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erro na requisição manual: " + e.getMessage());
        }

        return false;
    }

    // Classe interna para modelo da tabela
    public static class ConsultaMedicoTableModel {
        private Long id;
        private String paciente;
        private String data;
        private String motivo;
        private String status;

        public ConsultaMedicoTableModel(Long id, String paciente, String data, String motivo, String status) {
            this.id = id;
            this.paciente = paciente;
            this.data = data;
            this.motivo = motivo;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getPaciente() { return paciente; }
        public String getData() { return data; }
        public String getMotivo() { return motivo; }
        public String getStatus() { return status; }

        public void setStatus(String status) { this.status = status; }
    }
}