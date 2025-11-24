package com.example.demo19;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

public class GameController {

    // UI Elements
    private final Label categoryLevelLabel;
    private final Label scoreLabel;
    private final Label questionNumberLabel;
    private final Label questionTextLabel;
    private final ImageView mediaImageView;
    private final MediaView mediaVideoView;
    private final Button optionAButton;
    private final Button optionBButton;
    private final Button optionCButton;
    private final Button optionDButton;
    private final Label feedbackLabel;
    private final VBox optionsVBox;
    private final Button replayVideoButton;
    private final ProgressBar timerBar;

    // NEW UI elements
    private final Label globalTimerLabel;
    private final Button quitButton;

    // Menu and Container Elements
    private final VBox menuPanel;
    private final HBox gamePanel;

    // NEW: Containers for restoring the two-column layout
    private final VBox leftContainer;
    private final VBox rightContainer;

    // Menu Buttons (needed for constructor/initialization)
    private final Button btnLilotho;
    private final Button btnMaele;
    private final Button btnLipapali;


    // Game Logic Elements
    private List<Question> allQuestions;
    private List<Question> currentLevelQuestions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String currentCategory = "Lilotho";
    private String currentLevel = "Easy";
    private MediaPlayer mediaPlayer;

    // Background Music Logic
    private MediaPlayer bgMediaPlayer;

    // Path Variables
    private final String AUDIO_BASE_PATH = "/com/example/demo19/soundsE/";
    private final String MEDIA_BASE_PATH = "/com/example/demo19/media/";

    private final String[] BG_MUSIC_PATHS = {"level1.mp3", "level2.mp3", "level3.mp3"};

    // Audio Effects
    private MediaPlayer correctSoundPlayer;
    private MediaPlayer incorrectSoundPlayer;
    private MediaPlayer victorySoundPlayer;
    private MediaPlayer failureSoundPlayer;

    // Timer Logic
    private PauseTransition questionPause;
    private Timeline questionTimer;
    private static final int MEDIUM_LEVEL_TIME_SECONDS = 15;
    private double timeRemaining;

    // Global Timer Logic (HARD Level)
    private Timeline globalTimer;
    private static final int HARD_LEVEL_TOTAL_TIME_SECONDS = 30;
    private double globalTimeRemaining;

    // Game Progress Tracking
    private final Map<String, Map<String, Boolean>> currentProgressMap = new HashMap<>();
    private final List<String> CATEGORIES = Arrays.asList("Lilotho", "Maele", "Lipapali");
    private final List<String> LEVELS = Arrays.asList("Easy", "Medium", "Hard");


    // CONSTRUCTOR
    public GameController(
            Label categoryLevelLabel, Label scoreLabel,
            Label questionNumberLabel, Label questionTextLabel,
            ImageView mediaImageView, MediaView mediaVideoView,
            Button optionAButton, Button optionBButton, Button optionCButton, Button optionDButton,
            Label feedbackLabel, VBox optionsVBox, Button replayVideoButton, ProgressBar timerBar,
            VBox menuPanel, HBox gamePanel,
            Button btnLilotho, Button btnMaele, Button btnLipapali, Button btnDress,
            Label globalTimerLabel, Button quitButton,
            VBox leftContainer, VBox rightContainer) {

        this.categoryLevelLabel = categoryLevelLabel;
        this.scoreLabel = scoreLabel;
        this.questionNumberLabel = questionNumberLabel;
        this.questionTextLabel = questionTextLabel;
        this.mediaImageView = mediaImageView;
        this.mediaVideoView = mediaVideoView;
        this.optionAButton = optionAButton;
        this.optionBButton = optionBButton;
        this.optionCButton = optionCButton;
        this.optionDButton = optionDButton;
        this.feedbackLabel = feedbackLabel;
        this.optionsVBox = optionsVBox;
        this.replayVideoButton = replayVideoButton;
        this.timerBar = timerBar;

        this.menuPanel = menuPanel;
        this.gamePanel = gamePanel;

        this.btnLilotho = btnLilotho;
        this.btnMaele = btnMaele;
        this.btnLipapali = btnLipapali;


        this.globalTimerLabel = globalTimerLabel;
        this.quitButton = quitButton;

        this.leftContainer = leftContainer;
        this.rightContainer = rightContainer;
    }

    public void initialize() {
        setupAllQuestionsData();
        setupAudioEffects();
        initializeProgressMap();

        // Game Button Actions
        optionAButton.setOnAction(event -> handleAnswer(0));
        optionBButton.setOnAction(event -> handleAnswer(1));
        optionCButton.setOnAction(event -> handleAnswer(2));
        optionDButton.setOnAction(event -> handleAnswer(3));

        replayVideoButton.setOnAction(event -> replayVideo());

        // Quit button action
        quitButton.setOnAction(event -> {
            stopAllTimers();
            showMainMenu();
        });

        if (btnLilotho != null) btnLilotho.setVisible(false);
        if (btnMaele != null) btnMaele.setVisible(false);
        if (btnLipapali != null) btnLipapali.setVisible(false);


        playBackgroundMusic();
        showMainMenu();
    }

