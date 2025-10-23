package games.stendhal.client.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
import games.stendhal.client.actions.SlashActionRepository;
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
    private final String storedWeatherPreference;
    private String fallbackWeatherPreference;
    private boolean weatherPreferenceOverridden;
    private Stage stage;
    private SwingNode swingNode;
    private Label zoneLabel;
    private Label statusLabel;
    private Label weatherLabel;
    private Label weatherIntensityLabel;
    private FadeTransition zoneHighlight;
    private FadeTransition weatherHighlight;
    private FxWeatherOverlay weatherOverlay;
    private GaussianBlur unfocusedBlur;
    private CheckBox fxWeatherToggle;
    private Slider weatherIntensitySlider;
    private Button fullScreenButton;
    private CheckBox alwaysOnTopToggle;
    private boolean fxWeatherEnabled;
    private double weatherIntensity = 1.0;
    private Rectangle2D latestGamePaneBounds;
    private String latestWeatherName;
    private String latestZoneName = "-";
    private Pane overlayLayer;
    private VBox overlayPanel;
    private ToggleButton overlayToggle;
    private boolean overlayManuallyPositioned;
    private double overlayDragOffsetX;
    private double overlayDragOffsetY;

    public JavaFXClientGUI(StendhalClient client, UserContext context,
            NotificationChannelManager channelManager, JFrame splash) {
        delegate = new SwingClientGUI(client, context, channelManager, splash, false);
        WtWindowManager windowManager = WtWindowManager.getInstance();
        String preference = windowManager.getProperty("ui.draw_weather", "true");
        storedWeatherPreference = preference;
        fallbackWeatherPreference = preference;
        fxWeatherEnabled = Boolean.parseBoolean(preference);
        if (fxWeatherEnabled) {
            weatherPreferenceOverridden = true;
            windowManager.setProperty("ui.draw_weather", "false");
        }
        FxPlatform.ensureStarted();
        CountDownLatch stageLatch = new CountDownLatch(1);

        delegate.addGamePaneBoundsListener(bounds -> {
            if (bounds == null) {
                return;
            }
            Rectangle copy = new Rectangle(bounds);
            Platform.runLater(() -> applyWeatherViewport(new Rectangle2D(copy.getX(), copy.getY(),
                    copy.getWidth(), copy.getHeight())));
        });

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
            weatherOverlay.getView().setManaged(false);
            weatherOverlay.setIntensity(weatherIntensity);
            weatherOverlay.setActive(fxWeatherEnabled);
            if (latestWeatherName != null) {
                weatherOverlay.setWeather(latestWeatherName);
            }

            overlayLayer = new Pane();
            overlayLayer.setPickOnBounds(false);
            overlayLayer.setMouseTransparent(false);
            overlayLayer.setMinSize(0, 0);
            overlayLayer.prefWidthProperty().bind(root.widthProperty());
            overlayLayer.prefHeightProperty().bind(root.heightProperty());

            overlayPanel = buildOverlay();
            overlayPanel.setManaged(false);
            overlayLayer.getChildren().add(overlayPanel);
            overlayPanel.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> handleOverlayContainerChange());

            Dimension preferredSize = delegate.getPreferredClientSize();
            if (preferredSize == null) {
                preferredSize = new Dimension(1024, 768);
            }

            root.setPrefSize(preferredSize.width, preferredSize.height);
            stage.setMinWidth(640);
            stage.setMinHeight(480);

            root.getChildren().add(swingNode);
            StackPane.setAlignment(weatherOverlay.getView(), Pos.TOP_LEFT);
            root.getChildren().add(weatherOverlay.getView());
            root.getChildren().add(overlayLayer);
            StackPane.setAlignment(overlayLayer, Pos.TOP_LEFT);
            updateWeatherControlsState();
            updateWeatherNodeVisibility();

            Scene scene = new Scene(root, preferredSize.width, preferredSize.height);
            stage.setScene(scene);
            stage.setOnCloseRequest(evt -> {
                evt.consume();
                delegate.requestQuit(client);
            });

            scene.widthProperty().addListener((obs, oldValue, newValue) -> {
                updateSwingSize(newValue.doubleValue(), scene.getHeight());
                delegate.refreshGamePaneBounds();
                handleOverlayContainerChange();
            });
            scene.heightProperty().addListener((obs, oldValue, newValue) -> {
                updateSwingSize(scene.getWidth(), newValue.doubleValue());
                delegate.refreshGamePaneBounds();
                handleOverlayContainerChange();
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
            stage.alwaysOnTopProperty().addListener((obs, oldValue, newValue) -> {
                if (alwaysOnTopToggle != null && alwaysOnTopToggle.isSelected() != newValue) {
                    alwaysOnTopToggle.setSelected(newValue);
                }
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

            stage.show();
            stage.toFront();
            stage.requestFocus();
            if (alwaysOnTopToggle != null) {
                alwaysOnTopToggle.setSelected(stage.isAlwaysOnTop());
            }
            positionOverlayDefault();
            ensureOverlayInside();
            delegate.refreshGamePaneBounds();
            if (latestGamePaneBounds != null) {
                applyWeatherViewport(latestGamePaneBounds);
            } else {
                updateWeatherNodeVisibility();
            }
            applyFocusEffect(stage.isFocused());
            refreshWeatherSuspension();
            SwingUtilities.invokeLater(() -> delegate.getRootComponent().requestFocusInWindow());
            stageLatch.countDown();
        });

        waitForStage(stageLatch);
        registerZoneListener(client);
        GameLoop.get().runAtQuit(() -> {
            if (weatherPreferenceOverridden) {
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

        weatherLabel = new Label();
        weatherLabel.setTextFill(Color.LIGHTBLUE);
        weatherLabel.setStyle("-fx-font-size: 14px;");

        fxWeatherToggle = new CheckBox("FX pogoda");
        fxWeatherToggle.setSelected(fxWeatherEnabled);
        fxWeatherToggle.setTextFill(Color.LIGHTBLUE);
        fxWeatherToggle.setFocusTraversable(false);
        fxWeatherToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> setFxWeatherEnabled(isSelected));

        weatherIntensitySlider = new Slider(0.4, 1.6, weatherIntensity);
        weatherIntensitySlider.setMaxWidth(Double.MAX_VALUE);
        weatherIntensitySlider.setFocusTraversable(false);
        weatherIntensitySlider.valueProperty().addListener((obs, oldValue, newValue) ->
                setWeatherIntensity(newValue.doubleValue()));
        weatherIntensitySlider.setDisable(!fxWeatherEnabled);

        weatherIntensityLabel = new Label();
        weatherIntensityLabel.setTextFill(Color.LIGHTBLUE);
        weatherIntensityLabel.setStyle("-fx-font-size: 12px;");
        updateIntensityLabel(weatherIntensity);

        fullScreenButton = new Button();
        fullScreenButton.setFocusTraversable(false);
        fullScreenButton.setStyle("-fx-background-color: rgba(60,60,60,0.75); -fx-text-fill: white; -fx-background-radius: 6;");
        fullScreenButton.textProperty().bind(Bindings.when(stage.fullScreenProperty())
                .then("Tryb okna").otherwise("Pełny ekran"));
        fullScreenButton.setOnAction(evt -> stage.setFullScreen(!stage.isFullScreen()));
        fullScreenButton.setMaxWidth(Double.MAX_VALUE);

        alwaysOnTopToggle = new CheckBox("Zawsze na wierzchu");
        alwaysOnTopToggle.setFocusTraversable(false);
        alwaysOnTopToggle.setTextFill(Color.LIGHTBLUE);
        alwaysOnTopToggle.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (stage != null && stage.isAlwaysOnTop() != newValue) {
                stage.setAlwaysOnTop(newValue);
            }
        });

        Button screenshotButton = new Button("Zrzut ekranu");
        screenshotButton.setFocusTraversable(false);
        screenshotButton.setStyle("-fx-background-color: rgba(60,60,60,0.75); -fx-text-fill: white; -fx-background-radius: 6;");
        screenshotButton.setMaxWidth(Double.MAX_VALUE);
        screenshotButton.setOnAction(evt -> takeScreenshot());

        VBox infoBox = new VBox(6, zoneLabel, statusLabel, weatherLabel,
                fxWeatherToggle, weatherIntensitySlider, weatherIntensityLabel,
                fullScreenButton, alwaysOnTopToggle, screenshotButton);
        infoBox.setFillWidth(true);
        infoBox.setAlignment(Pos.TOP_RIGHT);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle("-fx-background-color: rgba(20,20,20,0.78); -fx-background-radius: 10;" +
                "-fx-border-color: rgba(255,255,255,0.18); -fx-border-radius: 10; -fx-border-width: 1px;");

        overlayToggle = new ToggleButton("Panel FX");
        overlayToggle.setSelected(false);
        overlayToggle.setFocusTraversable(false);
        overlayToggle.setStyle("-fx-background-color: rgba(60,60,60,0.75); -fx-text-fill: white; -fx-background-radius: 6;");
        infoBox.visibleProperty().bind(overlayToggle.selectedProperty());
        infoBox.managedProperty().bind(infoBox.visibleProperty());
        overlayToggle.selectedProperty().addListener((obs, wasSelected, selected) -> handleOverlayContainerChange());

        Label dragHandle = new Label("\u2630");
        dragHandle.setTextFill(Color.LIGHTGRAY);
        dragHandle.setStyle("-fx-font-size: 14px; -fx-padding: 2 6 2 6; -fx-background-color: rgba(60,60,60,0.4); -fx-background-radius: 6;");
        dragHandle.setOnMousePressed(this::beginOverlayDrag);
        dragHandle.setOnMouseDragged(this::continueOverlayDrag);

        HBox header = new HBox(8, dragHandle, overlayToggle);
        header.setAlignment(Pos.CENTER_RIGHT);

        VBox overlay = new VBox(8, header, infoBox);
        overlay.setAlignment(Pos.TOP_RIGHT);
        overlay.setPadding(new Insets(12));
        overlay.setMaxWidth(320);
        overlay.setMouseTransparent(false);
        overlay.setPickOnBounds(false);

        updateWeatherLabelText();

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
        latestZoneName = zoneName;
        latestWeatherName = weatherName;
        Platform.runLater(() -> {
            updateZoneLabel(zoneName);
            updateWeatherDisplay();
        });
    }

    private void updateZoneLabel(String zoneName) {
        if (zoneLabel == null) {
            return;
        }
        zoneLabel.setText("Strefa: " + zoneName);
        if (zoneHighlight != null) {
            zoneHighlight.stop();
            zoneLabel.setOpacity(0.4);
            zoneHighlight.playFromStart();
        }
    }

    private void updateWeatherDisplay() {
        updateWeatherLabelText();
        if (weatherOverlay != null) {
            weatherOverlay.setWeather(latestWeatherName);
        }
    }

    private void setWeatherIntensity(double intensityValue) {
        double clamped = Math.max(0.4, Math.min(1.6, intensityValue));
        double rounded = Math.round(clamped * 100.0) / 100.0;
        weatherIntensity = rounded;
        if (weatherOverlay != null) {
            weatherOverlay.setIntensity(rounded);
        }
        updateIntensityLabel(rounded);
        if (weatherIntensitySlider != null) {
            double sliderValue = weatherIntensitySlider.getValue();
            if (Math.abs(sliderValue - rounded) > 0.0001) {
                weatherIntensitySlider.setValue(rounded);
            }
        }
    }

    private void updateIntensityLabel(double intensity) {
        if (weatherIntensityLabel != null) {
            int percent = (int) Math.round(intensity * 100.0);
            weatherIntensityLabel.setText("Intensywność: " + percent + "%");
        }
    }

    private void handleOverlayContainerChange() {
        if (overlayPanel == null) {
            return;
        }
        if (overlayManuallyPositioned) {
            ensureOverlayInside();
        } else {
            positionOverlayDefault();
        }
    }

    private void beginOverlayDrag(MouseEvent event) {
        if (!event.isPrimaryButtonDown() || overlayLayer == null || overlayPanel == null) {
            return;
        }
        Point2D parentPoint = overlayLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
        overlayDragOffsetX = parentPoint.getX() - overlayPanel.getLayoutX();
        overlayDragOffsetY = parentPoint.getY() - overlayPanel.getLayoutY();
        overlayManuallyPositioned = true;
        event.consume();
    }

    private void continueOverlayDrag(MouseEvent event) {
        if (!event.isPrimaryButtonDown() || overlayLayer == null || overlayPanel == null) {
            return;
        }
        Point2D parentPoint = overlayLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
        relocateOverlay(parentPoint.getX() - overlayDragOffsetX, parentPoint.getY() - overlayDragOffsetY);
        event.consume();
    }

    private void relocateOverlay(double x, double y) {
        if (overlayLayer == null || overlayPanel == null) {
            return;
        }
        double maxX = Math.max(0, overlayLayer.getWidth() - overlayPanel.getWidth());
        double maxY = Math.max(0, overlayLayer.getHeight() - overlayPanel.getHeight());
        double clampedX = Math.min(Math.max(0, x), maxX);
        double clampedY = Math.min(Math.max(0, y), maxY);
        overlayPanel.relocate(clampedX, clampedY);
    }

    private void positionOverlayDefault() {
        if (overlayLayer == null || overlayPanel == null) {
            return;
        }
        double margin = 16.0;
        double layerWidth = overlayLayer.getWidth();
        double panelWidth = overlayPanel.prefWidth(-1);
        if (panelWidth <= 0) {
            panelWidth = overlayPanel.getWidth();
        }
        double x = Math.max(margin, layerWidth - panelWidth - margin);
        overlayPanel.relocate(x, margin);
    }

    private void ensureOverlayInside() {
        if (overlayLayer == null || overlayPanel == null) {
            return;
        }
        relocateOverlay(overlayPanel.getLayoutX(), overlayPanel.getLayoutY());
    }

    private void takeScreenshot() {
        SwingUtilities.invokeLater(() -> SlashActionRepository.get("takescreenshot").execute(new String[0], ""));
    }

    private void setFxWeatherEnabled(boolean enabled) {
        if (fxWeatherEnabled == enabled) {
            return;
        }
        fxWeatherEnabled = enabled;
        WtWindowManager manager = WtWindowManager.getInstance();
        if (enabled) {
            weatherPreferenceOverridden = true;
            fallbackWeatherPreference = manager.getProperty("ui.draw_weather", fallbackWeatherPreference);
            manager.setProperty("ui.draw_weather", "false");
        } else {
            manager.setProperty("ui.draw_weather", fallbackWeatherPreference);
        }
        if (weatherOverlay != null) {
            weatherOverlay.setActive(enabled);
            weatherOverlay.setWeather(latestWeatherName);
        }
        updateWeatherControlsState();
        updateWeatherLabelText();
        updateWeatherNodeVisibility();
        refreshWeatherSuspension();
    }

    private void updateWeatherControlsState() {
        if (fxWeatherToggle != null) {
            fxWeatherToggle.setSelected(fxWeatherEnabled);
        }
        if (weatherIntensitySlider != null) {
            weatherIntensitySlider.setDisable(!fxWeatherEnabled);
        }
    }

    private void updateWeatherNodeVisibility() {
        if (weatherOverlay == null) {
            return;
        }
        boolean hasBounds = latestGamePaneBounds != null
                && latestGamePaneBounds.getWidth() > 1 && latestGamePaneBounds.getHeight() > 1;
        weatherOverlay.getView().setVisible(fxWeatherEnabled && hasBounds);
    }

    private void applyWeatherViewport(Rectangle2D bounds) {
        if (bounds == null) {
            return;
        }
        latestGamePaneBounds = bounds;
        if (weatherOverlay != null) {
            weatherOverlay.getView().setLayoutX(bounds.getMinX());
            weatherOverlay.getView().setLayoutY(bounds.getMinY());
            weatherOverlay.resize(bounds.getWidth(), bounds.getHeight());
        }
        updateWeatherNodeVisibility();
    }

    private void updateWeatherLabelText() {
        if (weatherLabel == null) {
            return;
        }
        String newText = buildWeatherLabelText();
        String current = weatherLabel.getText();
        if (!newText.equals(current)) {
            weatherLabel.setText(newText);
            if (weatherHighlight != null) {
                weatherHighlight.stop();
                weatherLabel.setOpacity(0.4);
                weatherHighlight.playFromStart();
            }
        }
    }

    private String buildWeatherLabelText() {
        if (fxWeatherEnabled) {
            return formatWeatherLabel(latestWeatherName);
        }
        if (shouldDisplaySwingWeather()) {
            String base = formatWeatherLabel(latestWeatherName);
            if (!base.contains("(Swing)")) {
                base = base + " (Swing)";
            }
            return base;
        }
        return "Pogoda: wyłączona";
    }

    private boolean shouldDisplaySwingWeather() {
        if (fxWeatherEnabled) {
            return Boolean.parseBoolean(fallbackWeatherPreference);
        }
        return Boolean.parseBoolean(WtWindowManager.getInstance()
                .getProperty("ui.draw_weather", fallbackWeatherPreference));
    }

    private void refreshWeatherSuspension() {
        if (!fxWeatherEnabled) {
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
