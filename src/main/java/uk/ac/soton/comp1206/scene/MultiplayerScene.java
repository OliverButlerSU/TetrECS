package uk.ac.soton.comp1206.scene;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Multiplayer challenge scene. Holds the UI for the multiplayer challenge mode in the game.
 *
 * @author Windows
 * @version $Id: $Id
 */
public class MultiplayerScene extends ChallengeScene {

	/**
	 * A logger to keep track of the status of the class
	 */
	private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

	/**
	 * Multimedia to play audio
	 */
	private final Multimedia multimedia = new Multimedia();

	/**
	 * The username of the current player
	 */
	private final String username;

	/**
	 * A communicator to send and receive information to the server
	 */
	private final Communicator communicator;

	/**
	 * The game which the UI is tracking
	 */
	private MultiplayerGame multiGame;

	/**
	 * The text of the all player information
	 */
	private TextFlow playerInformationText;

	/**
	 * The ScrollPane which holds the playerInformationText
	 */
	private ScrollPane playerInformationBox;

	/**
	 * The Text representing any received messages
	 */
	private Text receivedMessage;

	/**
	 * A TextField to send messages
	 */
	private TextField sendMessage;

	/**
	 * A ScrollPane to scroll through the message
	 */
	private ScrollPane messageScroller;

	/**
	 * A queue representing the next pieces to use
	 */
	private Queue<GamePiece> nextnextPiece = new LinkedList<>();

