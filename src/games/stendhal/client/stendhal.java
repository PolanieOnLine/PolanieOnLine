/***************************************************************************
 *                   (C) Copyright 2003-2024 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client;

import java.awt.Dimension;
import java.io.File;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import games.stendhal.client.update.ClientGameConfiguration;
import games.stendhal.common.Debug;
import games.stendhal.common.MathHelper;
import games.stendhal.common.Version;
import marauroa.common.Log4J;
import marauroa.common.MarauroaUncaughtExceptionHandler;

/**
 * Main entry point for the PolanieOnline JavaFX client.
 */
public final class stendhal {

    private static final String LOG_FOLDER = "log/";
    private static final Logger logger = Logger.getLogger(stendhal.class);

    /** Use getGameFolder() where you need the real game data location. */
    private static final String STENDHAL_FOLDER;
    public static final String GAME_NAME;

    /** Directory for storing the persistent game data. */
    private static String gameFolder;

    /** Just a try to get Webstart working without additional rights. */
    static boolean WEB_START_SANDBOX = false;

    /** A latch that keeps the login status. */
    private static final CountDownLatch loginLatch = new CountDownLatch(1);

    /** Display sizes optimized for different screen resolutions. */
    private static final List<Dimension> displaySizes = new ArrayList<Dimension>(7);
    public static final Integer SIZE_INDEX = 0;
    public static final Integer SIZE_INDEX_LARGE = 2;
    public static final Integer SIZE_INDEX_WIDE = 2;
    public static final Integer DISPLAY_SIZE_INDEX = SIZE_INDEX_LARGE;

    private static final String DISPLAY_INDEX_PROPERTY = "display.index";

    public static final boolean SHOW_COLLISION_DETECTION = false;
    public static final boolean SHOW_EVERYONE_ATTACK_INFO = false;
    public static final boolean FILTER_ATTACK_MESSAGES = true;

    public static final int DEFAULT_FPS_LIMIT = 60;

    private static volatile int fpsLimit = DEFAULT_FPS_LIMIT;

    public static final String VERSION = Version.getVersion();

    // detect web start sandbox and init STENDHAL_FOLDER otherwise
    static {
        try {
            System.getProperty("user.home");
        } catch (final AccessControlException e) {
            WEB_START_SANDBOX = true;
        }

        /** We set the main game folder to the game name */
        GAME_NAME = ClientGameConfiguration.get("GAME_NAME");
        STENDHAL_FOLDER = File.separator + GAME_NAME.toLowerCase(Locale.ENGLISH) + File.separator;
        initGameFolder();
        initUsableDisplaySizes();
    }

    /** Make the class non-instantiable. */
    private stendhal() {
    }

    /**
     * Initialize the client game directory.
     * <p>
     * NOTE: IF YOU CHANGE THIS, CHANGE ALSO CORRESPONDING CODE IN Bootstrap.java
     */
    private static void initGameFolder() {
        String defaultFolder = System.getProperty("user.home") + STENDHAL_FOLDER;
        /*
         * Add any previously unrecognized unix like systems here. These will
         * try to use ~/.config/stendhal if the user does not have saved data
         * in ~/stendhal.
         *
         * OS X is counted in here too, but should it?
         *
         * List taken from:
         *      http://mindprod.com/jgloss/properties.html#OSNAME
         */
        String unixLikes = "AIX|Digital Unix|FreeBSD|HP UX|Irix|Linux|Mac OS X|Solaris";
        String system = System.getProperty("os.name");
        if (system.matches(unixLikes)) {
            // Check first if the user has important data in the default folder.
            File f = new File(defaultFolder + "user.dat");
            if (!f.exists()) {
                gameFolder = System.getProperty("user.home") + File.separator
                        + ".config" + File.separator + STENDHAL_FOLDER;
                return;
            }
        }
        // Everyone else should use the default top level directory in $HOME
        gameFolder = defaultFolder;
    }

    /**
     * Notify that the login has been performed and the client should proceed.
     */
    public static void setDoLogin() {
        loginLatch.countDown();
    }

