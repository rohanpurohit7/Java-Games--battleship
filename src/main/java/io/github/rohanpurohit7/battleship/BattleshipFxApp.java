package io.github.rohanpurohit7.battleship;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JavaFX modernization of the original console Battleship game.
 * The original classes remain preserved in the repository; this is the new GUI entry point.
 */
public class BattleshipFxApp extends Application {
    private static final int BOARD_SIZE = 8;
    private static final String[] ROW_LABELS = {"A", "B", "C", "D", "E", "F", "G", "H"};

    private final SecureRandom random = new SecureRandom();
    private final Map<String, FleetShip> fleetByCell = new HashMap<>();
    private final Set<String> firedCells = new HashSet<>();
    private final List<FleetShip> fleet = new ArrayList<>();
    private final Map<String, StackPane> cellNodes = new HashMap<>();

    private int torpedoesFired;
    private Label missionStatus;
    private Label statsLabel;
    private TextArea battleLog;
    private GridPane oceanGrid;

    @Override
    public void start(Stage stage) {
        resetModel();

        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("app-shell");

        VBox header = buildHeader();
        HBox content = new HBox(24, buildBoardPanel(), buildCommandPanel());
        content.setPadding(new Insets(22));
        HBox.setHgrow(oceanGrid, Priority.ALWAYS);

        shell.setTop(header);
        shell.setCenter(content);

        Scene scene = new Scene(shell, 1120, 760);
        scene.getStylesheets().add(BattleshipFxApp.class.getResource("/styles/battleship.css").toExternalForm());

        stage.setTitle("Battleship Command Center — JavaFX Edition");
        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.show();

        log("Mission initialized. Use the tactical grid to launch torpedoes.");
        log("Sink Captain Nemo, Admiral Nelson, and Admiral Yamamoto.");
    }

    private VBox buildHeader() {
        Label title = new Label("BATTLESHIP COMMAND CENTER");
        title.getStyleClass().add("title");

        missionStatus = new Label("Sonar sweep active — enemy fleet hidden.");
        missionStatus.getStyleClass().add("subtitle");

        VBox header = new VBox(4, title, missionStatus);
        header.setPadding(new Insets(22, 28, 18, 28));
        header.getStyleClass().add("header");
        return header;
    }

    private VBox buildBoardPanel() {
        Label boardTitle = new Label("Tactical Ocean Grid");
        boardTitle.getStyleClass().add("section-title");

        oceanGrid = new GridPane();
        oceanGrid.setHgap(6);
        oceanGrid.setVgap(6);
        oceanGrid.setAlignment(Pos.CENTER);
        oceanGrid.getStyleClass().add("ocean-grid");

        for (int col = 0; col < BOARD_SIZE; col++) {
            Label colHeader = new Label(String.valueOf(col));
            colHeader.getStyleClass().add("axis-label");
            oceanGrid.add(colHeader, col + 1, 0);
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            Label rowHeader = new Label(ROW_LABELS[row]);
            rowHeader.getStyleClass().add("axis-label");
            oceanGrid.add(rowHeader, 0, row + 1);

            for (int col = 0; col < BOARD_SIZE; col++) {
                String coordinate = ROW_LABELS[row] + col;
                StackPane cell = createCell(coordinate);
                cellNodes.put(coordinate, cell);
                oceanGrid.add(cell, col + 1, row + 1);
            }
        }

        VBox panel = new VBox(16, boardTitle, oceanGrid);
        panel.setPadding(new Insets(24));
        panel.getStyleClass().add("glass-panel");
        return panel;
    }

    private StackPane createCell(String coordinate) {
        Rectangle water = new Rectangle(62, 62);
        water.getStyleClass().add("water-cell");

        Label label = new Label(coordinate);
        label.getStyleClass().add("cell-coordinate");

        StackPane cell = new StackPane(water, label);
        cell.getStyleClass().add("cell");
        cell.setOnMouseClicked(event -> fireAt(coordinate));
        return cell;
    }

    private VBox buildCommandPanel() {
        Label commandTitle = new Label("Command Console");
        commandTitle.getStyleClass().add("section-title");

        statsLabel = new Label();
        statsLabel.getStyleClass().add("stats-card");
        updateStats();

        Button newGame = new Button("New Mission");
        newGame.getStyleClass().add("primary-button");
        newGame.setMaxWidth(Double.MAX_VALUE);
        newGame.setOnAction(event -> newMission());

        Button reveal = new Button("Training Reveal");
        reveal.getStyleClass().add("secondary-button");
        reveal.setMaxWidth(Double.MAX_VALUE);
        reveal.setOnAction(event -> revealFleet());

        battleLog = new TextArea();
        battleLog.setEditable(false);
        battleLog.setWrapText(true);
        battleLog.getStyleClass().add("battle-log");
        battleLog.setPrefRowCount(18);

        VBox legend = new VBox(8,
                new Label("Legend"),
                new Label("● Red = hit"),
                new Label("● White = miss"),
                new Label("★ Gold = ship sunk"));
        legend.getStyleClass().add("legend");

        VBox panel = new VBox(14, commandTitle, statsLabel, newGame, reveal, legend, battleLog);
        panel.setPadding(new Insets(24));
        panel.setPrefWidth(360);
        panel.getStyleClass().add("glass-panel");
        return panel;
    }

