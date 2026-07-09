package io.github.rohanpurohit7.battleship;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BattleshipFxApp extends Application {

    private static final int GRID_SIZE = 7;
    private static final int CELL_SIZE = 72;
    private static final Random RANDOM = new Random();

    private final CellView[][] cells = new CellView[GRID_SIZE][GRID_SIZE];
    private final List<Ship> fleet = new ArrayList<>();
    private final Set<Position> firedShots = new HashSet<>();

    private Pane effectsLayer;
    private Label statusLabel;
    private Label shotsLabel;
    private int shots;
    private int sunkShips;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #071820;");

        Label title = new Label("Battleship Command");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f4efe6;");

        statusLabel = new Label("Enemy ships are hidden. Select a grid coordinate to fire.");
        statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #d5e4e8;");

        shotsLabel = new Label("Torpedoes: 0");
        shotsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f7c66a;");

        Button resetButton = new Button("New Battle");
        resetButton.setOnAction(event -> resetGame());
        resetButton.setStyle("-fx-background-color: #b4463a; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox header = new HBox(18, title, shotsLabel, resetButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 14, 0));

        VBox top = new VBox(8, header, statusLabel);
        root.setTop(top);

        StackPane battlefield = new StackPane();
        GridPane grid = createGrid();
        effectsLayer = new Pane();
        effectsLayer.setMouseTransparent(true);
        effectsLayer.setPrefSize(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        battlefield.getChildren().addAll(grid, effectsLayer);
        root.setCenter(battlefield);

        Label legend = new Label("Blue: unknown water   White: miss   Red/orange: ship hit   Full ship reveals when sunk");
        legend.setStyle("-fx-font-size: 13px; -fx-text-fill: #9cb8bf;");
        BorderPane.setAlignment(legend, Pos.CENTER);
        BorderPane.setMargin(legend, new Insets(14, 0, 0, 0));
        root.setBottom(legend);

        resetGame();

        Scene scene = new Scene(root, 690, 650);
        stage.setTitle("Battleship Command");
        stage.setScene(scene);
        stage.show();
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: #12313d; -fx-padding: 8;");

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                CellView cell = new CellView(row, col);
                cells[row][col] = cell;
                grid.add(cell, col, row);
            }
        }

        return grid;
    }

    private void resetGame() {
        shots = 0;
        sunkShips = 0;
        fleet.clear();
        firedShots.clear();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].reset();
            }
        }

        placeFleet();
        updateShots();
        statusLabel.setText("Enemy ships are hidden. Select a grid coordinate to fire.");
    }

    private void placeFleet() {
        List<Ship> shipsToPlace = List.of(
                new Ship("Captain Nemo", 3),
                new Ship("Admiral Nelson", 3),
                new Ship("Admiral Yamamoto", 3)
        );

        for (Ship ship : shipsToPlace) {
            boolean placed = false;
            while (!placed) {
                boolean horizontal = RANDOM.nextBoolean();
                int row = RANDOM.nextInt(GRID_SIZE);
                int col = RANDOM.nextInt(GRID_SIZE);
                List<Position> positions = new ArrayList<>();

                for (int i = 0; i < ship.size; i++) {
                    int nextRow = row + (horizontal ? 0 : i);
                    int nextCol = col + (horizontal ? i : 0);
                    positions.add(new Position(nextRow, nextCol));
                }

                if (isValidPlacement(positions)) {
                    ship.positions.addAll(positions);
                    for (Position position : positions) {
                        cells[position.row][position.col].ship = ship;
                    }
                    fleet.add(ship);
                    placed = true;
                }
            }
        }
    }

    private boolean isValidPlacement(List<Position> positions) {
        for (Position position : positions) {
            if (position.row < 0 || position.row >= GRID_SIZE || position.col < 0 || position.col >= GRID_SIZE) {
                return false;
            }
            if (cells[position.row][position.col].ship != null) {
                return false;
            }
        }
        return true;
    }

    private void fireAt(CellView cell) {
        Position target = new Position(cell.row, cell.col);
        if (firedShots.contains(target) || sunkShips == fleet.size()) {
            return;
        }

        firedShots.add(target);
        shots++;
        updateShots();
        GameSounds.launch();
        animateTorpedo(cell);

        if (cell.ship == null) {
            cell.showMiss();
            statusLabel.setText("Splash. The admiral laughs over the radio.");
            GameSounds.miss();
            return;
        }

        cell.ship.hits.add(target);
        cell.showHit();

        if (cell.ship.isSunk()) {
            sunkShips++;
            revealShip(cell.ship);
            statusLabel.setText(cell.ship.name + " is sunk. The fleet is down to " + (fleet.size() - sunkShips) + ".");
            GameSounds.sunk();
            if (sunkShips == fleet.size()) {
                statusLabel.setText("Enemy fleet destroyed. The admiral is not taking it well.");
                GameSounds.victory();
                GameSounds.admiralTaunt();
            }
        } else {
            statusLabel.setText("Direct hit on " + cell.ship.name + ". Keep firing.");
            GameSounds.hit();
        }
    }

    private void revealShip(Ship ship) {
        for (Position position : ship.positions) {
            cells[position.row][position.col].showShipSegment();
        }
    }

    private void updateShots() {
        shotsLabel.setText("Torpedoes: " + shots);
    }

    private void animateTorpedo(CellView cell) {
        double targetX = cell.col * (CELL_SIZE + 4) + CELL_SIZE / 2.0 + 8;
        double targetY = cell.row * (CELL_SIZE + 4) + CELL_SIZE / 2.0 + 8;

        Circle torpedo = new Circle(7, Color.web("#f4efe6"));
        torpedo.setStroke(Color.web("#111111"));
        torpedo.setLayoutX(8);
        torpedo.setLayoutY(8);
        effectsLayer.getChildren().add(torpedo);

        TranslateTransition fly = new TranslateTransition(Duration.millis(260), torpedo);
        fly.setToX(targetX - 8);
        fly.setToY(targetY - 8);
        fly.setOnFinished(event -> {
            effectsLayer.getChildren().remove(torpedo);
            animateExplosion(targetX, targetY);
        });
        fly.play();
    }

    private void animateExplosion(double x, double y) {
        Circle flash = new Circle(x, y, 8, Color.web("#ffde7a"));
        flash.setStroke(Color.web("#f05d3d"));
        flash.setStrokeWidth(4);

        Circle smoke = new Circle(x, y, 16, Color.web("#d5e4e8", 0.35));
        effectsLayer.getChildren().addAll(smoke, flash);

        ScaleTransition flashScale = new ScaleTransition(Duration.millis(360), flash);
        flashScale.setToX(3.2);
        flashScale.setToY(3.2);
        FadeTransition flashFade = new FadeTransition(Duration.millis(360), flash);
        flashFade.setToValue(0);

        ScaleTransition smokeScale = new ScaleTransition(Duration.millis(520), smoke);
        smokeScale.setToX(2.8);
        smokeScale.setToY(2.8);
        FadeTransition smokeFade = new FadeTransition(Duration.millis(520), smoke);
        smokeFade.setToValue(0);

        ParallelTransition blast = new ParallelTransition(flashScale, flashFade, smokeScale, smokeFade);
        blast.setOnFinished(event -> effectsLayer.getChildren().removeAll(smoke, flash));
        blast.play();
    }

    private final class CellView extends StackPane {
        private final int row;
        private final int col;
        private Ship ship;
        private final Rectangle water = new Rectangle(CELL_SIZE, CELL_SIZE);
        private final Rectangle shipBlock = new Rectangle(CELL_SIZE - 18, CELL_SIZE - 28);
        private final Label marker = new Label();

        private CellView(int row, int col) {
            this.row = row;
            this.col = col;
            setPrefSize(CELL_SIZE, CELL_SIZE);
            setMinSize(CELL_SIZE, CELL_SIZE);
            setMaxSize(CELL_SIZE, CELL_SIZE);
            setAlignment(Pos.CENTER);

            water.setArcWidth(8);
            water.setArcHeight(8);
            water.setFill(Color.web("#19526a"));
            water.setStroke(Color.web("#71a7b7"));
            water.setStrokeWidth(1.5);

            shipBlock.setArcWidth(7);
            shipBlock.setArcHeight(7);
            shipBlock.setFill(Color.TRANSPARENT);

            marker.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
            getChildren().addAll(water, shipBlock, marker);
            setOnMouseClicked(event -> fireAt(this));
        }

        private void reset() {
            ship = null;
            water.setFill(Color.web("#19526a"));
            water.setStroke(Color.web("#71a7b7"));
            shipBlock.setFill(Color.TRANSPARENT);
            marker.setText("");
            setDisable(false);
            setOpacity(1);
        }

        private void showMiss() {
            water.setFill(Color.web("#24414b"));
            marker.setText("X");
            marker.setTextFill(Color.web("#f4efe6"));
            pulse();
        }

        private void showHit() {
            water.setFill(Color.web("#6f2420"));
            shipBlock.setFill(Color.web("#d17a43"));
            marker.setText("");
            pulse();
        }

        private void showShipSegment() {
            water.setFill(Color.web("#5d2f29"));
            shipBlock.setFill(Color.web("#e68a49"));
            water.setStroke(Color.web("#ffde7a"));
        }

        private void pulse() {
            ScaleTransition out = new ScaleTransition(Duration.millis(90), this);
            out.setToX(1.07);
            out.setToY(1.07);
            ScaleTransition in = new ScaleTransition(Duration.millis(110), this);
            in.setToX(1);
            in.setToY(1);
            new SequentialTransition(out, in).play();
        }
    }

    private static final class Ship {
        private final String name;
        private final int size;
        private final List<Position> positions = new ArrayList<>();
        private final Set<Position> hits = new HashSet<>();

        private Ship(String name, int size) {
            this.name = name;
            this.size = size;
        }

        private boolean isSunk() {
            return hits.containsAll(positions);
        }
    }

    private record Position(int row, int col) {
    }

    public static void main(String[] args) {
        launch(args);
    }
}
