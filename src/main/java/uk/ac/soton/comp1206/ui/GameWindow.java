package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.*;

/**
 * The GameWindow is the single window for the game where everything takes place. To move between screens in the game,
 * we simply change the scene.
 *
 * The GameWindow has methods to launch each of the different parts of the game by switching scenes. You can add more
 * methods here to add more screens to the game.
 *
 * @author Windows
 * @version $Id: $Id
 */
public class GameWindow {

    /**
     *A logger to log the status of a class
     */
    private static final Logger logger = LogManager.getLogger(GameWindow.class);

    /**
     * Width of the window
     */
    private final int width;

    /**
     * Height of the window
     */
    private final int height;

    /**
     * The stage of the window
     */
    private final Stage stage;

    /**
     * The current scene loaded
     */
    private BaseScene currentScene;

    /**
     * The current scene loaded
     */
    private Scene scene;

    /**
     * A communicator to communicate and connect to a server
     */
    private final Communicator communicator;

    /**
     * Create a new GameWindow attached to the given stage with the specified width and height
     *
     * @param stage stage
     * @param width width
     * @param height height
     */
    public GameWindow(Stage stage, int width, int height) {
        this.width = width;
        this.height = height;

        this.stage = stage;

        //Setup window
        setupStage();

        //Setup resources
        setupResources();

        //Setup default scene
        setupDefaultScene();

        //Setup communicator
        communicator = new Communicator("ws://ofb-labs.soton.ac.uk:9700");

        //Go to menu
        startIntro();
    }

    /**
     * Setup the font and any other resources we need
     */
    private void setupResources() {
        logger.info("Loading resources");

        //We need to load fonts here due to the Font loader bug with spaces in URLs in the CSS files
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Regular.ttf"),32);
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-Bold.ttf"),32);
        Font.loadFont(getClass().getResourceAsStream("/style/Orbitron-ExtraBold.ttf"),32);
    }

    /**
     * Display the main menu
     */
    public void startMenu() {
        loadScene(new MenuScene(this));
    }

    /**
     * Display the single player challenge
     */
    public void startChallenge() {
        loadScene(new ChallengeScene(this));
    }

    /**
     * Display an instruction menu / screen
     */
    public void startInstruction() {
        loadScene(new InstructionScene(this));
    }

    /**
     * Display the score menu
     *
     * @param game the last state of the game
     */
    public void startScore(Game game){
        loadScene(new ScoresScene(this, game, communicator));
    }

    /**
     * Display the intro animation
     */
    private void startIntro(){
        loadScene(new IntroScene(this));
    }

    /**
     * Display the multiplayer lobby menu
     */
    public void startLobby(){
        loadScene(new LobbyScene(this, communicator));
    }

    /**
     * Dusplay the multiplayer game
     *
     * @param username The username of the local player
     */
    public void startMultiplayer(String username){
        loadScene(new MultiplayerScene(this,communicator, username));
    }

    /**
     * Setup the default settings for the stage itself (the window), such as the title and minimum width and height.
     */
    public void setupStage() {
        stage.setTitle("TetrECS");
        stage.setMinWidth(width);
        stage.setMinHeight(height + 20);
        stage.setOnCloseRequest(ev -> App.getInstance().shutdown());
    }

    /**
     * Load a given scene which extends BaseScene and switch over.
     *
     * @param newScene new scene to load
     */
    public void loadScene(BaseScene newScene) {
        //Cleanup remains of the previous scene
        cleanup();

        //Create the new scene and set it up
        newScene.build();
        currentScene = newScene;
        scene = newScene.setScene();
        stage.setScene(scene);

        //Initialise the scene when ready
        Platform.runLater(() -> currentScene.initialise());
    }

    /**
     * Setup the default scene (an empty black scene) when no scene is loaded
     */
    public void setupDefaultScene() {
        this.scene = new Scene(new Pane(),width,height, Color.BLACK);
        stage.setScene(this.scene);
    }

    /**
     * When switching scenes, perform any cleanup needed, such as removing previous listeners
     */
    public void cleanup() {
        logger.info("Clearing up previous scene");
        communicator.clearListeners();
    }

    /**
     * Get the current scene being displayed
     *
     * @return scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Get the width of the Game Window
     *
     * @return width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Get the height of the Game Window
     *
     * @return height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the communicator
     *
     * @return communicator
     */
    public Communicator getCommunicator() {
        return communicator;
    }
}
