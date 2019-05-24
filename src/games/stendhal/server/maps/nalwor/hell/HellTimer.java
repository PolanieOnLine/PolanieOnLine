package games.stendhal.server.maps.nalwor.hell;

import java.util.IdentityHashMap;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.MathHelper;
import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.config.annotations.ServerModeUtil;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.ZoneEnterExitListener;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.GlobalVisualEffectEvent;
import marauroa.common.game.RPObject;

/**
 * Handles moving the player from hell to the pit, at irrwegular intervals.
 * The slot has form "last_kickout_time;status", where the kickout time is used
 * to decide if the player should be kicked sooner than normally. The status
 * indicates if the player has been noticed, and can be used to kick the player
 * out immediately.
 */
public class HellTimer implements ZoneConfigurator, ZoneEnterExitListener {
	private static final String QUEST_SLOT = "hell_timer";
	/**
	 * The mean time player may normally stay in hell (excluding grace time).
	 * The actual time is random.
	 */
	private static final int MEAN_WAIT_TIME = MathHelper.SECONDS_IN_ONE_HOUR;
	/**
	 * The mean time player may stay in hell (excluding grace time) when they
	 * have been caught recently. The actual time is random.
	 */
	private static final int SHORT_WAIT_TIME = 3 * MathHelper.SECONDS_IN_ONE_MINUTE;
	/**
	 * The time in minutes that the player should stay away from hell until the
	 * guardian has forgotten about them.
	 */
	private static final int GUARDIAN_WARNED_TIME = 6 * MathHelper.MINUTES_IN_ONE_HOUR;
	/**
	 * The time how long a caught state is considered valid. The player can log
	 * out before being moved to the pit. Normally they'd be moved immediately
	 * to the pit on login, but server releases can move players out of the
	 * hell. So the state expires to avoid the situation where a player enters
	 * the hell through the chasm but is immediately moved to the Pit - possibly
	 * after not even having played for a long time.
	 */
	private static final int CAUGHT_EXPIRE_TIME = 5 * GUARDIAN_WARNED_TIME;
	/**
	 * The player can stay in hell during the grace time, so that they can pick
	 * up items left on the ground etc. During the grace time the player is
	 * sent messages about what is happening at specified intervals.
	 * The full grace time is 3 * GRACE_TIME_INTERVAL in seconds.
	 */
	private static final int GRACE_TIME_INTERVAL = 5;
	private static final int LAWYER_FEE = 10000;
	private static final String STD_MSG = "Jakaś nieprzeparta siła wciąga cię do Piekła.";
	private static final String LAWYER_MSG = "Prawnik wysyła cię do żniwiarzy.";
	private static final String[][] MESSAGES = {
		{ "Strażnik piekieł krzyczy: Hej, co robią żywe dusze w piekle?"
			+ " Będziesz musiał odpowiedzieć na pytania od żniwiarzy.", STD_MSG },
		{ "Ognik brander krzyczy: Niepróbuj mnie oszukać! Nie oznaczę żywej"
			+ " skóry. Mógłby być źle dobrany kolor, a proces transformacji nie może"
			+ " być wykonany każdego dnia poprawnie, tak jak w przypadku martwej duszy."
			+ " Nie zdobędziesz ode mnie brandingu! I nie wracaj, dopóki nie umrzesz!", STD_MSG },
		{ "Biczownik krzyczy: *slash*. Hmm, co to za dziwny krzyk. Nie"
			+ " sądzę, że ty należysz tutaj. Spróbuj wyjaśnić to szefom!",
			STD_MSG },
		{ "Mistrz grabarz krzyczy: Ach, to będzie fajna, opalona skórka ..."
			+ " yikes, ta rzecz jest żywa! WYNOŚ SIĘ STĄD! Nie będę używał"
			+ " zwierzęcego futra, która może odrastać!", STD_MSG },
		{ "Praktyczny ściągacz do paznokci krzyczy: *tug*. Huh, to było zbyt luźne"
			+ " i wygląda na to, że odrastają powoli. Przykro mi, ale coś musi być nie"
			+ " tak z twoimi paznokciami, wyślę cię do moich przełożonych na sprawdzenie.", STD_MSG },
		{ "Kuchnia dusz krzyczy: Och, nie jesteś wystarczająco dojrzały do ​​gotowania."
			+ " Będę musiał zgłosić cię do żniwiarzy. Ale nie smuć się. *pat* "
			+ "*pat*. Za kilka lat będziesz martwy, a potem będziemy gotować"
			+ " cię każdego dnia! Obiecuję, że sprawię, że olej stanie się "
			+ " dla ciebie gorący.", STD_MSG },
		{ "Prawnik krzyczy: Zgodnie z umową, do której się zgodziłeś, spadając w"
			+ " przepaść, surowo zabrania się przebywania w piekle, gdy się żyje."
			+ " Możesz temu zaradzić, tracąc status żywej istoty. Jednakże,"
			+ " jako twój doradca prawny, radzę, że byłoby to bardziej korzystne"
			+ " dla ... zabierasz sprawę do żniwiarzy w arbitrażu."
			+ " Dziękuję za opłacenie mnie w wysokości $FEE money! Zalecam, abyś"
			+ " ponownie się ze mną skontaktował, na wypadek, gdybyś odwiedził jeszcze "
			+ " raz piekło żywy. Żegnaj, miło było robić z tobą interesy!", LAWYER_MSG },
		{ "Woźny piekieł krzyczy: Idź stąd! Mam dość pracy z demonami, a martwi robią bałagan.", STD_MSG },
		{ "Księgowy piekieł krzyczy: Nie jesteś zarejestrowany jako rezydent tutaj."
			+ " Proszę w takim razie wypełnij oto te formularze F42 oraz H6, aby"
			+ " aby ubiegać się o pozwolenie na zarejestrowanie się jako kandydat"
			+ " do złożenia wniosku o pozwolenie na możliwy pobyt w Piekle.",
				"Zgłaszasz się na ochotnika, aby wskoczyć do Otchłani." },
		{ "Sprzedawczyni piekieł krzyczy: Witaj przyjacielu! Mam wspaniałą ofertę"
			+ " tylko dla ciebie, $NAME! Spójrz na broszury o spędzeniu swojego"
			+ " życia pozagrobowego w Piekle, podczas gdy mówię o wyjątkowej okazji,"
			+ " $NAME, to zarejestrowaliśmy już ciebie osobiście."
			+ " Masy zabitych niewinnych stworzeń sprawiają, że kwalifikujesz się do naszego"
			+ " platynowego programu, a na dodatek osobiście się dla ciebie"
			+ " przygotowaliśmy ...", "Wskakujesz do dołu, aby uciec od"
			+ " sprzedawcy."},
		{ "Nauczyciel piekieł krzyczy: $NAME! Zauważyłem cię! Co ty sobie myślisz"
			+ " przychodząc tutaj? Idź natychmiast do domu! Oraz chcę, żebyś"
			+ " napisał #''Ukończę pracę domową przed rozpoczęciem gry online''."
			+ " Sto razy ... Najlepiej pismo odręczne. Do poniedziałku."
			+ " Niech to zostanie podpisane przez twoich rodziców. Kiedy skończysz,"
			+ " napiszesz wypracowanie na tysiąc słów na temat #''Piekło nie jest miejscem dla żywych''.",
			"Nauczyciel odwraca się, chwytając laskę, a ty decydujesz,"
			+ " że lepiej zacząć biec do dołu."
		}
	};
	private static final String PIT_ZONE_NAME = "int_hell_pit";

