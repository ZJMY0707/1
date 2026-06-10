package studentknowledge.view;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import studentknowledge.model.StatsData;
import studentknowledge.service.HttpClient;

import java.util.Map;

public class AdminStatsView {

    private final VBox root;

    public AdminStatsView() {
        root = new VBox(20);

        Label title = new Label("📈 系统概况（管理员）");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // 概览卡片
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER_LEFT);

        Label userCard   = createCard("总用户数", "0", "#9b59b6");
        Label entryCard  = createCard("总笔记数", "0", "#3498db");
        Label taskCard   = createCard("总任务数", "0", "#e67e22");
        cards.getChildren().addAll(userCard, entryCard, taskCard);

        // 图表区
        HBox chartBox = new HBox(20);
        chartBox.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(chartBox, Priority.ALWAYS);

        PieChart pieChart = new PieChart();
        pieChart.setTitle("全系统分类笔记占比");
        pieChart.setPrefSize(450, 350);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("状态");
        yAxis.setLabel("数量");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("全系统任务状态分布");
        barChart.setPrefSize(450, 350);
        barChart.setLegendVisible(false);

        chartBox.getChildren().addAll(pieChart, barChart);
        root.getChildren().addAll(title, cards, chartBox);

        // 加载数据
        new Thread(() -> {
            try {
                String resp = HttpClient.get("/stats/admin");
                StatsData stats = HttpClient.parseData(resp, StatsData.class);
                Platform.runLater(() -> {
                    if (stats == null) return;

                    ((Label)((VBox)userCard.getGraphic()).getChildren().get(1))
                        .setText(String.valueOf(stats.totalUsers));
                    ((Label)((VBox)entryCard.getGraphic()).getChildren().get(1))
                        .setText(String.valueOf(stats.totalEntries));
                    ((Label)((VBox)taskCard.getGraphic()).getChildren().get(1))
                        .setText(String.valueOf(stats.totalTasks));

                    if (stats.categoryStats != null) {
                        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                        for (Map<String, Object> m : stats.categoryStats) {
                            String name = String.valueOf(m.get("name"));
                            double count = ((Number) m.get("count")).doubleValue();
                            pieData.add(new PieChart.Data(name + " (" + (int)count + ")", count));
                        }
                        pieChart.setData(pieData);
                    }

                    if (stats.taskStatusStats != null) {
                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        Map<String, String> statusNames = Map.of(
                            "TODO","待开始","IN_PROGRESS","进行中","DONE","已完成","OVERDUE","已逾期");
                        for (Map<String, Object> m : stats.taskStatusStats) {
                            String name = String.valueOf(m.get("name"));
                            Number count = (Number) m.get("count");
                            series.getData().add(new XYChart.Data<>(
                                statusNames.getOrDefault(name, name), count));
                        }
                        barChart.getData().add(series);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                    new Alert(Alert.AlertType.ERROR, "加载系统统计失败: " + ex.getMessage()).showAndWait());
            }
        }).start();
    }

    private Label createCard(String label, String value, String color) {
        Label valLabel = new Label(value);
        valLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        valLabel.setStyle("-fx-text-fill: " + color + ";");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");

        VBox box = new VBox(5, nameLabel, valLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(25));
        box.setPrefWidth(200);

        Label card = new Label();
        card.setGraphic(box);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-padding: 5;");
        return card;
    }

    public VBox getRoot() { return root; }
}
