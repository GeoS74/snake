package com.geos.snake;

import java.io.InputStream;
import javafx.util.Duration;
import java.util.ArrayDeque;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class App extends Application {

    private final int speed = 180;
    private final int maxYammys = 8;
    private final int freqYammy = 4;
    private final boolean godMode = false;
    
    private final int widthScorePane = 120;
    private final int cellCount = 20;
    private final int sizeRect = 18;
    private String direction = "UP";
    private Stage mainStage;
    private StackPane gamePane;
    private Label score;
    private GridPane gridPane;
    private ArrayDeque<Rectangle> snake;
    private Timeline snakeAnimation;
    AnimationTimer yammyAnimation;
    private final Random random = new Random();
    private int nextTime = 0;
    private final ArrayDeque<Rectangle> yummys = new ArrayDeque<>();
    private final String[] icons = {"banana.png", "kiwi.png", "orange.png", "raspberry.png", "watermelon.png"};
    private boolean pause = false;
    private int bestResult = 0;
     
    
    private StackPane getScorePane() {
        Rectangle background = new Rectangle(widthScorePane, cellCount*sizeRect);
        background.setFill(Color.LIGHTGREEN);
        
        Label scoreText = new Label("score:");
        score = new Label("0");

        HBox hbox = new HBox(scoreText, score);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(5);

        Label record  = new Label("record: "+bestResult);
        VBox vbox = new VBox(hbox, record);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(5);
                
        return new StackPane(background, vbox);
    }
    
    private StackPane getGameOverPane() {
        Rectangle background = new Rectangle(cellCount*sizeRect +widthScorePane, cellCount*sizeRect);
        background.setFill(Color.BLACK);
        background.setOpacity(0.7);
        
        Label message = new Label("game over");
        message.setFont(new Font(20));
        message.setTextFill(Color.WHITE);
        
        Label record = new Label("score "+score.getText());
        record.setFont(new Font(28));
        record.setTextFill(Color.WHITE);
        record.setPadding(new Insets(0,0,30,0));
        
        Button restartButton = new Button("continue");
        restartButton.setMinSize(120, 30);
        restartButton.setCursor(Cursor.HAND);
        
        restartButton.setOnMouseClicked(e -> startNewGame());
        restartButton.setOnAction(e -> startNewGame());
         
        
        VBox vbox = new VBox(message, record, restartButton);
        vbox.setAlignment(Pos.CENTER);
   
        return new StackPane(background, vbox);
    }
    
    private Scene getGameScene() {
        HBox hbox = new HBox(gridPane, getScorePane());

        gamePane = new StackPane(hbox);
                
        Scene scene = new Scene(gamePane, cellCount*sizeRect + widthScorePane, cellCount*sizeRect);

        // обработка нажатия клавиш
        scene.setOnKeyReleased((KeyEvent e) -> direction = antiReverse(e.getCode().toString()));

        return scene;
    }
     
    private void gameOver() {
        snakeAnimation.stop();
        yammyAnimation.stop();

        finalAnimation();
        
        int currentResult = Integer.parseInt(score.getText());
        if(bestResult < currentResult){
            bestResult = currentResult;
        }
    }
    
    private void finalAnimation(){
        Timeline fatality = new Timeline();
        
        fatality.getKeyFrames().add(new KeyFrame(Duration.millis(180), event -> {
            for(Rectangle s: snake)
                s.setVisible(!s.isVisible());
        }));
        fatality.setCycleCount(11);
        
        fatality.setOnFinished(e -> gamePane.getChildren().add(getGameOverPane()));
        
        fatality.play();
    }
    
    private void startNewGame() {
        makeGridPane();
        initSnake();
        direction = "UP";
        
        createYamyAnimation();
        createSnakeAnimation();
        
        mainStage.setScene(getGameScene());
        
        yammyAnimation.start();
        snakeAnimation.play();
    }
    
    @Override
    public void start(Stage stage) throws InterruptedException {
        mainStage = stage;
        mainStage.setTitle("Snake");        
        mainStage.setScene(getStartScene());
        mainStage.setResizable(false);
        mainStage.getIcons().add(loadImage("image/snake.png"));
        mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    private Scene getStartScene() {
        Label label = new Label("Snake");
        label.setFont(new Font(24));
        
        Button startButton = new Button("start");
        startButton.setMinSize(150, 30);
        startButton.setCursor(Cursor.HAND);
        
        startButton.setOnMouseClicked(e -> startNewGame());
        startButton.setOnAction(e -> startNewGame());
     
        VBox startPane = new VBox(label, startButton);
        startPane.setAlignment(Pos.CENTER);

        startPane.setSpacing(25);

        return new Scene(startPane, cellCount*sizeRect + widthScorePane, cellCount*sizeRect);
    }
    
    private String antiReverse(String newDirection) {
        switch (newDirection) {
            case "UP":
                if (direction.equals("DOWN")) 
                    return direction;
                break;
            case "DOWN":
                if (direction.equals("UP")) 
                    return direction;
                break;
            case "LEFT":
                if (direction.equals("RIGHT")) 
                    return direction;
                break;
            case "RIGHT":
                if (direction.equals("LEFT")) 
                    return direction;
                break;
            case "SPACE":
                pauseGame();
                return direction;
            default:
                newDirection = direction;
        }
        return newDirection;
    }
    
    private void initSnake() {
        snake = new ArrayDeque<Rectangle>();
        for (int i = 0; i < 7; i++) {
            Rectangle rect = getRectangleSnake();
            snake.add(rect);
            gridPane.add(rect, 5, i + 12);
        }
    }
    
    private void makeGridPane() {
        gridPane = new GridPane();

        for (int i = 0; i < cellCount; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPrefWidth(sizeRect);
            gridPane.getColumnConstraints().add(column);

            RowConstraints row = new RowConstraints();
            row.setPrefHeight(sizeRect);
            gridPane.getRowConstraints().add(row);
        }
        gridPane.setGridLinesVisible(false);
    }
    
    private void createSnakeAnimation(){
        snakeAnimation = new Timeline();
        
        snakeAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(speed), event -> {
            Rectangle head = snake.getFirst();

            int[] nextCoord = getNextCoord(GridPane.getColumnIndex(head), GridPane.getRowIndex(head));

            Node nextCell = getNextCell(nextCoord[0], nextCoord[1]);

            Rectangle nextRect = getRectangleSnake();

            if (nextCell != null) {
                if(godMode == false){
                    if (nextCell.getId().equals("snake")) 
                        gameOver();
                }
                
                if (nextCell.getId().substring(0, 5).equals("yammy")) {
                    
                    int weight = Integer.parseInt(nextCell.getId().substring(5));
                    score.setText((Integer.parseInt(score.getText())+weight)+"");
                    
                    snake.addFirst(nextRect);
                    gridPane.add(nextRect, nextCoord[0], nextCoord[1]);

                    yummys.removeFirstOccurrence(nextCell);
                    gridPane.getChildren().remove(nextCell);
                    return;
                }
            }

            snake.addFirst(nextRect);
            gridPane.add(nextRect, nextCoord[0], nextCoord[1]);

            Rectangle delRect = snake.pollLast();
            gridPane.getChildren().remove(delRect);
        }));

        snakeAnimation.setCycleCount(Timeline.INDEFINITE);
    }
 
    private void createYamyAnimation(){
        yammyAnimation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now / 1_000_000_000 >= nextTime) {
                    int delay = random.nextInt(freqYammy) + 1;

                    // первая вкусняшка через 3 секунды
                    if (nextTime == 0) {
                        nextTime = (int) (now / 1_000_000_000) + 3;
                        return;
                    }

                    makeYammy();
                    nextTime = (int) (now / 1_000_000_000) + delay;
                }
            }
        };
    }
    
    private void pauseGame() {
        pause = !pause;
        
        if(pause == true){
            snakeAnimation.stop();
            yammyAnimation.stop();
            gamePane.getChildren().add(getPausePane());
        } else {
            gamePane.getChildren().remove(gamePane.getChildren().size()-1);
            yammyAnimation.start();
            snakeAnimation.play();
        }
    }

    private StackPane getPausePane(){
        Rectangle background = new Rectangle(cellCount*sizeRect + widthScorePane, cellCount*sizeRect);
        background.setFill(Color.BLACK);
        background.setOpacity(0.7);
        
        Label message = new Label("pause");
        message.setFont(new Font(20));
        message.setTextFill(Color.WHITE);
        
        return new StackPane(background, message);
    }
       
    private void makeYammy() {
        Rectangle yammy = getRectangleYummy();

        int[] yammyCoord = getFreeCellCoord();

        if (yammyCoord == null) {
            return;
        }

        if (yummys.size() > maxYammys) {
            gridPane.getChildren().remove(yummys.removeFirst());
        }

        yummys.addLast(yammy);

        gridPane.add(yammy, yammyCoord[0], yammyCoord[1]);
    }

    private int[] getFreeCellCoord() {
        int x = random.nextInt(cellCount - 1);
        int y = random.nextInt(cellCount - 1);
        if (isCellEmpty(x, y)) {
            return new int[]{x, y};
        }

        if (snake.size() == cellCount * cellCount) {
            return null;
        }
        return getFreeCellCoord();
    }
    
    // ожидает получить относительный путь 
    private Image loadImage(String path){
        try {
            InputStream is = getClass().getResourceAsStream("/"+path);
            if(is != null)
                return new Image(is);
            
            return new Image("file:"+path);
            
        } catch(Exception e){
            return null;
        }
    }
    
    private Rectangle getRectangleSnake() {
        Rectangle r = new Rectangle(sizeRect-1, sizeRect-1, Color.GREEN);
//        Image image = new Image("file:image/scales.png");
//        Image image = new Image(getClass().getResourceAsStream("/image/scales.png"));
        r.setFill(new ImagePattern(loadImage("image/scales.png")));
        r.setId("snake");
        return r;
    }

    private Rectangle getRectangleYummy() {
        int i = random.nextInt(icons.length);
        Rectangle r = new Rectangle(sizeRect + 0, sizeRect + 0, Color.RED);
//        Image image = new Image("file:image/" + icons[i]);
//        Image image = new Image(getClass().getResourceAsStream("/image/" + icons[i]));
        r.setFill(new ImagePattern(loadImage("image/" + icons[i])));
        r.setId("yammy"+(i*3+1)); // id состоит из префикса и веса вкусняшки
        return r;
    }

    private int[] getNextCoord(int colIndex, int rowIndex) {
        if (direction.equals("UP")) {
            rowIndex--;
            if (rowIndex < 0) {
                rowIndex = cellCount - 1;
            }
        }
        if (direction.equals("DOWN")) {
            rowIndex++;
            if (rowIndex > cellCount - 1) {
                rowIndex = 0;
            }
        }

        if (direction.equals("LEFT")) {
            colIndex--;
            if (colIndex < 0) {
                colIndex = cellCount - 1;
            }
        }

        if (direction.equals("RIGHT")) {
            colIndex++;
            if (colIndex > cellCount - 1) {
                colIndex = 0;
            }
        }

        return new int[]{colIndex, rowIndex};
    }
    
    private boolean isCellEmpty(int x, int y) {
        return gridPane.getChildren().stream().filter((node) -> {
            return GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y;
        }).findFirst().isEmpty();
    }

    private Node getNextCell(int x, int y) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y) {
                return node;
            }
        }
        return null;
    }
    
}