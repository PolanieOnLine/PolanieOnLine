package games.stendhal.server.maps.warszawa.blacksmith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
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

	private static final List<String> improvePhrases = Arrays.asList("improve", "upgrade", "ulepsz", "udoskonalić");
	private static final List<String> checkPhrases = Arrays.asList("check", "see", "how much", "sprawdź", "zobacz", "ile");

	private String currentImproveItem = null;
	private Integer currentToImproveCount = null;
	private Integer currentImproveFee = null;

	private boolean foundMoreThanOne = false;

	private void reset() {
		currentImproveItem = null;
		currentToImproveCount = null;
		currentImproveFee = null;

		foundMoreThanOne = false;
	}

	private Integer currentGold = null;
	private Integer currentMithril = null;
	private Integer currentAmethyst = null;
	private Integer currentRuby = null;
	private Integer currentSapphire = null;
	private Integer currentWood = null;
	private Integer currentFeather = null;

	private void defaultNeededValue() {
		if (currentGold == null) {
			currentGold = 10;
		}
		if (currentMithril == null) {
			currentMithril = 4;
		}
		if (currentAmethyst == null) {
			currentAmethyst = 7;
		}
		if (currentRuby == null) {
			currentRuby = 12;
		}
		if (currentSapphire == null) {
			currentSapphire = 9;
		}
		if (currentWood == null) {
			currentWood = 25;
		}
		if (currentFeather == null) {
			currentFeather = 15;
		}
	}

	private final Map<String,Integer> items = new HashMap<String, Integer>() {{
		// load the default value
		defaultNeededValue();

		put("sztabka złota", currentGold);
		put("sztabka mithrilu", currentMithril);
		put("ametyst", currentAmethyst);
		put("rubin", currentRuby);
		put("szafir", currentSapphire);
		put("polano", currentWood);
		put("piórko", currentFeather);
	}};

	public void add(final ImproverNPC improver) {
		improver.add(ConversationStates.ATTENDING,
				improvePhrases,
				null,
				ConversationStates.QUESTION_1,
				null,
				requestImproveAction(improver));

		improver.add(ConversationStates.ATTENDING,
				checkPhrases,
				null,
				ConversationStates.ATTENDING,
				null,
				requestCheckImproveAction(improver));

		improver.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Jak będziesz chciał udoskonalić swoje wyposażenie to podejdź.",
				null);

		// player dropped item before accepting
		improver.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(needsImproveCondition()),
				ConversationStates.ATTENDING,
				"Upuściłeś przedmiot?",
				null);

		// this should not happen
		improver.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				feeNotSetCondition(),
				ConversationStates.ATTENDING,
				"Wygląda na to, że nie mogę przetworzyć transakcji. Przepraszam.",
				null);

		improver.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new NotCondition(canAffordCondition())),
				ConversationStates.ATTENDING,
				"Nie masz dość pieniędzy.",
				null);

		improver.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(canAffordCondition()),
				ConversationStates.ATTENDING,
				null,
				improveAction());
	}

	private void setImproveItem(final String itemName) {
		currentImproveItem = itemName;
	}

	private void countImproveItems(final Player player) {
		List<Item> equipped = player.getAllEquipped(currentImproveItem);

		int count = 0;
		for (Item i : equipped) {
			if (!((ImprovableItem) i).isMaxImproved()) {
				count++;
			}
		}
		currentToImproveCount = count;
	}
	
	private boolean hasItemToImprove() {
		if (currentToImproveCount > 0) {
			return true;
		}
		return false;
	}

	private void setImprove(final Player player, final ImproverNPC improver) {
		List<Item> equipped = player.getAllEquipped(currentImproveItem);
		if (!equipped.isEmpty()) {
			if(equipped.size() > 1) {
				foundMoreThanOne = true;
			}

			Item toImprove = equipped.iterator().next();
			for (Item i : equipped) {
				if (i.getImproves() > toImprove.getImproves()) {
						toImprove = i;
				}
			}

			countImproveItems(player);

			if (toImprove.getMaxImproves() > 0) {
				if (hasItemToImprove()) {
					for (Item i : equipped) {
						if (toImprove.isMaxImproved()
								&& (i.getImproves() < toImprove.getImproves())) {
							toImprove = i;
						} else if (i.getImproves() > toImprove.getImproves()) {
							toImprove = i;
						}
					}
					int improves = toImprove.getImproves();

					int atk = toImprove.getAttack();
					int def = toImprove.getDefense();
					currentImproveFee = (improves + 1) * ((atk + def) * 2500);

					if (foundMoreThanOne) {
						improver.say("Wzmocnię #'"+currentImproveItem+"', lecz koszt będzie wynosił #'"+Integer.toString(currentImproveFee)+"' money. Chcesz, abym udoskonalił to?");
					} else {
						improver.say("Wzmocnię #'"+currentImproveItem+"', lecz koszt będzie wynosił #'"+Integer.toString(currentImproveFee)+"' money. Chcesz, abym udoskonalił to?");
					}
				} else {
					improver.say("Przedmiot #'"+currentImproveItem+"' został już maksymalnie udoskonalony. Poproś o ulepszenie jakiegoś innego wyposażenia.");
					improver.setCurrentState(ConversationStates.ATTENDING);
					return;
				}
			} else {
				improver.say("Wybacz. Przedmiot ten jest niemożliwy do udoskonalenia. Poproś o ulepszenie jakiegoś innego przedmiotu.");
				improver.setCurrentState(ConversationStates.ATTENDING);
				return;
			}
		}
	}

	private ChatAction requestImproveAction(final ImproverNPC improver) {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

				String request = sentence.getTrimmedText();
				if (improvePhrases.contains(request.toLowerCase())) {
					improver.say("Powiedz mi tylko co chcesz udoskonalić.");
					improver.setCurrentState(ConversationStates.ATTENDING);
					return;
				}

				for (final String rWord: improvePhrases) {
					if (request.startsWith(rWord)) {
						request = request.substring(rWord.length() + 1);
						break;
					}
				}

				setImproveItem(request);
				setImprove(player, improver);

				if (currentToImproveCount == null) {
					improver.say("Nie jestem w stanie ulepszyć #'" + currentImproveItem + "'.");
					improver.setCurrentState(ConversationStates.ATTENDING);
					return;
				}
			}
		};
	}

	private void checkImproves(final ImproverNPC improver) {
		Item item = SingletonRepository.getEntityManager().getItem(currentImproveItem);
		if (item == null) {
			improver.say("Pierwsze słyszę o takim wyposażeniu #'" + currentImproveItem + "'. Poproś o jakiś inny przedmiot do sprawdzenia.");
			return;
		}

		if (item.has("max_improves")) {
			String times = " razy";
			if (item.getMaxImproves() == 1) {
				times = " raz";
			}
			String improves = "#'" + Integer.toString(item.getMaxImproves()) + "'" + times;

			improver.say("Przedmiot ten maksymalnie mogę ulepszyć " + improves + ". Jeśli chciałbyś go udoskonalić to powiedz mi #ulepsz.");
		} else {
			improver.say("Wyposażenia takiego jak #'" + currentImproveItem + "' nie jestem w stanie ulepszyć.");
		}
	}

	private ChatAction requestCheckImproveAction(final ImproverNPC improver) {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

				String request = sentence.getTrimmedText();
				if (checkPhrases.contains(request.toLowerCase())) {
					improver.say("Powiedz mi tylko co chcesz sprawdzić.");
					return;
				}

				for (final String rWord: checkPhrases) {
					if (request.startsWith(rWord)) {
						request = request.substring(rWord.length() + 1);
						break;
					}
				}

				setImproveItem(request);
				checkImproves(improver);
			}
		};
	}

	private ChatAction improveAction() {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser repairer) {
				player.drop("money", currentImproveFee);

				List<Item> equipped = player.getAllEquipped(currentImproveItem);
				Item toImprove = player.getFirstEquipped(currentImproveItem);

				for (Item i : equipped) {
					if (toImprove.isMaxImproved()
							&& (i.getImproves() < toImprove.getImproves())) {
						toImprove = i;
					} else if (i.getImproves() > toImprove.getImproves()) {
						toImprove = i;
					}
				}

				if (hasItemToImprove()) {
					((ImprovableItem) toImprove).upgrade();
				}

				repairer.say("Zrobione! Twój przedmiot #'" + currentImproveItem + "' został udoskonalony i jest lepszy od jego poprzedniego stanu!");
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
				return foundMoreThanOne = true;
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