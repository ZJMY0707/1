package studentknowledge.view;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import studentknowledge.model.UserInfo;
import studentknowledge.service.HttpClient;

import java.util.*;

public class AdminUserView {

    private final VBox root;
    private final TableView<UserInfo> table = new TableView<>();
    private final ObservableList<UserInfo> data = FXCollections.observableArrayList();

    public AdminUserView() {
        root = new VBox(15);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("⚙️ 用户管理");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        Label hint = new Label("（管理员功能：查看所有用户、重置密码、删除用户）");
        hint.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refreshBtn = new Button("🔄 刷新");
        refreshBtn.setStyle("-fx-background-color:#95a5a6; -fx-text-fill:white; -fx-background-radius:8; -fx-cursor:hand; -fx-font-size:13; -fx-padding:6 14;");
        refreshBtn.setOnAction(e -> loadData());
        header.getChildren().addAll(title, hint, spacer, refreshBtn);

        setupTable();
        table.setItems(data);
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(header, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<UserInfo, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<UserInfo, String> nameCol = new TableColumn<>("用户名/学号");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameCol.setPrefWidth(150);

        TableColumn<UserInfo, String> emailCol = new TableColumn<>("邮箱");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<UserInfo, String> roleCol = new TableColumn<>("角色");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(90);
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); setStyle(""); return; }
                setText("ADMIN".equals(item) ? "管理员" : "学生");
                setStyle("ADMIN".equals(item)
                    ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                    : "-fx-text-fill: #3498db;");
            }
        });

        TableColumn<UserInfo, String> timeCol = new TableColumn<>("注册时间");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        timeCol.setPrefWidth(160);

        TableColumn<UserInfo, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button resetBtn = new Button("重置密码");
            private final Button delBtn = new Button("删除");
            {
                resetBtn.setStyle("-fx-background-color:#e67e22; -fx-text-fill:white; -fx-background-radius:4; -fx-font-size:11;");
                delBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:4; -fx-font-size:11;");
                resetBtn.setOnAction(e -> doResetPassword(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> doDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                UserInfo u = getTableView().getItems().get(getIndex());
                if ("ADMIN".equals(u.role)) {
                    setGraphic(new Label("—"));
                } else {
                    setGraphic(new HBox(5, resetBtn, delBtn));
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, emailCol, roleCol, timeCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("暂无用户数据"));
    }

    private void loadData() {
        new Thread(() -> {
            try {
                String resp = HttpClient.get("/admin/users");
                List<UserInfo> list = HttpClient.parseData(resp,
                    new TypeToken<List<UserInfo>>(){}.getType());
                Platform.runLater(() -> {
                    data.clear();
                    if (list != null) data.addAll(list);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "加载用户列表失败: " + ex.getMessage()).showAndWait());
            }
        }).start();
    }

    private void doResetPassword(UserInfo user) {
        TextInputDialog dialog = new TextInputDialog("123456");
        dialog.setTitle("重置密码");
        dialog.setHeaderText("重置用户「" + user.username + "」的密码");
        dialog.setContentText("新密码:");
        dialog.showAndWait().ifPresent(newPwd -> {
            if (newPwd.trim().length() < 6) {
                new Alert(Alert.AlertType.ERROR, "密码至少6位").showAndWait();
                return;
            }
            new Thread(() -> {
                try {
                    HttpClient.put("/admin/users/" + user.id + "/reset-password",
                        Map.of("password", newPwd.trim()));
                    Platform.runLater(() ->
                        new Alert(Alert.AlertType.INFORMATION, "密码已重置为: " + newPwd.trim()).showAndWait());
                } catch (Exception ex) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
                }
            }).start();
        });
    }

    private void doDelete(UserInfo user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "确定删除用户「" + user.username + "」？\n该操作将同时删除该用户的所有数据！");
        confirm.setHeaderText("⚠️ 危险操作");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        HttpClient.delete("/admin/users/" + user.id);
                        Platform.runLater(() -> {
                            new Alert(Alert.AlertType.INFORMATION, "用户已删除").showAndWait();
                            loadData();
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
                    }
                }).start();
            }
        });
    }

    public VBox getRoot() { return root; }
}
