package navalbattle;

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
import javafx.scene.control.ComboBox;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BattleshipFxApp extends Application {
    private static final int GRID_SIZE = 7;
    private static final int CELL_SIZE = 72;
    private static final Random RANDOM = new Random();

    private final CellView[][] cells = new CellView[GRID_SIZE][GRID_SIZE];
    private final List<Ship> fleet = new ArrayList<>();
    private final Set<Position> firedShots = new HashSet<>();
    private final Map<Ship, ScoreRow> scoreRows = new HashMap<>();

    private Pane effectsLayer;
    private VBox killBoard;
    private Label statusLabel;
    private Label shotsLabel;
    private ComboBox<Level> levelSelector;
    private int shotsUsed;
    private int sunkShips;
    private int torpedoes;
    private boolean missionOver;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #071820;");

        Label title = new Label("Battleship Command");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f4efe6;");

        levelSelector = new ComboBox<>();
        levelSelector.getItems().addAll(Level.EASY, Level.MEDIUM, Level.HIGH);
        levelSelector.setValue(Level.EASY);
        levelSelector.setOnAction(event -> resetGame());

        Button resetButton = new Button("New Battle");
        resetButton.setOnAction(event -> resetGame());
        resetButton.setStyle("-fx-background-color: #b4463a; -fx-text-fill: white; -fx-font-weight: bold;");

        shotsLabel = new Label();
        shotsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f7c66a;");

        Label levelLabel = new Label("Level");
        levelLabel.setStyle("-fx-text-fill: #d5e4e8;");
        HBox header = new HBox(16, title, levelLabel, levelSelector, shotsLabel, resetButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 12, 0));

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #d5e4e8;");
        root.setTop(new VBox(8, header, statusLabel));

        StackPane battlefield = new StackPane();
        GridPane grid = createGrid();
        effectsLayer = new Pane();
        effectsLayer.setMouseTransparent(true);
        effectsLayer.setPrefSize(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        battlefield.getChildren().addAll(grid, effectsLayer);
        root.setCenter(battlefield);

        killBoard = new VBox(12);
        killBoard.setPadding(new Insets(10, 0, 0, 18));
        root.setRight(killBoard);

        Label legend = new Label("Hit cells reveal submarines. Sink all three fleets before torpedoes run out.");
        legend.setStyle("-fx-font-size: 13px; -fx-text-fill: #9cb8bf;");
        BorderPane.setAlignment(legend, Pos.CENTER);
        BorderPane.setMargin(legend, new Insets(14, 0, 0, 0));
        root.setBottom(legend);

        resetGame();

        stage.setTitle("Battleship Command");
        stage.setScene(new Scene(root, 880, 650));
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
        shotsUsed = 0;
        sunkShips = 0;
        missionOver = false;
        torpedoes = levelSelector.getValue().torpedoes;
        fleet.clear();
        firedShots.clear();
        scoreRows.clear();
        killBoard.getChildren().clear();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].reset();
            }
        }

        placeFleet();
        buildKillBoard();
        updateShots();
        statusLabel.setText("Mission start. Fire carefully: " + torpedoes + " torpedoes available.");
    }

    private void placeFleet() {
        List<Ship> shipsToPlace = List.of(
                new Ship("Admiral Isoroku Yamamoto", "Japanese Fleet", "JP", 3,
                        Color.web("#5f676b"), Color.web("#b3222a"), Color.web("#f4efe6"),
                        "A hit, but not victory. The Pacific still hides my steel.",
                        "My Japanese fleet sinks with honor. Your aim has teeth.", -1),
                new Ship("Admiral Horatio Nelson", "English Fleet", "EN", 3,
                        Color.web("#516a7c"), Color.web("#f4efe6"), Color.web("#2d4e68"),
                        "A lucky shot, commander. England has weathered worse seas.",
                        "You have sunk the English line. I concede this broadside.", 1),
                new Ship("Admiral Karl Doenitz", "German Fleet", "DE", 3,
                        Color.web("#34393d"), Color.web("#f4efe6"), Color.web("#111111"),
                        "You clipped a U-boat. The rest still hunt beneath you.",
                        "The German wolfpack is gone. That was no ordinary strike.", -3)
        );

        for (Ship ship : shipsToPlace) {
            boolean placed = false;
            while (!placed) {
                boolean horizontal = RANDOM.nextBoolean();
                int row = RANDOM.nextInt(GRID_SIZE);
                int col = RANDOM.nextInt(GRID_SIZE);
                List<Position> positions = new ArrayList<>();

                for (int i = 0; i < ship.size; i++) {
                    positions.add(new Position(row + (horizontal ? 0 : i), col + (horizontal ? i : 0)));
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

    private void buildKillBoard() {
        Label heading = new Label("Fleet Kill Board");
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f4efe6;");
        killBoard.getChildren().add(heading);

        for (Ship ship : fleet) {
            ScoreRow row = new ScoreRow(ship);
            scoreRows.put(ship, row);
            killBoard.getChildren().add(row);
        }
    }

    private void fireAt(CellView cell) {
        Position target = new Position(cell.row, cell.col);
        if (missionOver || firedShots.contains(target)) {
            return;
        }

        firedShots.add(target);
        shotsUsed++;
        updateShots();
        GameSounds.launch();
        animateTorpedo(cell);

        if (cell.ship == null) {
            cell.showMiss();
            GameSounds.miss();
            statusLabel.setText("Splash. " + remainingTorpedoes() + " torpedoes remain.");
            checkOutOfTorpedoes();
            return;
        }

        Ship ship = cell.ship;
        ship.hits.add(target);
        cell.showHit();
        scoreRows.get(ship).update();

        if (ship.isSunk()) {
            sunkShips++;
            revealShip(ship);
            scoreRows.get(ship).update();
            statusLabel.setText(ship.commander + " sunk. " + (fleet.size() - sunkShips) + " fleets remain.");
            GameSounds.sunk();
            GameSounds.speak(ship.sunkLine, ship.voiceRate);
            if (sunkShips == fleet.size()) {
                missionOver = true;
                statusLabel.setText("Enemy fleet destroyed with " + remainingTorpedoes() + " torpedoes remaining.");
                GameSounds.victory();
                GameSounds.speak("You have sunk every fleet, commander. Enjoy this victory while the smoke clears.", 0);
            }
        } else {
            statusLabel.setText("Direct hit on " + ship.commander + ". " + remainingTorpedoes() + " torpedoes remain.");
            GameSounds.hit();
            GameSounds.speak(ship.hitLine, ship.voiceRate);
        }

        checkOutOfTorpedoes();
    }

    private void checkOutOfTorpedoes() {
        if (!missionOver && remainingTorpedoes() <= 0) {
            missionOver = true;
            statusLabel.setText("Mission failed. You are out of torpedoes.");
            GameSounds.speak("Out of torpedoes, commander. The sea belongs to us.", -2);
        }
    }

    private int remainingTorpedoes() {
        return torpedoes - shotsUsed;
    }

    private void revealShip(Ship ship) {
        for (Position position : ship.positions) {
            cells[position.row][position.col].showSubmarine();
        }
    }

    private void updateShots() {
        shotsLabel.setText("Torpedoes: " + remainingTorpedoes() + "/" + torpedoes);
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
            water.setFill(Color.web("#5f2422"));
            water.setStroke(Color.web("#ffde7a"));
            showSubmarine();
            pulse();
        }

        private void showSubmarine() {
            if (ship == null) {
                return;
            }
            hull.setFill(ship.hullColor);
            hull.setStroke(Color.web("#111111"));
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

    private static final class ScoreRow extends HBox {
        private final Ship ship;
        private final Label score;

        private ScoreRow(Ship ship) {
            this.ship = ship;
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(10);
            setPadding(new Insets(8));
            setStyle("-fx-background-color: #102a34; -fx-border-color: #315564; -fx-border-radius: 6; -fx-background-radius: 6;");

            StackPane logo = new StackPane();
            Circle badge = new Circle(18, ship.hullColor);
            badge.setStroke(ship.accentColor);
            badge.setStrokeWidth(3);
            Label code = new Label(ship.logo);
            code.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");
            logo.getChildren().addAll(badge, code);

            Label name = new Label(ship.commander + "\n" + ship.fleetName);
            name.setStyle("-fx-font-size: 12px; -fx-text-fill: #f4efe6;");
            score = new Label();
            score.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #f7c66a;");
            update();
            getChildren().addAll(logo, name, score);
        }

        private void update() {
            score.setText("Hits " + ship.hits.size() + "/" + ship.size + "\nKills " + (ship.isSunk() ? 1 : 0));
        }
    }

    private enum Level {
        EASY("Easy", 15),
        MEDIUM("Medium", 13),
        HIGH("High", 10);

        private final String label;
        private final int torpedoes;

        Level(String label, int torpedoes) {
            this.label = label;
            this.torpedoes = torpedoes;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class Ship {
        private final String commander;
        private final String fleetName;
        private final String logo;
        private final int size;
        private final Color hullColor;
        private final Color accentColor;
        private final Color detailColor;
        private final String hitLine;
        private final String sunkLine;
        private final int voiceRate;
        private final List<Position> positions = new ArrayList<>();
        private final Set<Position> hits = new HashSet<>();

        private Ship(String commander, String fleetName, String logo, int size, Color hullColor, Color accentColor,
                     Color detailColor, String hitLine, String sunkLine, int voiceRate) {
            this.commander = commander;
            this.fleetName = fleetName;
            this.logo = logo;
            this.size = size;
            this.hullColor = hullColor;
            this.accentColor = accentColor;
            this.detailColor = detailColor;
            this.hitLine = hitLine;
            this.sunkLine = sunkLine;
            this.voiceRate = voiceRate;
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
