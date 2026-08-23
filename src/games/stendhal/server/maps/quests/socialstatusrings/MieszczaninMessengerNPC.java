/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import java.util.Arrays;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.PlayerPrivateSpeakerNPC;
import games.stendhal.server.entity.player.Player;

/** The settlement messenger captured by the attackers. */
final class MieszczaninMessengerNPC extends PlayerPrivateSpeakerNPC {

	MieszczaninMessengerNPC(final Player owner) {
		super(owner, "Radomir");
		setDescription("Oto Radomir, posłaniec z przysiółka. Jest poobijany, a ręce ma skrępowane grubym sznurem.");
		setEntityClass("man_001_npc");
		setGender("M");
		setIdleDirection(Direction.DOWN);
	}

	@Override
	protected void createDialog() {
		add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new OwnedCondition(false, false),
				ConversationStates.ATTENDING,
				"Ciszej. Mam związane ręce, a oni pilnują wyjścia. Dopóki któryś z napastników tu stoi, nie próbuj mnie uwalniać.",
				null);

		add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new OwnedCondition(true, false),
				ConversationStates.INFORMATION_7,
				"Całe szczęście, że jesteś. Stach wysłał mnie po pomoc do straży, ale dopadli mnie na trakcie. To nie był zwykły rabunek. Ci #napastnicy pilnowali drogi i chwytali każdego, kto próbował sprowadzić pomoc.",
				null);

		add(ConversationStates.INFORMATION_7,
				"napastnicy",
				new OwnedCondition(true, false),
				ConversationStates.INFORMATION_8,
				"Związali mnie i zaciągnęli tutaj, żebym nie wrócił z pomocą. Po drodze widziałem łupy z rozbitych wozów. Wśród nich są #narzędzia Stacha.",
				null);

		add(ConversationStates.INFORMATION_8,
				"narzędzia",
				new OwnedCondition(true, false),
				ConversationStates.INFORMATION_9,
				"Poznałem je od razu: młotek, piłę i dłuta, którymi naprawia wozy i przeprawy. Leżą przy zachodniej ścianie. Najpierw przetnij moje #więzy, żebym mógł stąd odejść.",
				null);

		add(ConversationStates.INFORMATION_9,
				Arrays.asList("więzy", "wolność", "uwolnić", "rozwiązać"),
				new OwnedCondition(true, false),
				ConversationStates.ATTENDING,
				"Na Peruna, wreszcie mogę ruszyć rękami. Dam radę wrócić sam. Zabierz narzędzia Stacha i zanieś mu je. Ja opowiem Dobrawie, co napastnicy urządzili na trakcie.",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (!isOwnedBy(player) || !MieszczaninHideoutProgress.isCleared(player)) {
							return;
						}
						MieszczaninHideoutProgress.markMessengerFreed(player);
						setDescription("Oto Radomir, posłaniec z przysiółka. Jest poobijany, ale po przecięciu więzów może już wrócić do domu.");
						final StendhalRPZone zone = getZone();
						if (zone != null) {
							MieszczaninHideoutInstanceFactory.ensureStolenTools(zone, player);
						}
					}
				});

		add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new OwnedCondition(true, true),
				ConversationStates.ATTENDING,
				"Dam sobie radę. Jeśli jeszcze ich nie zabrałeś, weź narzędzia Stacha spod zachodniej ściany.",
				null);

		addGoodbye("Spotkamy się w przysiółku.");
	}

	private final class OwnedCondition implements ChatCondition {
		private final boolean cleared;
		private final boolean freed;

		OwnedCondition(final boolean cleared, final boolean freed) {
			this.cleared = cleared;
			this.freed = freed;
		}

		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
			return isOwnedBy(player)
					&& MieszczaninHideoutProgress.isCleared(player) == cleared
					&& MieszczaninHideoutProgress.isMessengerFreed(player) == freed;
		}
	}
}
