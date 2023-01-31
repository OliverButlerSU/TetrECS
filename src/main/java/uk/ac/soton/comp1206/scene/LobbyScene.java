package uk.ac.soton.comp1206.scene;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The lobby scene which allows users to create lobbies, send messages and play a multiplayer game
 *
 * @author Windows
 * @version $Id: $Id
 */
public class LobbyScene extends BaseScene{

	/**
	 * A communicator to communicate with the server
	 */
	private Communicator communicator;

	/**
	 * A logger to log the status of the class
	 */
	private static final Logger logger = LogManager.getLogger(MenuScene.class);

	/**
	 * Multimedia to play music
	 */
	private final Multimedia multimedia = new Multimedia();

	/**
	 * A timer which sends requests to the server
	 */
	private Timeline lobbyTimer;

	/**
	 * TextFlow which is used to hold the text of Lobbys
	 */
	private TextFlow lobbyTextFlow = new TextFlow();

	/**
	 * TextFlow which is used to hold the text of the chat
	 */
	private TextFlow chatTextFlow = new TextFlow();

	/**
	 * TextFlow which is sued to hold the tet of the usernames
	 */
	private TextFlow usernameTextFlow = new TextFlow();

	/**
	 * ScrollPane which holds the lobby text
	 */
	private ScrollPane lobbyScroller;

	/**
	 * ScrollPane which holds the chat text
	 */
	private ScrollPane chatScroller;

	/**
	 * ScrollPane which holds the username text
	 */
	private ScrollPane usernameScroller;

	/**
	 * Used to know if the user is a host
	 */
	private boolean isHost = false;

	/**
	 * A start button used to start the game
	 */
	private Button startButton = new Button("Start");

	/**
	 * The username of the main player
	 */
	private String username = "";

	/**
	 * The main pane of the screen
	 */
	private BorderPane mainPane = new BorderPane();

	/**
	 * A textfield to input a lobby
	 */
	private TextField inputLobby = new TextField();

	/**
	 * A VBox to hold the joined lobby information
	 */
	private VBox lobbyChat = null;