	/**
	 * reate a new scene, passing in the GameWindow the scene will be displayed in
	 *
	 * @param gameWindow the game window
	 * @param communicator the communicator to communicate with the server
	 * @param username the username of the player
	 */
	public MultiplayerScene(GameWindow gameWindow, Communicator communicator, String username) {
		super(gameWindow);
		this.communicator = communicator;
		this.username = username;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Initialise the game and start the game
	 */
	@Override
	public void initialise() {
		logger.info("Initialising Challenge");

		//Send a message to retrieve the scores
		communicator.send("SCORES");

		//Add a listener to listen for when a piece is sent
		communicator.addListener((message) -> Platform.runLater(() -> {
			if (message.startsWith("PIECE")) {
				setNextPiece(message);
			}
		}));

		//Add a listener to listen for when a score is received
		communicator.addListener((message) -> Platform.runLater(() -> {
			if (message.startsWith("SCORE ")) {
				communicator.send("SCORES");
			}
		}));

		//Add a listener to listen for when a list of scores is received
		communicator.addListener((message) -> Platform.runLater(() -> {
			if (message.startsWith("SCORES")) {
				updatePlayerInformation(message);
			}
		}));

		//Add a listener to listen for incoming messages
		communicator.addListener((message) -> Platform.runLater(() -> {
			if (message.startsWith("MSG")) {
				updateReceivedMessage(message);
			}
		}));

		//Add a listener to listen for when a player dies
		communicator.addListener((message) -> Platform.runLater(() ->{
			if(message.startsWith("DIE")) {
				communicator.send("SCORES");
			}
		}));

		//Start the game
		multiGame.start();
		scene.setOnKeyPressed((event -> {
			switch (event.getCode()) {
				case ESCAPE:
					//Pressing escape ends the game and goes back to the start menu
					multimedia.pauseBackgroundMusic();
					multiGame.endGame();
					communicator.send("DIE");
					gameWindow.startMenu();
					break;
				case UP: case W:
					//Pressing the up arrow or W changes the position of the keyboard in the game and highlights a piece
					board.deHighlightKeyboard(multiGame.getAim());
					multiGame.upAim();
					board.highlightKeyboard(multiGame.getAim());
					break;
				case DOWN: case S:
					//Pressing the down arrow or S changes the position of the keyboard in the game and highlights a piece
					board.deHighlightKeyboard(multiGame.getAim());
					multiGame.downAim();
					board.highlightKeyboard(multiGame.getAim());
					break;
				case LEFT: case A:
					//Pressing the left arrow or A changes the position of the keyboard in the game and highlights a piece
					board.deHighlightKeyboard(multiGame.getAim());
					multiGame.leftAim();
					board.highlightKeyboard(multiGame.getAim());
					break;
				case RIGHT: case D:
					//Pressing the right arrow or D changes the position of the keyboard in the game and highlights a piece
					board.deHighlightKeyboard(multiGame.getAim());
					multiGame.rightAim();
					board.highlightKeyboard(multiGame.getAim());
					break;
				case ENTER: case X:
					//Pressing the up enter or X places the piece
					multiGame.placeAim();
					break;
				case SPACE: case R:
					//Pressing space or R swaps the two pieces
					logger.info("Swapping pieces");
					multiGame.swapCurrentPiece();
					pieceBoard.displayPiece(multiGame.getCurrentPiece());
					nextPieceBoard.displayPiece(multiGame.getFollowingPiece());
					break;
				case OPEN_BRACKET: case Q: case Z:
					//Pressing [ or Q or Z counter rotates the piece
					logger.info("Rotating pieces");
					multiGame.counterRotateCurrentPiece();
					pieceBoard.displayPiece(multiGame.getCurrentPiece());
					break;
				case E: case C: case CLOSE_BRACKET:
					//Pressing E or C or ] rotates clockwise the piece
					logger.info("Rotating pieces");
					multiGame.rotateCurrentPiece();
					pieceBoard.displayPiece(multiGame.getCurrentPiece());
					break;
				case T:
					sendMessage.setVisible(true);
			}
		}));

		//Send to the communicator to get pieces
		communicator.send("PIECE");
		communicator.send("PIECE");
		communicator.send("PIECE");
	}

	/**
	 * {@inheritDoc}
	 *
	 * Build the Multiplayer window
	 */
	@Override
	public void build() {
		logger.info("Building " + this.getClass().getName());
		setupGame();

		//Create the main window
		root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
		multimedia.playBackgroundMusic("game.wav");
		var challengePane = new StackPane();
		challengePane.setMaxWidth(gameWindow.getWidth());
		challengePane.setMaxHeight(gameWindow.getHeight());
		challengePane.getStyleClass().add("challenge-background");
		root.getChildren().add(challengePane);
		var mainPane = new BorderPane();
		challengePane.getChildren().add(mainPane);

		//Create a VBox to hold the board information and title
		var boardBox = new VBox();
		boardBox.setAlignment(Pos.CENTER);
		boardBox.setMaxWidth(gameWindow.getWidth()/1.8);

		//Create the title and style
		var title = new Text("Multiplayer Mode");
		title.getStyleClass().add("title");

		//Create the board
		board = new GameBoard(multiGame.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);

		//Create a TextField to send messages
		sendMessage = new TextField();
		sendMessage.getStyleClass().add("TextField");
		sendMessage.setPromptText("Send a message:");
		sendMessage.maxWidth(board.getWidth());
		sendMessage.setVisible(false);

		//Create Text which represents received messages
		receivedMessage = new Text("Press 't' to send a message");
		receivedMessage.getStyleClass().add("messages");
		receivedMessage.getStyleClass().add("messages Text");
		receivedMessage.setTextAlignment(TextAlignment.CENTER);

		//Create a ScrollPane which holds the received messages
		messageScroller = new ScrollPane();
		messageScroller.getStyleClass().add("scroller");
		messageScroller.setContent(receivedMessage);
		messageScroller.setFitToWidth(true);
		messageScroller.setFitToHeight(true);

		//Add them to the scene
		boardBox.getChildren().addAll(title, board, sendMessage, messageScroller);

		//Set when typing a message and pressing enter, to send the message the server
		sendMessage.setOnAction((e) ->{
			communicator.send("MSG "+ sendMessage.getText());
			sendMessage.clear();
			sendMessage.setVisible(false);
		});
		mainPane.setCenter(boardBox);

		//Create Text to represent player information
		var livesText = new Text();
		var scoreText = new Text();

		//Add styling
		livesText.getStyleClass().add("lives");
		scoreText.getStyleClass().add("score");

		//Text information
		livesText.textProperty().bind(multiGame.getLives().asString());
		scoreText.textProperty().bind(multiGame.getScore().asString());

		//Create a VBox to hold the information
		var informationBox = new VBox();
		informationBox.setPadding(new Insets(4, 4, 4, 4));
		informationBox.setAlignment(Pos.CENTER);

		//Add titles and style
		var scoreTitle = new Text(username);
		scoreTitle.getStyleClass().add("heading");
		var livesTitle = new Text("Lives:");
		livesTitle.getStyleClass().add("heading");
		informationBox.getChildren()
				.addAll(scoreTitle, scoreText, livesTitle, livesText);

		//Create the TextFlow and ScrollPane to represent the scores of players
		playerInformationText = new TextFlow();
		playerInformationBox = new ScrollPane();
		playerInformationBox.setPrefHeight(200);
		playerInformationBox.getStyleClass().add("scroller");
		playerInformationBox.setPrefWidth(200);
		playerInformationBox.setMaxWidth(300);
		playerInformationBox.setFitToHeight(true);
		playerInformationBox.setPadding(new Insets(4, 4, 4, 4));
		playerInformationBox.setContent(playerInformationText);
		informationBox.getChildren().add(playerInformationBox);
		mainPane.setLeft(informationBox);

		//Create a VBox to hold the PieceBoards
		var pieceInformation = new VBox();
		pieceInformation.setPadding(new Insets(4, 4, 4, 4));
		pieceInformation.setAlignment(Pos.CENTER);

		//Create and style titles for each board
		var currentPieceText = new Text("Current Piece");
		currentPieceText.getStyleClass().add("heading");
		var nextPieceText = new Text("Next Piece");
		nextPieceText.getStyleClass().add("heading");

		//Crreate the boards
		this.pieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 8,
				gameWindow.getWidth() / 8);

		this.nextPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 12,
				gameWindow.getWidth() / 12);

