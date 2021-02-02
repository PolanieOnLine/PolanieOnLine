package games.stendhal.server.entity.mapstuff.useable;

import java.util.stream.Stream;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ImageEffectEvent;
import games.stendhal.server.events.SoundEvent;

public class SourceEntity extends PlayerActivityEntity {
	private static final Logger logger = Logger.getLogger(SourceEntity.class);

	/**
	 * The chance that prospecting is successful.
	 */
	private static final double FINDING_PROBABILITY = 0.02;

	private final String startSound = "pickaxe_01";
	private final String successSound = "rocks-1";
	private final int SOUND_RADIUS = 20;

	/**
	 * The equipment needed.
	 * [0] - zardzewiały kilof,
	 * [1] - kilof,
	 * [2] - kilof stalowy,
	 * [3] - kilof złoty,
	 * [4] - kilof obsydianowy.
	 */
	public static final String[] NEEDED_PICKS = { "zardzewiały kilof", "kilof",
			"kilof stalowy", "kilof złoty", "kilof obsydianowy" };

	/**
	 * Calculates the probability that the given player finds stone. This is
	 * based on the player's mining skills, however even players with no skills
	 * at all have a 5% probability of success.
	 *
	 * @param player
	 *            The player,
	 *
	 * @return The probability of success.
	 */
	private double getSuccessProbability(final Player player) {
		double probability = FINDING_PROBABILITY;

		final String skill = player.getSkill("mining");
		if (skill != null) {
			probability = probability * (player.getMining()/5);
		}

		return probability + player.useKarma(0.02);
	}

	@Override
	protected int getDuration(final Player player) {
		if (player.isEquipped(NEEDED_PICKS[4])) {
			return 6 + Rand.rand(4);
		} else if (player.isEquipped(NEEDED_PICKS[3])) {
			return 12 + Rand.rand(4);
		} else if (player.isEquipped(NEEDED_PICKS[2])) {
			return 16 + Rand.rand(4);
		} else if (player.isEquipped(NEEDED_PICKS[1])) {
			return 20 + Rand.rand(4);
		}

		return 30 + Rand.rand(4);
	}

	@Override
	protected boolean isPrepared(Player player) {
		for (final String itemName : NEEDED_PICKS) {
			if (player.isEquipped(itemName)) {
				return true;
			}
		}

		player.sendPrivateText("Potrzebujesz kilofa do wydobywania kamieni.");
		return false;
	}

	/**
	 * Decides if the activity was successful.
	 *
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isSuccessful(final Player player) {
		final int random = Rand.roll1D100();
		return (random <= (getSuccessProbability(player) * 100));
	}

	public void setMiningXP(final Player player, final boolean successful, final String itemName, final int xp) {
		final String skill = player.getSkill("mining");

		if (successful) {
			addEvent(new SoundEvent(successSound, SOUND_RADIUS, 100, SoundLayer.AMBIENT_SOUND));
	        notifyWorldAboutChanges();

			final Item item = SingletonRepository.getEntityManager().getItem(itemName);
			if (item != null) {
				String lastItem = Stream.of(NEEDED_PICKS).reduce((first,last) -> last).get();
				if (player.isEquipped(lastItem)) {
					int amount = Rand.throwCoin();
					((StackableItem) item).setQuantity(amount);
				}

				player.equipOrPutOnGround(item);
				player.incMinedForItem(item.getName(), item.getQuantity());
				if (skill != null) {
					player.incMiningXP(xp);
				}

				player.sendPrivateText("Wydobyłeś " + Grammar.a_noun(item.getTitle()) + ".");
			} else {
				logger.error("could not find item: " + itemName);
			}
		} else {
			if (skill != null) {
				player.incMiningXP((xp) / 10);
			}
			player.sendPrivateText("Nic nie wydobyłeś.");
		}
	}

	public void sendMessange(final Player player, final String mes) {
		addEvent(new SoundEvent(startSound, SOUND_RADIUS, 100, SoundLayer.AMBIENT_SOUND));
		player.sendPrivateText(mes);
		notifyWorldAboutChanges();
		addEvent(new ImageEffectEvent("mining", true));
		notifyWorldAboutChanges();
	}

	@Override
	protected void onFinished(final Player player, final boolean successful) {
		// Do nothing
	}
	@Override
	protected void onStarted(final Player player) {
		// Do nothing
	}
}