    private void initializeProgressMap() {
        for (String category : CATEGORIES) {
            currentProgressMap.put(category, new HashMap<>());
            for (String level : LEVELS) {
                boolean isLocked = true;
                if (category.equals(CATEGORIES.get(0)) && level.equals(LEVELS.get(0))) {
                    isLocked = false;
                }
                currentProgressMap.get(category).put(level, isLocked);
            }
        }
    }

    private void setupAudioEffects() {
        try {
            double startTime = 1.0;

            java.net.URL correctUrl = getClass().getResource(AUDIO_BASE_PATH + "correct.mp3");
            if (correctUrl != null) {
                correctSoundPlayer = new MediaPlayer(new Media(correctUrl.toExternalForm()));
                correctSoundPlayer.setVolume(0.8);
                correctSoundPlayer.setStartTime(Duration.seconds(startTime));
            } else {
                System.err.println("Error: Correct sound not found at " + AUDIO_BASE_PATH + "correct.mp3");
            }

            java.net.URL incorrectUrl = getClass().getResource(AUDIO_BASE_PATH + "wrong.mp3");
            if (incorrectUrl != null) {
                incorrectSoundPlayer = new MediaPlayer(new Media(incorrectUrl.toExternalForm()));
                incorrectSoundPlayer.setVolume(0.8);
                incorrectSoundPlayer.setStartTime(Duration.seconds(startTime));
            } else {
                System.err.println("Error: Incorrect sound not found at " + AUDIO_BASE_PATH + "wrong.mp3");
            }

            java.net.URL victoryUrl = getClass().getResource(AUDIO_BASE_PATH + "victory.mp3");
            if (victoryUrl != null) {
                victorySoundPlayer = new MediaPlayer(new Media(victoryUrl.toExternalForm()));
                victorySoundPlayer.setVolume(0.8);
                victorySoundPlayer.setStartTime(Duration.seconds(startTime));
            } else {
                System.err.println("Error: Victory sound not found at " + AUDIO_BASE_PATH + "victory.mp3");
            }

            java.net.URL failureUrl = getClass().getResource(AUDIO_BASE_PATH + "failed.mp3");
            if (failureUrl != null) {
                failureSoundPlayer = new MediaPlayer(new Media(failureUrl.toExternalForm()));
                failureSoundPlayer.setVolume(0.8);
                failureSoundPlayer.setStartTime(Duration.seconds(startTime));
            } else {
                System.err.println("Error: Failure sound not found at " + AUDIO_BASE_PATH + "failed.mp3");
            }

        } catch (Exception e) {
            System.err.println("Error initializing audio effects: " + e.getMessage());
        }
    }

    private void playBackgroundMusic() {
        if (bgMediaPlayer != null) {
            bgMediaPlayer.stop();
            bgMediaPlayer.dispose();
        }

        String randomMusicFile = BG_MUSIC_PATHS[new Random().nextInt(BG_MUSIC_PATHS.length)];
        String mediaPath = AUDIO_BASE_PATH + randomMusicFile;

        try {
            java.net.URL resourceUrl = getClass().getResource(mediaPath);
            if (resourceUrl == null) {
                System.err.println("Background music file not found: " + mediaPath);
                return;
            }

            Media media = new Media(resourceUrl.toExternalForm());
            bgMediaPlayer = new MediaPlayer(media);

            bgMediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgMediaPlayer.setVolume(0.5);
            bgMediaPlayer.play();

        } catch (Exception e) {
            System.err.println("Error playing background music: " + e.getMessage());
        }
    }

    private void stopAllTimers() {
        if (questionTimer != null) questionTimer.stop();
        if (globalTimer != null) globalTimer.stop();
        if (questionPause != null) questionPause.stop();

        if (victorySoundPlayer != null) victorySoundPlayer.stop();
        if (failureSoundPlayer != null) failureSoundPlayer.stop();
        if (correctSoundPlayer != null) correctSoundPlayer.stop();
        if (incorrectSoundPlayer != null) incorrectSoundPlayer.stop();
    }


    // --- NAVIGATION METHODS ---

    private void showMainMenu() {
        stopAllTimers();

        globalTimerLabel.setVisible(false);
        timerBar.setVisible(false);
        menuPanel.setVisible(true);
        gamePanel.setVisible(false);

        categoryLevelLabel.setText("Main Menu");
        scoreLabel.setText("");

        if (mediaPlayer != null) mediaPlayer.stop();

        if (bgMediaPlayer != null && bgMediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            bgMediaPlayer.play();
        }

        menuPanel.getChildren().clear();

        Label menuTitle = new Label("KHEHTHA SEHLOOHO LE BOEMO");
        menuTitle.setStyle("-fx-font-size: 24px; -fx-padding: 10 0 10 0; -fx-font-weight: bold;");
        menuPanel.getChildren().add(menuTitle);

        for (String category : CATEGORIES) {
            Label categoryHeader = new Label("--- " + category + " ---");
            categoryHeader.setStyle("-fx-font-size: 18px; -fx-padding: 10 0 5 0; -fx-font-weight: bold;");
            menuPanel.getChildren().add(categoryHeader);

            HBox levelBox = new HBox(10);
            levelBox.setAlignment(Pos.CENTER);

            for (String level : LEVELS) {
                boolean isLocked = currentProgressMap.get(category).getOrDefault(level, true);
                String buttonText = level + (isLocked ? " (Locked)" : " (Unlocked)");

                Button levelButton = new Button(buttonText);
                levelButton.setPrefWidth(140);
                levelButton.setDisable(isLocked);

                if (isLocked) {
                    levelButton.getStyleClass().add("locked-level");
                } else {
                    levelButton.getStyleClass().add("unlocked-level");
                }

                levelButton.setOnAction(e -> startCategoryLevel(category, level));
                levelBox.getChildren().add(levelButton);
            }
            menuPanel.getChildren().add(levelBox);
        }
    }