		//Add listeners to display the piece when a piece is played
		multiGame.setNextPieceListener((currentPiece, followingPiece) -> {
			pieceBoard.displayPiece(currentPiece);
			nextPieceBoard.displayPiece(followingPiece);
		});

		//Add a listener to listen for when a piece is placed, and to send the board information and
		//request a new piece
		multiGame.setPiecePlacedListener(()->{
			sendBoard();
			communicator.send("PIECE");
		});

		//Add a listener to listen for when a line is cleared, updating the server with the score
		multiGame.setLineClearedListener((blockCoordinates) -> {
			board.fadeOut(blockCoordinates);
			communicator.send("SCORE " + multiGame.getScore().get());
		});

		pieceInformation.getChildren()
				.addAll(currentPieceText, pieceBoard, nextPieceText, nextPieceBoard);
		mainPane.setRight(pieceInformation);

		//Handle block on gameboard grid being clicked
		board.setOnRightClicked(() -> {
			multiGame.rotateCurrentPiece();
			pieceBoard.displayPiece(multiGame.getCurrentPiece());
		});

		//Create a rectangle to represent a timebar
		Rectangle timeBar = new Rectangle();
		timeBar.setFill(Color.GREEN);
		timeBar.setHeight(20);
		timeBar.setWidth(gameWindow.getWidth() - 8);

		//Add a holder for it
		var timeBarBox = new HBox(timeBar);
		timeBarBox.setPadding(new Insets(4, 4, 4, 4));
		mainPane.setBottom(timeBarBox);

