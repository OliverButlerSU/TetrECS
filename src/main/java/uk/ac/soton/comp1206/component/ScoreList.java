package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A ScoreList is a visual component to represent a list of scores.
 * It extends a VBox to hold a list of scores.
 *
 * @author Windows
 * @version $Id: $Id
 */
public class ScoreList extends VBox {

	/**
	 *A logger to log the status of a class
	 */
	private static final Logger logger = LogManager.getLogger(ScoreList.class);

	/**
	 * A list of scores to represent
	 */
	private final SimpleListProperty<Pair<String, Integer>> scores = new SimpleListProperty<>(
			FXCollections.observableArrayList(new ArrayList<>())
	);

	/**
	 * The title / header of the scores.
	 */
	private final String header;

	/**
	 * Holds the Text which the scores are represented by
	 */
	private final ArrayList<Text> textScoreNodes = new ArrayList<>();

	/**
	 * Used to delay the timer which animates the scores
	 */
	private int timerDelay = 1;

	/**
	 * Initialise the creation of a list of scores
	 *
	 * @param header The title / header of the scores
	 * @param UIscores A list of scores to represent
	 */
	public ScoreList(String header, SimpleListProperty<Pair<String, Integer>> UIscores){
		this.scores.bind(UIscores);
		this.header = header;
		this.setPadding(new Insets( 10,10,10,10));
		this.setAlignment(Pos.CENTER);
	}

	/**
	 * Build and display the header / title of the scores
	 */
	public void build(){
		logger.info("Adding the header of the scores");
		Text scoreHeader = new Text(header);
		scoreHeader.getStyleClass().add("title");
		this.getChildren().add(scoreHeader);
	}

	/**
	 * Add the scores to the scene. They will however not be seen until you call the reveal() method.
	 */
	public void addScores(){
		logger.info("Adding the scores to the scene");
		//For each score
		for(int i = 0; i < scores.size(); i++){
			//Get the information of the score
			int position = i+1;
			String name = scores.get().get(i).getKey();
			String score = scores.get().get(i).getValue().toString();

			//Make a Text component with this information and style it
			Text currentScore = new Text(position + ". " + name + ": " + score);
			currentScore.setVisible(false);
			textScoreNodes.add(currentScore);
			this.getChildren().add(currentScore);
			currentScore.getStyleClass().add("scorelist"+(i+1));
		}
	}

	/**
	 * Call an animation to reveal the list of scores
	 */
	public void reveal(){
		logger.info("Revealing the scores");

		//For each score
		for (Text text: textScoreNodes) {

			//Add a timer to call an animation, which increases in time
			var timer = new Timeline(new KeyFrame(Duration.millis(250*timerDelay), e -> {
				//Create a fade animation
				FadeTransition fadeTransition = new FadeTransition();
				fadeTransition.setDuration(Duration.millis(500));
				fadeTransition.setNode(text);
				fadeTransition.setFromValue(0);
				fadeTransition.setToValue(1);
				fadeTransition.play();
				text.setVisible(true);
			}));
			timerDelay = timerDelay+1;
			timer.play();
		}
	}
}
