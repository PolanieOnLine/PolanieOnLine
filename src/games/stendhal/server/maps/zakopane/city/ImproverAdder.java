package games.stendhal.server.maps.zakopane.city;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.ImprovableItem;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.SoundEvent;

public class ImproverAdder {
	private static final Logger logger = Logger.getLogger(ImproverAdder.class);

	private static final List<String> phrases = Arrays.asList("improve", "upgrade", "ulepsz", "ulepszyć", "udoskonalić");

	private ImprovableItem item;
	private Player player;

	private String currentImproveItem = null;
	private Integer currentImproveCount = null;
	private Integer currentImproveFee = null;

	private void reset() {
		currentImproveItem = null;
		currentImproveCount = null;
		currentImproveFee = null;
	}

	public void add(final ImproverNPC improver) {
		improver.add(ConversationStates.ATTENDING,
				phrases,
				null,
				ConversationStates.QUESTION_2,
				null,
				requestImproveAction(improver));

		improver.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Jak będziesz chciał udoskonalić swoje wyposażenie to podejdź.",
				null);

		// player dropped item before accepting
		improver.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(needsImproveCondition()),
				ConversationStates.ATTENDING,
				"Upuściłeś przedmiot?",
				null);

		// this should not happen
		improver.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				feeNotSetCondition(),
				ConversationStates.ATTENDING,
				"Wygląda na to, że nie mogę przetworzyć transakcji. Przepraszam.",
				null);

		improver.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new NotCondition(canAffordCondition())),
				ConversationStates.ATTENDING,
				"Nie masz dość pieniędzy.",
				null);

		improver.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(canAffordCondition()),
				ConversationStates.ATTENDING,
				null,
				improveAction());
	}

	private void setImproveItem(final String itemName) {
		currentImproveItem = itemName;
	}

	private void setImproveCount(final Player player) {
		int count = 0;
		for (final Item item: player.getAllEquipped(currentImproveItem)) {
			if (((ImprovableItem) item).isUpgradeable()) {
				count++;
			}
		}

		currentImproveCount = count;
	}

	private void calculateImproveFee() {
		currentImproveFee = 10000;
	}

	private ChatAction requestImproveAction(final ImproverNPC improver) {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

				String request = sentence.getTrimmedText();
				if (phrases.contains(request.toLowerCase())) {
					improver.say("Powiedz mi tylko co chcesz udoskonalić.");
					improver.setCurrentState(ConversationStates.ATTENDING);
					return;
				}

				for (final String rWord: phrases) {
					if (request.startsWith(rWord)) {
						request = request.substring(rWord.length() + 1);
						break;
					}
				}

				setImproveItem(request);
				setImproveCount(player);

				if (currentImproveCount == null) {
					String cannotImproveReply = null;
					if (cannotImproveReply == null) {
						cannotImproveReply = "Nie jestem w stanie udoskonalić #'" + Grammar.plural(currentImproveItem) + "'.";
					}

					improver.say(cannotImproveReply);
					improver.setCurrentState(ConversationStates.ATTENDING);
					return;
				} else if (currentImproveCount < 1) {
					String notCarryingReply = null;
					if (notCarryingReply == null) {
						notCarryingReply = "Nie posiadasz ani jednego przedmiotu #'" + Grammar.plural(currentImproveItem) + "', który jest możliwy do udoskonalenia.";
					}

					improver.say(notCarryingReply);
					improver.setCurrentState(ConversationStates.ATTENDING);
					return;
				}

				calculateImproveFee();

				String sayCountReply = null;
				if (sayCountReply == null) {
					final StringBuilder sb = new StringBuilder("Mogę ulepszyć to #'" + currentImproveItem);
					sb.append("' za " + Integer.toString(currentImproveFee) + " money. Chcesz, abym to zrobił?");

					sayCountReply = sb.toString();
				}

				improver.say(sayCountReply);
			}
		};
	}

	private ChatAction improveAction() {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser repairer) {
				player.drop("money", currentImproveFee);

				for (final Item item: player.getAllEquipped(currentImproveItem)) {
					final ImprovableItem improvable = (ImprovableItem) item;
					if (improvable.isUpgradeable()) {
						improvable.upgrade();
					}
				}

				String doneReply = null;
				if (doneReply == null) {
					doneReply = "Zrobione! Twój przedmiot #'" + currentImproveItem + "' został udoskonalony i jest lepszy od jego poprzedniego stanu!";
				}

				repairer.say(doneReply);
				repairer.addEvent(new SoundEvent(SoundID.COMMERCE, SoundLayer.CREATURE_NOISE));

			}
		};
	}

	private ChatCondition feeNotSetCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity improver) {
				if (currentImproveFee == null) {
					logger.error("Cannot create transaction, improve fee not set");
					return true;
				}

				return false;
			}
		};
	}

	private ChatCondition canAffordCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity improver) {
				return player.isEquipped("money", currentImproveFee);
			}
		};
	}

	private ChatCondition needsImproveCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, Entity improver) {
				return currentImproveCount != null && currentImproveCount > 0;
			}
		};
	}

	public class ImproverNPC extends SpeakerNPC {
		public ImproverNPC(String name) {
			super(name);
		}

		@Override
		public void onGoodbye(final RPEntity attending) {
			// reset item name, count, & fee to null
			reset();
		}
	}
}