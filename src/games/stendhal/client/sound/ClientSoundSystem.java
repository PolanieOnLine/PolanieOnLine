/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.sound;

import org.apache.log4j.Logger;

import games.stendhal.client.sound.facade.SoundSystemFacade;
import games.stendhal.client.sound.nosound.NoSoundFacade;
import games.stendhal.client.sound.sound.SoundSystemFacadeImpl;
import games.stendhal.client.sprite.DataLoader;

/**
 * Creates the client sound system early enough for the first screen to use
 * it, while retaining the no-sound fallback used by the existing client.
 */
public final class ClientSoundSystem {
	private static final Logger logger = Logger.getLogger(ClientSoundSystem.class);

	private ClientSoundSystem() {
	}

	/**
	 * @return an operational sound facade or a safe no-sound facade
	 */
	public static SoundSystemFacade create() {
		try {
			if ((DataLoader.getResource("data/sounds/ui/login.ogg") != null)
					|| (DataLoader.getResource("data/music/the_old_tavern.ogg") != null)) {
				return new SoundSystemFacadeImpl();
			}
		} catch (RuntimeException e) {
			logger.error("Could not initialize the client sound system", e);
		}
		return new NoSoundFacade();
	}
}
