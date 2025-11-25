package com.example.demo19;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    // Main Containers
    private VBox menuPanel;
    private HBox gamePanel;

    // Containers for the two columns will be ppassed to the controller
    private VBox leftContainer;
    private VBox rightContainer;

    // Game Controller Instancee
    private GameController gameController;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Basotho Culture Quiz");

        //  Topp Bar Elements
        Label categoryLevelLabel = new Label("Main Menu");
        categoryLevelLabel.getStyleClass().add("category-level-label");

        Label scoreLabel = new Label("");
        scoreLabel.getStyleClass().add("score-label");

        Label globalTimerLabel = new Label("Time Remaining: 00:00");
        globalTimerLabel.getStyleClass().add("global-timer-label");
        globalTimerLabel.setVisible(false);

        Button quitButton = new Button("Quit Game");
        quitButton.getStyleClass().add("quit-button");


        HBox topBar = new HBox(50, categoryLevelLabel, scoreLabel, globalTimerLabel, quitButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));
        topBar.getStyleClass().add("top-bar");

        // Menu Panel
        menuPanel = new VBox(20);
        menuPanel.setAlignment(Pos.TOP_CENTER);
        menuPanel.setPadding(new Insets(50));
        menuPanel.getStyleClass().add("menu-panel");
        menuPanel.setMaxWidth(800);

        // Placeholders for menu buttons
        Button btnLilotho = new Button("Lilotho");
        Button btnMaele = new Button("Maele");
        Button btnLipapali = new Button("Lipapali");
        // Button btnDress = new Button("Liaparo tsa Basotho"); // Removed as requested

        // --- 3. Game Panel (HBox containing two VBox columns) ---

        // 3a. Left Column (Questions, Options, Feedback)
        Label questionNumberLabel = new Label("Question: 0 / 0");
        questionNumberLabel.getStyleClass().add("question-number-label");

        Label questionTextLabel = new Label("Question Text Here");
        questionTextLabel.setWrapText(true);
        questionTextLabel.getStyleClass().add("question-text-label");

        ProgressBar timerBar = new ProgressBar(0);
        timerBar.getStyleClass().add("timer-bar");
        timerBar.setPrefWidth(Double.MAX_VALUE);
        timerBar.setVisible(false);

        Button optionAButton = createOptionButton("A) Option 1");
        Button optionBButton = createOptionButton("B) Option 2");
        Button optionCButton = createOptionButton("C) Option 3");
        Button optionDButton = createOptionButton("D) Option 4");

        Label feedbackLabel = new Label("");
        feedbackLabel.getStyleClass().add("feedback-label");

        VBox optionsVBox = new VBox(15, optionAButton, optionBButton, optionCButton, optionDButton, feedbackLabel);
        optionsVBox.setAlignment(Pos.TOP_CENTER);
        optionsVBox.getStyleClass().add("options-vbox");

        VBox.setVgrow(optionsVBox, Priority.ALWAYS);

        leftContainer = new VBox(20, questionNumberLabel, questionTextLabel, optionsVBox, timerBar);
        leftContainer.setAlignment(Pos.TOP_CENTER);
        leftContainer.setPrefWidth(500);
        VBox.setVgrow(leftContainer, Priority.ALWAYS);
        HBox.setHgrow(leftContainer, Priority.ALWAYS);
        leftContainer.getStyleClass().add("game-container");
        leftContainer.setPadding(new Insets(20));


        // 3b. Right Column (Media, Replay Button)

        final double MEDIA_SIZE = 350;

        ImageView mediaImageView = new ImageView();
        mediaImageView.setFitWidth(MEDIA_SIZE);
        mediaImageView.setFitHeight(MEDIA_SIZE);
        mediaImageView.setPreserveRatio(true);
        mediaImageView.getStyleClass().add("media-image");

        MediaView mediaVideoView = new MediaView();
        mediaVideoView.setFitWidth(MEDIA_SIZE);
        mediaVideoView.setFitHeight(MEDIA_SIZE);
        mediaVideoView.setPreserveRatio(true);

        // StackPane ensures image and video share the exact same space/size
        StackPane mediaStack = new StackPane(mediaImageView, mediaVideoView);
        mediaStack.setPrefSize(MEDIA_SIZE, MEDIA_SIZE);
        mediaStack.setAlignment(Pos.CENTER);
        mediaStack.getStyleClass().add("media-stack");


        Button replayVideoButton = new Button("Replay Video");
        replayVideoButton.getStyleClass().add("replay-button");
        replayVideoButton.setVisible(false);

        rightContainer = new VBox(10, mediaStack, replayVideoButton);
        rightContainer.setAlignment(Pos.CENTER);
        rightContainer.setPrefWidth(380);
        VBox.setVgrow(rightContainer, Priority.ALWAYS);
        HBox.setHgrow(rightContainer, Priority.ALWAYS);
        rightContainer.getStyleClass().add("game-container");
        rightContainer.setPadding(new Insets(20));

        // 3c. Game Panel (HBox for the two-column play area)
        gamePanel = new HBox(20, leftContainer, rightContainer);
        gamePanel.setAlignment(Pos.TOP_CENTER);
        gamePanel.setPadding(new Insets(20));
        gamePanel.setVisible(false);

        // --- 4. Main Root Layout (BorderPane + StackPane) ---

        StackPane mainContentArea = new StackPane(menuPanel, gamePanel);
        StackPane.setAlignment(menuPanel, Pos.TOP_CENTER);
        StackPane.setAlignment(gamePanel, Pos.TOP_CENTER);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(mainContentArea);
        root.getStyleClass().add("root-pane");



        gameController = new GameController(
                categoryLevelLabel, scoreLabel,
                questionNumberLabel, questionTextLabel,
                mediaImageView, mediaVideoView,
                optionAButton, optionBButton, optionCButton, optionDButton,
                feedbackLabel, optionsVBox, replayVideoButton, timerBar,
                menuPanel, gamePanel,
                btnLilotho, btnMaele, btnLipapali, null, // Passing null for the removed btnDress
                globalTimerLabel, quitButton,
                leftContainer, rightContainer
        );
        gameController.initialize();


        // --- 6. Set Scene and Stage ---
        Scene scene = new Scene(root, 1000, 750);
        scene.getStylesheets().add(getClass().getResource("game-styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    // Helper method for consistent button creation
    private Button createOptionButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(400);
        button.setWrapText(true);
        button.getStyleClass().add("option-button");
        return button;
    }


    public static void main(String[] args) {
        launch();
    }
}