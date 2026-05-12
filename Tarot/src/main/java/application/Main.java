package application;

import static common.Log.CTRL;

import java.io.IOException;

import org.apache.logging.log4j.Level;

import common.Log;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {

        @Override
        public void start(Stage primaryStage) {

                Parent root;

                try {
                	
                        root = FXMLLoader.load(getClass().getResource("/fxml/TcpView.fxml"));

                        Scene scene = new Scene(root);
                        primaryStage.setScene(scene);
                        primaryStage.sizeToScene();
                        primaryStage.show();

                }
                catch (IOException e) {
                        e.printStackTrace();
                        return;
                }

        }
        public static void main(String[] args) {
        			Log.setLevel(CTRL, Level.TRACE); 
                launch(args); // lance le thread de JavaFX qui lui même lancera la méthode start() ci dessus.
        }
}
