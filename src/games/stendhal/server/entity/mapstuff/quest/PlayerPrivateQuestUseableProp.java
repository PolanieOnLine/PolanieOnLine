/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.quest;

import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

/**
 * Private quest prop that can be used by its owner.
 *
 * Visibility and collision are inherited from {@link PlayerPrivateQuestProp}.
 * The server additionally validates ownership and distance before delegating
 * the actual quest action to the subclass.
 */
public abstract class PlayerPrivateQuestUseableProp extends PlayerPrivateQuestProp implements UseListener {

	public static final String USEABLE_ENTITY_CLASS = "questuseable";

	public PlayerPrivateQuestUseableProp(final Player owner, final String tileset,
			final int tileIndex, final int tilesetColumns, final boolean solid) {
		super(owner, tileset, tileIndex, tilesetColumns, solid);
		put("class", USEABLE_ENTITY_CLASS);
		setCursor("ACTIVITY");
		setMenu("Użyj");
	}

	@Override
	public final boolean onUsed(final RPEntity user) {
		if (!(user instanceof Player)) {
			return false;
		}

		final Player player = (Player) user;
		if (!isOwnedBy(player)) {
			return false;
		}

		if (!player.nextTo(this)) {
			player.sendPrivateText("Musisz podejść bliżej.");
			return false;
		}

		return onUsedByOwner(player);
	}

	/**
	 * Executes the quest specific action after ownership and range checks.
	 *
	 * @param player owner of this private prop
	 * @return true when the use was handled successfully
	 */
	protected abstract boolean onUsedByOwner(Player player);
}
