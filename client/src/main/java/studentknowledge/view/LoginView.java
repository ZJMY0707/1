package studentknowledge.view;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import studentknowledge.client.MainWindow;
import studentknowledge.service.HttpClient;
import studentknowledge.service.SessionManager;

import java.util.Map;

public class LoginView {

    private final BorderPane root;
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField regUsername = new TextField();
    private final TextField regEmail = new TextField();
    private final PasswordField regPassword = new PasswordField();
    private final Label msgLabel = new Label();
    private final TabPane tabPane = new TabPane();

    /* ---- 极简配色 ---- */
    private static final String BG       = "#ffffff";
    private static final String TEXT_PRI  = "#1a1a1a";
    private static final String TEXT_SEC  = "#999999";
    private static final String BORDER    = "#e0e0e0";
    private static final String ACCENT    = "#1a1a1a";
    private static final String ERR       = "#d94f4f";
    private static final String OK        = "#3dae6f";

    public LoginView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");

        /* ============ 容器 ============ */
        VBox card = new VBox(28);
        card.setMaxWidth(380);
        card.setPadding(new Insets(60, 40, 48, 40));
        card.setAlignment(Pos.TOP_CENTER);

        /* ---- 标题区 ---- */
        Label title = new Label("知识库与日程系统");
        title.setFont(Font.font("System", FontWeight.LIGHT, 26));
        title.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-letter-spacing: 2;");

        Label subtitle = new Label("Student Knowledge & Schedule");
        subtitle.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 12;");

        /* ---- 分隔线 ---- */
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER + "; -fx-padding: 0;");
        sep.setMaxWidth(40);

        VBox titleBox = new VBox(8, title, subtitle, sep);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 8, 0));

        /* ============ 登录Tab ============ */
        Tab loginTab = new Tab("登录");
        loginTab.setClosable(false);
        VBox loginBox = new VBox(18);
        loginBox.setPadding(new Insets(24, 0, 0, 0));

        applyFieldStyle(usernameField, "用户名 / 学号");
        applyFieldStyle(passwordField, "密码");

        Button loginBtn = new Button("登  录");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(btnPrimaryStyle());
        loginBtn.setOnAction(e -> doLogin());
        passwordField.setOnAction(e -> doLogin());

        loginBox.getChildren().addAll(usernameField, passwordField, loginBtn);
        loginTab.setContent(loginBox);

        /* ============ 注册Tab ============ */
        Tab regTab = new Tab("注册");
        regTab.setClosable(false);
        VBox regBox = new VBox(18);
        regBox.setPadding(new Insets(24, 0, 0, 0));

        applyFieldStyle(regUsername, "用户名 / 学号");
        applyFieldStyle(regEmail, "邮箱");
        applyFieldStyle(regPassword, "密码 (6位以上)");

        Button regBtn = new Button("注  册");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        regBtn.setStyle(btnPrimaryStyle());
        regBtn.setOnAction(e -> doRegister());

        regBox.getChildren().addAll(regUsername, regEmail, regPassword, regBtn);
        regTab.setContent(regBox);

        /* ---- TabPane 极简化 ---- */
        tabPane.getTabs().addAll(loginTab, regTab);
        tabPane.setStyle(
            "-fx-font-size: 13px; "
          + "-fx-tab-min-width: 60; -fx-tab-max-width: 60; "
          + "-fx-tab-min-height: 32; "
          + "-fx-background-color: transparent;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        /* ---- 消息 ---- */
        msgLabel.setStyle("-fx-text-fill: " + ERR + "; -fx-font-size: 12;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(300);
        msgLabel.setAlignment(Pos.CENTER);

        /* ---- 底部版权 ---- */
        Label footer = new Label("© 2026 课程设计项目");
        footer.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11;");

        VBox bottomBox = new VBox(16, msgLabel, footer);
        bottomBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(titleBox, tabPane, bottomBox);
        root.setCenter(card);
    }

    /* ======================== 业务逻辑（不变）======================== */

    private void doLogin() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText().trim();
        if (u.isEmpty() || p.isEmpty()) { showMsg("请输入用户名和密码", true); return; }

        new Thread(() -> {
            try {
                String resp = HttpClient.post("/auth/login", Map.of("username", u, "password", p));
                JsonObject respObj = JsonParser.parseString(resp).getAsJsonObject();
                if (respObj.get("code").getAsInt() != 200) { Platform.runLater(() -> showMsg(respObj.get("message").getAsString(), true)); return; }
                JsonObject data = respObj.getAsJsonObject("data");
                String token = data.get("token").getAsString();
                Long uid = data.get("userId").getAsLong();
                String uname = data.get("username").getAsString();
                String email = data.get("email").getAsString();
                String role = data.get("role").getAsString();
                SessionManager.login(token, uid, uname, email, role);
                Platform.runLater(MainWindow::showMain);
            } catch (Exception ex) {
                Platform.runLater(() -> showMsg("登录失败: " + ex.getMessage(), true));
            }
        }).start();
    }

    private void doRegister() {
        String u = regUsername.getText().trim();
        String e = regEmail.getText().trim();
        String p = regPassword.getText().trim();
        if (u.isEmpty() || e.isEmpty() || p.isEmpty()) { showMsg("请填写完整注册信息", true); return; }
        if (p.length() < 6) { showMsg("密码至少6位", true); return; }

        new Thread(() -> {
            try {
                String resp = HttpClient.post("/auth/register",
                        Map.of("username", u, "email", e, "password", p));
                Platform.runLater(() -> {
                    showMsg("注册成功，请登录", false);
                    tabPane.getSelectionModel().select(0);
                    usernameField.setText(u);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showMsg("注册失败: " + ex.getMessage(), true));
            }
        }).start();
    }

    /* ======================== 样式工具 ======================== */

    private void showMsg(String msg, boolean isError) {
        msgLabel.setStyle("-fx-text-fill: " + (isError ? ERR : OK) + "; -fx-font-size: 12;");
        msgLabel.setText(msg);
    }

    /** 极简输入框: 无边框, 仅底部细线 */
    private void applyFieldStyle(TextField field, String prompt) {
        field.setPromptText(prompt);
        field.setStyle(
            "-fx-background-color: transparent; "
          + "-fx-border-color: transparent transparent " + BORDER + " transparent; "
          + "-fx-border-width: 0 0 1 0; "
          + "-fx-padding: 10 0 10 0; "
          + "-fx-font-size: 14; "
          + "-fx-text-fill: " + TEXT_PRI + "; "
          + "-fx-prompt-text-fill: #bfbfbf;");

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            String bottom = isFocused ? ACCENT : BORDER;
            String width  = isFocused ? "2" : "1";
            field.setStyle(
                "-fx-background-color: transparent; "
              + "-fx-border-color: transparent transparent " + bottom + " transparent; "
              + "-fx-border-width: 0 0 " + width + " 0; "
              + "-fx-padding: 10 0 10 0; "
              + "-fx-font-size: 14; "
              + "-fx-text-fill: " + TEXT_PRI + "; "
              + "-fx-prompt-text-fill: #bfbfbf;");
        });
    }

    /** 主按钮: 纯黑底白字, 无圆角 */
    private String btnPrimaryStyle() {
        return "-fx-background-color: " + ACCENT + "; "
             + "-fx-text-fill: #ffffff; "
             + "-fx-font-size: 14; "
             + "-fx-font-weight: normal; "
             + "-fx-padding: 12 0; "
             + "-fx-background-radius: 2; "
             + "-fx-cursor: hand;";
    }

    public BorderPane getRoot() { return root; }
}
