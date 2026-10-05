/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.seasonal.SeasonalEventService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/** Common, server-side delivery flow for the limited Mine Town quests. */
abstract class MineTownCollectionQuest extends AbstractQuest {
	static final String FESTIVAL_ZONE = "0_zakopane_s";
	private final String slot;
	private final String name;
	private final String npcName;
	private final String rewardName;
	private final int cooldownHours;
	private final Map<String, Integer> required;
	private final String request;
	private SpeakerNPC ownedNPC;

	MineTownCollectionQuest(final String slot, final String name, final String npcName,
			final String rewardName, final int cooldownHours,
			final Map<String, Integer> required, final String request) {
		this.slot = slot;
		this.name = name;
		this.npcName = npcName;
		this.rewardName = rewardName;
		this.cooldownHours = cooldownHours;
		this.required = required;
		this.request = request;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(name, request, cooldownHours > 0, 2);
	}

	protected final void createFestivalNPC(final String outfit, final int x, final int y,
			final String job) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(FESTIVAL_ZONE);
		if (zone == null) {
			throw new IllegalStateException("Brakuje strefy festynu: " + FESTIVAL_ZONE);
		}
		if (npcs.get(npcName) != null) {
			throw new IllegalStateException("NPC już istnieje: " + npcName);
		}
		ownedNPC = new SpeakerNPC(npcName) {
			@Override
			protected void createPath() {
				setPath(null);
			}
			@Override
			protected void createDialog() {
				addGreeting("Witaj na festynie Mine Town! Zapytaj mnie o #zadanie.");
				addJob(job);
				addHelp(request);
				addGoodbye("Do zobaczenia na festynie!");
			}
		};
		ownedNPC.setOutfit(outfit);
		ownedNPC.initHP(100);
		ownedNPC.setDescription("Oto " + npcName + ". " + job);
		Point position = null;
		for (int radius = 0; radius <= 6 && position == null; radius++) {
			for (int dx = -radius; dx <= radius && position == null; dx++) {
				for (int dy = -radius; dy <= radius; dy++) {
					if (!zone.collides(ownedNPC, x + dx, y + dy)
							&& zone.getEntitiesAt(x + dx, y + dy).isEmpty()
							&& zone.getPortal(x + dx, y + dy) == null) {
						position = new Point(x + dx, y + dy);
						break;
					}
				}
			}
		}
		if (position == null) {
			ownedNPC = null;
			throw new IllegalStateException("Brakuje miejsca dla NPC " + npcName);
		}
		ownedNPC.setPosition(position.x, position.y);
		zone.add(ownedNPC);
		attachDialog(ownedNPC);
	}

	/** Also used each time the independently controlled Guślarz is recreated. */
	public final void attachDialog(final SpeakerNPC npc) {
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, null,
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> offer(player, raiser));
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null, (player, sentence, raiser) -> {
					if (!checkAvailability(player, raiser)) {
						return;
					}
					player.setQuest(slot, "start;" + lastCompletion(player) + ";" + completionCount(player));
					raiser.say(request + " Gdy przyniesiesz wszystko, zapytaj ponownie o #zadanie.");
				});
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Może innym razem.", null);
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT, ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> complete(player, raiser));
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Zachowaj je i wróć, gdy zechcesz pomóc.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("nagroda", "reward"), null,
				ConversationStates.ATTENDING, "Za wykonanie zadania otrzymasz: " + rewardName + ".", null);
	}

	private void offer(final Player player, final EventRaiser npc) {
		if (!checkAvailability(player, npc)) {
			return;
		}
		if (isInProgress(player)) {
			if (hasRequiredItems(player)) {
				npc.say("Masz wszystko, czego potrzebuję. Czy chcesz przekazać mi przedmioty?");
				npc.setCurrentState(ConversationStates.QUEST_ITEM_BROUGHT);
			} else {
				npc.say(request);
			}
		} else {
			npc.say(request + " Nagroda: " + rewardName + ". Czy pomożesz?");
			npc.setCurrentState(ConversationStates.QUEST_OFFERED);
		}
	}

	private boolean checkAvailability(final Player player, final EventRaiser npc) {
		if (!SeasonalEventService.get().isMineTownEnabled()) {
			npc.say("To zadanie jest dostępne tylko podczas Mine Town.");
			return false;
		}
		if (!prerequisitesMet(player)) {
			npc.say("Najpierw pomóż Boguchwałowi przygotować festyn i przynieś Wolradowi zagubione latarenki w tej edycji Mine Town.");
			return false;
		}
		if (cooldownHours == 0 && completionCount(player) > 0) {
			npc.say("Rytuał został już odprawiony. Kolejna złota skrzynia będzie dostępna w następnej edycji Mine Town.");
			return false;
		}
		final long remaining = cooldownHours * 3600000L - (now() - lastCompletion(player));
		if (completionCount(player) > 0 && remaining > 0) {
			npc.say("Dziękuję za pomoc. Wróć za " + ((remaining + 59999) / 60000) + " min.");
			return false;
		}
		return true;
	}

	private void complete(final Player player, final EventRaiser npc) {
		if (!checkAvailability(player, npc) || !isInProgress(player)) {
			return;
		}
		// Recheck on confirmation: moving items after the offer must not grant a reward.
		if (!hasRequiredItems(player)) {
			npc.say("Nie masz przy sobie wszystkich potrzebnych przedmiotów. " + request);
			return;
		}
		final Item reward = SingletonRepository.getEntityManager().getItem(rewardName);
		if (reward == null) {
			npc.say("Nie mogę teraz przekazać nagrody. Zachowaj przedmioty i wróć później.");
			return;
		}
		reward.setBoundTo(player.getName());
		consumeRequiredItems(player);
		player.setQuest(slot, "done;" + now() + ";" + (completionCount(player) + 1));
		player.equipOrPutOnGround(reward);
		player.notifyWorldAboutChanges();
		npc.say("Dziękuję za pomoc! Twoja nagroda: " + rewardName + ".");
	}

	protected boolean hasRequiredItems(final Player player) {
		for (final Map.Entry<String, Integer> item : required.entrySet()) {
			if (!player.isEquipped(item.getKey(), item.getValue())) {
				return false;
			}
		}
		return true;
	}

	protected void consumeRequiredItems(final Player player) {
		for (final Map.Entry<String, Integer> item : required.entrySet()) {
			player.drop(item.getKey(), item.getValue());
		}
	}

	protected boolean prerequisitesMet(final Player player) {
		return true;
	}

	protected long now() {
		return System.currentTimeMillis();
	}

	private long stateNumber(final Player player, final int index) {
		if (!player.hasQuest(slot)) {
			return 0;
		}
		final String[] fields = player.getQuest(slot).split(";");
		try {
			return fields.length > index ? Math.max(0, Long.parseLong(fields[index])) : 0;
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	private long lastCompletion(final Player player) {
		return stateNumber(player, 1);
	}

	final int completionCount(final Player player) {
		return (int) Math.min(Integer.MAX_VALUE, stateNumber(player, 2));
	}

	private boolean isInProgress(final Player player) {
		return player.hasQuest(slot) && player.getQuest(slot).startsWith("start;");
	}

	@Override
	public boolean isCompleted(final Player player) {
		return player.hasQuest(slot) && player.getQuest(slot).startsWith("done;");
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return SeasonalEventService.get().isMineTownEnabled() && cooldownHours > 0
				&& isCompleted(player) && now() - lastCompletion(player) >= cooldownHours * 3600000L;
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return SeasonalEventService.get().isMineTownEnabled();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> history = new ArrayList<String>();
		if (player.hasQuest(slot)) {
			history.add(request);
			if (completionCount(player) > 0) {
				history.add("Liczba wykonań w tej edycji Mine Town: " + completionCount(player) + ".");
			}
			if (isInProgress(player)) {
				history.add("Przedmioty należy oddać: " + npcName + ".");
			}
		}
		return history;
	}

	@Override
	public boolean removeFromWorld() {
		if (ownedNPC != null && ownedNPC.getZone() != null) {
			ownedNPC.getZone().remove(ownedNPC);
		}
		ownedNPC = null;
		return true;
	}

	@Override public String getSlotName() { return slot; }
	@Override public String getName() { return name; }
	@Override public String getNPCName() { return npcName; }
	@Override public String getRegion() { return Region.TATRY_MOUNTAIN; }
}
