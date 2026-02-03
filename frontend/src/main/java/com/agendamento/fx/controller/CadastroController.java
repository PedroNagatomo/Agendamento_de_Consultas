package com.agendamento.fx.controller;

import com.agendamento.fx.service.ApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class CadastroController {

    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private PasswordField txtConfirmarSenha;
    @FXML private TextField txtCpf;
    @FXML private TextField txtTelefone;
    @FXML private ComboBox<String> comboTipoUsuario;

    @FXML private VBox medicoFields;
    @FXML private TextField txtCRM;
    @FXML private ComboBox<String> comboEspecialidade;

    @FXML private VBox pacienteFields;
    @FXML private DatePicker dateNascimento;
    @FXML private TextArea txtObservacoes;

    @FXML private Button btnCadastrar;
    @FXML private Button btnVoltar;

    @FXML
    public void initialize() {
        System.out.println("CadastroController inicializado!");

        configurarCombos();
        configurarBotoes();
        configurarListeners();
        if (btnVoltar != null) {
            btnVoltar.setOnAction(e -> voltarParaLogin());
        }
    }

    private void configurarCombos() {
        comboTipoUsuario.getItems().addAll("PACIENTE", "MEDICO", "ADMIN");
        comboTipoUsuario.setValue("PACIENTE");

        comboEspecialidade.getItems().addAll(
                "Cardiologia", "Dermatologia", "Ortopedia",
                "Pediatria", "Ginecologia", "Clínico Geral"
        );

        // Mostrar/ocultar campos baseados no tipo de usuário
        comboTipoUsuario.setOnAction(e -> atualizarCamposVisiveis());
        atualizarCamposVisiveis();
    }

    private void atualizarCamposVisiveis() {
        String tipo = comboTipoUsuario.getValue();

        // Campos de médico
        boolean isMedico = "MEDICO".equals(tipo);
        medicoFields.setVisible(isMedico);
        medicoFields.setManaged(isMedico);

        // Campos de paciente
        boolean isPaciente = "PACIENTE".equals(tipo);
        pacienteFields.setVisible(isPaciente);
        pacienteFields.setManaged(isPaciente);
    }

    private void configurarBotoes() {
        btnCadastrar.setOnAction(e -> realizarCadastro());
        btnVoltar.setOnAction(e -> voltarParaLogin());
    }

    private void configurarListeners() {
        // Para CPF
        UnaryOperator<TextFormatter.Change> filterCPF = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,11}")) {
                // Formatar como 000.000.000-00
                if (newText.length() == 11) {
                    String formatado = newText.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
                    change.setText(formatado);
                }
                return change;
            }
            return null;
        };
        txtCpf.setTextFormatter(new TextFormatter<>(filterCPF));

        // Para telefone
        UnaryOperator<TextFormatter.Change> filterTel = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9\\s\\-()]{0,15}")) {
                return change;
            }
            return null;
        };
        txtTelefone.setTextFormatter(new TextFormatter<>(filterTel));
    }

    private void realizarCadastro() {
        if (!validarFormulario()) {
            return;
        }

        // Desabilitar botão durante cadastro
        btnCadastrar.setDisable(true);
        btnCadastrar.setText("Cadastrando...");

        new Thread(() -> {
            try {
                Map<String, Object> dadosCadastro = new HashMap<>();
                dadosCadastro.put("nome", txtNome.getText());
                dadosCadastro.put("email", txtEmail.getText());
                dadosCadastro.put("senha", txtSenha.getText());
                dadosCadastro.put("cpf", txtCpf.getText().replaceAll("[^\\d]", ""));
                dadosCadastro.put("telefone", txtTelefone.getText().replaceAll("[^\\d]", ""));
                dadosCadastro.put("tipo", comboTipoUsuario.getValue());

                // Dados específicos para MÉDICO
                if ("MEDICO".equals(comboTipoUsuario.getValue())) {
                    dadosCadastro.put("crm", txtCRM.getText());
                    dadosCadastro.put("especialidade", comboEspecialidade.getValue());
                    // Adicione mais campos se necessário
                    dadosCadastro.put("ativo", true); // Médico ativo por padrão
                }
                // Dados para PACIENTE
                else if ("PACIENTE".equals(comboTipoUsuario.getValue())) {
                    if (dateNascimento.getValue() != null) {
                        dadosCadastro.put("dataNascimento", dateNascimento.getValue().toString());
                    }
                    dadosCadastro.put("observacoes", txtObservacoes.getText());
                }

                System.out.println("Dados para cadastro: " + dadosCadastro);

                // Enviar para API
                Map<String, Object> response = ApiService.cadastrar(dadosCadastro);

                Platform.runLater(() -> {
                    btnCadastrar.setDisable(false);
                    btnCadastrar.setText("Cadastrar");

                    if (response != null && response.containsKey("id")) {
                        System.out.println("Cadastro bem-sucedido: " + response);

                        Notifications.create()
                                .title("Cadastro realizado!")
                                .text("Usuário cadastrado com sucesso. ID: " + response.get("id"))
                                .showInformation();

                        // Se for médico, mostrar mensagem especial
                        if ("MEDICO".equals(comboTipoUsuario.getValue())) {
                            Notifications.create()
                                    .title("Médico Cadastrado")
                                    .text("Agora você aparecerá na lista para pacientes agendarem consultas!")
                                    .showInformation();
                        }

                        voltarParaLogin();

                    } else if (response != null && response.containsKey("error")) {
                        mostrarErro("Erro no cadastro: " + response.get("error"));
                    } else {
                        mostrarErro("Erro no cadastro. Resposta: " + response);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnCadastrar.setDisable(false);
                    btnCadastrar.setText("Cadastrar");

                    mostrarErro("Erro: " + e.getMessage());
                });
            }
        }).start();
    }

    private void simularCadastroLocal() {
        Notifications.create()
                .title("Cadastro (Local)")
                .text("Usuário cadastrado localmente: " + txtNome.getText())
                .showInformation();
    }

    private boolean validarFormulario() {
        StringBuilder erros = new StringBuilder();

        if (txtNome.getText().isEmpty()) {
            erros.append("• Nome é obrigatório!\n");
        }

        if (txtEmail.getText().isEmpty() || !txtEmail.getText().contains("@")) {
            erros.append("• Email inválido!\n");
        }

        if (txtSenha.getText().length() < 6) {
            erros.append("• Senha deve ter no mínimo 6 caracteres!\n");
        }

        if (!txtSenha.getText().equals(txtConfirmarSenha.getText())) {
            erros.append("• Senhas não conferem!\n");
        }

        String cpfLimpo = txtCpf.getText().replaceAll("[^\\d]", "");
        if (cpfLimpo.length() != 11) {
            erros.append("• CPF inválido! Deve ter 11 dígitos.\n");
        }

        // Validações específicas
        if ("MEDICO".equals(comboTipoUsuario.getValue())) {
            if (txtCRM.getText().isEmpty()) {
                erros.append("• CRM é obrigatório para médicos!\n");
            }

            if (comboEspecialidade.getValue() == null) {
                erros.append("• Especialidade é obrigatória!\n");
            }
        }

        if (erros.length() > 0) {
            mostrarAlerta("Corrija os erros:", erros.toString());
            return false;
        }
        return true;
    }

    private void voltarParaLogin() {
        try {
            Stage currentStage = (Stage) btnVoltar.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("MediSchedule - Login");

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

            currentStage.close();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void mostrarErro(String mensagem) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro no Cadastro");
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }


}