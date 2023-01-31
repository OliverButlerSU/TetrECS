package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Introduction Scene plays an animation. After 8 seconds or escape is pressed, it loads MenuScene
 *
 * @author Windows
 * @version $Id: $Id
 */
public class IntroScene extends BaseScene{

	/**
	 * A logger to log the status of a class
	 */
	private static final Logger logger = LogManager.getLogger(MenuScene.class);

	/**
	 * Media player to play music
	 */
	private final Multimedia multimedia = new Multimedia();

	/**
	 * A timer to track how long the scene has been loaded, and load the next scene after 8 seconds
	 */
	private Timeline introTimer;

	/**
	 * Create a new scene, passing in the GameWindow the scene will be displayed in
	 *
	 * @param gameWindow the game window
	 */
	public IntroScene(GameWindow gameWindow) {
		super(gameWindow);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Initialise the scene
	 */
	@Override
	public void initialise() {
		scene.setOnKeyPressed((event -> {
			//If escape is pressed
			if(event.getCode() == KeyCode.ESCAPE){
				//Load the menu scene and stop the timer
				introTimer.stop();
				gameWindow.startMenu();
			}
		}));
	}

	/**
	 * {@inheritDoc}
	 *
	 * Build the Introduction scene
	 */
	@Override
	public void build() {
		logger.info("Building " + this.getClass().getName());
		multimedia.playSoundFile("intro.mp3");

		//Create the window
		root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
		var introPane = new StackPane();
		var mainPane = new BorderPane();
		introPane.setMaxWidth(gameWindow.getWidth());
		introPane.setMaxHeight(gameWindow.getHeight());
		introPane.getStyleClass().add("intro");
		root.getChildren().add(introPane);

		//Add the ECSGames.png image to the middle of the screen
		Image image = new Image(getClass().getResource("/images/ECSGames.png").toExternalForm());
		ImageView imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(500);
		imageView.setFitWidth(500);
		mainPane.setCenter(imageView);
		introPane.getChildren().add(mainPane);

		//Create a fade transition animation which is set to the image for 4 seconds
		FadeTransition fadeTransition = new FadeTransition();
		fadeTransition.setDuration(Duration.seconds(4));
		fadeTransition.setNode(imageView);
		fadeTransition.setFromValue(0);
		fadeTransition.setToValue(1);
		fadeTransition.play();

		//Play a timer for 6 seconds which when finishes, loads the start menu
		introTimer = new Timeline(new KeyFrame(Duration.seconds(6), e-> gameWindow.startMenu()));
		introTimer.play();
	}


}