    private void startCategoryLevel(String category, String level) {
        menuPanel.setVisible(false);
        gamePanel.setVisible(true);

        // --- Restore the two-column game layout ---
        gamePanel.getChildren().clear();
        gamePanel.getChildren().addAll(leftContainer, rightContainer);
        gamePanel.setAlignment(Pos.CENTER);

        // Restore default options VBox content for gameplay
        optionsVBox.getChildren().clear();
        optionsVBox.getChildren().addAll(optionAButton, optionBButton, optionCButton, optionDButton, feedbackLabel);

        // Hide media display in the right container when starting a new question flow
        mediaImageView.setVisible(false);
        mediaVideoView.setVisible(false);
        replayVideoButton.setVisible(false);


        loadQuestionsForLevel(category, level);
    }


    // --- QUESTION SETUP ---
    private void setupAllQuestionsData() {
        allQuestions = new ArrayList<>();

        // Lilotho - Easy (Q/A Media uses MEDIA_BASE_PATH)
        allQuestions.add(new Question("Lilotho", "Easy", "Ke eane ke eena.",
                Arrays.asList("Thaba", "Mahlo", "Sefate", "Lefika"), 1, MEDIA_BASE_PATH + "mahlo.mp4"));
        allQuestions.add(new Question("Lilotho", "Easy", "Ha moruti lifate li ea oa",
                Arrays.asList("Lints'i","Makala",  "Sefate", "Lerumo"), 0, MEDIA_BASE_PATH + "mahlo.mp4"));
        allQuestions.add(new Question("Lilotho", "Easy", "Seotloana sa Mmankokotiele",
                Arrays.asList("Meno", "Tšepe", "Lejoe", "Seotlo"), 0, MEDIA_BASE_PATH + "meno.jpeg"));
        allQuestions.add(new Question("Lilotho", "Easy", "Ka qhala phoofo ka ja mokotla",
                Arrays.asList("Mokotla", "Lijo", "Phoofo", "Moholu"), 3, MEDIA_BASE_PATH + "moholu.jpeg"));
        allQuestions.add(new Question("Lilotho", "Easy", "Phutse le hara thota",
                Arrays.asList("Moeti", "Lefika", "Mokhubu", "Noka"), 2, MEDIA_BASE_PATH + "mokhubu.jpeg"));

        // Lilotho - Medium
        allQuestions.add(new Question("Lilotho", "Medium", "Mohlankana ea lulang lehaheng",
                Arrays.asList("Mohlankana", "Leleme", "Thaba", "Lehaha"), 1, MEDIA_BASE_PATH + "leleme.jpeg"));
        allQuestions.add(new Question("Lilotho", "Medium", "Maqheku a qabana ka lehaheng",
                Arrays.asList("Metsoalle", "Likhobe", "Maqheku", "Lehaha"), 0, MEDIA_BASE_PATH + "likhobe.jpeg"));
        allQuestions.add(new Question("Lilotho", "Medium", "Botala ke ba joang,Bofubelu ke ba mali, monate ke oa tsoekere.",
                Arrays.asList("tseekere", "Joang", "Mali", "lehapu"), 3, MEDIA_BASE_PATH + "lehapu.jpeg"));
        allQuestions.add(new Question("Lilotho", "Medium", "phate lia lekana",
                Arrays.asList("Lerumo", "Moeti", "Leholimo le lefatse", "Makoko"), 2, MEDIA_BASE_PATH + "lehlimo.jpeg"));
        allQuestions.add(new Question("Lilotho", "Medium", "lithunthung tsa tlapa le leholo",
                Arrays.asList("Mohlabahlabane", "Lithunthung", "Lipalesa", "linaleli"), 3, MEDIA_BASE_PATH + "linaleli.jpeg"));

        // Lilotho - Hard
        allQuestions.add(new Question("Lilotho", "Hard", "Nthethe a bina Moholo a lutse",
                Arrays.asList("Moholo", "Sefate", "Nthethe", "Nthethe le Moholo"), 1, MEDIA_BASE_PATH + "sefate.jpeg"));
        allQuestions.add(new Question("Lilotho", "Hard", "Mala a nku marang-rang",
                Arrays.asList("Mohloa", "Marang-rang", "Mehala", "Mala"), 0, MEDIA_BASE_PATH + "mohloa.jpeg"));
        allQuestions.add(new Question("Lilotho", "Hard", "Monna eo e reng ha a khotse a roalle",
                Arrays.asList("Mohlankana", "Monna", "Noka", "Molisana"), 2, MEDIA_BASE_PATH + "noka.jpeg"));
        allQuestions.add(new Question("Lilotho", "Hard","'Mamonyamane matsoa lehlakeng",
                Arrays.asList("katse", "Metsi", "Tjobolo", "Mamonyame"), 0, MEDIA_BASE_PATH + "katse.jpeg"));
        allQuestions.add(new Question("Lilotho", "Hard", "lehalima lereli le pota motse",
                Arrays.asList("Moeti", "Leleme", "ntja", "Namane"), 3, MEDIA_BASE_PATH + "namane.jpeg"));

        // Maele - Easy
        allQuestions.add(new Question("Maele", "Easy", "Ho aha ka tshiba tsa emong",
                Arrays.asList("Ho rata ho kalima", "Ho sebelisa bohlale ba motho emong ho phethahatsa merero ea hao", "Ho aha ntlong ka matla a emong", "Ho aha ka litshiba"), 1, MEDIA_BASE_PATH + "emong.mp4"));
        allQuestions.add(new Question("Maele", "Easy", "Ho aha serobe phiri e se e jele ke ho etsa eng?",
                Arrays.asList("Ho fana ka lijo", "Ho etsa ketso ee itseng nako e se e tsamaile ebile ho se ho senyehile", "Ho fepa phiri", "Ho haha serobe"), 1, MEDIA_BASE_PATH + "serobe.mp4"));
        allQuestions.add(new Question("Maele", "Easy", "Bo stsholoa bo chesa, bo tsohe bo folile, ho boleloa eng ka mantsoe aao? ",
                Arrays.asList("Ho leka feela", "Maikutlo a khalefo a kokobela ha nako e ntse e tsamaea", "bohobe", "Ho ja tse batang"), 1, MEDIA_BASE_PATH + ""));
        allQuestions.add(new Question("Maele", "Easy", "Ho ipha limenyane",
                Arrays.asList("ho ipha lintho tsa beng ba tsona", "ho ja ha monate", "Ho baleha", "Ho ipha ntho tse monate"), 2, MEDIA_BASE_PATH + "run.mp4"));
        allQuestions.add(new Question("Maele", "Easy", "Tse sa jeseng litheohelang",
                Arrays.asList("Ho utloa tlala maleng", "Litaba kapa liketsahalo tse mpe ebile li sa khotsofatse", "Ho se jese e mong", "Ho timana"), 1, MEDIA_BASE_PATH + "litheohelang.mp4"));

        // Maele - Medium
        allQuestions.add(new Question("Maele", "Medium", "Letsoalo le molato le ea ikahlola, ho boleloang moo?",
                Arrays.asList("letsoalo le ea ikahlolo","Motho ea molato o ea iponahatsa ka liketso","Ho ba motho ea letsoalo","Ho ikahlola"), 1, MEDIA_BASE_PATH + "molato.mp4"));

        allQuestions.add(new Question("Maele", "Medium", "Ho kenya Tshotso linaleng, ho boleloa eng?",
                Arrays.asList("Ho tshoara tshotso ka manala","Ho se kuti manala","Ho tshoara lintho tse mafura","Ho ikenya ka hara mathata"), 3, MEDIA_BASE_PATH + "tloae.mp4"));

        allQuestions.add(new Question("Maele", "Medium", "Kea shoa kea ikepela, moo ho lekoa ho boleloa eng?",
                Arrays.asList("Ho ikepela","Ho shoa","ke o rata ka pelo eaka kaofela","Ke o rata ha nyenyana"), 2, MEDIA_BASE_PATH + "love.jpeg"));

        allQuestions.add(new Question("Maele", "Medium", "Ho ineha naha",
                Arrays.asList("Ho baleha","Ho ipha naha","Ho tseba naha","Ho itshaba"), 0, MEDIA_BASE_PATH + "naha.mp4"));

        allQuestions.add(new Question("Maele", "Medium", "Ho ja tloae",
                Arrays.asList("Ho ja lijo tse monate","Ho ja lijo tse hlabosang","Ho ba lekhoba la ketso tse itseng","Ho ja haholo"), 2, MEDIA_BASE_PATH + "tloae.mp4"));

        // Maele - Hard
        allQuestions.add(new Question("Maele", "Hard", "Ho lala taba ka mmele, Ho boleoa eng?",
                Arrays.asList("Ho khaleha","Ho iphapanya","Ho hlaisa litaba ka mmele","Ho se tsebe"), 1, MEDIA_BASE_PATH + "lala.jpeg"));

        allQuestions.add(new Question("Maele", "Hard", "Ho latola bosehla, Ho boleloa eng?",
                Arrays.asList("Ho latola mmala","Ho se tsebe mmala","Ho latola litaba tse nang le bosehla","Ho hana ka botlalo"), 3, MEDIA_BASE_PATH + "latola.mp4"));

        allQuestions.add(new Question("Maele", "Hard", "Ho ipona mafolomabe, hoa be ho boleloa eng?",
                Arrays.asList("Ho ipona hampe","Ho icheba khafetsa","Ho ipona sefahleho hore ha se bohehe","Ho itshoaea liphoso"), 3, MEDIA_BASE_PATH + "mafolomabe.mp4"));

        allQuestions.add(new Question("Maele", "Hard", "Ho li hlaba Malotsana, Ho boleloa eng?",
                Arrays.asList("Ho elellisoa","Ho Hlokomelisa ","Ho eletsa","Ho fa maele kapa maelana","Kaofela ho tse ka holimo"), 3, MEDIA_BASE_PATH + "advise.jpeg"));

        allQuestions.add(new Question("Maele", "Hard", "Phiso li omile methalali, Ho boleloa eng?",
                Arrays.asList("Pula ha esa na","Ho oma","Lijo li felile ka botlalo","Ho oma hoa sebaka"), 2, MEDIA_BASE_PATH + "tlala.png"));

        // Lipapali - Easy
        allQuestions.add(new Question("Lipapali", "Easy", "Mokhibo ke Papali e bapaloang ke bo mang?",
                Arrays.asList("Bo 'me'", "Bahlankana", "Bo 'ntate", "Baroetsana"), 0, MEDIA_BASE_PATH + "bomme.jpeg"));
        allQuestions.add(new Question("Lipapali", "Easy", "Selialia ke papali e bapaloang ke bo mang?",
                Arrays.asList("Baroetsana", "Bahlankana", "Baroetsana le Bahlankana", "Bashanyana"), 2, MEDIA_BASE_PATH + "selialia.jpeg"));
        allQuestions.add(new Question("Lipapali", "Easy", "Ke papali efe e bapaloang ka majoe mme ebe a bitsoa likhomo?",
                Arrays.asList("Ntlamo", "Morabaraba", "Sekata-majoana", "Liketoana"), 1, MEDIA_BASE_PATH + "morabaraba.jpeg"));
        allQuestions.add(new Question("Lipapali", "Easy", "Ke seaparo sefeng se tenoang ha ho bapaloa ntlamo?",
                Arrays.asList("Borikhoe", "Thethana", "Mose", "Tsheha"), 3, MEDIA_BASE_PATH + "ntlamo.jpeg"));
        allQuestions.add(new Question("Lipapali", "Easy", "Papali e bapaloang ke bashanyana le banna ke e bitsoang?",
                Arrays.asList("Mohobelo", "Selialia", "Mokhibo", "Liketoana"), 0, MEDIA_BASE_PATH + "mohobelo.jpeg"));

        // Lipapali - Medium
        allQuestions.add(new Question("Lipapali", "Medium", "Ke efe papali ea basotho moo ho sebelisoang matsoe (Seallela le Ha-sellele)?",
                Arrays.asList("Liketoane", "Mokhibo", "Sekata-majoana", "Khati"), 2, MEDIA_BASE_PATH + "majoana.jpeg"));

        allQuestions.add(new Question("Lipapali", "Medium", "Papali ea Liketoane e bapaloa haholo ka eng?",
                Arrays.asList("Lipalo", "Melamu", "Majoe a manyenyane", "Libolo"), 2, MEDIA_BASE_PATH + "majoe.jpeg"));

        allQuestions.add(new Question("Lipapali", "Medium", "Ke papali efeng e bapaloang ka nako ea komello ha ho bitsoa pula?",
                Arrays.asList("Morabaraba", "Lesokoana", "Liketoane", "Khati"), 1, MEDIA_BASE_PATH + "lesokoana.jpeg"));

        allQuestions.add(new Question("Lipapali", "Medium", "Papali ea Basotho eo emong atla ipata ebe o ea batloa e bitsoa eng?",
                Arrays.asList("Morabaraba", "Khati", "Mmaipatile", "Liketoane"), 2, MEDIA_BASE_PATH + "mmaipatile.jpeg"));

        allQuestions.add(new Question("Lipapali", "Medium", "Ke efe ho lipapali tsena e hlalosoang haholo e le motjeko oa basali?",
                Arrays.asList("Lesokoana", "Morabaraba", "Mokhibo", "Manketjoane"), 2, MEDIA_BASE_PATH + "mokhibo.jpeg"));
    }