    private void fireAt(String coordinate) {
        if (firedCells.contains(coordinate)) {
            log("Duplicate targeting rejected at " + coordinate + ". Choose a new coordinate.");
            pulse(cellNodes.get(coordinate));
            return;
        }

        firedCells.add(coordinate);
        torpedoesFired++;

        FleetShip target = fleetByCell.get(coordinate);
        if (target == null) {
            markMiss(coordinate);
            log("MISS at " + coordinate + ". Sonar shows open water.");
        } else {
            target.hit(coordinate);
            markHit(coordinate, target.isSunk());
            if (target.isSunk()) {
                log("KILL CONFIRMED: " + target.name() + " has been sunk.");
            } else {
                log("HIT at " + coordinate + " on " + target.name() + ". Continue tracking.");
            }
        }

        updateStats();
        if (fleet.stream().allMatch(FleetShip::isSunk)) {
            missionStatus.setText("Victory — all hostile vessels destroyed in " + torpedoesFired + " launches.");
            log("MISSION COMPLETE. Enemy fleet neutralized.");
        }
    }

    private void markMiss(String coordinate) {
        StackPane cell = cellNodes.get(coordinate);
        Circle marker = new Circle(8, Color.web("#e9f5ff"));
        marker.getStyleClass().add("miss-marker");
        cell.getChildren().add(marker);
        cell.getStyleClass().add("missed");
        animateMarker(marker);
    }

    private void markHit(String coordinate, boolean sunk) {
        StackPane cell = cellNodes.get(coordinate);
        Circle marker = new Circle(sunk ? 14 : 11, sunk ? Color.web("#ffd166") : Color.web("#ff4d6d"));
        marker.getStyleClass().add(sunk ? "sunk-marker" : "hit-marker");
        cell.getChildren().add(marker);
        cell.getStyleClass().add(sunk ? "sunk" : "hit");
        animateMarker(marker);
    }

    private void animateMarker(Circle marker) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(220), marker);
        scale.setFromX(0.25);
        scale.setFromY(0.25);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    private void pulse(StackPane cell) {
        FadeTransition fade = new FadeTransition(Duration.millis(180), cell);
        fade.setFromValue(0.45);
        fade.setToValue(1.0);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);
        fade.play();
    }

    private void revealFleet() {
        for (Map.Entry<String, FleetShip> entry : fleetByCell.entrySet()) {
            StackPane cell = cellNodes.get(entry.getKey());
            if (!firedCells.contains(entry.getKey())) {
                cell.getStyleClass().add("revealed");
            }
        }
        log("Training overlay enabled. Hidden fleet cells are highlighted.");
    }

    private void newMission() {
        resetModel();
        for (StackPane cell : cellNodes.values()) {
            cell.getChildren().removeIf(node -> node instanceof Circle);
            cell.getStyleClass().removeAll("missed", "hit", "sunk", "revealed");
        }
        battleLog.clear();
        missionStatus.setText("New mission started — enemy fleet hidden.");
        updateStats();
        log("New mission generated with three hidden ships.");
    }

    private void resetModel() {
        torpedoesFired = 0;
        firedCells.clear();
        fleet.clear();
        fleetByCell.clear();

        fleet.add(new FleetShip("Captain Nemo", 3));
        fleet.add(new FleetShip("Admiral Nelson", 3));
        fleet.add(new FleetShip("Admiral Yamamoto", 3));

        for (FleetShip ship : fleet) {
            placeShip(ship);
        }
    }

    private void placeShip(FleetShip ship) {
        boolean placed = false;
        while (!placed) {
            boolean horizontal = random.nextBoolean();
            int row = random.nextInt(BOARD_SIZE);
            int col = random.nextInt(BOARD_SIZE);
            List<String> candidate = new ArrayList<>();

            for (int i = 0; i < ship.length(); i++) {
                int r = horizontal ? row : row + i;
                int c = horizontal ? col + i : col;
                if (r >= BOARD_SIZE || c >= BOARD_SIZE) {
                    candidate.clear();
                    break;
                }
                candidate.add(ROW_LABELS[r] + c);
            }

            if (!candidate.isEmpty() && candidate.stream().noneMatch(fleetByCell::containsKey)) {
                for (String cell : candidate) {
                    ship.addCell(cell);
                    fleetByCell.put(cell, ship);
                }
                placed = true;
            }
        }
    }

    private void updateStats() {
        if (statsLabel == null) {
            return;
        }
        long sunk = fleet.stream().filter(FleetShip::isSunk).count();
        statsLabel.setText("Torpedoes launched: " + torpedoesFired + "\nShips sunk: " + sunk + " / " + fleet.size()
                + "\nRemaining cells: " + fleet.stream().mapToInt(FleetShip::remainingCells).sum());
    }

    private void log(String message) {
        if (battleLog != null) {
            battleLog.appendText("• " + message + System.lineSeparator());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
