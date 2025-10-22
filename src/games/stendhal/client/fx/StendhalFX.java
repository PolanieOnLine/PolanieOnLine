package games.stendhal.client.fx;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX application entry point that coordinates the rebuilt client UI.
 */
public class StendhalFX extends Application {

        private FxClientController controller;

        @Override
        public void start(Stage primaryStage) {
                controller = new FxClientController(primaryStage);
                controller.start();
        }

        @Override
        public void stop() throws Exception {
                if (controller != null) {
                        controller.stop();
                }
                super.stop();
        }
}
