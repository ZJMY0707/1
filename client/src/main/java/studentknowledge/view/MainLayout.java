package studentknowledge.view;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import studentknowledge.client.MainWindow;
import studentknowledge.service.SessionManager;

public class MainLayout {

    private final BorderPane root;
    private final StackPane contentArea;
    private final VBox menuBox;
    private Button activeBtn = null;

    public MainLayout() {
        root = new BorderPane();

        // ===== 顶部导航 =====
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 20, 0, 20));
        topBar.setSpacing(15);
        topBar.setPrefHeight(56);
        topBar.setStyle("-fx-background-color: #2c3e50;");

        Label logo = new Label("📚 知识库与日程系统");
        logo.setFont(Font.font("System", FontWeight.BOLD, 17));
        logo.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label userLabel = new Label("👤 " + SessionManager.getUsername()
                + (SessionManager.isAdmin() ? " [管理员]" : " [学生]"));
        userLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 13;");

        Button logoutBtn = new Button("退出登录");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12;");
        logoutBtn.setOnAction(e -> {
            SessionManager.logout();
            MainWindow.showLogin();
        });

        topBar.getChildren().addAll(logo, spacer, userLabel, logoutBtn);

        // ===== 左侧菜单 =====
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #34495e;");
        sidebar.setPadding(new Insets(10, 0, 10, 0));

        menuBox = new VBox(2);
        menuBox.setPadding(new Insets(5));

        // 菜单项
        addMenuSection("知识库管理");
        Button btnNotes = addMenuItem("📝 我的笔记");
        Button btnCat   = addMenuItem("📂 笔记分类");
        Button btnTag   = addMenuItem("🏷️ 标签管理");

        addMenuSection("日程管理");
        Button btnTask   = addMenuItem("📅 我的日程");
        Button btnDone   = addMenuItem("✅ 已完成任务");

        addMenuSection("数据统计");
        Button btnStats  = addMenuItem("📊 学习分析");

        if (SessionManager.isAdmin()) {
            addMenuSection("系统管理");
            Button btnAdmin = addMenuItem("⚙️ 用户管理");
            Button btnSysStats = addMenuItem("📈 系统概况");
            btnAdmin.setOnAction(e -> switchTo(btnAdmin, new AdminUserView().getRoot()));
            btnSysStats.setOnAction(e -> switchTo(btnSysStats, new AdminStatsView().getRoot()));
        }

        sidebar.getChildren().add(menuBox);

        // ===== 底部版权 =====
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(8));
        footer.setStyle("-fx-background-color: #ecf0f1;");
        Label copyright = new Label("© 2026 学生知识库系统 | 课程设计项目");
        copyright.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11;");
        footer.getChildren().add(copyright);

        // ===== 内容区 =====
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f5f6fa;");
        contentArea.setPadding(new Insets(20));

        // 事件绑定
        btnNotes.setOnAction(e -> switchTo(btnNotes, new KnowledgeListView().getRoot()));
        btnCat.setOnAction(e -> switchTo(btnCat, new CategoryView().getRoot()));
        btnTag.setOnAction(e -> switchTo(btnTag, new TagView().getRoot()));
        btnTask.setOnAction(e -> switchTo(btnTask, new TaskListView(false).getRoot()));
        btnDone.setOnAction(e -> switchTo(btnDone, new TaskListView(true).getRoot()));
        btnStats.setOnAction(e -> switchTo(btnStats, new StatsView().getRoot()));

        // 布局
        root.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(contentArea);
        root.setBottom(footer);

        // 默认显示笔记
        switchTo(btnNotes, new KnowledgeListView().getRoot());
    }

    private void addMenuSection(String title) {
        Label label = new Label(title);
        label.setPadding(new Insets(12, 15, 4, 15));
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: #95a5a6;");
        menuBox.getChildren().add(label);
    }

    private Button addMenuItem(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 20));
        btn.setStyle(menuBtnStyle(false));
        btn.setOnMouseEntered(e -> { if (btn != activeBtn) btn.setStyle(menuBtnHover()); });
        btn.setOnMouseExited(e -> { if (btn != activeBtn) btn.setStyle(menuBtnStyle(false)); });
        menuBox.getChildren().add(btn);
        return btn;
    }

    private void switchTo(Button btn, javafx.scene.Node content) {
        if (activeBtn != null) activeBtn.setStyle(menuBtnStyle(false));
        activeBtn = btn;
        btn.setStyle(menuBtnStyle(true));
        contentArea.getChildren().setAll(content);
    }

    private String menuBtnStyle(boolean active) {
        return active
            ? "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 13; "
              + "-fx-background-radius: 8; -fx-cursor: hand;"
            : "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 13; "
              + "-fx-background-radius: 8; -fx-cursor: hand;";
    }
    private String menuBtnHover() {
        return "-fx-background-color: #3d566e; -fx-text-fill: white; -fx-font-size: 13; "
             + "-fx-background-radius: 8; -fx-cursor: hand;";
    }

    public BorderPane getRoot() { return root; }
}