    private void loadQuestionsForLevel(String category, String level) {
        stopAllTimers();

        currentLevelQuestions = allQuestions.stream()
                .filter(q -> q.getCategory().equals(category) && q.getLevel().equals(level))
                .collect(Collectors.toList());

        Collections.shuffle(currentLevelQuestions);
        currentQuestionIndex = 0;
        score = 0;
        currentCategory = category;
        currentLevel = level;

        categoryLevelLabel.setText(category + " - " + level);
        scoreLabel.setText("Score: 0 / 0");

        // Hide media display in the right container when starting a new question flow
        mediaImageView.setVisible(false);
        mediaVideoView.setVisible(false);
        replayVideoButton.setVisible(false);


        if (currentLevel.equals("Hard")) {
            startHardLevelGlobalTimer();
            globalTimerLabel.setVisible(false);
            timerBar.setVisible(true);
        } else if (currentLevel.equals("Medium")) {
            globalTimerLabel.setVisible(false);
            timerBar.setVisible(true);
        } else { // Easy
            globalTimerLabel.setVisible(false);
            timerBar.setVisible(false);
        }


        if (!currentLevelQuestions.isEmpty()) {
            displayCurrentQuestion();
        } else {
            questionTextLabel.setText("No questions available for " + category + " - " + level);
            setOptionsDisabled(true);
        }
    }

