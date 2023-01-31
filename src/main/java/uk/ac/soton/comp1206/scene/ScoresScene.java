package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.component.ScoreList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The score scene which shows a list of scores either saved in a text file or received online
 *
 * @author Windows
 * @version $Id: $Id
 */
public class ScoresScene extends BaseScene{

	/**
	 * The final game instance
	 */
	private final Game game;

	/**
	 * A logger to log the status of a class
	 */
	private static final Logger logger = LogManager.getLogger(ScoresScene.class);

	/**
	 * A media player to play music
	 */
	private final Multimedia multimedia = new Multimedia();

	/**
	 * The main pane of the window
	 */
	private final BorderPane mainPane = new BorderPane();

	/**
	 * Boolean to check if the user can exit the menu
	 */
	private boolean canExitMenu = true;

	/**
	 * Boolean to see if the user got a high score
	 */
	private boolean gotHighScore = false;

	/**
	 * A list of pairs to represent local scores
	 */
	private SimpleListProperty<Pair<String,Integer>> localScores = new SimpleListProperty<>(
			FXCollections.observableArrayList(new ArrayList<>())
	);

	/**
	 * A list of pairs to represent online scores
	 */
	private SimpleListProperty<Pair<String,Integer>> remoteScores = new SimpleListProperty<>(
			FXCollections.observableArrayList(new ArrayList<>())
	);

	/**
	 * A communicator to communicate to a server
	 */
	private final Communicator communicator;

	/**
	 * Create a new scene, passing in the GameWindow the scene will be displayed in
	 *
	 * @param gameWindow the current window
	 * @param game the final game state
	 * @param communicator the communicator
	 */
	public ScoresScene(GameWindow gameWindow, Game game, Communicator communicator) {
		super(gameWindow);
		this.game = game;
		this.communicator = communicator;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Initialise the scene
	 */
	@Override
	public void initialise() {
		if(canExitMenu){
			//If the user is allowed to exit the scene
			scene.setOnKeyPressed((event -> {
				if(event.getCode() == KeyCode.ESCAPE){
					//If escape is pressed, open the start menu
					multimedia.pauseBackgroundMusic();
					gameWindow.startMenu();
				}
			}));
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Build the scene
	 */
	@Override
	public void build() {
		logger.info("Building " + this.getClass().getName());
		multimedia.playBackgroundMusic("menu.mp3");

		//Create the main window
		root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
		StackPane scorePane = new StackPane();
		scorePane.setMaxWidth(gameWindow.getWidth());
		scorePane.setMaxHeight(gameWindow.getHeight());
		scorePane.getStyleClass().add("scores-background");
		root.getChildren().add(scorePane);
		scorePane.getChildren().add(mainPane);

		//Add the TetrECS.png image to the screen
		Image image = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());
		ImageView imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(image.getWidth()/12);
		imageView.setFitWidth(image.getWidth()/12);

		//Add a game over title
		Text gameOver = new Text("GAME OVER");
		gameOver.getStyleClass().add("bigtitle");

		//Add a VBox to hold the title and image
		VBox imageBox = new VBox(imageView, gameOver);
		imageBox.setPadding(new Insets(4,4,4,4));
		imageBox.setAlignment(Pos.TOP_CENTER);
		mainPane.setTop(imageBox);

		//Load the local scores and sort them
		loadScores();
		localScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));

		if(checkHighScore(game.getScore().get())){
			//If the user got a high score
			gotHighScore = true;

			//Vbox to hold the buttons and TextField
			VBox inputFields = new VBox();
			inputFields.setAlignment(Pos.CENTER);
			inputFields.setPadding(new Insets(4,4,4,4));

			//TetField to input name
			TextField scoreField = new TextField();
			scoreField.getStyleClass().add("TextField");
			scoreField.setPromptText("Input your username. It must be alphanumeric and less than 11 characters");

			//Confirm button to verify the name is valid
			Button confirmButton = new Button("CONFIRM");
			inputFields.getChildren().addAll(scoreField, confirmButton);
			mainPane.setCenter(inputFields);

			//When clicking the button
			confirmButton.setOnAction((event -> {
				//Check if the name is valid (between 1-10 characters and is alphabetical)
				String input = scoreField.getText();
				if(input.matches("[a-zA-Z]+") && input.length() > 0 && input.length() < 11){
					//Update the new high scores
					updateHighScore(input, game.getScore().get());
					writeScores(localScores);

					//Remove the TextFields and Button
					inputFields.getChildren().removeAll(scoreField,confirmButton);
					mainPane.getChildren().remove(inputFields);

					//Display the scores
					displayScores();

					//Request for a new highscore
					requestNewHighScore(input, game.getScore().get());

					//Add a listener to handle incoming online high scores
					communicator.addListener((message) -> Platform.runLater(()->{
						if(message.startsWith("HISCORES")){
							loadOnlineScores(message);
						}
					}));

					//Add a listener to handle incoming new scores
					communicator.addListener((message) -> Platform.runLater(() ->{
						if(message.startsWith("NEWSCORE")){
							writeOnlineScores(message);
						}
					}));
				}
			}));
		} else{
			//Write the scores back and display them
			//Add a listener to handle incoming online high scores
			communicator.addListener((message) -> Platform.runLater(()->{
				if(message.startsWith("HISCORES")){
					loadOnlineScores(message);
				}
			}));

			//Add a listener to handle incoming new scores
			communicator.addListener((message) -> Platform.runLater(() ->{
				if(message.startsWith("NEWSCORE")){
					writeOnlineScores(message);
				}
			}));

			writeScores(localScores);
			displayScores();
		}
	}

