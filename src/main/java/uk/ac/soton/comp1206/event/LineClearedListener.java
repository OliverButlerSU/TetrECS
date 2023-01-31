package uk.ac.soton.comp1206.event;

import java.util.Set;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The LineCleared Listener is used for listening to blocks being removed from the game.
 *
 * @author Windows
 * @version $Id: $Id
 */
public interface LineClearedListener {

	/**
	 * Handle the blocks to be cleared
	 *
	 * @param blocks A set of blocks to remove
	 */
	void LineCleared(Set<GameBlockCoordinate> blocks);
}
