package games.stendhal.client.fx;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import games.stendhal.client.fx.model.CharacterSlot;
import games.stendhal.client.fx.model.ChatMessage;

/**
 * Simple backend that simulates server responses for local development.
 */
public class DummyClientBackend implements FxClientBackend {

        private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "fx-backend");
                thread.setDaemon(true);
                return thread;
        });

        private final List<CharacterSlot> characters = new ArrayList<CharacterSlot>(Arrays.asList(
                        new CharacterSlot("Wojownik", 42, "Rycerz"),
                        new CharacterSlot("Lucznik", 28, "Zwiadowca"),
                        new CharacterSlot("Mag", 17, "Mag")));

        private final List<Consumer<ChatMessage>> chatListeners = new CopyOnWriteArrayList<Consumer<ChatMessage>>();
        private final Random random = new Random();

        private volatile boolean loggedIn;
        private volatile CharacterSlot activeCharacter;

        @Override
        public CompletableFuture<LoginResult> login(String host, int port, String username, String password) {
                return CompletableFuture.supplyAsync(() -> {
                        sleep(Duration.ofMillis(450));
                        if ((username == null) || username.trim().isEmpty()) {
                                return new LoginResult(false, "Nazwa użytkownika jest wymagana");
                        }
                        loggedIn = true;
                        notifySystem("Połączono z " + host + ":" + port + ".");
                        return new LoginResult(true, "Zalogowano pomyślnie");
                }, executor);
        }

        @Override
        public CompletableFuture<List<CharacterSlot>> loadCharacters() {
                return CompletableFuture.supplyAsync(() -> {
                        sleep(Duration.ofMillis(300));
                        return new ArrayList<CharacterSlot>(characters);
                }, executor);
        }

        @Override
        public CompletableFuture<Void> selectCharacter(String characterName) {
                return CompletableFuture.runAsync(() -> {
                        sleep(Duration.ofMillis(350));
                        if (!loggedIn) {
                                throw new IllegalStateException("Brak aktywnej sesji logowania");
                        }
                        for (CharacterSlot slot : characters) {
                                if (slot.getName().equals(characterName)) {
                                        activeCharacter = slot;
                                        notifySystem("Witamy bohatera " + characterName + "!");
                                        scheduleGreeting();
                                        return;
                                }
                        }
                        throw new IllegalArgumentException("Nie odnaleziono postaci " + characterName);
                }, executor);
        }

        @Override
        public void sendChatMessage(String message) {
                if ((message == null) || message.trim().isEmpty()) {
                        return;
                }
                String sender = (activeCharacter != null) ? activeCharacter.getName() : "Ty";
                broadcast(new ChatMessage(sender, message, false));
                scheduleEcho(message);
        }

        @Override
        public void addChatListener(Consumer<ChatMessage> listener) {
                if (listener != null) {
                        chatListeners.add(listener);
                }
        }

        @Override
        public void shutdown() {
                executor.shutdownNow();
        }

        private void broadcast(ChatMessage message) {
                for (Consumer<ChatMessage> listener : chatListeners) {
                        listener.accept(message);
                }
        }

        private void notifySystem(String content) {
                broadcast(new ChatMessage("System", content, true));
        }

        private void scheduleGreeting() {
                executor.submit(() -> {
                        sleep(Duration.ofSeconds(1));
                        if (activeCharacter != null) {
                                notifySystem(activeCharacter.getName() + ": pamiętaj, aby sprawdzić tablicę z zadaniami!");
                        }
                });
        }

        private void scheduleEcho(String original) {
                executor.submit(() -> {
                        sleep(Duration.ofMillis(600 + random.nextInt(400)));
                        if (activeCharacter != null) {
                                broadcast(new ChatMessage("Przyjaciel", "Słyszałem: " + original, false));
                        }
                });
        }

        private static void sleep(Duration duration) {
                try {
                        TimeUnit.MILLISECONDS.sleep(Math.max(1, duration.toMillis()));
                } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                }
        }
}
