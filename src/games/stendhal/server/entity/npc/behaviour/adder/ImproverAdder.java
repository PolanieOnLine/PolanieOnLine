package games.stendhal.server.entity.npc.behaviour.adder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
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

/**
 * @author KarajuSs
 */
public class ImproverAdder {
	private static final Logger logger = Logger.getLogger(ImproverAdder.class);

	private static final List<String> improvePhrases = Arrays.asList("improve", "upgrade", "ulepsz", "udoskonalić");
	private static final List<String> checkPhrases = Arrays.asList("check", "see", "how much", "sprawdź", "zobacz", "ile");

	private String currentUpgradingItem = null;
	private Integer currentToUpgradeCount = null;
	private Integer currentUpgradeFee = null;

	private boolean foundMoreThanOne = false;

	private void reset() {
		currentUpgradingItem = null;
		currentToUpgradeCount = null;
		currentUpgradeFee = null;

		foundMoreThanOne = false;
	}

	//START: UNUSED YET
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
		// load the default value's
		defaultNeededValue();

		put("sztabka złota", currentGold);
		put("sztabka mithrilu", currentMithril);
		put("ametyst", currentAmethyst);
		put("rubin", currentRuby);
		put("szafir", currentSapphire);
		put("polano", currentWood);
		put("piórko", currentFeather);
	}};
	//END: UNUSED YET

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
		currentUpgradingItem = itemName;
	}

	private void countImproveItems(final Player player) {
		List<Item> equipped = player.getAllEquipped(currentUpgradingItem);

		int count = 0;
		for (Item i : equipped) {
			if (!i.isMaxImproved()) {
				count++;
			}
		}
		currentToUpgradeCount = count;
	}
	
	private boolean hasItemToImprove() {
		if (currentToUpgradeCount > 0) {
			return true;
		}
		return false;
	}

	private void setImprove(final Player player, final ImproverNPC improver) {
		List<Item> equipped = player.getAllEquipped(currentUpgradingItem);
		if (!equipped.isEmpty()) {
			if(equipped.size() > 1) {
				foundMoreThanOne = true;
			}

			Item toImprove = equipped.iterator().next();
			for (Item i : equipped) {
				if (i.getImprove() > toImprove.getImprove()) {
						toImprove = i;
				}
			}

			countImproveItems(player);

			if (toImprove.getMaxImproves() > 0) {
				if (hasItemToImprove()) {
					for (Item i : equipped) {
						if (toImprove.isMaxImproved()
								&& (i.getImprove() < toImprove.getImprove())) {
							toImprove = i;
						} else if (i.getImprove() > toImprove.getImprove()) {
							toImprove = i;
						}
					}

					calculateFee(toImprove);

					String youWant = " Chcesz, abym udoskonalił to?";
					String offerupgrade = "Wzmocnię #'"+currentUpgradingItem+"', lecz koszt będzie wynosił #'"+Integer.toString(currentUpgradeFee)+"' money.";
					if (toImprove.getImprove() > 0) {
						offerupgrade += " Szansa na powodzenie wynosi #'"+Integer.toString((int) (getSuccessProbability(player, toImprove) * 100))+"%'.";
					}

					// Special answer for mithril items
					if (toImprove.getName().endsWith(" z mithrilu") && toImprove.getMaxImproves() == 1) {
						offerupgrade = "Czy jesteś pewien, aby udoskonalać #'"+currentUpgradingItem+"'? Jest to bardzo wyjątkowy przedmiot, także cena też będzie wyjątkowa, koszt wynosi #'"+Integer.toString(currentUpgradeFee)+"' money.";
					}

					if (foundMoreThanOne) {
						improver.say(offerupgrade + youWant);
					} else {
						improver.say(offerupgrade + youWant);
					}
				} else {
					improver.say("Przedmiot #'"+currentUpgradingItem+"' został już maksymalnie udoskonalony. Poproś o ulepszenie jakiegoś innego wyposażenia.");
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

	private void calculateFee(final Item item) {
		int improves = item.getImprove();

		int atk = item.getAttack();
		int def = item.getDefense();
		currentUpgradeFee = (improves + 1) * ((atk + def) * 3000);

		// Fee only for 1 upgrade
		if (item.getMaxImproves() == 1) {
			currentUpgradeFee = (atk + def) * 59500;
		}
		// Special fee for special item
		if (item.getName().endsWith(" z mithrilu") && item.getMaxImproves() == 1) {
			currentUpgradeFee = 5000000;
		}

		/*
		 * This condition is only used if someone didn't add the item to list in PlayerTransformer
		 * if "max_improves" has been changed for item.
		 */
		if (item.getImprove() > item.getMaxImproves()) {
			// Set fee to '0'.
			currentUpgradeFee *= 0;
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

				if (currentToUpgradeCount == null) {
					improver.say("Nie jestem w stanie ulepszyć #'" + currentUpgradingItem + "'.");
					improver.setCurrentState(ConversationStates.ATTENDING);
					return;
				}
			}
		};
	}

	private void checkImproves(final ImproverNPC improver) {
		Item item = SingletonRepository.getEntityManager().getItem(currentUpgradingItem);
		if (item == null) {
			improver.say("Pierwsze słyszę o takim wyposażeniu #'" + currentUpgradingItem + "'. Poproś o jakiś inny przedmiot do sprawdzenia.");
			return;
		}

		if (item.has("max_improves")) {
			String times = " razy";
			if (item.getMaxImproves() == 1) {
				times = " raz";
			}
			String improves = "#'" + Integer.toString(item.getMaxImproves()) + "'" + times;

			improver.say("Przedmiot ten maksymalnie mogę ulepszyć " + improves + ". Jeśli chcesz go udoskonalić to powiedz mi #ulepsz.");
		} else {
			improver.say("Wyposażenia takiego jak #'" + currentUpgradingItem + "' nie jestem w stanie ulepszyć.");
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
				List<Item> equipped = player.getAllEquipped(currentUpgradingItem);
				Item toImprove = player.getFirstEquipped(currentUpgradingItem);
				player.drop("money", currentUpgradeFee);

				for (Item i : equipped) {
					if (toImprove.isMaxImproved()
							&& (i.getImprove() < toImprove.getImprove())) {
						toImprove = i;
					} else if (i.getImprove() > toImprove.getImprove()) {
						toImprove = i;
					}
				}

				if (isSuccessful(player, toImprove)) {
					if (hasItemToImprove()) {
						toImprove.upgradeItem();
					}

					repairer.say("Zrobione! Twój przedmiot #'" + currentUpgradingItem + "' został udoskonalony i jest lepszy od jego poprzedniego stanu!");
					repairer.addEvent(new SoundEvent(SoundID.COMMERCE, SoundLayer.CREATURE_NOISE));
				} else {
					repairer.say("Przepraszam, lecz nie udało mi się udoskonalić twojego przedmiotu.");
				}
			}
		};
	}

	private ChatCondition feeNotSetCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity improver) {
				if (currentUpgradeFee == null) {
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
				return player.isEquipped("money", currentUpgradeFee);
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

	protected boolean isSuccessful(final Player player , final Item item) {
		final int random = Rand.roll1D100();
		return (random <= (getSuccessProbability(player, item) * 100));
	}

	private double getSuccessProbability(final Player player, final Item item) {
		double probability;
		if (item.getImprove() == 0) {
			probability = 1.0;
		} else if (item.getImprove() == 1) {
			probability = 0.9;
		} else if (item.getImprove() == 2) {
			probability = 0.8;
		} else if (item.getImprove() == 3) {
			probability = 0.7;
		} else if (item.getImprove() == 4) {
			probability = 0.5;
		} else {
			probability = 0.2;
		}

		return probability + player.useKarma(0.1);
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