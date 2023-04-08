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
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
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

	private void reset() {
		currentUpgradingItem = null;
		currentToUpgradeCount = null;
		currentUpgradeFee = null;
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

	@SuppressWarnings({ "serial", "unused" })
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
		improver.put("job_producer", "");

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
		Item toImprove = foundItem(player);
	    if (toImprove == null) {
	        improver.say("Wybacz. Nie posiadasz przedmiotu #'" + currentUpgradingItem + "' możliwego do ulepszenia.");
	        improver.setCurrentState(ConversationStates.ATTENDING);
	        return;
	    }

	    if (toImprove.isMaxImproved()) {
	        improver.say("Przedmiot #'" + currentUpgradingItem + "' został już maksymalnie udoskonalony. Poproś o ulepszenie innego przedmiotu.");
	        improver.setCurrentState(ConversationStates.ATTENDING);
	        return;
	    }

	    countImproveItems(player);

	    if (!hasItemToImprove()) {
	        improver.say("Wybacz. Przedmiot #'" + currentUpgradingItem + "' jest niemożliwy do udoskonalenia. Poproś o ulepszenie innego przedmiotu.");
	        improver.setCurrentState(ConversationStates.ATTENDING);
	        return;
	    }

	    calculateFee(player, toImprove);

	    String youWant = " Chcesz, abym udoskonalił to?";
	    String offerupgrade = "Wzmocnię #'" + currentUpgradingItem + "', lecz koszt będzie wynosił #" + Integer.toString(currentUpgradeFee) + " money.";
	    if (toImprove.getImprove() > 0) {
	        offerupgrade += " Szansa na powodzenie wynosi około #" + Integer.toString((int) (getSuccessProbability(player, toImprove) * 100)) + "%.";
	    }

	    // Special answer for mithril items
	    if (toImprove.getName().endsWith(" z mithrilu") && toImprove.getMaxImproves() == 1) {
	        offerupgrade = "Czy jesteś pewien, aby udoskonalać #" + currentUpgradingItem + "? Jest to bardzo wyjątkowy przedmiot, także cena też będzie wyjątkowa, koszt wynosi #" + Integer.toString(currentUpgradeFee) + " money.";
	    }

	    improver.say(offerupgrade + youWant);
	}

	private Item foundItem(Player player) {
	    List<Item> equipped = player.getAllEquipped(currentUpgradingItem);
	    Item toImprove = null;
	    for (Item i : equipped) {
	        if (i.getMaxImproves() <= i.getImprove()) {
	            continue; // omijamy przedmioty, które są już maksymalnie ulepszone
	        }
	        if (toImprove == null || i.getImprove() < toImprove.getImprove()) {
	            toImprove = i;
	        }
	    }
	    return toImprove;
	}

	private void calculateFee(final Player player, final Item item) {
	    int improves = item.getImprove();
	    int atk = item.getAttack();
	    int def = item.getDefense();

	    int feePerLevel = item.getMaxImproves() <= 3 ? 3000 : 5000;
	    currentUpgradeFee = (improves + 1) * ((atk + def) * feePerLevel);

	    if ((item.getName().equals("sztylecik z mithrilu") && item.getMaxImproves() <= 2) || item.getMaxImproves() == 1) {
	        currentUpgradeFee = (improves + 1) * ((atk + def) * 17400);
	    }

	    if (item.getName().endsWith(" z mithrilu") && item.getMaxImproves() == 1) {
	        currentUpgradeFee = 5000000;
	    }

	    currentUpgradeFee *= player.isQuestCompleted("ciupaga_trzy_wasy") ? (int) 0.7 : 1;

	    if (improves > item.getMaxImproves()) {
	        currentUpgradeFee = 0;
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
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				Item toImprove = foundItem(player);
				player.drop("money", currentUpgradeFee);

				if (isSuccessful(player, toImprove)) {
					toImprove.upgradeItem();
	                player.incImprovedForItem(player.getName(), 1);
	                player.incImprovedForItem(toImprove.getName(), 1);

	                npc.say("Zrobione! Twój przedmiot #'" + currentUpgradingItem + "' został udoskonalony i jest lepszy od jego poprzedniego stanu!");
	                npc.addEvent(new SoundEvent(SoundID.COMMERCE, SoundLayer.CREATURE_NOISE));

					new GameEvent(player.getName(), "upgraded-item", toImprove.getName(), "+" + Integer.toString(toImprove.getImprove())).raise();
				} else {
					final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
					money.setQuantity((int) (currentUpgradeFee * 0.4));
					player.equipOrPutOnGround(money);

					npc.say("Przepraszam, nie udało mi się udoskonalić twojego przedmiotu. Otrzymujesz " + money.getQuantity() + " monet jako rekompensatę.");
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
				return player.isEquipped(currentUpgradingItem);
			}
		};
	}

	protected boolean isSuccessful(final Player player , final Item item) {
		final int random = Rand.roll1D100();
		return (random <= (getSuccessProbability(player, item) * 100));
	}

	private double getSuccessProbability(final Player player, final Item item) {
	    int improveLevel = item.getImprove();
	    double probability = 1.0 - (0.1 * improveLevel);
	    
	    if (improveLevel > 4) {
	    	probability = Math.max(probability, 0.2);
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
