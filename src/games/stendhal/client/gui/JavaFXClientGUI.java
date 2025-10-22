package games.stendhal.client.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import games.stendhal.client.GameLoop;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.UserContext;
import games.stendhal.client.Zone;
import games.stendhal.client.fx.FxPlatform;
import games.stendhal.client.fx.FxWeatherOverlay;
import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.stendhal;
import games.stendhal.client.update.ClientGameConfiguration;
import games.stendhal.common.Debug;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.User;
import games.stendhal.client.listener.PositionChangeListener;
import games.stendhal.common.NotificationType;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import marauroa.common.game.RPObject;

/**
 * JavaFX based implementation of {@link J2DClientGUI}. The class wraps the existing
 * Swing implementation inside a {@link SwingNode} and adds a lightweight JavaFX overlay
 * for status indicators.
 */
public class JavaFXClientGUI implements J2DClientGUI {

    private final SwingClientGUI delegate;
    private final boolean usingFxWeather;
    private final String storedWeatherPreference;
    private Stage stage;
    private SwingNode swingNode;
    private Label zoneLabel;
    private Label statusLabel;
    private Label weatherLabel;
    private FadeTransition zoneHighlight;
    private FadeTransition weatherHighlight;
    private FxWeatherOverlay weatherOverlay;
    private GaussianBlur unfocusedBlur;

