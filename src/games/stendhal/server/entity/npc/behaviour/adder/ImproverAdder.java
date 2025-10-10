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
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
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

	@SuppressWarnings("serial")
	private final Map<Integer, Map<String, Integer>> upgradeRequirements = new HashMap<Integer, Map<String, Integer>>() {{
		// upgrade requirements for level 1
		Map<String, Integer> level1Requirements = new HashMap<String, Integer>() {{
			put("polano", 5);
			put("szafir", 1);
		}};
		put(1, level1Requirements);

		// upgrade requirements for level 2
		Map<String, Integer> level2Requirements = new HashMap<String, Integer>() {{
			put("polano", 7);
			put("szafir", 2);
			put("ametyst", 1);
		}};
		put(2, level2Requirements);

		// upgrade requirements for level 3
		Map<String, Integer> level3Requirements = new HashMap<String, Integer>() {{
			put("polano", 9);
			put("szafir", 2);
			put("ametyst", 2);
			put("szmaragd", 1);
		}};
		put(3, level3Requirements);

		// upgrade requirements for level 4
		Map<String, Integer> level4Requirements = new HashMap<String, Integer>() {{
			put("polano", 12);
			put("szafir", 5);
			put("ametyst", 3);
			put("szmaragd", 3);
			put("rubin", 1);
		}};
		put(4, level4Requirements);

		// upgrade requirements for level 5
		Map<String, Integer> level5Requirements = new HashMap<String, Integer>() {{
			put("polano", 15);
			put("ametyst", 5);
			put("szmaragd", 5);
			put("rubin", 3);
			put("obsydian", 1);
		}};
		put(5, level5Requirements);
		
		Map<String, Integer> level6Requirements = new HashMap<String, Integer>() {{
			put("polano", 15);
			put("ametyst", 7);
			put("szmaragd", 7);
			put("rubin", 6);
			put("obsydian", 3);
		}};
		put(6, level6Requirements);
		
		Map<String, Integer> level7Requirements = new HashMap<String, Integer>() {{
			put("polano", 15);
			put("ametyst", 8);
			put("rubin", 7);
			put("obsydian", 5);
			put("diament", 1);
		}};
		put(7, level7Requirements);
		
		Map<String, Integer> level8Requirements = new HashMap<String, Integer>() {{
			put("polano", 15);
			put("ametyst", 10);
			put("rubin", 8);
			put("obsydian", 5);
			put("diament", 2);
		}};
		put(8, level8Requirements);
	}};

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
				new OrCondition(
					new NotCondition(canAffordCondition()),
					new NotCondition(needsResourcesCondition())),
				ConversationStates.ATTENDING,
				"Nie masz dość pieniędzy lub surowców potrzebnych do ulepszenia.",
				null);

		improver.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
					canAffordCondition(),
					needsResourcesCondition()),
				ConversationStates.ATTENDING,
				null,
				improveAction());
	}

	private void setTargetItemName(final String itemName) {
		currentUpgradingItem = itemName;
	}

	private String getTargetItemName() {
		return currentUpgradingItem;
	}

	private boolean isEquippedTargetItem(final Player player) {
		return player.isEquipped(getTargetItemName());
	}

	private List<Item> getAllEquippedTargetItems(final Player player) {
		return player.getAllEquipped(getTargetItemName());
	}

	private void countImproveItems(final Player player) {
		int count = 0;
		for (Item i : getAllEquippedTargetItems(player)) {
			if (i.hasMaxImproves() && !i.isMaxImproved()) {
				count++;
			}
		}
		currentToUpgradeCount = count;
	}

	private boolean hasItemToImprove() {
		return currentToUpgradeCount > 0;
	}

	private void setOffer(final Player player, final ImproverNPC improver) {
		Item toImprove = foundItem(player);
		if (toImprove == null) {
			improver.say("Wybacz. Nie posiadasz przedmiotu #'" + getTargetItemName() + "' możliwego do ulepszenia.");
			improver.setCurrentState(ConversationStates.ATTENDING);
			return;
		}

		countImproveItems(player);
		if (!hasItemToImprove()) {
			improver.say("Wybacz. Przedmiot #'" + getTargetItemName() + "' jest niemożliwy do udoskonalenia."
					+ " Poproś o ulepszenie innego przedmiotu.");
			improver.setCurrentState(ConversationStates.ATTENDING);
			return;
		}

		calculateFee(player, toImprove);

		String youWant = " Chcesz, abym udoskonalił to?";
		String offerUpgrade = "Wzmocnię #'" + getTargetItemName() + "', lecz koszt "
				+ "będzie wynosił " + MoneyUtils.formatPrice(currentUpgradeFee) + ". "
				+ getNeedResourcesNames(player);
		if (toImprove.getImprove() > 0) {
			offerUpgrade += " Szansa na powodzenie wynosi około #" + Integer.toString((int) (getSuccessProbability(player, toImprove) * 100)) + "%.";
		}

		// Special answer for mithril items
		if (toImprove.getName().endsWith(" z mithrilu") && toImprove.getMaxImproves() == 1) {
			offerUpgrade = "Czy jesteś pewien, aby udoskonalać #'" + getTargetItemName() + "'? Jest to bardzo wyjątkowy przedmiot,"
					+ " także cena też będzie wyjątkowa, koszt wynosi " + MoneyUtils.formatPrice(currentUpgradeFee) + "."
					+ getNeedResourcesNames(player);
		}

		improver.say(offerUpgrade + youWant);
	}

	private Item foundItem(final Player player) {
		Item toImprove = null;
		for (Item i : getAllEquippedTargetItems(player)) {
			if (i.isMaxImproved()) {
				continue; // avoid max improved items
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

		double discount = player.isQuestCompleted("ciupaga_trzy_wasy") ? 0.7 : 1.0;
		currentUpgradeFee = (int) Math.round(currentUpgradeFee * discount);

		if (improves > item.getMaxImproves()) {
			currentUpgradeFee = 0;
		}
	}

	private boolean playerHaveResources(final Player player, int level) {
		Map<String, Integer> requirements = upgradeRequirements.get(level);
		if (requirements == null) {
			return false;
		}

		for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
			String resourceName = entry.getKey();
			int requiredAmount = entry.getValue();
			if (!player.isEquipped(resourceName, requiredAmount)) {
				return false;
			}
		}
		return true;
	}

	private void dropNeededResources(final Player player) {
		Item targetItem = foundItem(player);
		Map<String, Integer> requirements = upgradeRequirements.get(targetItem.getImprove() + 1);

		for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
			player.drop(entry.getKey(), entry.getValue());
		}
	}

	private String getNeedResourcesNames(final Player player) {
		Item targetItem = foundItem(player);
		Map<String, Integer> requirements = upgradeRequirements.get(targetItem.getImprove() + 1);

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
			sb.append(entry.getValue() + " #'" + entry.getKey() + "'").append(", ");
		}
		sb.setLength(sb.length() - 2);

		return "Będę potrzebował również nieprzypadkowe surowce do podniesienia jakości, takie jak " + sb.toString() + ".";
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

				setTargetItemName(request);
				setOffer(player, improver);
			}
		};
	}

	private ChatAction improveAction() {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				Item toImprove = foundItem(player);
				MoneyUtils.removeMoney(player, currentUpgradeFee);
				dropNeededResources(player);

				if (isSuccessful(player, toImprove)) {
					toImprove.upgradeItem();
					player.incImprovedForItem(player.getName(), 1);
					player.incImprovedForItem(toImprove.getName(), 1);

					npc.say("Zrobione! Twój przedmiot #'" + getTargetItemName() + "' został ulepszony!");
					npc.addEvent(new SoundEvent(SoundID.COMMERCE, SoundLayer.CREATURE_NOISE));

					new GameEvent(player.getName(), "upgraded-item", toImprove.getName(), "+" + Integer.toString(toImprove.getImprove())).raise();
				} else {
					Map<String, Integer> refundMap = MoneyUtils.fromCopper((int)(currentUpgradeFee * 0.4));
					for (Map.Entry<String, Integer> entry : refundMap.entrySet()) {
					    if (entry.getValue() <= 0) continue;
					    try {
					        StackableItem refundCoin = (StackableItem)
					            SingletonRepository.getEntityManager().getItem(entry.getKey());
					        refundCoin.setQuantity(entry.getValue());
					        player.equipOrPutOnGround(refundCoin);
					    } catch (Exception e) {
					        logger.error("Błąd przy dodawaniu rekompensaty: " + entry.getKey(), e);
					    }
					}

					npc.say("Przepraszam, nie udało mi się udoskonalić twojego przedmiotu. "
							+ "Otrzymujesz " + MoneyUtils.formatPrice((int)(currentUpgradeFee * 0.4)) + " jako rekompensatę.");
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
				return MoneyUtils.hasEnoughMoney(player, currentUpgradeFee);
			}
		};
	}

	private ChatCondition needsImproveCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, Entity improver) {
				return isEquippedTargetItem(player);
			}
		};
	}

	private ChatCondition needsResourcesCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, Entity improver) {
				Item targetItem = foundItem(player);
				return playerHaveResources(player, targetItem.getImprove() + 1);
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

	private void checkImproves(final ImproverNPC improver) {
		Item item = SingletonRepository.getEntityManager().getItem(getTargetItemName());
		if (item == null) {
			improver.say("Pierwsze słyszę o takim wyposażeniu #'" + getTargetItemName() + "'. Poproś o jakiś inny przedmiot do sprawdzenia.");
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
			improver.say("Wyposażenia takiego jak #'" + getTargetItemName() + "' nie jestem w stanie ulepszyć.");
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

				setTargetItemName(request);
				checkImproves(improver);
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