	/**
	 * Create a lobby scene
	 *
	 * @param gameWindow the current window
	 * @param communicator the communicator to communicate with the server
	 */
	public LobbyScene(GameWindow gameWindow, Communicator communicator) {
		super(gameWindow);
		this.communicator = communicator;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Initialise the scene
	 */
	@Override
	public void initialise() {
		//If Escape is pressed
		scene.setOnKeyPressed((event -> {
			if(event.getCode() == KeyCode.ESCAPE){
				//Stop the timer, leave the lobby and start the start menu
				lobbyTimer.stop();
				lobbyTimer = new Timeline();
				if(lobbyChat != null){
					communicator.send("PART");
				}
				gameWindow.startMenu();
			}
		}));

		//Add a listener to handle incoming channels
		communicator.addListener((message) -> Platform.runLater(()->{
			if(message.startsWith("CHANNELS")){
				receiveLobbies(message);
			}
		}));

		//Add a listener to handle joining a lobby
		communicator.addListener((message) -> Platform.runLater(()->{
			if(message.startsWith("JOIN")){
				buildJoinLobby(message);
			}
		}));

		//Add a listener to handle errors
		communicator.addListener((message) -> Platform.runLater(()->{
			if(message.startsWith("ERROR")){
				//Draw an Alert with the message on the screen
				Alert error = new Alert(Alert.AlertType.ERROR, message);
				error.show();
			}
		}));

		//Add a listener to handle nicking
		communicator.addListener((message) -> Platform.runLater(()->{
			if(message.startsWith("NICK")){
				//If you are the player nicking
				if(!message.contains(":")){
					//Set the username to the new name
					username = message.replaceAll("NICK ", "");
				}
				//Else request a list of users
				communicator.send("USERS");
			}
		}));

		//Add a listener to start the game
		communicator.addListener((message) -> Platform.runLater(()->{
			if(message.startsWith("START")){
				//Stop the timer and create a multiplayer game
				gameWindow.startMultiplayer(username);
				lobbyTimer.stop();
				lobbyTimer = new Timeline();
			}
		}));

		//Add a listener to leave a lobby
		communicator.addListener((message) -> Platform.runLater(()->{
			if(message.startsWith("PARTED")){
				//Remove lobby UI
				mainPane.getChildren().remove(lobbyChat);
				lobbyChat = null;
				chatTextFlow = new TextFlow();
				usernameTextFlow = new TextFlow();
				username = "";
				isHost = false;
			}
		}));

		//Add a listener to handle a list of users
		communicator.addListener((message) -> Platform.runLater(()->{
			if(message.startsWith("USERS")){
				receivePlayers(message);
			}
		}));

		//Add a listener to handle messages being received
		communicator.addListener((message) -> Platform.runLater(()->{
			if(message.startsWith("MSG")){
				receiveMessage(message);
			}
		}));

		//Add a listener to handle transfering host
		communicator.addListener((message) -> Platform.runLater(()->{
			if(message.startsWith("HOST")){
				//Set the start button visible
				isHost = true;
				startButton.setVisible(true);
			}
		}));

		//Create a timer to request a list of lobbies which loops indefinitely
		communicator.send("LIST");
		lobbyTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
			communicator.send("LIST");
			lobbyTimer.play();
		}));
		lobbyTimer.setCycleCount(Animation.INDEFINITE);
		lobbyTimer.play();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Build the scene
	 */
	@Override
	public void build() {
		//Create the main window
		root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
		var lobbyPane = new StackPane();
		lobbyPane.setMaxWidth(gameWindow.getWidth());
		lobbyPane.setMaxHeight(gameWindow.getHeight());
		lobbyPane.getStyleClass().add("menu-background");
		root.getChildren().add(lobbyPane);
		lobbyPane.getChildren().add(mainPane);

		//Add a title and style
		HBox titleBox = new HBox();
		titleBox.setAlignment(Pos.TOP_CENTER);
		titleBox.setPadding(new Insets(10,10,10,10));
		var title = new Text("Multiplayer");
		title.getStyleClass().add("title");
		title.setTextAlignment(TextAlignment.CENTER);
		titleBox.getChildren().add(title);
		mainPane.setTop(titleBox);

		//Create A HBox to hold information about lobbies
		HBox lobbyTitleAndButton = new HBox();
		lobbyTitleAndButton.setPadding(new Insets(6,6,6,6));
		lobbyTitleAndButton.setSpacing(20);

		//Create a title and style
		Text scoreHeader = new Text("Lobbies");
		scoreHeader.getStyleClass().add("title");

		//Add a button to create a lobby
		Button createChannel = new Button("Create Lobby");

		//Set the TextField visible to create a lobby
		createChannel.setOnMouseClicked((event -> {
			inputLobby.setVisible(true);
		}));
		lobbyTitleAndButton.getChildren().addAll(scoreHeader, createChannel);

		//Add a vbox to hold the lobby information
		VBox lobbies = new VBox();
		lobbies.setMinHeight(gameWindow.getHeight()/1.5);
		lobbies.setPadding(new Insets(6,6,6,6));
		lobbies.setAlignment(Pos.TOP_LEFT);

		//Style the text field
		inputLobby.getStyleClass().add("TextField");
		inputLobby.setVisible(false);
		inputLobby.setPromptText("Input a lobby to make.");

		//When a lobby name is inputted, verify if you can create it, if so create it
		inputLobby.setOnAction((event -> {
			if(verifyLobbyName(inputLobby.getText())){
				communicator.send("CREATE "+inputLobby.getText());
				inputLobby.setVisible(false);
			}
		}));

		//Create a scroller which is used to hold the different lobbies to join
		lobbyScroller = new ScrollPane();
		lobbyScroller.getStyleClass().add("scroller");
		lobbyScroller.setContent(lobbyTextFlow);
		lobbyScroller.setFitToWidth(true);
		lobbyScroller.setMinHeight(gameWindow.getHeight()/1.2);
		lobbyScroller.setMinWidth(gameWindow.getWidth()/3);

		lobbies.getChildren().addAll(lobbyTitleAndButton, inputLobby, lobbyScroller);

		mainPane.setLeft(lobbies);
	}

	/**
	 * Build the UI for when joining a lobby
	 * @param message the lobby name
	 */
	private void buildJoinLobby(String message){
		//Create a VBox and style
		lobbyChat = new VBox();
		lobbyChat.setMinWidth(gameWindow.getWidth()/1.8);
		lobbyChat.setPadding(new Insets(6,6,6,6));
		lobbyChat.setAlignment(Pos.TOP_LEFT);
		lobbyChat.getStyleClass().add("gameBox");
		lobbyChat.setMaxHeight(gameWindow.getHeight()/1.5);

		//Add a title
		Text scoreHeader = new Text(message.split(" ")[1]);
		scoreHeader.setTextAlignment(TextAlignment.CENTER);
		scoreHeader.getStyleClass().add("title");

		//Create a scroller to hold usernames
		usernameScroller = new ScrollPane();
		usernameScroller.getStyleClass().add("scroller");
		usernameScroller.setContent(usernameTextFlow);
		usernameScroller.setFitToWidth(true);
		usernameScroller.setMaxWidth(gameWindow.getWidth()/1.8);
		usernameScroller.setMaxHeight(30);

		//Create a scroller to hold incoming messages
		chatScroller = new ScrollPane();
		chatScroller.getStyleClass().add("scroller");
		chatScroller.setContent(chatTextFlow);
		chatScroller.setFitToWidth(true);
		chatScroller.setMaxWidth(gameWindow.getWidth()/1.8);
		chatScroller.setMinHeight(gameWindow.getHeight()/2);

		//Create a TextField to send messages
		TextField sendMessage = new TextField();
		sendMessage.getStyleClass().add("TextField");
		sendMessage.setPromptText("Send a message");

		//Send a message
		sendMessage.setOnAction((e) ->{
			sendMessage(sendMessage.getText());
			sendMessage.clear();
		});

		//Create a HBox to hold the buttons to leave a lobby and start the game
		HBox startLeaveGameButtons = new HBox();
		startLeaveGameButtons.setSpacing(lobbyChat.getWidth()/2);
		startLeaveGameButtons.setAlignment(Pos.CENTER);

		//Create the buttons
		Button leaveButton = new Button("Leave");
		startButton = new Button("Start");

		//Only display the start button when host
		if(!isHost){
			startButton.setVisible(false);
		}

		//Leave the lobby when clicked
		leaveButton.setOnMouseClicked((event -> {
			communicator.send("PART");
		}));

		//Start the game when clicked
		startButton.setOnMouseClicked(event -> {
			communicator.send("START");
		});

		startLeaveGameButtons.getChildren().addAll(startButton, leaveButton);

		lobbyChat.getChildren().addAll(scoreHeader, usernameScroller, chatScroller, sendMessage, startLeaveGameButtons);
		mainPane.setRight(lobbyChat);

		//Add a message to the chat
		addMessage("Welcome to the lobby");
		addMessage("Type /nick NewName to change your name");
		addMessage("");
		addMessage("");
	}

	/**
	 * Handles when a list of lobbies are received
	 * @param message the message containing the lobbies
	 */
	private void receiveLobbies(String message){
		lobbyTextFlow = new TextFlow();
		lobbyScroller.setContent(lobbyTextFlow);

		//Format the text
		message = message.replaceAll("CHANNELS ", "").strip();
		if(message.length() == 0){
			return;
		}
		String[] lobbies = message.split("\\r?\\n");

		//For each lobby
		for (String lobby: lobbies) {
			//Create a text and style
			Text lobbyMessage = new Text(lobby +"\n");
			lobbyMessage.getStyleClass().add("channelItem");

			//When clicking on it, join the lobby
			lobbyMessage.setOnMouseClicked((event -> {
				communicator.send("JOIN " +lobby);
			}));
			lobbyTextFlow.getChildren().add(lobbyMessage);
		}
	}

	/**
	 * Verify if a lobby has a valid name
	 * @param name name of the lobby
	 * @return if the lobby is valid
	 */
	private boolean verifyLobbyName(String name){
		//If the name is alphabetical and between 1-10 characters long
		if(name.matches("[a-zA-Z]+") && name.length() > 0 && name.length() < 11){
			return true;
		}
		return false;
	}

	/**
	 * Add a message to the chatTextFlow
	 * @param message message to add
	 */
	private void addMessage(String message){
		//Format and add the message
		Text textMessage = new Text(message +"\n");
		textMessage.getStyleClass().add("messages");
		textMessage.getStyleClass().add("messages Text");
		chatTextFlow.getChildren().add(textMessage);
	}

	/**
	 * Receive a message and format it
	 * @param message message to format
	 */
	private void receiveMessage(String message){
		//Format the message
		message = message.replaceAll("MSG ", "");
		String[] splitMessage = message.split(":",2);

		//Get the name and message
		String name = "<" + splitMessage[0] + "> ";
		String fullMessage = splitMessage[1];

		//Add a date to the message
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String time = "[" + formatter.format(LocalDateTime.now()) + "] ";

		//Add the message to the scene
		String finalMessage = time + name + fullMessage;
		addMessage(finalMessage);
	}

	/**
	 * Handle when a list of players a re sent
	 * @param message list of players
	 */
	private void receivePlayers(String message){
		usernameTextFlow = new TextFlow();
		usernameScroller.setContent(usernameTextFlow);

		//Format the message
		message = message.replaceAll("USERS ", "");
		String[] splitMessage = message.split("\\r?\\n");

		//For each user
		for (String user : splitMessage) {
			//Style it
			Text userText = new Text(user+" ");
			userText.getStyleClass().add("playerBox");
			//If the user is you
			if(user.equals(username)){
				//Make bold
				userText.getStyleClass().add("myname");
			}
			usernameTextFlow.getChildren().add(userText);
		}
	}

	/**
	 * Send a message to the server
	 * @param message message to send
	 */
	private void sendMessage(String message){
		//If the message is to nick yourself
		if(message.startsWith("/Nick ") || message.startsWith("/nick ")){
			try{
				//Format message
				String[] splitMessage = message.split(" ", 2);
				String newName = splitMessage[1];
				//If the name is valid, request to nick
				if(newName.matches("[a-zA-Z0-9]*$") && newName.length() > 0 && newName.length() < 11){
					communicator.send("NICK "+newName);
					return;
				}
			} catch (Exception e){
				logger.info(e.getMessage());
				return;
			}
		} else{
			//Send the message
			communicator.send("MSG "+ message);
		}
	}
}
