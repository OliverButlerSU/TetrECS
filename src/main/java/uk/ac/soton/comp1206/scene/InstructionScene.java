package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Instruction Scene. Provides information on how to play the game
 *
 * @author Windows
 * @version $Id: $Id
 */
public class InstructionScene extends BaseScene {

	/**
	 * A logger to log the status of a class
	 */
	private static final Logger logger = LogManager.getLogger(InstructionScene.class);

	/**
	 * Media player to play music
	 */
	private final Multimedia multimedia = new Multimedia();

	/**
	 * Create a new scene, passing in the GameWindow the scene will be displayed in
	 *
	 * @param gameWindow the game window
	 */
	public InstructionScene(GameWindow gameWindow) {
		super(gameWindow);
		logger.info("Creating Instruction Scene");
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
				//Go to the main menu
				multimedia.pauseBackgroundMusic();
				gameWindow.startMenu();
			}
		}));
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

		//Build the main window
		root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
		var instructionPane = new StackPane();
		var mainPane = new BorderPane();
		instructionPane.setMaxWidth(gameWindow.getWidth());
		instructionPane.setMaxHeight(gameWindow.getHeight());
		instructionPane.getStyleClass().add("instruction-background");
		root.getChildren().add(instructionPane);

		//Add a VBox to hold the instructions
		var instructionBox = new VBox();
		instructionBox.setAlignment(Pos.CENTER);

		//Create a title and add to the scene
		var title = new Text("Instructions");
		title.getStyleClass().add("title");
		var instructionText1 = new Text(
				"TetreECS is a fast-paced gravity-free block placement game, where you must survive "
						+ "by clearing rows through careful placement"
		);
		var instructionText2 = new Text(
				"of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!"
		);
		instructionText2.getStyleClass().add("instructions");
		instructionText1.getStyleClass().add("instructions");
		instructionBox.getChildren().addAll(title, instructionText1, instructionText2);
		mainPane.setTop(instructionBox);

		//Add an image Instructions.png to the center of the scene
		Image image = new Image(getClass().getResource("/images/Instructions.png").toExternalForm());
		ImageView imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(450);
		imageView.setFitWidth(450);
		mainPane.setCenter(imageView);

		//Add a Vbox to hold the piece information
		var pieceBox = new VBox();
		pieceBox.setAlignment(Pos.CENTER);
		pieceBox.setPadding(new Insets(4,4,4,4));

		//Add a tile
		var piecesTitle = new Text("Pieces");
		piecesTitle.getStyleClass().add("heading");

		///Create all 15 pieces and allocate them to a PieceBoard
		PieceBoard[] pieces = new PieceBoard[15];
		for(int i = 0; i < 15; i++){
			GamePiece piece = GamePiece.createPiece(i);
			PieceBoard pieceBoard = new PieceBoard(3,3,gameWindow.getWidth()/12, gameWindow.getWidth()/12);
			pieces[i] = pieceBoard;
			pieceBoard.displayPiece(piece);

			//Rotate the piece when clicked
			pieceBoard.setOnBlockClick((e) ->{
				piece.rotate();
				pieceBoard.displayPiece(piece);
			});
		}

		//Create a GridPane to hold the pieces in UI
		GridPane pieceGrid = new GridPane();
		int mod = -1;
		for(int i = 0; i < pieces.length; i++){
			if(i%5==0){
				mod++;
			}
			pieceGrid.add(pieces[i], i%5, mod, 1, 1);
		}
		pieceGrid.setAlignment(Pos.BOTTOM_CENTER);
		pieceGrid.setHgap(6);
		pieceGrid.setVgap(6);
		pieceGrid.setPadding(new Insets(6,6,6,6));
		pieceBox.getChildren().addAll(piecesTitle, pieceGrid);
		mainPane.setBottom(pieceBox);

		instructionPane.getChildren().add(mainPane);
	}
}
