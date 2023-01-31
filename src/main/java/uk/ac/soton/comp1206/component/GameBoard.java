package uk.ac.soton.comp1206.component;

import java.util.Set;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of its own, for example, for displaying an upcoming block. It also is
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 *
 * @author Windows
 * @version $Id: $Id
 */
public class GameBoard extends GridPane {

    /**
     *A logger to log the status of a class
     */
    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
    protected GameBlock[][] blocks;

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;

    /**
     * The listener to call when a specific block is right-clicked
     */
    private RightClickedListener rightClickedListener;


    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     *
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        //Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with its own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols,rows);

        //Build the GameBoard
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by its row and column
     *
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}",cols,rows);

        //set the lengths of the board
        setMaxWidth(width);
        setMaxHeight(height);
        setGridLinesVisible(true);

        //Set the blocks in the board
        blocks = new GameBlock[cols][rows];
        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x,y);
            }
        }
    }

    /**
     * Call the animation to fade out a set of blocks
     *
     * @param blocks - Set of blocks to fade out
     */
    public void fadeOut(Set<GameBlockCoordinate> blocks){
        for (var block: blocks) {
            //get the position of the block
            int x = block.getX();
            int y = block.getY();

            //fade the block
            getBlock(x,y).fadeOut();
        }
    }

    /**
     * Highlight the block the keyboard is currently over
     *
     * @param aim - Position of the keyboard where, aim[0] : X coordinate, aim[1] : Y coordinate
     */
    public void highlightKeyboard(int[] aim){
        getBlock(aim[1], aim[0]).paintHighlight();
    }

    /**
     * Unhighlight the previous block the keyboard was over
     *
     * @param aim - Position of the keyboard where, aim[0] : X coordinate, aim[1] : Y coordinate
     */
    public void deHighlightKeyboard(int[] aim){
        getBlock(aim[1], aim[0]).paint();
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     *
     * @param x column
     * @param y row
     * @return the block created
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));

        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));

        //Add a mouse enter handler to the block to trigger it to be highlighted
        block.setOnMouseEntered((e) -> block.paintHighlight());

        //Add a mouse exit handler to the block to trigger it to be unhighlighted
        block.setOnMouseExited((e) -> block.paint());

        return block;
    }

    /**
     * Set the listener to handle an event when a block is clicked
     *
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Set the listener to handle an event when a block is right clicked
     *
     * @param listener listener to add
     */
    public void setOnRightClicked(RightClickedListener listener){
        this.rightClickedListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * @param event mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block);

        //If the block is on the piece board, call the appropriate listener
        if(this instanceof PieceBoard) {
            blockClickedListener.blockClicked(block);
            return;
        }

        if(event.getButton() == MouseButton.PRIMARY){
            //On left click
            blockClickedListener.blockClicked(block);
        } else if(event.getButton() == MouseButton.SECONDARY){
            //On right click
            rightClickedListener.setOnRightClicked();
        }
    }

}
