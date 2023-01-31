package uk.ac.soton.comp1206.event;

/**
 * The GameLoop Listener is used for listening to when the Game class calls the gameLoop method which updates
 * the UI
 *
 * @author Windows
 * @version $Id: $Id
 */
public interface GameLoopListener {

	/**
	 * Handle a game loop for UI
	 */
	void gameLoopListener();

}
