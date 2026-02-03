package com.agendamento.fx.controller;

import com.agendamento.fx.AppContext;
import com.agendamento.fx.service.ApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.geometry.Insets;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> tipoUsuarioComboBox;
    @FXML private Button loginButton;
    @FXML private Button btnCadastrar;

    @FXML
    public void initialize() {
        System.out.println("LoginController inicializado!");

        // Configurar ComboBox
        tipoUsuarioComboBox.getItems().addAll("PACIENTE", "MEDICO", "ADMIN");
        tipoUsuarioComboBox.setValue("PACIENTE");

        // Configurar botões
        loginButton.setOnAction(e -> realizarLogin());
        btnCadastrar.setOnAction(e -> abrirCadastro());

        // Preencher dados de teste
        usernameField.setText("***@email.***");
        passwordField.setText("******");

        // Testar conexão com backend (com delay para garantir que a janela está pronta)
        Platform.runLater(() -> {
            // Aguardar 500ms para garantir que a janela está totalmente carregada
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void realizarLogin() {
        String email = usernameField.getText();
        String senha = passwordField.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            mostrarAlerta("Erro", "Preencha email e senha", Alert.AlertType.ERROR);
            return;
        }

        // Desabilitar botão durante login
        loginButton.setDisable(true);
        loginButton.setText("Conectando...");

        // Executar login em thread separada
        new Thread(() -> {
            try {
                System.out.println("Tentando login com API... Email: " + email);

                // Tentar login com API real
                Map<String, Object> response = ApiService.login(email, senha);

                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Login");

                    if (response != null && response.containsKey("id")) {
                        System.out.println("✓ Login via API bem-sucedido!");
                        System.out.println("Dados recebidos: " + response);

                        // Salvar usuário no contexto
                        AppContext.setUsuarioLogado(response);

                        // Garantir que temos o tipo
                        String tipo = AppContext.getUsuarioTipo();
                        if (tipo == null || tipo.isEmpty()) {
                            tipo = tipoUsuarioComboBox.getValue();
                        }

                        String nome = AppContext.getUsuarioNome();
                        if (nome == null) {
                            nome = email;
                        }

                        mostrarAlerta("Login Realizado", "Bem-vindo, " + nome + "!", Alert.AlertType.INFORMATION);
                        abrirDashboard(tipo);

                    } else if (response != null && response.containsKey("error")) {
                        System.out.println("✗ API retornou erro: " + response.get("error"));
                        mostrarAlerta("Erro no Login",
                                "Erro: " + response.get("error") + "\n\n" +
                                        "Tente:\n" +
                                        "• admin@email.com / 123\n" +
                                        "• Ou use dados locais", Alert.AlertType.ERROR);
                        loginMock(email, senha);
                    } else {
                        System.out.println("✗ API respondeu mas sem dados válidos");
                        System.out.println("Response completo: " + response);

                        mostrarAlerta("API Offline",
                                "A API respondeu mas não retornou dados válidos.\n" +
                                        "Usando dados locais para demonstração.", Alert.AlertType.WARNING);
                        loginMock(email, senha);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Login");

                    System.out.println("✗ Erro na API: " + e.getMessage());

                    // Fallback para login mock
                    mostrarAlerta("Erro na Conexão",
                            "Erro: " + e.getMessage() + "\n\n" +
                                    "Usando dados locais para demonstração.\n" +
                                    "Usuários de teste disponíveis:\n" +
                                    "• Paciente: carlos@email.com / 123\n" +
                                    "• Médico: maria@email.com / 123\n" +
                                    "• Admin: joao@email.com / 123", Alert.AlertType.WARNING);
                    loginMock(email, senha);
                });
            }
        }).start();
    }

    private void loginMock(String email, String senha) {
        System.out.println("Usando login mock para: " + email);

        // Se o usuário digitar apenas "maria", assumimos que é o email completo
        String emailCompleto = email;
        if (!email.contains("@")) {
            if (email.equals("carlos")) {
                emailCompleto = "carlos@email.com";
            } else if (email.equals("maria")) {
                emailCompleto = "maria@email.com";
            } else if (email.equals("joao")) {
                emailCompleto = "joao@email.com";
            }
        }

        // Fallback para dados mock
        if ((email.equals("carlos") || emailCompleto.equals("carlos@email.com")) && senha.equals("123")) {
            Map<String, Object> mockUsuario = Map.of(
                    "id", 1L,
                    "nome", "Carlos Silva",
                    "email", "carlos@email.com",
                    "tipo", "PACIENTE"
            );
            AppContext.setUsuarioLogado(mockUsuario);
            mostrarAlerta("Login Realizado", "Bem-vindo, Carlos Silva!", Alert.AlertType.INFORMATION);
            abrirDashboard("PACIENTE");

        } else if ((email.equals("maria") || emailCompleto.equals("maria@email.com")) && senha.equals("123")) {
            Map<String, Object> mockUsuario = Map.of(
                    "id", 2L,
                    "nome", "Maria Santos",
                    "email", "maria@email.com",
                    "tipo", "MEDICO",
                    "especialidade", "Cardiologia",
                    "crm", "CRM/SP 123456"
            );
            AppContext.setUsuarioLogado(mockUsuario);
            mostrarAlerta("Login Realizado", "Bem-vindo, Dra. Maria Santos!", Alert.AlertType.INFORMATION);
            abrirDashboard("MEDICO");

        } else if ((email.equals("joao") || emailCompleto.equals("joao@email.com")) && senha.equals("123")) {
            Map<String, Object> mockUsuario = Map.of(
                    "id", 3L,
                    "nome", "João Administrador",
                    "email", "joao@email.com",
                    "tipo", "ADMIN"
            );
            AppContext.setUsuarioLogado(mockUsuario);
            mostrarAlerta("Login Realizado", "Bem-vindo, João Administrador!", Alert.AlertType.INFORMATION);
            abrirDashboard("ADMIN");

        } else if (email.equals("admin@email.com") && senha.equals("123")) {
            Map<String, Object> mockUsuario = Map.of(
                    "id", 4L,
                    "nome", "Administrador",
                    "email", email,
                    "tipo", "ADMIN"
            );
            AppContext.setUsuarioLogado(mockUsuario);
            mostrarAlerta("Login Realizado", "Bem-vindo, Administrador!", Alert.AlertType.INFORMATION);
            abrirDashboard("ADMIN");

        } else {
            mostrarAlerta("Login falhou",
                    "Credenciais inválidas.\n\n" +
                            "Tente os usuários de teste:\n" +
                            "• Paciente: carlos@email.com / 123\n" +
                            "• Médico: maria@email.com / 123\n" +
                            "• Admin: joao@email.com / 123\n\n" +
                            "Ou tente login via API com:\n" +
                            "• admin@email.com / 123", Alert.AlertType.ERROR);
        }
    }

    private void abrirCadastro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Cadastro.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Cadastro de Usuário - MediSchedule");

            Scene scene = new Scene(root);

            // Carregar CSS
            try {
                URL cssUrl = getClass().getResource("/styles/style.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠ CSS não encontrado para cadastro");
            }

            stage.setScene(scene);

            // CONFIGURAR PARA TELA CHEIA
            stage.setMaximized(true);
            stage.setMinWidth(1024);
            stage.setMinHeight(768);

            // Definir como modal
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnCadastrar.getScene().getWindow());

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Tela de cadastro não encontrada!\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void abrirDashboard(String tipo) {
        try {
            String fxmlPath = "";
            String titulo = "";

            switch (tipo.toUpperCase()) {
                case "PACIENTE":
                    fxmlPath = "/views/PacienteDashboard.fxml";
                    titulo = "Dashboard do Paciente";
                    break;
                case "MEDICO":
                    fxmlPath = "/views/MedicoDashboard.fxml";
                    titulo = "Dashboard do Médico";
                    break;
                case "ADMIN":
                    fxmlPath = "/views/AdminDashboard.fxml";
                    titulo = "Dashboard do Administrador";
                    break;
                default:
                    mostrarAlerta("Erro", "Tipo de usuário não suportado: " + tipo,
                            Alert.AlertType.ERROR);
                    return;
            }

            System.out.println("Carregando FXML: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Configurar controller com dados do contexto
            Map<String, Object> usuarioData = AppContext.getUsuarioLogado();
            if (tipo.equalsIgnoreCase("PACIENTE")) {
                PacienteDashboardController controller = loader.getController();
                if (controller != null) {
                    controller.setUsuario(usuarioData);
                }
            } else if (tipo.equalsIgnoreCase("MEDICO")) {
                MedicoDashboardController controller = loader.getController();
                if (controller != null) {
                    controller.setUsuario(usuarioData);
                }
            }

            Stage stage = new Stage();
            stage.setTitle(titulo + " - MediSchedule");

            Scene scene = new Scene(root);

            // Carregar CSS
            try {
                URL cssUrl = getClass().getResource("/styles/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                System.out.println("⚠ CSS não encontrado para dashboard");
            }

            stage.setScene(scene);

            // ABRIR EM TELA CHEIA
            stage.setMaximized(true);
            stage.setMinWidth(1024);
            stage.setMinHeight(768);

            // Fechar tela de login
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            if (loginStage != null) {
                loginStage.close();
            }

            stage.show();

        } catch (IOException e) {
            System.err.println("Erro ao abrir dashboard do tipo " + tipo + ":");
            e.printStackTrace();

            // Fallback: criar dashboard básico
            mostrarAlerta("Dashboard não encontrado",
                    "O arquivo FXML para " + tipo + " não foi encontrado.\n" +
                            "Criando dashboard básico...", Alert.AlertType.WARNING);
            criarDashboardBasico(tipo);
        }
    }

    private void abrirDashboard(Map<String, Object> usuarioData) {
        // Este método é mantido para compatibilidade
        String tipo = (String) usuarioData.get("tipo");
        abrirDashboard(tipo);
    }

    private void criarDashboardBasico(String tipo) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Dashboard " + tipo + " - " + AppContext.getUsuarioNome());

            // Configurar para tela cheia
            stage.setMaximized(true);
            stage.setMinWidth(1024);
            stage.setMinHeight(768);

            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(20);
            vbox.setPadding(new Insets(30));
            vbox.setAlignment(javafx.geometry.Pos.CENTER);

            javafx.scene.control.Label lblTitulo = new javafx.scene.control.Label("Bem-vindo ao MediSchedule!");
            lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

            javafx.scene.control.Label lblUsuario = new javafx.scene.control.Label(
                    "Tipo: " + tipo +
                            "\nNome: " + AppContext.getUsuarioNome() +
                            "\nEmail: " + AppContext.getUsuarioLogado().get("email")
            );
            lblUsuario.setStyle("-fx-font-size: 16px; -fx-text-fill: #7F8C8D;");

            javafx.scene.control.Button btnVoltar = new javafx.scene.control.Button("Sair do Sistema");
            btnVoltar.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-padding: 10 20;");
            btnVoltar.setOnAction(e -> {
                stage.close();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
                    Parent root = loader.load();
                    Stage loginStage = new Stage();

                    Scene scene = new Scene(root);

                    // Carregar CSS
                    try {
                        URL cssUrl = getClass().getResource("/styles/styles.css");
                        if (cssUrl != null) {
                            scene.getStylesheets().add(cssUrl.toExternalForm());
                        }
                    } catch (Exception ex) {
                        System.out.println("⚠ CSS não encontrado para login");
                    }

                    loginStage.setScene(scene);
                    loginStage.setTitle("MediSchedule - Login");

                    // Abrir em tela cheia
                    loginStage.setMaximized(true);
                    loginStage.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            vbox.getChildren().addAll(lblTitulo, lblUsuario, btnVoltar);

            Scene scene = new Scene(vbox);
            stage.setScene(scene);
            stage.show();

            // Fechar tela de login
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            if (loginStage != null) {
                loginStage.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Platform.runLater(() -> {
            Alert alert = new Alert(tipo);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }
}