    private void displayCurrentQuestion() {
        if (currentQuestionIndex < currentLevelQuestions.size()) {
            Question currentQuestion = currentLevelQuestions.get(currentQuestionIndex);

            questionNumberLabel.setText("Question: " + (currentQuestionIndex + 1) + " / " + currentLevelQuestions.size());
            questionTextLabel.setText(currentQuestion.getQuestionText());
            feedbackLabel.setText("");

            animateFadeIn(questionTextLabel);

            setOptionsDisabled(false);
            resetButtonStyles();

            if (questionTimer != null) questionTimer.stop();

            List<String> options = currentQuestion.getOptions();
            optionAButton.setText("A) " + options.get(0));
            optionBButton.setText("B) " + options.get(1));
            optionCButton.setText("C) " + options.get(2));
            optionDButton.setText("D) " + options.get(3));

            loadMedia(currentQuestion.getMediaPath());

            if (currentLevel.equals("Medium")) {
                timerBar.setVisible(true);
                timerBar.setProgress(1.0);
                timerBar.getStyleClass().removeAll("timer-red", "timer-yellow", "timer-green");
                timerBar.getStyleClass().add("timer-green");
                startMediumLevelPerQuestionTimer();
            } else if (currentLevel.equals("Hard")) {
            } else {
                timerBar.setVisible(false);
            }

        } else {
            showLevelSummary();
        }
    }

