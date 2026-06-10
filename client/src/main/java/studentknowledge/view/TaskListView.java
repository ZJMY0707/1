package studentknowledge.view;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import studentknowledge.model.TaskInfo;
import studentknowledge.service.HttpClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskListView {

    private final VBox root;
    private final TableView<TaskInfo> table = new TableView<>();
    private final ObservableList<TaskInfo> data = FXCollections.observableArrayList();
    private final boolean showDoneOnly;

    private static final Map<String, String> STATUS_MAP = Map.of(
        "TODO", "待开始", "IN_PROGRESS", "进行中", "DONE", "已完成", "OVERDUE", "已逾期");
    private static final Map<String, String> PRIORITY_MAP = Map.of(
        "LOW", "低", "MEDIUM", "中", "HIGH", "高", "URGENT", "紧急");
    private static final Map<String, String> STATUS_COLOR = Map.of(
        "TODO", "#95a5a6", "IN_PROGRESS", "#3498db", "DONE", "#27ae60", "OVERDUE", "#e74c3c");

    public TaskListView(boolean showDoneOnly) {
        this.showDoneOnly = showDoneOnly;
        root = new VBox(15);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(showDoneOnly ? "✅ 已完成任务" : "📅 我的日程");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (!showDoneOnly) {
            Button addBtn = new Button("➕ 新建任务");
            addBtn.setStyle("-fx-background-color:#27ae60; -fx-text-fill:white; -fx-background-radius:8; -fx-cursor:hand; -fx-font-size:13; -fx-padding:6 14;");
            addBtn.setOnAction(e -> showEditDialog(null));
            header.getChildren().addAll(title, spacer, addBtn);
        } else {
            header.getChildren().addAll(title, spacer);
        }

        setupTable();
        table.setItems(data);
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(header, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<TaskInfo, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<TaskInfo, String> titleCol = new TableColumn<>("任务标题");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(220);

        TableColumn<TaskInfo, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); setStyle(""); return; }
                setText(STATUS_MAP.getOrDefault(item, item));
                String color = STATUS_COLOR.getOrDefault(item, "#333");
                setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold;");
            }
        });

        TableColumn<TaskInfo, String> priorityCol = new TableColumn<>("优先级");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(70);
        priorityCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); return; }
                setText(PRIORITY_MAP.getOrDefault(item, item));
                String c = "URGENT".equals(item) ? "#e74c3c" : "HIGH".equals(item) ? "#e67e22" : "#333";
                setStyle("-fx-text-fill:" + c + ";");
            }
        });

        TableColumn<TaskInfo, String> deadlineCol = new TableColumn<>("截止时间");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        deadlineCol.setPrefWidth(150);

        TableColumn<TaskInfo, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button delBtn = new Button("删除");
            private final Button doneBtn = new Button("完成");
            {
                editBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; -fx-background-radius:4; -fx-font-size:11;");
                delBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:4; -fx-font-size:11;");
                doneBtn.setStyle("-fx-background-color:#27ae60; -fx-text-fill:white; -fx-background-radius:4; -fx-font-size:11;");
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> doDelete(getTableView().getItems().get(getIndex())));
                doneBtn.setOnAction(e -> markDone(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                TaskInfo t = getTableView().getItems().get(getIndex());
                HBox box = new HBox(5);
                if (!"DONE".equals(t.status)) box.getChildren().add(doneBtn);
                box.getChildren().addAll(editBtn, delBtn);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(idCol, titleCol, statusCol, priorityCol, deadlineCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("暂无任务"));
    }

    private void loadData() {
        new Thread(() -> {
            try {
                String path = showDoneOnly ? "/task/status/DONE" : "/task";
                String resp = HttpClient.get(path);
                List<TaskInfo> list = HttpClient.parseData(resp,
                    new TypeToken<List<TaskInfo>>(){}.getType());
                Platform.runLater(() -> { data.clear(); if (list != null) data.addAll(list); });
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
            }
        }).start();
    }

    private void showEditDialog(TaskInfo task) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle(task == null ? "新建任务" : "编辑任务");

        TextField titleField = new TextField(task != null ? task.title : "");
        titleField.setPromptText("任务标题（必填）");
        TextArea descArea = new TextArea(task != null ? task.description : "");
        descArea.setPromptText("任务描述");
        descArea.setPrefRowCount(4);

        ComboBox<String> statusCombo = new ComboBox<>(
            FXCollections.observableArrayList("TODO", "IN_PROGRESS", "DONE", "OVERDUE"));
        statusCombo.setValue(task != null ? task.status : "TODO");

        ComboBox<String> priorityCombo = new ComboBox<>(
            FXCollections.observableArrayList("LOW", "MEDIUM", "HIGH", "URGENT"));
        priorityCombo.setValue(task != null ? task.priority : "MEDIUM");

        DatePicker deadlinePicker = new DatePicker();
        if (task != null && task.deadline != null) {
            try {
                deadlinePicker.setValue(LocalDate.parse(task.deadline.substring(0, 10)));
            } catch (Exception ignored) {}
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.setPrefWidth(500);
        grid.add(new Label("标题:"), 0, 0);    grid.add(titleField, 1, 0);
        grid.add(new Label("描述:"), 0, 1);    grid.add(descArea, 1, 1);
        grid.add(new Label("状态:"), 0, 2);    grid.add(statusCombo, 1, 2);
        grid.add(new Label("优先级:"), 0, 3);  grid.add(priorityCombo, 1, 3);
        grid.add(new Label("截止日期:"), 0, 4); grid.add(deadlinePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Map<String, Object> r = new HashMap<>();
                r.put("title", titleField.getText().trim());
                r.put("description", descArea.getText());
                r.put("status", statusCombo.getValue());
                r.put("priority", priorityCombo.getValue());
                LocalDate d = deadlinePicker.getValue();
                if (d != null) {
                    r.put("deadline", d.atTime(LocalTime.of(23, 59, 59))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                return r;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (((String)result.get("title")).isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "标题不能为空").showAndWait(); return;
            }
            if (result.get("deadline") == null) {
                new Alert(Alert.AlertType.ERROR, "截止时间不能为空").showAndWait(); return;
            }
            new Thread(() -> {
                try {
                    if (task == null) HttpClient.post("/task", result);
                    else HttpClient.put("/task/" + task.id, result);
                    Platform.runLater(this::loadData);
                } catch (Exception ex) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
                }
            }).start();
        });
    }

    private void markDone(TaskInfo task) {
        new Thread(() -> {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("title", task.title);
                body.put("description", task.description);
                body.put("status", "DONE");
                body.put("priority", task.priority);
                body.put("deadline", task.deadline);
                HttpClient.put("/task/" + task.id, body);
                Platform.runLater(this::loadData);
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
            }
        }).start();
    }

    private void doDelete(TaskInfo task) {
        new Alert(Alert.AlertType.CONFIRMATION, "确定删除任务「" + task.title + "」？")
            .showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    new Thread(() -> {
                        try {
                            HttpClient.delete("/task/" + task.id);
                            Platform.runLater(this::loadData);
                        } catch (Exception ex) {
                            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
                        }
                    }).start();
                }
            });
    }

    public VBox getRoot() { return root; }
}