    public JavaFXClientGUI(StendhalClient client, UserContext context,
            NotificationChannelManager channelManager, JFrame splash) {
        delegate = new SwingClientGUI(client, context, channelManager, splash, false);
        WtWindowManager windowManager = WtWindowManager.getInstance();
        String preference = windowManager.getProperty("ui.draw_weather", "true");
        storedWeatherPreference = preference;
        boolean preferWeather = Boolean.parseBoolean(preference);
        if (preferWeather) {
            windowManager.setProperty("ui.draw_weather", "false");
        }
        usingFxWeather = preferWeather;
        FxPlatform.ensureStarted();
        CountDownLatch stageLatch = new CountDownLatch(1);

        Platform.runLater(() -> {
            stage = new Stage();
            stage.setTitle(buildWindowTitle());
            applyStageIcon(stage);

            swingNode = new SwingNode();
            swingNode.setFocusTraversable(false);
            unfocusedBlur = new GaussianBlur(12);

            StackPane root = new StackPane();
            root.getStyleClass().add("stendhal-fx-root");

            weatherOverlay = new FxWeatherOverlay();
            weatherOverlay.setActive(usingFxWeather);

            VBox overlay = buildOverlay();
            StackPane.setAlignment(overlay, Pos.TOP_RIGHT);
            overlay.setPickOnBounds(false);

            Dimension preferredSize = delegate.getPreferredClientSize();
            if (preferredSize == null) {
                preferredSize = new Dimension(1024, 768);
            }

            root.setPrefSize(preferredSize.width, preferredSize.height);
            stage.setMinWidth(640);
            stage.setMinHeight(480);

            root.getChildren().add(swingNode);
            StackPane.setAlignment(weatherOverlay.getView(), Pos.CENTER);
            root.getChildren().add(weatherOverlay.getView());
            root.getChildren().add(overlay);

            Scene scene = new Scene(root, preferredSize.width, preferredSize.height);
            stage.setScene(scene);
            stage.setOnCloseRequest(evt -> {
                evt.consume();
                delegate.requestQuit(client);
            });

            scene.widthProperty().addListener((obs, oldValue, newValue) -> {
                updateSwingSize(newValue.doubleValue(), scene.getHeight());
                resizeWeather(newValue.doubleValue(), scene.getHeight());
            });
            scene.heightProperty().addListener((obs, oldValue, newValue) -> {
                updateSwingSize(scene.getWidth(), newValue.doubleValue());
                resizeWeather(scene.getWidth(), newValue.doubleValue());
            });

            stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                delegate.setExternalWindowActive(isFocused);
                applyFocusEffect(isFocused);
                refreshWeatherSuspension();
            });
            stage.iconifiedProperty().addListener((obs, wasIconified, isIconified) -> {
                delegate.setExternalWindowIconified(isIconified);
                refreshWeatherSuspension();
            });

            zoneHighlight = new FadeTransition(Duration.millis(240), zoneLabel);
            zoneHighlight.setFromValue(0.4);
            zoneHighlight.setToValue(1.0);
            zoneHighlight.setCycleCount(2);
            zoneHighlight.setAutoReverse(true);

            weatherHighlight = new FadeTransition(Duration.millis(320), weatherLabel);
            weatherHighlight.setFromValue(0.4);
            weatherHighlight.setToValue(1.0);
            weatherHighlight.setCycleCount(2);
            weatherHighlight.setAutoReverse(true);

            SwingUtilities.invokeLater(() -> swingNode.setContent(delegate.getRootComponent()));

            resizeWeather(preferredSize.width, preferredSize.height);

            stage.show();
            applyFocusEffect(stage.isFocused());
            refreshWeatherSuspension();
            stageLatch.countDown();
        });

        waitForStage(stageLatch);
        registerZoneListener(client);
        GameLoop.get().runAtQuit(() -> {
            if (usingFxWeather) {
                WtWindowManager.getInstance().setProperty("ui.draw_weather", storedWeatherPreference);
            }
            Platform.runLater(() -> {
                if (stage != null) {
                    stage.close();
                }
            });
        });
    }

    private void waitForStage(CountDownLatch latch) {
        boolean interrupted = false;
        while (latch.getCount() > 0) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateSwingSize(double width, double height) {
        int w = Math.max(640, (int) Math.round(width));
        int h = Math.max(480, (int) Math.round(height));
        SwingUtilities.invokeLater(() -> {
            JComponent root = delegate.getRootComponent();
            root.setPreferredSize(new Dimension(w, h));
            root.revalidate();
        });
    }

    private VBox buildOverlay() {
        zoneLabel = new Label("Strefa: -");
        zoneLabel.setTextFill(Color.WHITESMOKE);
        zoneLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        statusLabel = new Label("Status: offline");
        statusLabel.setTextFill(Color.ORANGERED);
        statusLabel.setStyle("-fx-font-size: 14px;");

        String initialWeatherText = usingFxWeather ? "Pogoda: brak" : "Pogoda: wyłączona";
        weatherLabel = new Label(initialWeatherText);
        weatherLabel.setTextFill(Color.LIGHTBLUE);
        weatherLabel.setStyle("-fx-font-size: 14px;");

        ToggleButton toggle = new ToggleButton("Panel stanu");
        toggle.setSelected(true);
        toggle.setFocusTraversable(false);
        toggle.setStyle("-fx-background-color: rgba(60,60,60,0.75); -fx-text-fill: white; -fx-background-radius: 6;");

        VBox infoBox = new VBox(6, zoneLabel, statusLabel, weatherLabel);
        infoBox.setAlignment(Pos.TOP_RIGHT);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle("-fx-background-color: rgba(20,20,20,0.78); -fx-background-radius: 10;" +
                "-fx-border-color: rgba(255,255,255,0.18); -fx-border-radius: 10; -fx-border-width: 1px;");
        infoBox.visibleProperty().bind(toggle.selectedProperty());
        infoBox.managedProperty().bind(infoBox.visibleProperty());

        VBox overlay = new VBox(8, toggle, infoBox);
        overlay.setAlignment(Pos.TOP_RIGHT);
        overlay.setPadding(new Insets(16));
        overlay.setMaxWidth(280);
        overlay.setMouseTransparent(false);

        return overlay;
    }

    private void registerZoneListener(StendhalClient client) {
        client.addZoneChangeListener(new StendhalClient.ZoneChangeListener() {
            @Override
            public void onZoneUpdate(Zone zone) {
                updateZone(zone);
            }

            @Override
            public void onZoneChangeCompleted(Zone zone) {
                updateZone(zone);
            }

            @Override
            public void onZoneChange(Zone zone) {
                updateZone(zone);
            }
        });
    }

    private void updateZone(Zone zone) {
        final String zoneName = (zone == null) ? "-" : zone.getReadableName();
        final String weatherName = (zone == null) ? null : zone.getWeatherName();
        final String formattedWeather = usingFxWeather
                ? formatWeatherLabel(weatherName)
                : "Pogoda: wyłączona";
        Platform.runLater(() -> {
            if (zoneLabel != null) {
                zoneLabel.setText("Strefa: " + zoneName);
                if (zoneHighlight != null) {
                    zoneHighlight.stop();
                    zoneLabel.setOpacity(0.4);
                    zoneHighlight.playFromStart();
                }
            }
            if (weatherLabel != null) {
                if (!formattedWeather.equals(weatherLabel.getText())) {
                    weatherLabel.setText(formattedWeather);
                    if (weatherHighlight != null) {
                        weatherHighlight.stop();
                        weatherLabel.setOpacity(0.4);
                        weatherHighlight.playFromStart();
                    }
                }
            }
            if (usingFxWeather && weatherOverlay != null) {
                weatherOverlay.setWeather(weatherName);
            }
        });
    }

    private void resizeWeather(double width, double height) {
        if (weatherOverlay != null) {
            weatherOverlay.resize(width, height);
        }
    }

    private void refreshWeatherSuspension() {
        if (!usingFxWeather) {
            return;
        }
        if (weatherOverlay != null && stage != null) {
            weatherOverlay.setSuspended(stage.isIconified() || !stage.isFocused());
        }
    }

    private void applyFocusEffect(boolean focused) {
        if (swingNode == null) {
            return;
        }
        if (focused) {
            swingNode.setEffect(null);
        } else if (unfocusedBlur != null) {
            swingNode.setEffect(unfocusedBlur);
        }
    }

    private String formatWeatherLabel(String weatherName) {
        if (weatherName == null || weatherName.isEmpty()) {
            return "Pogoda: brak";
        }
        String lower = weatherName.toLowerCase();
        if (lower.contains("snow")) {
            if (lower.contains("heavy")) {
                return "Pogoda: śnieżyca";
            }
            if (lower.contains("light")) {
                return "Pogoda: lekki śnieg";
            }
            return "Pogoda: śnieg";
        }
        if (lower.contains("rain")) {
            if (lower.contains("heavy")) {
                return "Pogoda: ulewa";
            }
            if (lower.contains("light")) {
                return "Pogoda: lekki deszcz";
            }
            return "Pogoda: deszcz";
        }
        if (lower.contains("fog")) {
            return "Pogoda: mgła";
        }
        if (lower.contains("cloud")) {
            return "Pogoda: chmury";
        }
        return "Pogoda: " + weatherName.replace('_', ' ');
    }

    private void applyStageIcon(Stage fxStage) {
        URL url = DataLoader.getResource(ClientGameConfiguration.get("GAME_ICON"));
        if (url != null) {
            fxStage.getIcons().add(new Image(url.toExternalForm()));
        }
    }

    private String buildWindowTitle() {
        String preRelease = "";
        if (Debug.PRE_RELEASE_VERSION != null) {
            preRelease = " - " + Debug.PRE_RELEASE_VERSION;
        }
        return ClientGameConfiguration.get("GAME_NAME") + " " + stendhal.VERSION + preRelease
                + " - darmowa gra MMORPG - polanieonline.eu";
    }

    private void updateStatus(boolean offline) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(offline ? "Status: offline" : "Status: połączony");
                statusLabel.setTextFill(offline ? Color.ORANGERED : Color.LIGHTGREEN);
            }
        });
    }

    @Override
    public void addDialog(Component dialog) {
        delegate.addDialog(dialog);
    }

    @Override
    public void addAchievementBox(String title, String description, String category) {
        delegate.addAchievementBox(title, description, category);
    }

    @Override
    @Deprecated
    public void addGameScreenText(double x, double y, String text, NotificationType type, boolean isTalking) {
        delegate.addGameScreenText(x, y, text, type, isTalking);
    }

    @Override
    @Deprecated
    public void addGameScreenText(Entity entity, String text, NotificationType type, boolean isTalking) {
        delegate.addGameScreenText(entity, text, type, isTalking);
    }

    @Override
    public void afterPainting() {
        delegate.afterPainting();
    }

    @Override
    public void beforePainting() {
        delegate.beforePainting();
    }

    @Override
    public void chooseOutfit() {
        delegate.chooseOutfit();
    }

    @Override
    public Collection<PositionChangeListener> getPositionChangeListeners() {
        return delegate.getPositionChangeListeners();
    }

    @Override
    public JFrame getFrame() {
        return delegate.getFrame();
    }

    @Override
    public boolean isOffline() {
        return delegate.isOffline();
    }

    @Override
    public void resetClientDimensions() {
        delegate.resetClientDimensions();
        if (stage != null) {
            Platform.runLater(() -> stage.sizeToScene());
        }
    }

    @Override
    public void requestQuit(StendhalClient client) {
        delegate.requestQuit(client);
    }

    @Override
    public void getVisibleRunicAltar() {
        delegate.getVisibleRunicAltar();
    }

    @Override
    public void setChatLine(String text) {
        delegate.setChatLine(text);
    }

    @Override
    public void setOffline(boolean offline) {
        delegate.setOffline(offline);
        updateStatus(offline);
    }

    @Override
    public void switchToSpellState(RPObject spell) {
        delegate.switchToSpellState(spell);
    }

    @Override
    public void triggerPainting() {
        delegate.triggerPainting();
    }

    @Override
    public void updateUser(User user) {
        delegate.updateUser(user);
    }
}
