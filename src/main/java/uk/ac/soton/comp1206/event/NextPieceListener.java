package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The NextPiece Listener is used for listening to when pieces are updated
 *
 * @author Windows
 * @version $Id: $Id
 */
public interface NextPieceListener {

	/**
	 * Handle the next pieces to display
	 *
	 * @param gamePiece the current piece to display
	 * @param followingPiece the following piece to display
	 */
	void nextPiece(GamePiece gamePiece, GamePiece followingPiece);

}
