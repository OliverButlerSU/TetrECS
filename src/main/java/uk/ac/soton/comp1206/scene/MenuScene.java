package uk.ac.soton.comp1206.scene;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 *
 * @author Windows
 * @version $Id: $Id
 */
public class MenuScene extends BaseScene {

    /**
     * A logger to log the status of a class
     */
    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Media player to play music
     */
    private final Multimedia multimedia = new Multimedia();

    /**
     * Create a new menu scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * {@inheritDoc}
     *
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        multimedia.playBackgroundMusic("menu.mp3");

        //Build the main window
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);
        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Add a VBox to hold buttons
        VBox buttons = new VBox();
        buttons.setPadding(new Insets(6, 6, 6, 6));
        buttons.setAlignment(Pos.BOTTOM_CENTER);
        mainPane.setBottom(buttons);


        //Add buttons to load scenes
        var playButton = new Button("Play");
        var lobbyButton = new Button("Multiplayer");
        var instructionButton = new Button("Instructions");
        var exitButton = new Button("Exit");

        //Style the buttons
        playButton.getStyleClass().add("menuItem");
        lobbyButton.getStyleClass().add("menuItem");
        instructionButton.getStyleClass().add("menuItem");
        exitButton.getStyleClass().add("menuItem");
        buttons.getChildren().addAll(playButton, lobbyButton, instructionButton, exitButton);

        //Bind the button action to load a scene
        playButton.setOnAction(this::startGame);
        instructionButton.setOnAction(this::startInstruction);
        exitButton.setOnAction(this::exitGame);
        lobbyButton.setOnAction(this::startLobby);

        //Add the TetrECS.png image to the middle of the screen
        Image image = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(image.getWidth()/8);
        imageView.setFitWidth(image.getWidth()/8);
        mainPane.setCenter(imageView);

        //Add a scale animation which replays and changes the size of the image
        ScaleTransition scale = new ScaleTransition();
        scale.setNode(imageView);
        scale.setDuration(Duration.millis(1200));
        scale.setCycleCount(ScaleTransition.INDEFINITE);
        scale.setInterpolator(Interpolator.LINEAR);
        scale.setByX(imageView.getFitWidth()/image.getWidth() * 1.12);
        scale.setByY(imageView.getFitHeight()/image.getHeight() * 1.12);
        scale.setAutoReverse(true);
        scale.play();

        //Add a rotate animation which rotates the image
        RotateTransition rotate = new RotateTransition();
        rotate.setNode(imageView);
        rotate.setDuration(Duration.millis(1200));
        rotate.setCycleCount(TranslateTransition.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setByAngle(10);
        rotate.setAutoReverse(true);
        rotate.play();
    }

    /**
     * {@inheritDoc}
     *
     * Initialise the menu
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed((event -> {
            //If escape is pressed
            if(event.getCode() == KeyCode.ESCAPE){
                //Close the game
                logger.info("Shutting down");
                System.exit(0);
            }
        }));
    }

    /**
     * Exit the game
     * @param event the action pressed to call it
     */
    private void exitGame(ActionEvent event){
        logger.info("Shutting down");
        System.exit(0);
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event the action pressed to call it
     */
    private void startGame(ActionEvent event) {
        multimedia.pauseBackgroundMusic();
        gameWindow.startChallenge();
    }

    /**
     * Handle when the Start Instruction button is pressed
     * @param event the action pressed to call it
     */
    private void startInstruction(ActionEvent event){
        multimedia.pauseBackgroundMusic();
        gameWindow.startInstruction();
    }

    /**
     * Handle when the Start Lobby button is pressed
     * @param event the action pressed to call it
     */
    private void startLobby(ActionEvent event){
        multimedia.pauseBackgroundMusic();
        gameWindow.startLobby();
    }

}