    // --- HARD LEVEL GLOBAL TIMER ---
    private void startHardLevelGlobalTimer() {
        globalTimeRemaining = HARD_LEVEL_TOTAL_TIME_SECONDS;
        timerBar.setVisible(true);
        timerBar.setProgress(1.0);

        timerBar.getStyleClass().removeAll("timer-red", "timer-yellow", "timer-green");
        timerBar.getStyleClass().add("timer-green");
        globalTimerLabel.setVisible(false);

        if (globalTimer != null) globalTimer.stop();

        globalTimer = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            globalTimeRemaining -= 0.1;
            double progress = globalTimeRemaining / HARD_LEVEL_TOTAL_TIME_SECONDS;
            timerBar.setProgress(progress);

            timerBar.getStyleClass().removeAll("timer-red", "timer-yellow", "timer-green");
            if (progress < 0.3) timerBar.getStyleClass().add("timer-red");
            else if (progress < 0.6) timerBar.getStyleClass().add("timer-yellow");
            else timerBar.getStyleClass().add("timer-green");

            if (globalTimeRemaining <= 0) {
                globalTimer.stop();
                handleGlobalTimeout();
            }
        }));
        globalTimer.setCycleCount(Timeline.INDEFINITE);
        globalTimer.play();
    }

    private void handleGlobalTimeout() {
        stopAllTimers();
        globalTimerLabel.setVisible(false);
        timerBar.setVisible(false);
        setOptionsDisabled(true);

        questionNumberLabel.setText("TIME EXPIRED!");
        questionTextLabel.setText("Your time for the Hard level has run out. Let's see your final score.");

        questionPause = new PauseTransition(Duration.seconds(3));
        questionPause.setOnFinished(event -> showLevelSummary());
        questionPause.play();
    }

    // --- MEDIUM LEVEL PER-QUESTION TIMER ---
    private void startMediumLevelPerQuestionTimer() {
        timeRemaining = MEDIUM_LEVEL_TIME_SECONDS;
        questionTimer = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            timeRemaining -= 0.1;
            double progress = timeRemaining / MEDIUM_LEVEL_TIME_SECONDS;
            timerBar.setProgress(progress);

            timerBar.getStyleClass().removeAll("timer-red", "timer-yellow", "timer-green");
            if (progress < 0.3) timerBar.getStyleClass().add("timer-red");
            else if (progress < 0.6) timerBar.getStyleClass().add("timer-yellow");
            else timerBar.getStyleClass().add("timer-green");

            if (timeRemaining <= 0) {
                questionTimer.stop();
                handleAnswer(-1); // Timeout
            }
        }));
        questionTimer.setCycleCount(Timeline.INDEFINITE);
        questionTimer.play();
    }

    private void handleAnswer(int selectedIndex) {
        if (questionTimer != null) questionTimer.stop();

        if (optionAButton.isDisable()) return;


        setOptionsDisabled(true);

        Question currentQuestion = currentLevelQuestions.get(currentQuestionIndex);
        boolean isTimeout = (selectedIndex == -1);

        Button selectedButton = (isTimeout) ? null : getButtonForIndex(selectedIndex);
        Button correctButton = getButtonForIndex(currentQuestion.getCorrectAnswerIndex());
        boolean isCorrect = (!isTimeout && selectedIndex == currentQuestion.getCorrectAnswerIndex());

        if (correctSoundPlayer != null) correctSoundPlayer.stop();
        if (incorrectSoundPlayer != null) incorrectSoundPlayer.stop();


        if (isTimeout) {
            feedbackLabel.setText("Timeout! The correct answer is displayed.");
            if (currentLevel.equals("Medium")) timerBar.setProgress(0);
            animateCorrectButton(correctButton);
            if (incorrectSoundPlayer != null) {
                incorrectSoundPlayer.stop(); incorrectSoundPlayer.play();
            }
        } else if (isCorrect) {
            score++;
            feedbackLabel.setText("Correct!");
            animateSuccess(selectedButton);
            if (correctSoundPlayer != null) {
                correctSoundPlayer.stop(); correctSoundPlayer.play();
            }
        } else {
            feedbackLabel.setText("Incorrect. The correct answer is displayed.");
            animateShake(selectedButton);
            animateCorrectButton(correctButton);
            if (incorrectSoundPlayer != null) {
                incorrectSoundPlayer.stop(); incorrectSoundPlayer.play();
            }
        }

        scoreLabel.setText("Score: " + score + " / " + (currentQuestionIndex + 1));

        questionPause = new PauseTransition(Duration.seconds(2));
        questionPause.setOnFinished(event -> {
            currentQuestionIndex++;
            displayCurrentQuestion();
        });
        questionPause.play();
    }

    // --- MEDIA HANDLING ---
    private void loadMedia(String mediaPath) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        mediaImageView.setVisible(false);
        mediaVideoView.setVisible(false);
        replayVideoButton.setVisible(false);

        try {
            java.net.URL resourceUrl = getClass().getResource(mediaPath);
            if (resourceUrl == null) return;

            String lowerCasePath = mediaPath.toLowerCase();

            // Check for image files
            if (lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg") || lowerCasePath.endsWith(".png")) {
                mediaImageView.setImage(new Image(resourceUrl.toExternalForm(), true));
                mediaImageView.setVisible(true);
                animateFadeIn(questionTextLabel);
                animatePhotoShake();

                if (bgMediaPlayer != null && bgMediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                    bgMediaPlayer.play();
                }

                // Check for video files
            } else if (lowerCasePath.endsWith(".mp4")) {
                Media media = new Media(resourceUrl.toExternalForm());
                mediaPlayer = new MediaPlayer(media);

                mediaPlayer.setVolume(0.0);

                mediaVideoView.setMediaPlayer(mediaPlayer);
                mediaVideoView.setVisible(true);
                replayVideoButton.setVisible(true);

                if (questionTimer != null) questionTimer.pause();
                if (currentLevel.equals("Hard") && globalTimer != null) globalTimer.pause();

                mediaPlayer.setOnReady(() -> {
                    if (mediaPlayer.getTotalDuration().greaterThan(Duration.seconds(5))) {
                        mediaPlayer.setStopTime(Duration.seconds(5));
                    }
                    mediaPlayer.play();
                });

                mediaPlayer.setOnEndOfMedia(() -> {
                    if (questionTimer != null) questionTimer.play();
                    if (currentLevel.equals("Hard") && globalTimer != null) globalTimer.play();
                });
            }
        } catch (Exception e) {
            System.err.println("Media load error: " + e.getMessage());
        }
    }

    private void replayVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.play();

            if (currentLevel.equals("Hard") && globalTimer != null) {
                globalTimer.pause();
            }
        }
    }

    // --- LEVEL PROGRESSION AND SUMMARY ---

    private HBox createStarRating(double percentage) {
        HBox starBox = new HBox(5);
        starBox.setAlignment(Pos.CENTER);

        int stars = 0;
        String starStyle;

        if (percentage >= 90) {
            stars = 3;
            starStyle = "star-gold";
        } else if (percentage >= 75) {
            stars = 2;
            starStyle = "star-silver";
        } else if (percentage >= 60) {
            stars = 1;
            starStyle = "star-bronze";
        } else {
            stars = 0;
            starStyle = "star-fail";
        }

        for (int i = 0; i < 3; i++) {
            Label star = new Label(i < stars ? "★" : "☆");
            star.getStyleClass().add(starStyle);
            star.setStyle("-fx-font-size: 40px;");
            starBox.getChildren().add(star);
        }
        return starBox;
    }

    private VBox createSummaryLayout(String title, double percentage, boolean passed, String actionButtonText, Runnable action) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new javafx.geometry.Insets(40));
        layout.getStyleClass().add("center-summary-panel");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(passed ? "summary-title-pass" : "summary-title-fail");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        Label scoreDisplay = new Label("Final Score: " + String.format("%.0f", percentage) + "% (" + score + "/" + currentLevelQuestions.size() + ")");
        scoreDisplay.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        HBox starRating = createStarRating(percentage);

        Label instructionLabel = new Label(
                passed ? "Proceed to the next challenge!" : "You need at least 60% to unlock the next level. Try again!"
        );
        instructionLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaa; -fx-text-alignment: center;");

        Button controlButton = new Button(actionButtonText);
        controlButton.getStyleClass().add("control-button");
        controlButton.setPrefWidth(450);
        controlButton.setOnAction(e -> {
            action.run();
        });

        Button menuButton = new Button("Back to Main Menu");
        menuButton.getStyleClass().add("option-button");
        menuButton.setPrefWidth(450);
        menuButton.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(titleLabel, scoreDisplay, starRating, instructionLabel, controlButton, menuButton);

        return layout;
    }


    private void showLevelSummary() {
        stopAllTimers();
        globalTimerLabel.setVisible(false);
        timerBar.setVisible(false);

        if (victorySoundPlayer != null) victorySoundPlayer.stop();
        if (failureSoundPlayer != null) failureSoundPlayer.stop();

        int totalQuestions = currentLevelQuestions.size();
        double percentage = totalQuestions > 0 ? (score / (double) totalQuestions) * 100 : 0;
        boolean levelPassed = percentage >= 60.0;

        questionNumberLabel.setText("Level Summary");
        questionTextLabel.setText("");
        scoreLabel.setText("Final Score: " + score + " / " + totalQuestions);

        mediaImageView.setVisible(false);
        mediaVideoView.setVisible(false);
        replayVideoButton.setVisible(false);
        if (mediaPlayer != null) mediaPlayer.stop();

        // --- CRITICAL CHANGE: Clear the gamePanel for the single center container ---
        gamePanel.getChildren().clear();

        final String nextCategory;
        final String nextLevel;
        String nextButtonText;
        VBox summaryLayout;

        if (levelPassed) {
            if (victorySoundPlayer != null) { victorySoundPlayer.stop(); victorySoundPlayer.play(); }

            int currentCatIndex = CATEGORIES.indexOf(currentCategory);
            int currentLvlIndex = LEVELS.indexOf(currentLevel);

            if (currentLvlIndex < LEVELS.size() - 1) {
                nextCategory = currentCategory;
                nextLevel = LEVELS.get(currentLvlIndex + 1);
            } else if (currentCatIndex < CATEGORIES.size() - 1) {
                nextCategory = CATEGORIES.get(currentCatIndex + 1);
                nextLevel = "Easy";
            } else {
                VBox finalSummary = createSummaryLayout(
                        "CONGRATULATIONS! ALL LEVELS COMPLETE!",
                        100.0,
                        true,
                        "Return to Main Menu",
                        () -> showMainMenu()
                );

                gamePanel.getChildren().add(finalSummary);
                gamePanel.setAlignment(Pos.CENTER);
                animateSuccess((Button) finalSummary.getChildren().get(4));
                return;
            }

            if (currentProgressMap.containsKey(nextCategory) && currentProgressMap.get(nextCategory).containsKey(nextLevel)) {
                currentProgressMap.get(nextCategory).put(nextLevel, false);
            }
            nextButtonText = "Start " + nextCategory + " - " + nextLevel;

            summaryLayout = createSummaryLayout(
                    "Level Passed!",
                    percentage,
                    true,
                    nextButtonText,
                    () -> startCategoryLevel(nextCategory, nextLevel)
            );

            animateSuccess((Button) summaryLayout.getChildren().get(4));

        } else {
            if (failureSoundPlayer != null) { failureSoundPlayer.stop(); failureSoundPlayer.play(); }

            nextCategory = currentCategory;
            nextLevel = currentLevel;
            nextButtonText = "Replay " + nextCategory + " - " + currentLevel;

            summaryLayout = createSummaryLayout(
                    "Level Failed!",
                    percentage,
                    false,
                    nextButtonText,
                    () -> startCategoryLevel(nextCategory, nextLevel)
            );

            animateShake((Button) summaryLayout.getChildren().get(4));
        }

        gamePanel.getChildren().add(summaryLayout);
        gamePanel.setAlignment(Pos.CENTER);
    }

    // --- ANIMATION HELPER METHODS ---
    private void animateSuccess(Button button) {
        button.getStyleClass().add("correct-answer");
        ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
        st.setByX(0.1); st.setByY(0.1); st.setCycleCount(2); st.setAutoReverse(true); st.play();
    }

    private void animateShake(Button button) {
        button.getStyleClass().add("incorrect-answer");
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), button);
        tt.setByX(10); tt.setCycleCount(4); tt.setAutoReverse(true); tt.play();
    }

    private void animatePhotoShake() {
        RotateTransition rt = new RotateTransition(Duration.millis(100), mediaImageView);
        rt.setByAngle(5); rt.setCycleCount(4); rt.setAutoReverse(true); rt.play();
    }

    private void animateCorrectButton(Button button) {
        button.getStyleClass().add("correct-answer");
        ScaleTransition st = new ScaleTransition(Duration.millis(300), button);
        st.setToX(1.05); st.setToY(1.05); st.play();
    }

    private void animateFadeIn(Label label) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), label);
        ft.setFromValue(0.0); ft.setToValue(1.0); ft.play();
    }

    private Button getButtonForIndex(int index) {
        return switch (index) {
            case 0 -> optionAButton;
            case 1 -> optionBButton;
            case 2 -> optionCButton;
            case 3 -> optionDButton;
            default -> {
                System.err.println("Error: Attempted to get button for unexpected index: " + index);
                yield null;
            }
        };
    }

    private void setOptionsDisabled(boolean disable) {
        optionAButton.setDisable(disable);
        optionBButton.setDisable(disable);
        optionCButton.setDisable(disable);
        optionDButton.setDisable(disable);
    }

    private void resetButtonStyles() {
        optionAButton.getStyleClass().removeAll("correct-answer", "incorrect-answer");
        optionBButton.getStyleClass().removeAll("correct-answer", "incorrect-answer");
        optionCButton.getStyleClass().removeAll("correct-answer", "incorrect-answer");
        optionDButton.getStyleClass().removeAll("correct-answer", "incorrect-answer");
        optionAButton.setScaleX(1.0); optionAButton.setScaleY(1.0);
        optionBButton.setScaleX(1.0); optionBButton.setScaleY(1.0);
        optionCButton.setScaleX(1.0); optionCButton.setScaleY(1.0);
        optionDButton.setScaleX(1.0); optionDButton.setScaleY(1.0);
    }
}