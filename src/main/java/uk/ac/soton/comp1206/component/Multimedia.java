package uk.ac.soton.comp1206.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A MediaPlayer which plays audio from a resource file
 *
 * @author Windows
 * @version $Id: $Id
 */
public class Multimedia {

	/**
	 * The media player to primarily play sound effects
	 */
	private static MediaPlayer mediaPlayer;

	/**
	 * A background music media player
	 */
	private static MediaPlayer backgroundPlayer;

	/**
	 * A logger to log the status of a class
	 */
	private static final Logger logger = LogManager.getLogger(Multimedia.class);

	/**
	 * Tracks whether the audio is enabled or not
	 */
	private static SimpleBooleanProperty audioEnabled = new SimpleBooleanProperty(true);

	/**
	 * Play a sound file from the resources sound directory
	 *
	 * @param file the file to play
	 */
	public void playSoundFile(String file){
		if(!audioEnabled.get()) return;

		//Set the audio file path
		String toPlay = Multimedia.class.getResource("/sounds/"+file).toExternalForm();
		logger.info("Playing audio: " + toPlay);

		try{
			//Play the music
			Media play = new Media(toPlay);
			mediaPlayer = new MediaPlayer(play);
			mediaPlayer.play();
		} catch(Exception e){
			audioEnabled.set(false);
			e.printStackTrace();;
			logger.error("Unable to play audio, disabling audio");
		}
	}

	/**
	 * Play a music file from the resources music directory
	 *
	 * @param file the file to play
	 */
	public void playBackgroundMusic(String file){
		if(!audioEnabled.get()) return;

		//Set the audio file path
		String toPlay = Multimedia.class.getResource("/music/"+file).toExternalForm();
		logger.info("Playing audio: " + toPlay);

		try{
			//Play the music
			Media play = new Media(toPlay);
			backgroundPlayer = new MediaPlayer(play);
			backgroundPlayer.play();
			//Loop media
			backgroundPlayer.setOnEndOfMedia(() -> {
				backgroundPlayer.seek(Duration.ZERO);
				backgroundPlayer.play();
			});
		} catch(Exception e){
			audioEnabled.set(false);
			e.printStackTrace();;
			logger.error("Unable to play audio, disabling audio");
		}
	}

	/**
	 * Get the audio enabled property
	 *
	 * @return audioEnabled value
	 */
	public BooleanProperty audioEnabledProperty(){
		return audioEnabled;
	}

	/**
	 * Pause the backround music
	 */
	public void pauseBackgroundMusic(){
		backgroundPlayer.seek(Duration.ZERO);
		backgroundPlayer.pause();
	}
}
