package games.stendhal.client.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Hardware accelerated particle overlay for weather effects.
 */
public final class FxWeatherOverlay {

    private final Pane container;
    private final Canvas canvas;
    private final AnimationTimer timer;
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    private WeatherType currentWeather = WeatherType.NONE;
    private boolean active = true;
    private boolean suspended;
    private boolean running;
    private long lastFrameTime;
    private double fogPhase;

    public FxWeatherOverlay() {
        canvas = new Canvas();
        container = new Pane(canvas);
        container.setPickOnBounds(false);
        container.setMouseTransparent(true);
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render(now);
            }
        };
    }

    public Node getView() {
        return container;
    }

    public void resize(double width, double height) {
        double clampedWidth = Math.max(1.0, width);
        double clampedHeight = Math.max(1.0, height);
        if (canvas.getWidth() != clampedWidth) {
            canvas.setWidth(clampedWidth);
        }
        if (canvas.getHeight() != clampedHeight) {
            canvas.setHeight(clampedHeight);
        }
        resetParticles();
        updateTimerState();
    }

    public void setWeather(String weatherName) {
        WeatherType next = WeatherType.fromName(weatherName);
        if (next != currentWeather) {
            currentWeather = next;
            fogPhase = 0.0;
            resetParticles();
        }
        updateTimerState();
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            if (!active) {
                clear();
            }
            updateTimerState();
        }
    }

    public void setSuspended(boolean suspended) {
        if (this.suspended != suspended) {
            this.suspended = suspended;
            updateTimerState();
        }
    }

    private void updateTimerState() {
        boolean shouldRun = active && !suspended && currentWeather != WeatherType.NONE
                && canvas.getWidth() > 1 && canvas.getHeight() > 1;
        if (shouldRun && !running) {
            running = true;
            lastFrameTime = 0L;
            timer.start();
        } else if (!shouldRun && running) {
            running = false;
            timer.stop();
            clear();
        }
    }

    private void clear() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void resetParticles() {
        particles.clear();
        if (currentWeather == WeatherType.NONE) {
            clear();
            return;
        }
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 1 || height <= 1) {
            return;
        }
        switch (currentWeather) {
            case SNOW:
                initialiseSnow(width, height);
                break;
            case RAIN:
                initialiseRain(width, height);
                break;
            case FOG:
                initialiseFog(width, height);
                break;
            default:
                break;
        }
    }

    private void initialiseSnow(double width, double height) {
        int count = (int) Math.min(280, Math.max(120, (width * height) / 6000));
        for (int i = 0; i < count; i++) {
            particles.add(Particle.snow(random, width, height));
        }
    }

    private void initialiseRain(double width, double height) {
        int count = (int) Math.min(320, Math.max(140, (width * height) / 5000));
        for (int i = 0; i < count; i++) {
            particles.add(Particle.rain(random, width, height));
        }
    }

    private void initialiseFog(double width, double height) {
        int count = 12;
        for (int i = 0; i < count; i++) {
            particles.add(Particle.fog(random, width, height));
        }
    }

    private void render(long now) {
        if (currentWeather == WeatherType.NONE) {
            return;
        }
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 1 || height <= 1) {
            return;
        }
        if (lastFrameTime == 0L) {
            lastFrameTime = now;
            return;
        }
        double deltaSeconds = (now - lastFrameTime) / 1_000_000_000.0;
        lastFrameTime = now;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        switch (currentWeather) {
            case SNOW:
                renderSnow(gc, width, height, deltaSeconds);
                break;
            case RAIN:
                renderRain(gc, width, height, deltaSeconds);
                break;
            case FOG:
                renderFog(gc, width, height, deltaSeconds);
                break;
            default:
                break;
        }
    }

    private void renderSnow(GraphicsContext gc, double width, double height, double dt) {
        gc.setFill(Color.rgb(255, 255, 255, 0.95));
        for (Particle particle : particles) {
            particle.x += particle.vx * dt;
            particle.y += particle.vy * dt;
            particle.vx += particle.ax * dt;
            if (particle.y > height) {
                particle.resetSnow(random, width);
            }
            if (particle.x < -20) {
                particle.x = width + random.nextDouble() * 20;
            } else if (particle.x > width + 20) {
                particle.x = -random.nextDouble() * 20;
            }
            gc.setGlobalAlpha(particle.opacity);
            gc.fillOval(particle.x, particle.y, particle.size, particle.size);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void renderRain(GraphicsContext gc, double width, double height, double dt) {
        gc.setStroke(Color.rgb(160, 200, 255, 0.8));
        for (Particle particle : particles) {
            particle.x += particle.vx * dt;
            particle.y += particle.vy * dt;
            if (particle.y > height + 20) {
                particle.resetRain(random, width);
            }
            gc.setGlobalAlpha(particle.opacity);
            gc.setLineWidth(particle.size);
            gc.strokeLine(particle.x, particle.y, particle.x + particle.vx * 0.08,
                    particle.y + particle.length);
        }
        gc.setGlobalAlpha(1.0);
        gc.setLineWidth(1.0);
    }

    private void renderFog(GraphicsContext gc, double width, double height, double dt) {
        fogPhase += dt;
        double baseAlpha = 0.12 + (Math.sin(fogPhase * 0.6) + 1.0) * 0.08;
        gc.setFill(Color.rgb(200, 205, 210, baseAlpha));
        gc.fillRect(0, 0, width, height);
        gc.setGlobalAlpha(1.0);
        for (Particle particle : particles) {
            particle.x = (particle.x + particle.vx * dt + width + particle.size) % (width + particle.size);
            particle.y = (particle.y + particle.vy * dt + height + particle.size) % (height + particle.size);
            gc.setFill(Color.rgb(220, 225, 235, particle.opacity));
            gc.fillOval(particle.x - particle.size / 2.0, particle.y - particle.size / 2.0,
                    particle.size, particle.size);
        }
        gc.setGlobalAlpha(1.0);
    }

    private enum WeatherType {
        NONE,
        SNOW,
        RAIN,
        FOG;

        static WeatherType fromName(String weatherName) {
            if (weatherName == null || weatherName.isEmpty()) {
                return NONE;
            }
            String lower = weatherName.toLowerCase();
            if (lower.contains("snow")) {
                return SNOW;
            }
            if (lower.contains("rain")) {
                return RAIN;
            }
            if (lower.contains("fog") || lower.contains("cloud")) {
                return FOG;
            }
            return NONE;
        }
    }

    private static final class Particle {
        double x;
        double y;
        double vx;
        double vy;
        double ax;
        double size;
        double opacity;
        double length;

        static Particle snow(Random random, double width, double height) {
            Particle particle = new Particle();
            particle.x = random.nextDouble() * width;
            particle.y = random.nextDouble() * height;
            particle.vx = (random.nextDouble() * 30) - 15;
            particle.vy = 30 + random.nextDouble() * 55;
            particle.ax = (random.nextDouble() - 0.5) * 10;
            particle.size = 2.2 + random.nextDouble() * 2.8;
            particle.opacity = 0.55 + random.nextDouble() * 0.45;
            return particle;
        }

        static Particle rain(Random random, double width, double height) {
            Particle particle = new Particle();
            particle.x = random.nextDouble() * width;
            particle.y = random.nextDouble() * height;
            particle.vx = -50 + random.nextDouble() * 40;
            particle.vy = 380 + random.nextDouble() * 180;
            particle.size = 1.0 + random.nextDouble() * 1.4;
            particle.length = 8 + random.nextDouble() * 18;
            particle.opacity = 0.35 + random.nextDouble() * 0.4;
            return particle;
        }

        static Particle fog(Random random, double width, double height) {
            Particle particle = new Particle();
            particle.x = random.nextDouble() * width;
            particle.y = random.nextDouble() * height;
            particle.vx = (random.nextDouble() - 0.5) * 10;
            particle.vy = (random.nextDouble() - 0.5) * 6;
            particle.size = Math.max(width, height) * (0.15 + random.nextDouble() * 0.25);
            particle.opacity = 0.08 + random.nextDouble() * 0.12;
            return particle;
        }

        void resetSnow(Random random, double width) {
            x = random.nextDouble() * width;
            y = -random.nextDouble() * 40;
            vx = (random.nextDouble() * 30) - 15;
            vy = 30 + random.nextDouble() * 55;
            ax = (random.nextDouble() - 0.5) * 10;
            size = 2.2 + random.nextDouble() * 2.8;
            opacity = 0.55 + random.nextDouble() * 0.45;
        }

        void resetRain(Random random, double width) {
            x = random.nextDouble() * width;
            y = -20 - random.nextDouble() * 40;
            vx = -50 + random.nextDouble() * 40;
            vy = 380 + random.nextDouble() * 180;
            size = 1.0 + random.nextDouble() * 1.4;
            length = 8 + random.nextDouble() * 18;
            opacity = 0.35 + random.nextDouble() * 0.4;
        }
    }
}
