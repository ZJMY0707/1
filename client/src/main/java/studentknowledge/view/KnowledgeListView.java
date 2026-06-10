package studentknowledge.view;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import studentknowledge.model.*;
import studentknowledge.service.HttpClient;

import java.util.*;
import java.util.stream.Collectors;

public class KnowledgeListView {

    private final VBox root;
    private final TableView<KnowledgeEntry> table = new TableView<>();
    private final ObservableList<KnowledgeEntry> data = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();

    public KnowledgeListView() {
        root = new VBox(15);
        root.setPadding(new Insets(0));

        // 标题栏
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("📝 我的笔记");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        searchField.setPromptText("搜索笔记标题...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius: 8; -fx-padding: 8;");
        Button searchBtn = new Button("🔍 搜索");
        searchBtn.setStyle(btnStyle("#3498db"));
        searchBtn.setOnAction(e -> doSearch());
        searchField.setOnAction(e -> doSearch());

        Button refreshBtn = new Button("🔄 刷新");
        refreshBtn.setStyle(btnStyle("#95a5a6"));
        refreshBtn.setOnAction(e -> loadData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ 新建笔记");
        addBtn.setStyle(btnStyle("#27ae60"));
        addBtn.setOnAction(e -> showEditDialog(null));

        header.getChildren().addAll(title, searchField, searchBtn, refreshBtn, spacer, addBtn);

        // 表格
        setupTable();
        table.setItems(data);
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(header, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<KnowledgeEntry, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<KnowledgeEntry, String> titleCol = new TableColumn<>("标题");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

        TableColumn<KnowledgeEntry, Boolean> pinCol = new TableColumn<>("置顶");
        pinCol.setCellValueFactory(new PropertyValueFactory<>("isPinned"));
        pinCol.setPrefWidth(60);
        pinCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item ? "📌" : ""));
            }
        });

        TableColumn<KnowledgeEntry, Set<TagInfo>> tagCol = new TableColumn<>("标签");
        tagCol.setCellValueFactory(new PropertyValueFactory<>("tags"));
        tagCol.setPrefWidth(200);
        tagCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Set<TagInfo> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); return; }
                setText(item.stream().map(t -> t.name).collect(Collectors.joining(", ")));
            }
        });

        TableColumn<KnowledgeEntry, String> timeCol = new TableColumn<>("创建时间");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        timeCol.setPrefWidth(160);

        TableColumn<KnowledgeEntry, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button delBtn = new Button("删除");
            private final Button viewBtn = new Button("查看");
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11;");
                delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11;");
                viewBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-size: 11;");
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> doDelete(getTableView().getItems().get(getIndex())));
                viewBtn.setOnAction(e -> showDetail(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : new HBox(5, viewBtn, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(idCol, titleCol, pinCol, tagCol, timeCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("暂无笔记数据，点击「新建笔记」开始"));
    }

    private void loadData() {
        new Thread(() -> {
            try {
                String resp = HttpClient.get("/knowledge");
                List<KnowledgeEntry> list = HttpClient.parseData(resp,
                    new TypeToken<List<KnowledgeEntry>>(){}.getType());
                Platform.runLater(() -> {
                    data.clear();
                    if (list != null) data.addAll(list);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert("加载失败", ex.getMessage()));
            }
        }).start();
    }

    private void doSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        new Thread(() -> {
            try {
                String resp = HttpClient.get("/knowledge/search?keyword=" + kw);
                List<KnowledgeEntry> list = HttpClient.parseData(resp,
                    new TypeToken<List<KnowledgeEntry>>(){}.getType());
                Platform.runLater(() -> {
                    data.clear();
                    if (list != null) data.addAll(list);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert("搜索失败", ex.getMessage()));
            }
        }).start();
    }

    private void showEditDialog(KnowledgeEntry entry) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle(entry == null ? "新建笔记" : "编辑笔记");
        dialog.setHeaderText(null);

        TextField titleField = new TextField(entry != null ? entry.title : "");
        titleField.setPromptText("笔记标题（必填）");
        TextArea contentArea = new TextArea(entry != null ? entry.content : "");
        contentArea.setPromptText("笔记内容（支持Markdown）");
        contentArea.setPrefRowCount(12);
        CheckBox pinCheck = new CheckBox("置顶");
        if (entry != null && entry.isPinned != null) pinCheck.setSelected(entry.isPinned);

        // 加载分类和标签
        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.setPromptText("选择分类");
        catCombo.setEditable(false);
        Map<String, Long> catMap = new HashMap<>();

        // 加载标签多选
        VBox tagBox = new VBox(5);
        Label tagLabel = new Label("标签:");
        FlowPane tagFlow = new FlowPane(5, 5);
        Map<CheckBox, Long> tagChecks = new LinkedHashMap<>();

        new Thread(() -> {
            try {
                String catResp = HttpClient.get("/category");
                List<CategoryNode> cats = HttpClient.parseData(catResp,
                    new TypeToken<List<CategoryNode>>(){}.getType());
                String tagResp = HttpClient.get("/tag");
                List<TagInfo> tags = HttpClient.parseData(tagResp,
                    new TypeToken<List<TagInfo>>(){}.getType());

                Platform.runLater(() -> {
                    if (cats != null) {
                        for (CategoryNode c : cats) {
                            catCombo.getItems().add(c.name);
                            catMap.put(c.name, c.id);
                            if (entry != null && c.id.equals(entry.categoryId))
                                catCombo.setValue(c.name);
                        }
                    }
                    if (tags != null) {
                        Set<Long> entryTagIds = new HashSet<>();
                        if (entry != null && entry.tags != null)
                            entry.tags.forEach(t -> entryTagIds.add(t.id));
                        for (TagInfo t : tags) {
                            CheckBox cb = new CheckBox(t.name);
                            cb.setSelected(entryTagIds.contains(t.id));
                            tagChecks.put(cb, t.id);
                            tagFlow.getChildren().add(cb);
                        }
                    }
                });
            } catch (Exception ignored) {}
        }).start();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(600);
        grid.add(new Label("标题:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("分类:"), 0, 1); grid.add(catCombo, 1, 1);
        grid.add(new Label("内容:"), 0, 2); grid.add(contentArea, 1, 2);
        grid.add(tagLabel, 0, 3); grid.add(tagFlow, 1, 3);
        grid.add(pinCheck, 1, 4);
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        GridPane.setHgrow(contentArea, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Map<String, Object> result = new HashMap<>();
                result.put("title", titleField.getText().trim());
                result.put("content", contentArea.getText());
                result.put("isPinned", pinCheck.isSelected());
                String catName = catCombo.getValue();
                result.put("categoryId", catName != null ? catMap.get(catName) : null);
                List<Long> tagIds = tagChecks.entrySet().stream()
                    .filter(e2 -> e2.getKey().isSelected())
                    .map(Map.Entry::getValue).collect(Collectors.toList());
                result.put("tagIds", tagIds);
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (((String)result.get("title")).isEmpty()) {
                showAlert("错误", "标题不能为空");
                return;
            }
            new Thread(() -> {
                try {
                    if (entry == null) {
                        HttpClient.post("/knowledge", result);
                    } else {
                        HttpClient.put("/knowledge/" + entry.id, result);
                    }
                    Platform.runLater(() -> {
                        showInfo(entry == null ? "创建成功" : "更新成功");
                        loadData();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert("操作失败", ex.getMessage()));
                }
            }).start();
        });
    }

    private void showDetail(KnowledgeEntry entry) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("笔记详情");
        alert.setHeaderText(entry.title);
        TextArea area = new TextArea(entry.content != null ? entry.content : "(无内容)");
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(15);
        area.setPrefColumnCount(50);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    private void doDelete(KnowledgeEntry entry) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除笔记「" + entry.title + "」？");
        confirm.setHeaderText("删除确认");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        HttpClient.delete("/knowledge/" + entry.id);
                        Platform.runLater(() -> { showInfo("删除成功"); loadData(); });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert("删除失败", ex.getMessage()));
                    }
                }).start();
            }
        });
    }

    private String btnStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white; "
             + "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13; -fx-padding: 6 14;";
    }
    private void showAlert(String t, String m) { new Alert(Alert.AlertType.ERROR, m).showAndWait(); }
    private void showInfo(String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }

    public VBox getRoot() { return root; }
}
