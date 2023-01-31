package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 *
 * @author Windows
 * @version $Id: $Id
 */
public class Game{

    /**
     *A logger to log the status of a class
     */
    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * Keeps track of the current piece
     */
    protected GamePiece currentPiece;

    /**
     * Keeps track of the following piece
     */
    protected GamePiece followingPiece;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * The score of the player
     */
    protected final IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * The level of the game
     */
    protected final IntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * The lives the player has
     */
    protected final IntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * The multiplier the player has
     */
    protected final IntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * A media player to play music and sound effects
     */
    protected final Multimedia multimedia = new Multimedia();

    /**
     * Position of the keyboard where, aim[0] : X coordinate, aim[1] : Y coordinate
     */
    protected final int[] aim = new int[2];

    /**
     * A timer used to call the game loop if it ever reaches 0
     */
    protected Timeline timer = new Timeline();

    /**
     * Used to represent the scores of the multiplayer game
     */
    private String multiScores = null;

    /**
     * Listens for when the next piece is played
     */
    protected NextPieceListener nextPieceListener;

    /**
     * Listens for when the gameLoop function is called
     */
    protected GameLoopListener gameLoopListener;

    /**
     * Listens for when a line is cleared
     */
    protected LineClearedListener lineClearedListener;


    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * End the game
     */
    public void endGame(){
        logger.info("Ending game");
        multimedia.pauseBackgroundMusic();
        timer.stop();
    }

    /**
     * A game loop which subtracts a life from the player
     */
    public void gameLoop(){
        if(lives.get() > 0){
            //If the player is alive, lose a life, get the next piece and reset the timer
            multimedia.playSoundFile("lifelose.wav");
            lives.set(lives.get()-1);
            resetTimer();
            nextPiece();
            multiplier.set(1);
        } else{
            //If the player is dead, end the game
            lives.set(lives.get()-1);
            endGame();
            gameLoopListener.gameLoopListener();
        }
    }


    /**
     * Returns a random Game Piece
     *
     * @return - GamePiece
     */
    protected GamePiece spawnPiece(){
        return GamePiece.createPiece(new Random().nextInt(0,15));
    }

