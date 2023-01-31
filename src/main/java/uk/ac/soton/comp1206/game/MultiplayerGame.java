package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.PiecePlacedListener;

/**
 * The MultiplayerGame class handles the main logic, state and properties of the TetrECS game for Multiplayer.
 * Methods to manipulate the game state and to handle actions made by the player should take place inside this class.
 *
 * The Class extends Game but implements some new methods to work with multiplayer
 *
 * @author Windows
 * @version $Id: $Id
 */
public class MultiplayerGame extends Game{

	/**
	 * Listens for when a piece is successfully placed on the board
	 */
	private PiecePlacedListener piecePlacedListener;

	/**
	 *A logger to log the status of a class
	 */
	private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

	/**
	 * Create a new game with the specified rows and columns. Creates a corresponding grid model.
	 *
	 * @param cols number of columns
	 * @param rows number of rows
	 */
	public MultiplayerGame(int cols, int rows) {
		super(cols, rows);
	}

	/**
	 * {@inheritDoc}
	 *
	 * A game loop which subtracts a life from the player
	 */
	@Override
	public void gameLoop(){
		if(lives.get() > 0){
			//If the player is still alive, lose a life, get the next piece and reset the timer
			multimedia.playSoundFile("lifelose.wav");
			lives.set(lives.get()-1);
			resetTimer();
			multiplier.set(1);
			piecePlacedListener.piecePlacedListener();
		} else{
			//If the player is dead, end the game
			lives.set(lives.get()-1);
			endGame();
			gameLoopListener.gameLoopListener();
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 * Handle what should happen when a particular block is clicked
	 */
	@Override
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
		multimedia.playSoundFile("place.wav");
		afterPiece();

		//Get the next piece
		if(piecePlacedListener != null){
			piecePlacedListener.piecePlacedListener();
		}
		afterPiece();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Keyboard input to place a piece
	 */
	@Override
	public void placeAim(){
		//Get the position of this block
		int x = aim[1];
		int y = aim[0];

		//If you cant play the piece
		if(!grid.canPlayPiece(currentPiece, x, y)){
			multimedia.playSoundFile("fail.wav");
			return;
		}

		//Play the piece and reset the timer
		resetTimer();
		grid.playPiece(currentPiece, x, y);
		multimedia.playSoundFile("place.wav");


		if(piecePlacedListener != null){
			piecePlacedListener.piecePlacedListener();
		}
		//Get the next piece
		afterPiece();
	}


	/**
	 * {@inheritDoc}
	 *
	 * Initialise a new game and set up anything that needs to be done at the start
	 */
	@Override
	public void initialiseGame() {
		logger.info("Initialising game");
		resetTimer();
		nextPieceListener.nextPiece(currentPiece,followingPiece);
	}

	/**
	 * Set the piecePlacedListener to a new listener
	 *
	 * @param piecePlacedListener the new listener
	 */
	public void setPiecePlacedListener(PiecePlacedListener piecePlacedListener) {
		this.piecePlacedListener = piecePlacedListener;
	}
}
