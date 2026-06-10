package studentknowledge.view;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import studentknowledge.model.TagInfo;
import studentknowledge.service.HttpClient;

import java.util.*;

public class TagView {

    private final VBox root;
    private final TableView<TagInfo> table = new TableView<>();
    private final ObservableList<TagInfo> data = FXCollections.observableArrayList();

    public TagView() {
        root = new VBox(15);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("🏷️ 标签管理");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button addBtn = new Button("➕ 新建标签");
        addBtn.setStyle("-fx-background-color:#27ae60; -fx-text-fill:white; -fx-background-radius:8; -fx-cursor:hand; -fx-font-size:13; -fx-padding:6 14;");
        addBtn.setOnAction(e -> showEditDialog(null));
        header.getChildren().addAll(title, spacer, addBtn);

        setupTable();
        table.setItems(data);
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(header, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<TagInfo, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<TagInfo, String> nameCol = new TableColumn<>("标签名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<TagInfo, String> colorCol = new TableColumn<>("颜色");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorCol.setPrefWidth(120);
        colorCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); setStyle(""); return; }
                setText(item);
                setStyle("-fx-background-color: " + item + "22; -fx-text-fill: " + item + ";");
            }
        });

        TableColumn<TagInfo, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(160);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button delBtn = new Button("删除");
            {
                editBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white; -fx-background-radius:4; -fx-font-size:11;");
                delBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:4; -fx-font-size:11;");
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> doDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(idCol, nameCol, colorCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("暂无标签"));
    }

    private void loadData() {
        new Thread(() -> {
            try {
                String resp = HttpClient.get("/tag");
                List<TagInfo> list = HttpClient.parseData(resp,
                    new TypeToken<List<TagInfo>>(){}.getType());
                Platform.runLater(() -> { data.clear(); if (list != null) data.addAll(list); });
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
            }
        }).start();
    }

    private void showEditDialog(TagInfo tag) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle(tag == null ? "新建标签" : "编辑标签");

        TextField nameField = new TextField(tag != null ? tag.name : "");
        nameField.setPromptText("标签名称");
        ColorPicker colorPicker = new ColorPicker();
        if (tag != null && tag.color != null) {
            try { colorPicker.setValue(javafx.scene.paint.Color.web(tag.color)); } catch (Exception ignored) {}
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("名称:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("颜色:"), 0, 1); grid.add(colorPicker, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Map<String, String> r = new HashMap<>();
                r.put("name", nameField.getText().trim());
                javafx.scene.paint.Color c = colorPicker.getValue();
                r.put("color", String.format("#%02x%02x%02x",
                    (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)));
                return r;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.get("name").isEmpty()) return;
            new Thread(() -> {
                try {
                    if (tag == null) HttpClient.post("/tag", result);
                    else HttpClient.put("/tag/" + tag.id, result);
                    Platform.runLater(this::loadData);
                } catch (Exception ex) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
                }
            }).start();
        });
    }

    private void doDelete(TagInfo tag) {
        new Alert(Alert.AlertType.CONFIRMATION, "确定删除标签「" + tag.name + "」？")
            .showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    new Thread(() -> {
                        try {
                            HttpClient.delete("/tag/" + tag.id);
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
