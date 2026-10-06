/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.stendhal.common.QuestMarker;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.seasonal.SeasonalEventService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.TransitionContext;
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
				addGreeting("Witaj, wędrowcze. Rąk do pomocy nigdy dość. Mam dla ciebie #zadanie, jeśli zechcesz się go podjąć.");
				addJob(job);
				addHelp(request);
				addGoodbye("Bywaj zdrów. Niech ci droga lekką będzie.");
			}
		};
		ownedNPC.setOutfit(outfit);
		ownedNPC.initHP(100);
		ownedNPC.setDescription("Oto " + npcName + ". Krząta się przy miejscu biesiady.");
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
		final IQuest previous = TransitionContext.getMarkerQuest();
		try {
			TransitionContext.setMarkerQuest(this);
			attachQuestDialog(npc);
		} finally {
			TransitionContext.setMarkerQuest(previous);
		}
	}

	private void attachQuestDialog(final SpeakerNPC npc) {
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, null,
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> offer(player, raiser));
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null, (player, sentence, raiser) -> {
					if (!checkAvailability(player, raiser)) {
						return;
					}
					player.setQuest(slot, "start;" + lastCompletion(player) + ";" + completionCount(player));
					raiser.say("Dobrze, będę cię wypatrywał. Gdy wrócisz, przypomnij mi nasze #zadanie. Bywaj zdrów.");
				});
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Może innym razem.", null);
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT, ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> complete(player, raiser));
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Zachowaj je i wróć, gdy zechcesz pomóc.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("nagroda", "reward"), null,
				ConversationStates.ATTENDING, rewardOffer(), null);
	}

	private void offer(final Player player, final EventRaiser npc) {
		if (!checkAvailability(player, npc)) {
			return;
		}
		if (isInProgress(player)) {
			if (hasRequiredItems(player)) {
				npc.say("Widzę, że niczego nie brakuje. Oddasz mi to, co przyniosłeś?");
				npc.setCurrentState(ConversationStates.QUEST_ITEM_BROUGHT);
			} else {
				npc.say(request);
			}
		} else {
			npc.say(request + " " + rewardOffer() + " Pomożesz mi?");
			npc.setCurrentState(ConversationStates.QUEST_OFFERED);
		}
	}

	private boolean checkAvailability(final Player player, final EventRaiser npc) {
		if (!SeasonalEventService.get().isMineTownEnabled()) {
			npc.say("Z taką sprawą przyjdź do mnie w czas jesiennego święta.");
			return false;
		}
		if (!prerequisitesMet(player)) {
			npc.say("Najpierw pomóż Boguchwałowi napełnić stoły i Wolradowi odnaleźć zguby. Gdy obaj będą gotowi na tegoroczne święto, zajmiemy się gusłami.");
			return false;
		}
		if (cooldownHours == 0 && completionCount(player) > 0) {
			npc.say("Duchy przyjęły już nasz dar. Na ten rok gusła skończone. Wróć, gdy nadejdzie kolejna jesień.");
			return false;
		}
		final long remaining = cooldownHours * 3600000L - (now() - lastCompletion(player));
		if (completionCount(player) > 0 && remaining > 0) {
			final long minutes = (remaining + 59999) / 60000;
			npc.say("Dobrze się spisałeś. Teraz odpocznij. Wróć za " + waitingTime(minutes) + ", jeśli zechcesz znów pomóc.");
			return false;
		}
		return true;
	}

	@Override
	public QuestMarker getNPCQuestMarker(final Player player, final SpeakerNPC npc) {
		if (!SeasonalEventService.get().isMineTownEnabled() || !prerequisitesMet(player)
				|| (cooldownHours == 0 && completionCount(player) > 0)
				|| (completionCount(player) > 0
					&& now() - lastCompletion(player) < cooldownHours * 3600000L)) {
			return QuestMarker.NONE;
		}
		if (isInProgress(player)) {
			return hasRequiredItems(player) ? QuestMarker.READY : QuestMarker.IN_PROGRESS;
		}
		return cooldownHours > 0 ? QuestMarker.REPEATABLE : QuestMarker.AVAILABLE;
	}

	private void complete(final Player player, final EventRaiser npc) {
		if (!checkAvailability(player, npc) || !isInProgress(player)) {
			return;
		}
		// Recheck on confirmation: moving items after the offer must not grant a reward.
		if (!hasRequiredItems(player)) {
			npc.say("Poczekaj, jeszcze czegoś tu brakuje. " + request);
			return;
		}
		final Item reward = SingletonRepository.getEntityManager().getItem(rewardName);
		if (reward == null) {
			npc.say("Nie mam teraz czym ci się odwdzięczyć. Niczego mi nie oddawaj. Zajrzyj później.");
			return;
		}
		reward.setBoundTo(player.getName());
		consumeRequiredItems(player);
		player.setQuest(slot, "done;" + now() + ";" + (completionCount(player) + 1));
		player.equipOrPutOnGround(reward);
		player.notifyWorldAboutChanges();
		npc.say(rewardThanks());
	}

	protected abstract String rewardOffer();

	protected abstract String rewardThanks();

	private static String waitingTime(final long minutes) {
		final long hours = minutes / 60;
		final long remainder = minutes % 60;
		if (hours == 0) {
			return minutes + " " + countedWord(minutes, "minutę", "minuty", "minut");
		}
		final String time = hours + " " + countedWord(hours, "godzinę", "godziny", "godzin");
		return remainder == 0 ? time
				: time + " i " + remainder + " " + countedWord(remainder, "minutę", "minuty", "minut");
	}

	private static String countedWord(final long count, final String singular,
			final String few, final String many) {
		if (count == 1) {
			return singular;
		}
		final long lastTwo = count % 100;
		final long last = count % 10;
		return last >= 2 && last <= 4 && (lastTwo < 12 || lastTwo > 14) ? few : many;
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
				history.add(completionCount(player) == 1
						? "Pomogłem już raz podczas tegorocznego święta."
						: "Pomogłem już " + completionCount(player) + " razy podczas tegorocznego święta.");
			}
			if (isInProgress(player)) {
				history.add("Na mój powrót czeka " + npcName + ".");
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
