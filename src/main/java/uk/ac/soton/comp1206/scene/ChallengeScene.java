package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 *
 * @author Windows
 * @version $Id: $Id
 */
public class ChallengeScene extends BaseScene {

    /**
     * A logger to log the status of the class
     */
    protected static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * The game which the UI is tracking
     */
    private Game game;

    /**
     * A media playeer to play music
     */
    protected final Multimedia multimedia = new Multimedia();

    /**
     * A piece board to display the current piece
     */
    protected PieceBoard pieceBoard;

    /**
     * A piece board to display the next piece
     */
    protected PieceBoard nextPieceBoard;

    /**
     * The game board which the game is attached to
     */
    protected GameBoard board;

    /**
     * The high score of the player
     */
    private SimpleIntegerProperty highScore;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }


    /**
     * {@inheritDoc}
     *
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        scene.setOnKeyPressed((event -> {
            switch(event.getCode()){
                case ESCAPE:
                    //Pressing escape ends the game and goes back to the start menu
                    multimedia.pauseBackgroundMusic();
                    game.endGame();
                    gameWindow.startMenu();
                    break;
                case UP: case W:
                    //Pressing the up arrow or W changes the position of the keyboard in the game and highlights a piece
                    board.deHighlightKeyboard(game.getAim());
                    game.upAim();
                    board.highlightKeyboard(game.getAim());
                    break;
                case DOWN: case S:
                    //Pressing the down arrow or S changes the position of the keyboard in the game and highlights a piece
                    board.deHighlightKeyboard(game.getAim());
                    game.downAim();
                    board.highlightKeyboard(game.getAim());
                    break;
                case LEFT: case A:
                    //Pressing the left arrow or A changes the position of the keyboard in the game and highlights a piece
                    board.deHighlightKeyboard(game.getAim());
                    game.leftAim();
                    board.highlightKeyboard(game.getAim());
                    break;
                case RIGHT: case D:
                    //Pressing the right arrow or D changes the position of the keyboard in the game and highlights a piece
                    board.deHighlightKeyboard(game.getAim());
                    game.rightAim();
                    board.highlightKeyboard(game.getAim());
                    break;
                case ENTER: case X:
                    //Pressing the up enter or X places the piece and changes the high score if needed
                    game.placeAim();
                    if(game.getScore().get() > highScore.get()){
                        highScore.set(game.getScore().get());
                    }
                    break;
                case SPACE: case R:
                    //Pressing space or R swaps the two pieces
                    logger.info("Swapping pieces");
                    game.swapCurrentPiece();
                    pieceBoard.displayPiece(game.getCurrentPiece());
                    nextPieceBoard.displayPiece(game.getFollowingPiece());
                    break;
                case OPEN_BRACKET: case Q: case Z:
                    //Pressing [ or Q or Z counter rotates the piece
                    logger.info("Rotating pieces");
                    game.counterRotateCurrentPiece();
                    pieceBoard.displayPiece(game.getCurrentPiece());
                    break;
                case E: case C: case CLOSE_BRACKET:
                    //Pressing E or C or ] rotates clockwise the piece
                    logger.info("Rotating pieces");
                    game.rotateCurrentPiece();
                    pieceBoard.displayPiece(game.getCurrentPiece());
                    break;
            }
        }));
    }

    /**
     * {@inheritDoc}
     *
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        setupGame();

        //Create the main window
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        multimedia.playBackgroundMusic("game.wav");
        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);
        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        //Create a VBox to hold the board and title
        var boardBox = new VBox();
        boardBox.setAlignment(Pos.CENTER);

        //Create a title
        var title = new Text("Challenge Mode");
        title.getStyleClass().add("title");

        //Create the board and add to scene
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        boardBox.getChildren().addAll(title,board);
        mainPane.setCenter(boardBox);

        //Create the text to represent the players information
        var livesText = new Text();
        var multiplierText = new Text();
        var levelText = new Text();
        var scoreText = new Text();
        var highScoreText = new Text();
        highScore = new SimpleIntegerProperty(getHighScore());

        //Style it
        livesText.getStyleClass().add("lives");
        multiplierText.getStyleClass().add("multiplier");
        levelText.getStyleClass().add("level");
        scoreText.getStyleClass().add("score");
        highScoreText.getStyleClass().add("hiscore");

        //Bind the text to its information to track
        livesText.textProperty().bind(game.getLives().asString());
        multiplierText.textProperty().bind(game.getMultiplier().asString());
        levelText.textProperty().bind(game.getLevel().asString());
        scoreText.textProperty().bind(game.getScore().asString());
        highScoreText.textProperty().bind(highScore.asString());

        //Create a VBox to hold the information in
        var informationBox = new VBox();
        informationBox.setPadding(new Insets(4,4,4,4));
        informationBox.setAlignment(Pos.CENTER);

        //Create a title for each text
        var highScoreTitle = new Text("High Score:");
        highScoreTitle.getStyleClass().add("heading");
        var scoreTitle = new Text("Score:");
        scoreTitle.getStyleClass().add("heading");
        var livesTitle = new Text("Lives:");
        livesTitle.getStyleClass().add("heading");
        var levelTitle = new Text("Level:");
        levelTitle.getStyleClass().add("heading");
        var multiplierTitle = new Text("Multiplier:");
        multiplierTitle.getStyleClass().add("heading");

        //Add the information to the scene
        informationBox.getChildren().addAll(highScoreTitle, highScoreText, scoreTitle, scoreText, livesTitle, livesText,
                                            levelTitle, levelText, multiplierTitle, multiplierText);
        mainPane.setLeft(informationBox);

        //Create a VBox to hold the PieceBoards
        var pieceInformation = new VBox();
        pieceInformation.setPadding(new Insets(4,4,4,4));
        pieceInformation.setAlignment(Pos.CENTER);

        //Create and style titles for each board
        var currentPieceText = new Text("Current Piece");
        currentPieceText.getStyleClass().add("heading");
        var nextPieceText = new Text("Next Piece");
        nextPieceText.getStyleClass().add("heading");

        //Create the piece boards
        this.pieceBoard = new PieceBoard(3, 3, gameWindow.getWidth()/8,gameWindow.getWidth()/8);
        this.nextPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth()/12,gameWindow.getWidth()/12);

        //Add them to the scene
        pieceInformation.getChildren().addAll(currentPieceText, pieceBoard, nextPieceText, nextPieceBoard);
        mainPane.setRight(pieceInformation);

        //Set the nextPieceListener to display both boards
        game.setNextPieceListener((currentPiece, followingPiece) -> {
            pieceBoard.displayPiece(currentPiece);
            nextPieceBoard.displayPiece(followingPiece);
        });

        //Set the lineClearedListener to call the fade animation on the blocks
        game.setLineClearedListener((blockCoordinates) ->{
            board.fadeOut(blockCoordinates);
        });

        //Handle block on gameboard grid being clicked
        board.setOnRightClicked(() ->{
            game.rotateCurrentPiece();
            pieceBoard.displayPiece(game.getCurrentPiece());
        });

        //Create a rectangle to represent the timer bar
        Rectangle timeBar = new Rectangle();
        timeBar.setFill(Color.GREEN);
        timeBar.setHeight(20);
        timeBar.setWidth(gameWindow.getWidth()-8);

        //Create a holder for it
        var timeBarBox = new HBox(timeBar);
        timeBarBox.setPadding(new Insets(4, 4, 4, 4));
        mainPane.setBottom(timeBarBox);

        //Create a scale transition to scale the rectangle to 0
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(game.getTimerDelay()), timeBar);
        scaleTransition.setInterpolator(Interpolator.LINEAR);
        scaleTransition.setFromX(1);
        scaleTransition.setToX(0);

        //Create a fill transition to change the colour of the bar
        FillTransition ft = new FillTransition(Duration.millis(game.getTimerDelay()), timeBar, Color.GREEN, Color.RED);
        ft.setInterpolator(Interpolator.LINEAR);

        //Set the gameLoopListener to call the animation
        game.setGameLoopListener(() ->{
            //Stop the animation
            scaleTransition.stop();
            ft.stop();

            //If the player is dead, end the game
            if(game.getLives().get() == -1){
                endGame();
                return;
            }

            //Else, reset and play the animation
            timeBar.setWidth(gameWindow.getWidth()-8);
            ft.setDuration(Duration.millis(game.getTimerDelay()));
            scaleTransition.setDuration(Duration.millis(game.getTimerDelay()));
            ft.play();
            scaleTransition.play();
        });

        //Set listeners
        board.setOnBlockClick(this::blockClicked);
        pieceBoard.setOnBlockClick(this::currentBoardClicked);
        nextPieceBoard.setOnBlockClick(this::nextBoardClicked);
    }


    /**
     * Handles when a block is clicked on the first piece board, which rotates the first piece and redraws the value
     * @param gameblock
     */
    private void currentBoardClicked(GameBlock gameblock){
        logger.info("Rotating pieces");
        game.rotateCurrentPiece();
        pieceBoard.displayPiece(game.getCurrentPiece());
    }


    /**
     * Handles when a block is clicked on the second piece board, which swaps the pieces then redraws the values
     * @param gameBlock the Game Block that was clicked on
     */
    private void nextBoardClicked(GameBlock gameBlock){
        logger.info("Swapping pieces");
        game.swapCurrentPiece();
        pieceBoard.displayPiece(game.getCurrentPiece());
        nextPieceBoard.displayPiece(game.getFollowingPiece());
    }


    /**
     * Handle when a block is clicked
     *
     * @param gameBlock the Game Block that was clicked on
     */
    protected void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);

        if(game.getScore().get() > highScore.get()){
            highScore.set(game.getScore().get());
        }
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Ends the game by loading the scores scene
     */
    private void endGame() {
        multimedia.pauseBackgroundMusic();
        gameWindow.startScore(game);
    }

    /**
     * Get the high score from a text file
     * @return high score
     */
    private int getHighScore(){
        Integer score = -1;
        logger.info("Loading scores from a file");

        //Load the text file
        File scoreFile = new File("scores.txt");
        if(!scoreFile.exists()){
            //If it does not exist, create a fake high score of 10000
            logger.error("No file was found, returning a fake score");
            return 10000;
        }

        try{
            //Load the high score
            BufferedReader reader = new BufferedReader(new FileReader(scoreFile));
            String line = reader.readLine();

            if(line != null){
                String[] splitLine = line.strip().split(":");
                score = Integer.parseInt(splitLine[1]);
            }
            reader.close();
        } catch(Exception e){
            logger.error(e.getMessage());
        }

        if(score == -1){
            //If the score didnt load properly, return a fake score of 10000
            logger.error("Score was not properly loaded. Giving a high score of 10000");
            score = 10000;
        }
        return score;
    }

}
