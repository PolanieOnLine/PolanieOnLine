package games.stendhal.client.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Basic JavaFX application shell that hosts the chat view.
 */
public class StendhalFX extends Application {

        @Override
        public void start(Stage primaryStage) {
                BorderPane root = new BorderPane();
                ChatView chatView = new ChatView();
                root.setCenter(chatView);
                root.setStyle("-fx-background-color: #101010;");

                Scene scene = new Scene(root, 720, 480);
                primaryStage.setTitle("PolanieOnLine");
                primaryStage.setScene(scene);
                primaryStage.show();

                chatView.addMessage("System", "Witamy w nowym czacie PolanieOnLine!", true);
        }
}
