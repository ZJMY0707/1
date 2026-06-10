package studentknowledge.view;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import studentknowledge.model.CategoryNode;
import studentknowledge.service.HttpClient;

import java.util.*;

public class CategoryView {

    private final VBox root;
    private final TableView<CategoryNode> table = new TableView<>();
    private final ObservableList<CategoryNode> data = FXCollections.observableArrayList();
    private List<CategoryNode> allCategories = new ArrayList<>();

    public CategoryView() {
        root = new VBox(15);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("📂 笔记分类管理");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button addBtn = new Button("➕ 新建分类");
        addBtn.setStyle(btnStyle("#27ae60"));
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
        TableColumn<CategoryNode, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<CategoryNode, String> nameCol = new TableColumn<>("分类名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<CategoryNode, Long> parentCol = new TableColumn<>("父分类ID");
        parentCol.setCellValueFactory(new PropertyValueFactory<>("parentId"));
        parentCol.setPrefWidth(100);
        parentCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText("顶级分类"); return; }
                allCategories.stream().filter(c -> c.id.equals(item)).findFirst()
                    .ifPresentOrElse(c -> setText(c.name), () -> setText(String.valueOf(item)));
            }
        });

        TableColumn<CategoryNode, Integer> sortCol = new TableColumn<>("排序");
        sortCol.setCellValueFactory(new PropertyValueFactory<>("sortOrder"));
        sortCol.setPrefWidth(60);

        TableColumn<CategoryNode, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(160);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button delBtn = new Button("删除");
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11;");
                delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11;");
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> doDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(idCol, nameCol, parentCol, sortCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("暂无分类"));
    }

    private void loadData() {
        new Thread(() -> {
            try {
                String resp = HttpClient.get("/category");
                List<CategoryNode> list = HttpClient.parseData(resp,
                    new TypeToken<List<CategoryNode>>(){}.getType());
                Platform.runLater(() -> {
                    data.clear();
                    allCategories = list != null ? list : new ArrayList<>();
                    data.addAll(allCategories);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
            }
        }).start();
    }

    private void showEditDialog(CategoryNode cat) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle(cat == null ? "新建分类" : "编辑分类");

        TextField nameField = new TextField(cat != null ? cat.name : "");
        nameField.setPromptText("分类名称");
        ComboBox<String> parentCombo = new ComboBox<>();
        parentCombo.getItems().add("(顶级分类)");
        Map<String, Long> parentMap = new HashMap<>();
        for (CategoryNode c : allCategories) {
            if (cat == null || !c.id.equals(cat.id)) {
                parentCombo.getItems().add(c.name);
                parentMap.put(c.name, c.id);
            }
        }
        if (cat != null && cat.parentId != null) {
            allCategories.stream().filter(c -> c.id.equals(cat.parentId)).findFirst()
                .ifPresent(c -> parentCombo.setValue(c.name));
        } else {
            parentCombo.setValue("(顶级分类)");
        }
        Spinner<Integer> sortSpinner = new Spinner<>(0, 100, cat != null ? cat.sortOrder : 0);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("名称:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("父分类:"), 0, 1); grid.add(parentCombo, 1, 1);
        grid.add(new Label("排序:"), 0, 2); grid.add(sortSpinner, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Map<String, Object> r = new HashMap<>();
                r.put("name", nameField.getText().trim());
                String pName = parentCombo.getValue();
                r.put("parentId", "(顶级分类)".equals(pName) ? null : parentMap.get(pName));
                r.put("sortOrder", sortSpinner.getValue());
                return r;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (((String)result.get("name")).isEmpty()) return;
            new Thread(() -> {
                try {
                    if (cat == null) HttpClient.post("/category", result);
                    else HttpClient.put("/category/" + cat.id, result);
                    Platform.runLater(this::loadData);
                } catch (Exception ex) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
                }
            }).start();
        });
    }

    private void doDelete(CategoryNode cat) {
        new Alert(Alert.AlertType.CONFIRMATION, "确定删除分类「" + cat.name + "」？")
            .showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    new Thread(() -> {
                        try {
                            HttpClient.delete("/category/" + cat.id);
                            Platform.runLater(this::loadData);
                        } catch (Exception ex) {
                            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait());
                        }
                    }).start();
                }
            });
    }

    private String btnStyle(String c) {
        return "-fx-background-color:"+c+"; -fx-text-fill:white; -fx-background-radius:8; -fx-cursor:hand; -fx-font-size:13; -fx-padding:6 14;";
    }
    public VBox getRoot() { return root; }
}
