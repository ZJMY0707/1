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

public class StatsView {

    private final VBox root;

    public StatsView() {
        root = new VBox(20);

        Label title = new Label("📊 学习分析");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        // 概览卡片
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER_LEFT);

        Label entryCard = createCard("笔记总数", "0", "#3498db");
        Label taskCard = createCard("任务总数", "0", "#e67e22");
        Label doneCard = createCard("已完成", "0", "#27ae60");
        Label overdueCard = createCard("已逾期", "0", "#e74c3c");
        cards.getChildren().addAll(entryCard, taskCard, doneCard, overdueCard);

        // 图表区
        HBox chartBox = new HBox(20);
        chartBox.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(chartBox, Priority.ALWAYS);

        PieChart pieChart = new PieChart();
        pieChart.setTitle("各分类笔记占比");
        pieChart.setPrefSize(450, 350);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("状态");
        yAxis.setLabel("数量");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("任务状态分布");
        barChart.setPrefSize(450, 350);
        barChart.setLegendVisible(false);

        chartBox.getChildren().addAll(pieChart, barChart);
        root.getChildren().addAll(title, cards, chartBox);

        // 加载数据
        new Thread(() -> {
            try {
                String resp = HttpClient.get("/stats/my");
                StatsData stats = HttpClient.parseData(resp, StatsData.class);
                Platform.runLater(() -> {
                    if (stats == null) return;
                    ((Label)((VBox)entryCard.getGraphic()).getChildren().get(1))
                        .setText(String.valueOf(stats.totalEntries));
                    ((Label)((VBox)taskCard.getGraphic()).getChildren().get(1))
                        .setText(String.valueOf(stats.totalTasks));
                    ((Label)((VBox)doneCard.getGraphic()).getChildren().get(1))
                        .setText(String.valueOf(stats.completedTasks));
                    ((Label)((VBox)overdueCard.getGraphic()).getChildren().get(1))
                        .setText(String.valueOf(stats.overdueTasks));

                    // 饼图
                    if (stats.categoryStats != null) {
                        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                        for (Map<String, Object> m : stats.categoryStats) {
                            String name = String.valueOf(m.get("name"));
                            double count = ((Number) m.get("count")).doubleValue();
                            pieData.add(new PieChart.Data(name + " (" + (int)count + ")", count));
                        }
                        pieChart.setData(pieData);
                    }

                    // 柱状图
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
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "加载统计失败: " + ex.getMessage()).showAndWait());
            }
        }).start();
    }

    private Label createCard(String label, String value, String color) {
        Label valLabel = new Label(value);
        valLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        valLabel.setStyle("-fx-text-fill: " + color + ";");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");

        VBox box = new VBox(5, nameLabel, valLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setPrefWidth(180);

        Label card = new Label();
        card.setGraphic(box);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-padding: 5;");
        return card;
    }

    public VBox getRoot() { return root; }
}
