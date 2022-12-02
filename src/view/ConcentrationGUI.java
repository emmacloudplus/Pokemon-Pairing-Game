package view;

import controller.ConcentrationController;
import game.ConcentrationCard;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.application.Application;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.ConcentrationModel;
import model.Observer;

import java.util.*;
/**
 * GUI Class
 *
 */
public class ConcentrationGUI extends Application implements Observer<ConcentrationModel> {
    public static String[] imageNames = {
            "abra.png", "bulbasaur.png", "charizard.png", "diglett.png", "golbat.png", "golem.png", "jigglypuff.png",
            "magikarp.png", "meowth.png", "mewtwo.png", "natu.png", "pidgey.png", "pikachu.png", "poliwag.png",
            "psyduck.png", "rattata.png", "slowpoke.png", "snorlak.png", "squirtle.png"};
    public static String pokeBallName = "pokeball.png";

    private List<Image> images;
    private Image pokeBallImage;

    private ConcentrationModel model;
    // Client is also controller;
    private ConcentrationController controller;

    private PokemonButton[][] buttons;

    private Label label1;
    private Label label2;
    private Label label3;

    private Map<Character, Integer> letterToImageIndex;

    /**
     * Constructor for GUI class
     * Assigns images to imageNames using stream
     * Initialize model and controller
     * create labels objects

     */
    public ConcentrationGUI() {
        images = new ArrayList<>();
        for (String imageName : imageNames) {
            Image imageRead = new Image(getClass().getResourceAsStream(
                    "images/" + imageName));
            images.add(imageRead);
        }
        Collections.shuffle(images);
        pokeBallImage = new Image(getClass().getResourceAsStream(
                "images/" + pokeBallName));
        model = new ConcentrationModel();
        controller = null;

        label1 = new Label();
        label2 = new Label();
        label3 = new Label();

        this.letterToImageIndex = new HashMap<>();
        model.addObserver(this);
    }
    /**
     * init method that parses arguments to assign prtNumber and gameboard Dimension
     * creates the game board
     */
    @Override
    public void init() throws Exception {
        List<String> args = getParameters().getRaw();
        String hostName = args.get(0);
        int portNumber = Integer.parseInt(args.get(1));
        controller = new ConcentrationController(hostName, portNumber, model);
        int dim = controller.getBoardDimension();
        model.initBoard(dim);
        buttons = new PokemonButton[dim][dim];
    }
    /**
     * Start method. sets up GridPane and BorderPane to structure gamebaord
     * Stages scene
     */
    @Override
    public void start(Stage stage) throws Exception {
        GridPane gridPane = createButtons();

        BorderPane labelPane = createLabels();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(gridPane);
        borderPane.setBottom(labelPane);
        BorderPane.setAlignment(labelPane, Pos.CENTER);

        Scene scene = new Scene(borderPane);

        stage.setScene(scene);
        stage.setTitle("ConcentrationGUI");
        stage.show();
    }
    /**
     * Update method
     */
    @Override
    public void update(ConcentrationModel concentrationModel) {
        if (Platform.isFxApplicationThread()) {
            this.refresh(model);
        } else {
            Platform.runLater(() -> this.refresh(model));
        }
    }
    /**
     * Refresh method.
     * updates the game board with new game board state
     */
    public void refresh(ConcentrationModel concentrationModel){
        int dim = concentrationModel.getDim();
        for (int row = 0; row < dim; ++row) {
            for (int col = 0; col < dim; ++col) {
                PokemonButton button = buttons[row][col];
                if(concentrationModel.isHidden(row, col)){
                    button.setGraphic(new ImageView(pokeBallImage));
                } else {
                    char letter = concentrationModel.getLetter(row, col);
                    if(letterToImageIndex.containsKey(letter)){
                        int imageIndex = letterToImageIndex.get(letter);
                        Image imageToUse = images.get(imageIndex);
                        button.setGraphic(new ImageView(imageToUse));
                    } else {
                        int nextIndex = letterToImageIndex.size();
                        letterToImageIndex.put(letter, nextIndex);
                        Image imageToUse = images.get(nextIndex);
                        button.setGraphic(new ImageView(imageToUse));
                    }
                }
            }
        }
        // refresh labels.
        label1.setText("Moves: " + this.model.getMoves());
        label2.setText("Matches: " + this.model.getMatches());
        label3.setText(this.model.getStatus().toString());
    }
    /**
     * this class creates the grid of button/images that the user will select to play the Concentration game
     */
    private GridPane createButtons() {
        GridPane gridPane = new GridPane();
        int dim = model.getDim();
        for (int row = 0; row < dim; ++row) {
            for (int col = 0; col < dim; ++col) {
                PokemonButton button = new PokemonButton(row, col);
                buttons[row][col] = button;
                button.setOnAction(event -> {
                    int buttonRow = button.getRow();
                    int buttonCol = button.getCol();
                    this.controller.createClientThread(buttonRow, buttonCol);
                });
                // JavaFX uses (x, y) pixel coordinates instead of
                // (row, col), so must invert when adding
                gridPane.add(button, col, row);
            }
        }
        return gridPane;
    }
    /**
     * Method to make the labels for the gamebaord
     */
    private BorderPane createLabels() {
        label1.setFont(new Font("Arial", 14));
        label2.setFont(new Font("Arial", 14));
        label3.setFont(new Font("Arial", 14));
        label1.setText("Moves: " + this.model.getMoves());
        label2.setText("Matches: " + this.model.getMatches());
        label3.setText(this.model.getStatus().toString());

        BorderPane subBorderPane = new BorderPane();
        subBorderPane.setLeft(label1);
        subBorderPane.setCenter(label2);
        subBorderPane.setRight(label3);

        return subBorderPane;
    }
    /**
     * Method that sets the position of the Pokemon cards
     */
    private class PokemonButton extends Button {
        private int row;
        private int col;

        public PokemonButton(int row, int col) {
            this.row = row;
            this.col = col;
            this.setGraphic(new ImageView(pokeBallImage));
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ConcentrationGUI <host name> <port number>");
            System.exit(1);
        }
        Application.launch(args);
    }
}