    /**
     * Creates the next piece
     */
    protected void nextPiece(){
        logger.info("Getting next piece");
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * Swaps the current and next piece
     */
    public void swapCurrentPiece(){
        logger.info("Swapping pieces");
        multimedia.playSoundFile("rotate.wav");
        var cPiece = currentPiece;
        currentPiece = followingPiece;
        followingPiece = cPiece;
    }

    /**
     * Initialise a new game and create the pieces
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        resetTimer();
        currentPiece = spawnPiece();
        followingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * Handle what should happen when a particular block is clicked
     *
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        logger.info("Clicked block");
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        //If you cant play the piece
        if(!grid.canPlayPiece(currentPiece, x, y)){
            multimedia.playSoundFile("fail.wav");
           return;
        }

        //Play the piece and reset the timer
        grid.playPiece(currentPiece, x, y);
        resetTimer();

        //Get the next pieces
        multimedia.playSoundFile("place.wav");
        nextPiece();
        afterPiece();
    }

    /**
     * Reset the game loop timer
     */
    public void resetTimer(){
        logger.info("Resetting timer to " + getTimerDelay() + " ms");
        timer.stop();

        //Make a timer which gets its delay from the getTimerDelay function
        timer = new Timeline(new KeyFrame(Duration.millis(getTimerDelay()), e -> {
            logger.info("Out of time");
            gameLoop();
        }));

        //Call the listener to update UI
        gameLoopListener.gameLoopListener();
        timer.play();
    }

    /**
     * Rotate the current piece
     */
    public void rotateCurrentPiece(){
        logger.info("Rotating piece");
        currentPiece.rotate();
        multimedia.playSoundFile("rotate.wav");
    }

    /**
     * Counter rotate the current piece
     */
    public void counterRotateCurrentPiece() {
        logger.info("Counter rotating piece");
        currentPiece.rotate();
        currentPiece.rotate();
        currentPiece.rotate();
        multimedia.playSoundFile("rotate.wav");
    }

    /**
     * Calculates if any lines are cleared
     */
    protected void afterPiece(){
        logger.info("Checking for line clears");
        //Clear the lines and calculate the score
        Set<int[]> blocks = new HashSet<>();
        ArrayList<int[]> linesToClear = new ArrayList<>();
        Set<GameBlockCoordinate> coordinates = new HashSet<>();


        //Check for horizontal lines
        for(int i = 0; i < cols; i++){
            int count = 0;
            for(int j = 0; j< rows; j++){
                //If there is no piece (meaning there the line is cleared), continue
                if(grid.get(i,j)==0){
                    continue;
                }
                count++;
            }
            //If the row is clearable
            if(count == rows){
                for(int j =0; j < rows; j++){
                    //Add a position to clear
                    blocks.add(new int[] {i,j});
                }
                //0 specifies it is a horizontal line
                linesToClear.add(new int[]{i,0});
            }
        }

        //Check for vertical lines
        for(int i = 0; i < rows; i++){
            int count = 0;
            for(int j = 0; j< cols; j++){
                //If there is no piece (meaning there the line is cleared), continue
                if(grid.get(j,i)!=0){
                    count++;
                }
            }
            //If the row is clearable
            if(count == cols){
                for(int j =0; j < cols; j++){
                    blocks.add(new int[] {j,i});
                }
                //1 specifies it is a vertical line
                linesToClear.add(new int[]{i,1});
            }
        }

        //Remove the lines (replace with 0)
        for (int[] position : linesToClear){
            if(position[1] == 0){
                //horizontal
                for(int i = 0; i < rows; i++){
                    coordinates.add(new GameBlockCoordinate(position[0], i));
                    grid.set(position[0],i,0);
                }
            } else{
                //vertical
                for(int i = 0; i < cols; i++){
                    grid.set(i,position[0],0);
                    coordinates.add(new GameBlockCoordinate(i,position[0]));
                }
            }
        }

        //Set the new score
        score.set(score.get() + score(linesToClear.size(), blocks.size()));

        //If the game cleared lines
        if(linesToClear.size() > 0){
            //Call the listener and increase the multiplier
            lineClearedListener.LineCleared(coordinates);
            logger.info("Cleared "+ linesToClear.size() + " lines");
            multimedia.playSoundFile("clear.wav");
            multiplier.set(multiplier.getValue()+1);
        } else{
            //Reset the multiplier back to 1
            multiplier.set(1);
        }

        //Set the level, which plays a sound on level up
        int prevLevel = level.get();
        level.set(Math.floorDiv(score.getValue(), 1000));
        if(prevLevel != level.get()){
            multimedia.playSoundFile("level.wav");
        }
    }

    /**
     * Calculate the score
     *
     * @param lines the number of lines cleared
     * @param blocks the number of blocks cleared
     * @return the score calculated
     */
    protected int score(int lines, int blocks){
        return lines * blocks * 10 * multiplier.get();
    }

    /**
     * Calculate the time to place a block
     *
     * @return the time
     */
    public int getTimerDelay(){
        return Math.max(2500, 12000 - 500 * level.get());
    }

    /**
     * Move the keyboard aim down
     */
    public void downAim(){
        if(aim[0]+1 < rows){
            aim[0]++;
        }
    }

    /**
     * Move the keyboard aim up
     */
    public void upAim(){
        if(aim[0]>0){
            aim[0]--;
        }
    }

    /**
     * Move the board aim right
     */
    public void rightAim(){
        if(aim[1]+1 < cols) {
            aim[1]++;
        }
    }

    /**
     * Move the board aim left
     */
    public void leftAim(){
        if(aim[1]>0){
            aim[1]--;
        }
    }

    /**
     * Keyboard input to place a piece
     */
    public void placeAim(){
        int x = aim[1];
        int y = aim[0];

        //If you cant play the current piece, return
        if(!grid.canPlayPiece(currentPiece, x, y)){
            multimedia.playSoundFile("fail.wav");
            return;
        }

        //Play the piece, get the next pieces, and reset the timer
        resetTimer();
        grid.playPiece(currentPiece, x, y);
        multimedia.playSoundFile("place.wav");
        nextPiece();
        afterPiece();
    }

    /**
     * Set the lineClearedListener to a new listener
     *
     * @param lineClearedListener the listener
     */
    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * Set the lineNextPieceListener to a new listener
     *
     * @param nextPieceListener the listener
     */
    public void setNextPieceListener(NextPieceListener nextPieceListener){
        this.nextPieceListener = nextPieceListener;
    }

    /**
     * Set the gameLoopListener to a new listener
     *
     * @param gameLoopListener the listener
     */
    public void setGameLoopListener(GameLoopListener gameLoopListener){
        this.gameLoopListener = gameLoopListener;
    }


    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the current piece
     *
     * @return GamePiece - currentPiece
     */
    public GamePiece getCurrentPiece(){
        return currentPiece;
    }

    /**
     * Get the following piece
     *
     * @return GamePiece - followingPiece
     */
    public GamePiece getFollowingPiece(){
        return followingPiece;
    }

    /**
     * Set the current Piece
     *
     * @param currentPiece piece to change to
     */
    public void setCurrentPiece(GamePiece currentPiece) {
        this.currentPiece = currentPiece;
    }

    /**
     * Set the following Piece
     *
     * @param followingPiece piece to change to
     */
    public void setFollowingPiece(GamePiece followingPiece) {
        this.followingPiece = followingPiece;
    }

    /**
     * Get the level
     *
     * @return IntegerPropety - Level
     */
    public IntegerProperty getLevel() {
        return level;
    }

    /**
     * Get the lives
     *
     * @return IntegerProperty - Lives
     */
    public IntegerProperty getLives() {
        return lives;
    }

    /**
     * Get the multiplier
     *
     * @return IntegerProperty - Multiplier
     */
    public IntegerProperty getMultiplier() {
        return multiplier;
    }

    /**
     * Get the score
     *
     * @return IntegerProperty - Score
     */
    public IntegerProperty getScore() {
        return score;
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the position of the keyboard
     *
     * @return int[] aim where: aim[0] : X coordinate, aim[1] : Y coordinate
     */
    public int[] getAim(){
        return aim;
    }

    /**
     * Get the multiplayer scores
     *
     * @return String - multi Scores
     */
    public String getMultiScores() {
        return multiScores;
    }

    /**
     * Set the multi scores
     *
     * @param multiScores - scores to change to
     */
    public void setMultiScores(String multiScores){
        this.multiScores = multiScores;
    }
}