	Map<Player, TurnListener> runningTimers = new IdentityHashMap<>();

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		zone.addZoneEnterExitListener(this);
	}

	@Override
	public void onEntered(RPObject object, StendhalRPZone zone) {
		if (object instanceof Player) {
			Player player = (Player) object;
			// Disable moving admins out, except on the test server
			if (player.getAdminLevel() >= 1000 && !ServerModeUtil.isTestServer()) {
				return;
			}

			TurnListener timer;
			int seconds;
			if (new QuestInStateCondition(QUEST_SLOT, 1, "caught").fire(player, null, null)
					&& !new TimePassedCondition(QUEST_SLOT, 0, CAUGHT_EXPIRE_TIME).fire(player, null, null)) {
				// They are still considered caught.
				timer = new FinalTimer(player);
				seconds = 0;
			} else if (new TimePassedCondition(QUEST_SLOT, 0, GUARDIAN_WARNED_TIME).fire(player, null, null)) {
				timer = new TimerStage1(player, false);
				seconds = Rand.randExponential(MEAN_WAIT_TIME);
			} else {
				timer = new TimerStage1(player, true);
				player.sendPrivateText("Ponieważ zostałeś złapany w Piekle bez "
						+ "pozwolenia, strażnicy mogą być przygotowani "
						+ "na twój powrót");
				seconds = Rand.randExponential(SHORT_WAIT_TIME);
			}
			runningTimers.put(player, timer);
			SingletonRepository.getTurnNotifier().notifyInSeconds(seconds, timer);
		}
	}

	@Override
	public void onExited(RPObject object, StendhalRPZone zone) {
		if (object instanceof Player) {
			TurnListener listener = runningTimers.get(object);
			if (listener != null) {
				SingletonRepository.getTurnNotifier().dontNotify(listener);
				runningTimers.remove(object);
			}
		}
	}

	private class TimerStage1 implements TurnListener {
		private final Player player;
		private final boolean recaught;

		TimerStage1(Player player, boolean recaught) {
			this.player = player;
			this.recaught = recaught;
		}

		@Override
		public void onTurnReached(int currentTurn) {
			player.sendPrivateText(NotificationType.SCENE_SETTING, "Piekielny urzędnik zauważył cię i podchodzi.");
			// The state is set immediately so that the player can't avoid
			// being kicked out by logging out when they get the message
			new SetQuestToTimeStampAction(QUEST_SLOT, 0).fire(player, null, null);
			new SetQuestAction(QUEST_SLOT, 1, "caught").fire(player, null, null);
			TurnListener timer = new TimerStage2(player, recaught);
			runningTimers.put(player, timer);
			SingletonRepository.getTurnNotifier().notifyInSeconds(GRACE_TIME_INTERVAL, timer);
		}
	}

	/**
	 * Timer that sends the player the message from the official, and does any
	 * extra action.
	 */
	private class TimerStage2 implements TurnListener {
		private final Player player;
		private final boolean recaught;

		TimerStage2(Player player, boolean recaught) {
			this.player = player;
			this.recaught = recaught;
		}

		@Override
		public void onTurnReached(int currentTurn) {
			String[] msg;
			if (recaught) {
				msg = new String[]{"Strażnik piekieł krzyczy: Ha, złapałem cię."
						+ " Żniwiarze ostrzegli mnie, że mógłbyś spróbować tu wrócić.",
						STD_MSG };
			} else {
				msg = Rand.rand(MESSAGES);
			}
			String message = msg[0].replaceAll("\\$NAME", player.getName());
			if (LAWYER_MSG.equals(msg[1])) {
				// In principle a player can logout before being deducted the
				// money. That's a feature, not a bug - that can be interpreted
				// as the player jumping to the pit as soon as noticing the
				// official, and not giving them chance to talk.
				int sum = 0;
				for (Item item : player.getAllEquipped("money")) {
					sum += ((StackableItem) item).getQuantity();
				}
				int fee = Math.min(sum, LAWYER_FEE);
				new DropItemAction("money", fee).fire(player, null, null);
				message = msg[0].replaceAll("\\$FEE", Integer.toString(fee));
			}
			player.sendPrivateText(message);
			TurnListener timer = new TimerStage3(player, msg[1]);
			runningTimers.put(player, timer);
			SingletonRepository.getTurnNotifier().notifyInSeconds(GRACE_TIME_INTERVAL, timer);
		}
	}

	/**
	 * Timer that sends the player the leaving message and starts blanking the
	 * screen.
	 */
	private class TimerStage3 implements TurnListener {
		private final Player player;
		private final String message;

		TimerStage3(Player player, String message) {
			this.player = player;
			this.message = message;
		}

		@Override
		public void onTurnReached(int currentTurn) {
			player.sendPrivateText(NotificationType.SCENE_SETTING, message);
			TurnListener timer = new FinalTimer(player);
			runningTimers.put(player, timer);
			SingletonRepository.getTurnNotifier().notifyInSeconds(GRACE_TIME_INTERVAL, timer);
			player.addEvent(new GlobalVisualEffectEvent("blacken", 1000 * GRACE_TIME_INTERVAL));
		}
	}

	/**
	 * The timer that finally sends the player to the pit.
	 */
	private class FinalTimer implements TurnListener {
		private final Player player;

		FinalTimer(Player player) {
			this.player = player;
		}

		@Override
		public void onTurnReached(int currentTurn) {
			StendhalRPZone pit = SingletonRepository.getRPWorld().getZone(PIT_ZONE_NAME);
			if (!player.teleport(pit, 7, 10, Direction.UP, null)) {
				// Failing is extremely unlikely, but schedule a retry anyway
				// if that happens.
				SingletonRepository.getTurnNotifier().notifyInSeconds(GRACE_TIME_INTERVAL, this);
			} else {
				// once properly moved, clear the caught state
				new SetQuestAction(QUEST_SLOT, 1, "").fire(player, null, null);
				// ...but renew the time stamp, so that players can't easily
				// avoid any ill effects from the moving by just logging out and
				// waiting a bit. Anyway, it's about guardians remembering the
				// person so the time being in the pit matters. On the other
				// hand a player that spends a long time *in* the pit does not
				// look like someone who'd get immediately back, so the reapers
				// don't warn the guardians. (So the time stamp is not set at
				// *leaving*).
				new SetQuestToTimeStampAction(QUEST_SLOT, 0).fire(player, null, null);
			}
		}
	}
}