		//Create a scale transition animation to scale it to 0
		ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(multiGame.getTimerDelay()),
				timeBar);
		scaleTransition.setInterpolator(Interpolator.LINEAR);
		scaleTransition.setFromX(1);
		scaleTransition.setToX(0);

		//Create a fill transition to change the colour of the bar
		FillTransition ft = new FillTransition(Duration.millis(multiGame.getTimerDelay()), timeBar,
				Color.GREEN, Color.RED);
		ft.setInterpolator(Interpolator.LINEAR);

		//Set the gameLoopListener to call the animation
		multiGame.setGameLoopListener(() -> {
			scaleTransition.stop();
			ft.stop();
			//If the player dies
			if (multiGame.getLives().get() == -1) {
				//End the game, and tell the server you died
				communicator.send("DIE");
				endGame();
				return;
			}
			//Reset and play the animation
			timeBar.setWidth(gameWindow.getWidth() - 8);
			ft.setDuration(Duration.millis(multiGame.getTimerDelay()));
			scaleTransition.setDuration(Duration.millis(multiGame.getTimerDelay()));
			ft.play();
			scaleTransition.play();
		});

		//Set the listeners
		board.setOnBlockClick(this::blockClicked);
		pieceBoard.setOnBlockClick(this::currentBoardClicked);
		nextPieceBoard.setOnBlockClick(this::nextBoardClicked);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Setup the game object and model
	 */
	@Override
	public void setupGame() {
		logger.info("Starting a new multiplayer game");

		//Start new game
		multiGame = new MultiplayerGame(5, 5);
	}

	/**
	 * Handles when a block is clicked on the first piece board, which rotates the first piece and redraws the value
	 * @param gameblock the block clicked on
	 */
	private void currentBoardClicked(GameBlock gameblock){
		logger.info("Rotating pieces");
		multiGame.rotateCurrentPiece();
		pieceBoard.displayPiece(multiGame.getCurrentPiece());
	}

	/**
	 * Handles when a block is clicked on the second piece board, which swaps the pieces then redraws the values
	 * @param gameBlock the Game Block that was clicked on
	 */
	private void nextBoardClicked(GameBlock gameBlock){
		logger.info("Swapping pieces");
		multiGame.swapCurrentPiece();
		pieceBoard.displayPiece(multiGame.getCurrentPiece());
		nextPieceBoard.displayPiece(multiGame.getFollowingPiece());
	}

	/**
	 * End the game and open the score menu
	 */
	private void endGame() {
		multimedia.pauseBackgroundMusic();
		gameWindow.startScore(multiGame);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Handle when a block is clicked
	 */
	@Override
	protected void blockClicked(GameBlock gameBlock) {
		multiGame.blockClicked(gameBlock);
	}

	/**
	 * Update the receivedMessage TextFlow with a new message
	 * @param message new message
	 */
	private void updateReceivedMessage(String message){

		//Format string to get just the message
		message = message.replaceAll("MSG ", "");
		String[] splitMessage = message.split(":",2);

		//Get the name and message
		String name = "<" + splitMessage[0] + "> ";
		String fullMessage = splitMessage[1];

		//Add the time
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String time = "[" + formatter.format(LocalDateTime.now()) + "] ";

		//Set the text to the final message
		String finalMessage = time + name + fullMessage;
		receivedMessage.setText(finalMessage);
	}

	/**
	 * Update the information of other players in the game
	 * @param message new message
	 */
	private void updatePlayerInformation(String message){
		try{
			//Format the message
			multiGame.setMultiScores(message);
			message = message.replaceAll("SCORES ", "");
			String[] playersUnedited = message.split("\\r?\\n");
			playerInformationText = new TextFlow();
			playerInformationBox.setContent(playerInformationText);

			//For each player
			for(int i = 0; i < playersUnedited.length; i++){
				//Format the message
				String[] playerInfo = playersUnedited[i].split(":");

				//Get the name and score
				String name = playerInfo[0];
				Integer score = Integer.parseInt(playerInfo[1]);

				//Create the Text from the final message and style
				Text finalPlayerInfo = new Text(name + ": " + score +"\n");
				finalPlayerInfo.getStyleClass().add("playerBox");
				if(playerInfo[2].equals("DEAD")){
					//If the player is dead
					finalPlayerInfo.getStyleClass().add("deadscore");
				}
				playerInformationText.getChildren().add(finalPlayerInfo);
			}
		} catch (Exception e){
			logger.info(e.getMessage());
		}
	}

	/**
	 * Send the board information to the server
	 */
	private void sendBoard(){
		StringBuilder gameBoard = new StringBuilder("BOARD");

		for(int col = 0; col < multiGame.getCols(); col++){
			for(int row = 0; row < multiGame.getRows(); row++){
				//Append the value on the grid
				gameBoard.append(" " + multiGame.getGrid().get(col, row));
			}
		}
		communicator.send(gameBoard.toString());
	}

	/**
	 * Set the next pieces to the message
	 * @param message piece
	 */
	private void setNextPiece(String message){
		//Format string
		message = message.replaceAll("PIECE ", "");


		if(multiGame.getCurrentPiece() == null){
			//If the current piece is null, set the piece to that
			multiGame.setCurrentPiece(GamePiece.createPiece(Integer.parseInt(message)));
			pieceBoard.displayPiece(multiGame.getCurrentPiece());
		} else if(multiGame.getFollowingPiece() == null){
			//If the following piece is null, set the piece to that
			multiGame.setFollowingPiece(GamePiece.createPiece(Integer.parseInt(message)));
			nextPieceBoard.displayPiece(multiGame.getFollowingPiece());
		} else if(nextnextPiece.isEmpty()){
			//If the queue is empty, add the piece to that
			nextnextPiece.add(GamePiece.createPiece(Integer.parseInt(message)));
			pieceBoard.displayPiece(multiGame.getCurrentPiece());
			nextPieceBoard.displayPiece(multiGame.getFollowingPiece());
		} else{
			//Else, set the pieces to new values
			multiGame.setCurrentPiece(multiGame.getFollowingPiece());
			multiGame.setFollowingPiece(nextnextPiece.remove());
			nextnextPiece.add(GamePiece.createPiece(Integer.parseInt(message)));
			pieceBoard.displayPiece(multiGame.getCurrentPiece());
			nextPieceBoard.displayPiece(multiGame.getFollowingPiece());
		}

	}
}
