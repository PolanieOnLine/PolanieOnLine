/*
 * @(#) src/games/stendhal/server/entity/item/scroll/InvitationScroll.java
 *
 * $Id$
 */
package games.stendhal.server.entity.item.scroll;

import java.util.Map;
import java.util.StringTokenizer;

//
//

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPRuleProcessor;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.marriage.MarriageQuestInfo;

/**
 * Represents an teleport scroll that acts as an invitation to an event. The
 * programmatic event name is in the <code>itemdata</code> attribute. As
 * they are by invitation, it ignores destination zone anti-teleport rules.
 */
public class InvitationScroll extends TeleportScroll {

	private static final String WEDDING_ZONE = "int_fado_church";
	private static final String WEDDING_SPOT = "12 20";

	private static final String HOTEL_ZONE = "int_fado_hotel_0";
	private static final String HOTEL_SPOT = "4 40";
	private static final MarriageQuestInfo marriageInfo = new MarriageQuestInfo();
	/*
	 * Creates a new invitation teleport scroll.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public InvitationScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public InvitationScroll(final InvitationScroll item) {
		super(item);
	}

	/**
	 * Try to teleport to a marked scroll itemdata style place.
	 *
	 * @param where
	 *            A location in the form of <em>zone x y</em>.
	 * @param player
	 *            The player to teleport.
	 *
	 * @return <code>true</code> if teleport was successful.
	 */
	protected boolean teleportTo(final String where, final Player player) {
		StringTokenizer st;
		StendhalRPZone zone;
		int x;
		int y;

		st = new StringTokenizer(where, " ");

		if (!st.hasMoreTokens()) {
			return false;
		}

		zone = SingletonRepository.getRPWorld().getZone(st.nextToken());

		if (!st.hasMoreTokens()) {
			return false;
		}

		try {
			x = Integer.parseInt(st.nextToken());
		} catch (final NumberFormatException ex) {
			return false;
		}

		if (!st.hasMoreTokens()) {
			return false;
		}

		try {
			y = Integer.parseInt(st.nextToken());
		} catch (final NumberFormatException ex) {
			return false;
		}

		return player.teleport(zone, x, y, null, player);
	}

	/**
	 * Is invoked when a teleporting scroll is actually used.
	 *
	 * @param player
	 *            The player who used the scroll and who will be teleported.
	 *
	 * @return <code>true</code> if teleport was successful.
	 */
	@Override
	protected boolean useTeleportScroll(final Player player) {
		final String dest = getItemData();

		if (dest == null) {
			player.sendPrivateText("Zaproszenie nie zostało jeszcze wypełnione.");
			return false;
		}

  		final String[] info = dest.split(",");
		if (info.length < 2) {
			player.sendPrivateText("Ten zwój jest tak stary, że stracił swoją magiczną moc.");
			return false;
		}

		if (info[0].equals("marriage")) {
			return handleTeleportToChurch(player, info[1]);
		} else if (info[0].equals("honeymoon")) {
			return handleTeleportToHotel(player, info[1]);
		} else {
			player.sendPrivateText("Coś musi być nie tak z tym zwojem weselnym.");
			return false;
		}
	}

	private boolean handleTeleportToChurch(final Player player, final String playerName) {
		final Player engagedPlayer = StendhalRPRuleProcessor.get().getPlayer(playerName);
		if (engagedPlayer == null) {
			player.sendPrivateText("Nie będzie ślubu dopóki " + playerName + " nie wejdzie do gry.");
			return false;
		}
		if (marriageInfo.isMarried(engagedPlayer)) {
			player.sendPrivateText(playerName + " już wziął ślub i to zaproszenie straciło moc.");
			return false;
		}
		if (marriageInfo.isMarried(player)) {
			player.sendPrivateText("Wygląda na to, że już jesteś żonaty, więc to zaproszenie nie jest ci potrzebne.");
			return false;
		}
		if (!marriageInfo.isEngaged(engagedPlayer) || !marriageInfo.isEngaged(player)) {
			player.sendPrivateText("Zaproszenie działa tylko dla par, które nadal są zaręczone.");
			return false;
		}
		return teleportTo(WEDDING_ZONE + " " + WEDDING_SPOT, player);
	}

	private boolean handleTeleportToHotel(final Player player, final String playerName) {
		// check player was original recipient
		if (!player.getTitle().equals(playerName)) {
			player.sendPrivateText("Ten zwój weselny został wręczony " + playerName + ".");
			return false;
		}
		// check player is inside a lovers room when they try to use it
		if (!player.getZone().getName().startsWith("int_fado_lovers_room")) {
			player.sendPrivateText("Ten zwój weselny można tylko użyć do wyjścia z Fado lovers room.");
			return false;
		}
		return teleportTo(HOTEL_ZONE + " " + HOTEL_SPOT, player);
	}

	/**
	 * Get a description of the event and it's time.
	 *
	 * @return A description.
	 */
	@Override
	public String describe() {
		/*
		 * Base description (set by creator?)
		 */
		if (hasDescription()) {
			return getDescription();
		} else {
			final String dest = getItemData();
			final String[] info = dest.split(",");
			if (info.length == 2) {
				// if this was set on creation in maps.quests.marriage.Engagement then both engaged players names could be added.
				if (info[0].equals("marriage")) {
					return "Czytasz: Serdecznie zapraszamy na ślub " + info[1] + ". Proszę potwierdzić kiedy i o której godzinie się pojawisz, a później użyj tego zwoju, aby dostać się do kościoła w Fado.";
				}
				if (info[0].equals("honeymoon")) {
					return "Oto zwój, który przeniesie Ciebie z Fado Lovers Room z powrotem do Hotelu w Fado.";
				}
				return "Zaproszenie na ślub.";
			}
			return "Zaproszenie na ślub.";
		}
	}
}