	/**
	 * Display either the local or multiplayer scores
	 */
	private void displayScores(){
		canExitMenu = true;
		if(game.getMultiScores() == null){
			//If there are no multiplayer scores, draw the local scores
			ScoreList localScoreList = new ScoreList("Local Scores:", localScores);

			localScoreList.build();
			localScoreList.addScores();
			localScoreList.reveal();

			mainPane.setLeft(localScoreList);
		} else{
			//If there are multiplayer scores, draw them
			ScoreList multiScoreList = new ScoreList("Multi Scores:", loadMultiScores(game.getMultiScores()));
			multiScoreList.build();
			multiScoreList.addScores();
			multiScoreList.reveal();

			mainPane.setLeft(multiScoreList);
		}

		//Request online scores
		requestOnlineScores();
	}

	/**
	 * Display the online scores
	 */
	private void displayOnlineScore(){
		//Draw the onlines scores and place on the right
		ScoreList onlineScoreList = new ScoreList("Online Scores:", remoteScores);

		onlineScoreList.build();
		onlineScoreList.addScores();
		onlineScoreList.reveal();

		mainPane.setRight(onlineScoreList);
	}

	/**
	 * Get a message and turn it into a list of scores
	 * @param message message of Online scores
	 * @return List of scores
	 */
	private SimpleListProperty<Pair<String,Integer>> loadMultiScores(String message){
		SimpleListProperty<Pair<String,Integer>> multiScores = new SimpleListProperty<>(
				FXCollections.observableArrayList(new ArrayList<>())
		);

		//Format the message
		message = message.replaceAll("SCORES ", "");
		String[] playersUnedited = message.split("\\r?\\n");

		//For each player
		for(int i = 0; i < playersUnedited.length && i < 10; i++){
			String[] playerInfo = playersUnedited[i].split(":");

			//Get the player information
			String name = playerInfo[0];
			Integer score = Integer.parseInt(playerInfo[1]);

			//Create a new pair and add it
			Pair<String, Integer> player = new Pair<>(name, score);
			multiScores.add(player);
		}

		//Sort the scores
		multiScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
		return multiScores;
	}

