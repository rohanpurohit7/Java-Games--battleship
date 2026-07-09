package battleshipfx;

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
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
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

        statusLabel = new Label("Enemy submarines are hidden. Select a grid coordinate to fire.");
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

        Label legend = new Label("Japanese fleet: gray/red   English fleet: blue-gray/white   German fleet: charcoal/white");
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
        statusLabel.setText("Enemy submarines are hidden. Select a grid coordinate to fire.");
    }

    private void placeFleet() {
        List<Ship> shipsToPlace = List.of(
                new Ship("Admiral Yamamoto", "Japanese fleet", 3, Color.web("#5f676b"), Color.web("#b3222a"), Color.web("#f4efe6")),
                new Ship("Admiral Nelson", "English fleet", 3, Color.web("#516a7c"), Color.web("#f4efe6"), Color.web("#2d4e68")),
                new Ship("Admiral Doenitz", "German fleet", 3, Color.web("#34393d"), Color.web("#f4efe6"), Color.web("#111111"))
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
            statusLabel.setText(cell.ship.commander + " sunk. " + (fleet.size() - sunkShips) + " fleet groups remain.");
            GameSounds.sunk();
            if (sunkShips == fleet.size()) {
                statusLabel.setText("Enemy fleet destroyed. The admiral is not taking it well.");
                GameSounds.victory();
                GameSounds.admiralTaunt();
            }
        } else {
            statusLabel.setText("Direct hit on the " + cell.ship.fleetName + ". Keep firing.");
            GameSounds.hit();
        }
    }

    private void revealShip(Ship ship) {
        for (Position position : ship.positions) {
            cells[position.row][position.col].showSubmarine();
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
        private final Label marker = new Label();
        private final Ellipse hull = new Ellipse(25, 11);
        private final Rectangle tower = new Rectangle(16, 14);
        private final Rectangle deckStripe = new Rectangle(36, 4);
        private final Circle portholeA = new Circle(3);
        private final Circle portholeB = new Circle(3);
        private final Circle portholeC = new Circle(3);

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

            tower.setArcWidth(5);
            tower.setArcHeight(5);
            tower.setTranslateY(-13);
            deckStripe.setTranslateY(-4);
            portholeA.setTranslateX(-13);
            portholeB.setTranslateX(0);
            portholeC.setTranslateX(13);

            marker.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
            getChildren().addAll(water, hull, tower, deckStripe, portholeA, portholeB, portholeC, marker);
            hideSubmarine();
            setOnMouseClicked(event -> fireAt(this));
        }

        private void reset() {
            ship = null;
            water.setFill(Color.web("#19526a"));
            water.setStroke(Color.web("#71a7b7"));
            marker.setText("");
            hideSubmarine();
            setDisable(false);
            setOpacity(1);
        }

        private void showMiss() {
            hideSubmarine();
            water.setFill(Color.web("#24414b"));
            marker.setText("X");
            marker.setTextFill(Color.web("#f4efe6"));
            pulse();
        }

        private void showHit() {
            marker.setText("");
            showSubmarine();
            water.setFill(Color.web("#5f2422"));
            water.setStroke(Color.web("#ffde7a"));
            pulse();
        }

        private void showSubmarine() {
            if (ship == null) {
                return;
            }
            hull.setFill(ship.hullColor);
            hull.setStroke(Color.web("#111111"));
            hull.setStrokeWidth(1.2);
            tower.setFill(ship.hullColor.darker());
            tower.setStroke(Color.web("#111111"));
            deckStripe.setFill(ship.accentColor);
            portholeA.setFill(ship.detailColor);
            portholeB.setFill(ship.detailColor);
            portholeC.setFill(ship.detailColor);
            hull.setVisible(true);
            tower.setVisible(true);
            deckStripe.setVisible(true);
            portholeA.setVisible(true);
            portholeB.setVisible(true);
            portholeC.setVisible(true);
        }

        private void hideSubmarine() {
            hull.setVisible(false);
            tower.setVisible(false);
            deckStripe.setVisible(false);
            portholeA.setVisible(false);
            portholeB.setVisible(false);
            portholeC.setVisible(false);
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
        private final String commander;
        private final String fleetName;
        private final int size;
        private final Color hullColor;
        private final Color accentColor;
        private final Color detailColor;
        private final List<Position> positions = new ArrayList<>();
        private final Set<Position> hits = new HashSet<>();

        private Ship(String commander, String fleetName, int size, Color hullColor, Color accentColor, Color detailColor) {
            this.commander = commander;
            this.fleetName = fleetName;
            this.size = size;
            this.hullColor = hullColor;
            this.accentColor = accentColor;
            this.detailColor = detailColor;
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