    /**
     * Utility method for legacy callers that still wait for the login latch.
     * This keeps the public API stable while the JavaFX client is being built.
     */
    public static void awaitLogin() {
        try {
            loginLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get the maximum size of the visible game area.
     *
     * @return screen dimensions
     */
    public static Dimension getDisplaySize() {
        int sizeIndex = getDisplaySizeIndex();
        return displaySizes.get(MathHelper.clamp(sizeIndex, 0, Math.max(0, displaySizes.size() - 1)));
    }

    public static List<Dimension> getAvailableDisplaySizes() {
        return new ArrayList<Dimension>(displaySizes);
    }

    public static int getDisplaySizeIndex() {
        String spec = System.getProperty(DISPLAY_INDEX_PROPERTY);
        int parsedIndex = MathHelper.parseIntDefault(spec, DISPLAY_SIZE_INDEX);
        if ((parsedIndex < 0) || (parsedIndex >= displaySizes.size())) {
            logger.error("Invalid client size index: " + spec + " (" + parsedIndex + ")");
        }
        return MathHelper.clamp(parsedIndex, 0, Math.max(0, displaySizes.size() - 1));
    }

    public static void setDisplaySizeIndex(int index) {
        int sanitized = MathHelper.clamp(index, 0, Math.max(0, displaySizes.size() - 1));
        System.setProperty(DISPLAY_INDEX_PROPERTY, Integer.toString(sanitized));
    }

    public static int getFpsLimit() {
        return fpsLimit;
    }

    public static void setFpsLimit(int limit) {
        fpsLimit = Math.max(1, limit);
    }

    /**
     * Initialize list of dimensions that can be used for the client's
     * display area.
     */
    private static void initUsableDisplaySizes() {
        if (!displaySizes.isEmpty()) {
            return;
        }
        // Optimized display dimensions for display resolutions
        displaySizes.add(new Dimension(800, 600)); // Smaller 4:3 (1024x768 and smaller)
        displaySizes.add(new Dimension(864, 600)); // Larger 4:3 (1280x1024)
        displaySizes.add(new Dimension(928, 644)); // Baseline widescreen (1366x768 and larger)
        displaySizes.add(new Dimension(1056, 732)); // Widescreen +1 step
        displaySizes.add(new Dimension(1184, 820)); // Widescreen +2 steps (height capped)
        displaySizes.add(new Dimension(1280, 820)); // Widescreen +3 steps (height capped)
        displaySizes.add(new Dimension(1376, 820)); // Widescreen maximum (height capped)
    }

    /**
     * Starts the LogSystem.
     */
    private static void startLogSystem() {
        prepareLoggingSystemEnvironment();

        logger.info("-Setting base at :" + STENDHAL_FOLDER);

        Log4J.init("data/conf/log4j.properties");

        logger.info("Setting base at :" + STENDHAL_FOLDER);
        logger.info("PolanieOnLine " + VERSION);
        logger.info(Debug.PRE_RELEASE_VERSION);
        logger.info("Logging to directory: " + getLogFolder());

        String patchLevel = System.getProperty("sun.os.patch.level");
        if ((patchLevel == null) || (patchLevel.equals("unknown"))) {
            patchLevel = "";
        }

        logger.info("OS: " + System.getProperty("os.name") + " " + patchLevel
                + " " + System.getProperty("os.version") + " "
                + System.getProperty("os.arch"));
        logger.info("Java-Runtime: " + System.getProperty("java.runtime.name")
                + " " + System.getProperty("java.runtime.version") + " from "
                + System.getProperty("java.home"));
        logger.info("Java-VM: " + System.getProperty("java.vm.vendor") + " "
                + System.getProperty("java.vm.name") + " "
                + System.getProperty("java.vm.version"));
    }

    /**
     * Initialize the logging system.
     */
    private static void prepareLoggingSystemEnvironment() {
        // property configuration relies on this parameter
        System.setProperty("log.directory", getLogFolder());
        // log4j is capable of creating the directory automatically
    }

    /**
     * @return the name of the log folder
     */
    private static String getLogFolder() {
        return getGameFolder() + LOG_FOLDER;
    }

    /**
     * Get the location of persistent game client data.
     *
     * @return game's home directory
     */
    public static String getGameFolder() {
        return gameFolder;
    }

    /**
     * Main Entry point.
     *
     * @param args
     *            command line arguments
     */
    public static void main(final String[] args) {
        startLogSystem();
        MarauroaUncaughtExceptionHandler.setup(false);
        logger.info("Launching PolanieOnline JavaFX client");
        launchPolanieClient(args);
    }

    private static void launchPolanieClient(String[] args) {
        try {
            PolanieClientFX.launchClient(args);
        } catch (IllegalStateException alreadyRunning) {
            logger.warn("JavaFX runtime already active, skipping relaunch", alreadyRunning);
        }
    }
}