	/**
	 * Load scores from scores.txt (a local text file which holds the scores)
	 */
	private void loadScores(){
		logger.info("Loading scores from a file");

		//Create the file
		File scoreFile = new File("scores.txt");
		if(!scoreFile.exists()){
			//If the file does not exist, create fake scores
			logger.error("No file was found, returning a fake score list");
			for(int i = 10; i > 0; i--){
				localScores.add(new Pair<>("Oliver", i*1000));
			}
		}
		try{
			//Read the file
			BufferedReader reader = new BufferedReader(new FileReader(scoreFile));
			String line = reader.readLine();

			//For each line
			while(line != null){
				//Format the line
				String[] splitLine = line.strip().split(":");

				//Get the name and score and add it to the localScores
				String name = splitLine[0];
				Integer score = Integer.parseInt(splitLine[1]);
				localScores.add(new Pair<>(name, score));
				line = reader.readLine();
			}
			reader.close();
		} catch(Exception e){
			logger.error(e.getMessage());
		}

		//If there are too many scores, or too little (0), make a new list.
		if(localScores.size() == 0 || localScores.size() > 10 ){
			logger.error("Error in getting scores. The size of loaded scores was " + localScores.size());
			localScores = new SimpleListProperty<>(
					FXCollections.observableArrayList(new ArrayList<>())
			);
			for(int i = 1; i < 11; i++){
				localScores.add(new Pair<>("Oliver",i*1000));
			}
		}
	}

	/**
	 * Write a list of scores to a text file
	 * @param scores list of scores
	 */
	private void writeScores(ListProperty<Pair<String,Integer>> scores){
		File scoreFile = new File("scores.txt");
		logger.info("Writing the scores to file");

		try{
			//Open the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(scoreFile, false));

			//For each score
			for (Pair<String, Integer> score : scores) {
				//Get the values and write it
				String name = score.getKey();
				String value = score.getValue().toString();
				writer.write(name + ":" + value + "\n");
			}
			writer.close();
		} catch(Exception e){
			logger.error(e.getMessage());
		}
	}

	/**
	 * Check if the score is a local high score
	 * @param score score achieved
	 * @return if the score is a high score
	 */
	private boolean checkHighScore(Integer score){
		logger.info("Checking if score was a high score");
		for(int i = 0; i < localScores.getSize(); i++){
			if(localScores.get(i).getValue() < score){
				logger.info("Score was a high score");
				return true;
			}
		}
		logger.info("Score was not a high score");
		return false;
	}

	/**
	 * Update the high score
	 * @param name name of user
	 * @param score score of user
	 */
	private void updateHighScore(String name, Integer score){
		//Create a new pair
		localScores.add(new Pair<>(name,score));

		//Sort the list
		localScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));

		//Remove the last (smallest score) as it is no longer a top 10 score
		localScores.remove(localScores.getSize()-1);
	}

	/**
	 * Format a message to get online scores
	 * @param message list of online scores
	 */
	private void loadOnlineScores(String message){
		try{
			//Format message
			message = message.replaceAll("HISCORES ", "");
			String[] players = message.split("\\r?\\n");

			//For each player
			for (String player: players) {
				//Get their information and add it
				String[] information = player.split(":");
				String name = information[0];
				Integer score = Integer.parseInt(information[1]);
				remoteScores.add(new Pair<>(name, score));
			}

			if(!gotHighScore){
				//If you didnt get a high score, display them
				displayOnlineScore();
			}
		} catch (Exception e){
			logger.error(e.getMessage());
		}
	}

	/**
	 * Receive a message when requesting a new score
	 * @param message new score
	 */
	private void writeOnlineScores(String message){
		try{
			//Format message
			message = message.replaceAll("NEWSCORE ", "");
			String[] information = message.split(":");

			//Get the user information
			String name = information[0];
			Integer score = Integer.parseInt(information[1]);

			//Add it and sort the list
			remoteScores.add(new Pair<>(name, score));
			remoteScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
			//Remove the last element (as it is no longer top 10)
			remoteScores.remove(remoteScores.getSize()-1);

			//Display the online scores
			displayOnlineScore();
		} catch (Exception e){
			logger.error(e.getMessage());
		}
	}

	/**
	 * Request to the server the high scores
	 */
	private void requestOnlineScores(){
		communicator.send("HISCORES");
	}

	/**
	 * Request to the score a new high score achieved
	 * @param name username
	 * @param value score achieved
	 */
	private void requestNewHighScore(String name, Integer value){
		communicator.send("HISCORE "+name+":"+value);
	}

	/**
	 * Request a list of unique scores
	 */
	private void requestUniqueScores(){
		communicator.send("HISCORES UNIQUE");
	}

	/**
	 * Request a list of default scores. Used for debugging.
	 */
	private void requestDefaultScores(){
		communicator.send("HISCORES DEFAULT");
	}
}
