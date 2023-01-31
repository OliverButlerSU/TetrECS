package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;


/**
 * A PieceBoard is a visual component to represent the visual PieceBoard.
 * It extends a GameBoard which extends a GridPane to hold a grid of GameBlocks.
 *
 *
 * The PieceBoard can hold an internal grid of its own which is used to display a piece
 *
 * The PieceBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 *
 * @author Windows
 * @version $Id: $Id
 */
public class PieceBoard extends GameBoard{

	/**
	 *A logger to log the status of a class
	 */
	private static final Logger logger = LogManager.getLogger(GameBoard.class);

	/**
	 * Create a new PieceBoard with its own internal grid, specifying the number of columns and rows, along with the
	 * visual width and height.
	 *
	 * @param cols number of columns for internal grid
	 * @param rows number of rows for internal grid
	 * @param width the visual width
	 * @param height the visual height
	 */
	public PieceBoard(int cols, int rows, double width, double height) {
		super(cols, rows, width, height);
	}

	/**
	 * Displays a piece on the board corresponding to the piece passed
	 *
	 * @param gamePiece Piece to display
	 */
	public void displayPiece(GamePiece gamePiece){
		logger.info("Displaying piece on PieceBoard");
		//If there is no piece to display, return
		if(gamePiece == null){
			return;
		}

		//For each block in the piece, set the amount into the grid such it is displayed
		int[][] pieceBlocks = gamePiece.getBlocks();
		for(int i = 0; i < pieceBlocks.length; i++) {
			for (int j = 0; j < pieceBlocks.length; j++) {
				grid.set(i, j, pieceBlocks[i][j]);

				//Set the listeners such to do nothing when clicked
				blocks[i][j].setOnMouseEntered((e) ->{
					//Nothing -> To ensure the board does not get highlighted
				});
				blocks[i][j].setOnMouseExited((e) ->{
					//Nothing -> To ensure the board does not get highlighted
				});
			}
		}
		//Paint the middle block
		blocks[1][1].paintMiddle();
	}
}
