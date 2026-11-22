/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import games.stendhal.common.QuestMarker;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.navigation.NavigationGoal;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.zakopane.hospital.JadzkaAntidoteProduction;
import games.stendhal.server.util.ItemCollection;
import games.stendhal.server.util.TimeUtil;

/** Persistent daily herb deliveries; the first completed order unlocks brewing. */
public class HerbalOrdersForJadzka extends AbstractQuest {
	public static final String SLOT = "jadzka_herbal_order";
	private static final String LAST = "jadzka_herbal_order_last";
	private static final String NPC = "Gaździna Jadźka";
	private static final long DAY = 24L * 60 * 60 * 1000;
	private static final String FIRST = "arandula=5;mięta=2;babka lekarska=2";
	private static final String[] ORDERS = {
		"arandula=6;mięta=3", "pokrzywa=4;mięta=2", "babka lekarska=3;mięta=2"
	};
	private static final List<String> REQUESTS = ConversationPhrases.combine(
			ConversationPhrases.QUEST_FINISH_MESSAGES,
			Arrays.asList("zamówienie", "zamowienie", "zamówienia", "zamówieniu", "zamowieniu", "zioła", "ziół", "składniki", "skladniki", "zapasy", "gotowe", "order", "herbs", "ingredients", "encargo", "hierbas"));

	protected long now() { return System.currentTimeMillis(); }
	@Override public String getSlotName() { return SLOT; }
	@Override public String getName() { return "Zamówienie zielarskie"; }
	@Override public String getNPCName() { return NPC; }
	@Override public int getMinLevel() { return 3; }
	@Override public String getRegion() { return Region.ZAKOPANE_CITY; }

	public static boolean hasLegacyOrder(final Player player) {
		final String state = player.getQuest(HerbsForJadzka.QUEST_SLOT);
		return state != null && !state.isEmpty() && !"done".equals(state) && !"rejected".equals(state);
	}

	private boolean active(final Player player) {
		return player.hasQuest(SLOT) && !player.isQuestCompleted(SLOT);
	}

	private ItemCollection remaining(final Player player) {
		final ItemCollection items = new ItemCollection();
		if (active(player)) items.addFromQuestStateString(player.getQuest(SLOT));
		return items;
	}

	private boolean ready(final Player player) {
		if (!active(player)) return false;
		final ItemCollection items = remaining(player);
		if (items.isEmpty()) return false;
		for (Map.Entry<String, Integer> item : items.entrySet()) {
			if (!player.isEquipped(item.getKey(), item.getValue())) return false;
		}
		return true;
	}

	private long cooldown(final Player player) {
		if (!player.isQuestCompleted(SLOT)) return 0;
		try {
			return Math.max(0L, Long.parseLong(player.getQuest(LAST)) + DAY - now());
		} catch (RuntimeException invalidOrMissingTimestamp) {
			return 0;
		}
	}

	@Override public boolean isRepeatable(final Player player) {
		return isCompleted(player) && cooldown(player) == 0 && !hasLegacyOrder(player);
	}

	@Override public void addToWorld() {
		fillQuestInfo(getName(), "Dostarcz zioła Gaździnie Jadźce do lecznicy w Zakopanem. Pierwsza dostawa odblokowuje produkcję antidotum.", true);
		questInfo.setRewards(new QuestRewards(500, 300)
				.note("2 punkty karmy. Za pierwszą dostawę: możliwość zamawiania antidotum u Jadźki.").build());
		final SpeakerNPC npc = npcs.get(NPC);
		npc.add(ConversationStates.ATTENDING, REQUESTS,
				(p, s, n) -> !hasLegacyOrder(p), ConversationStates.ATTENDING, null,
				(p, s, n) -> request(p, n));
		npc.add(ConversationStates.ATTENDING, Arrays.asList("zamówienie", "zamowienie", "zamówieniu", "zamowieniu", "order", "encargo"),
				(p, s, n) -> hasLegacyOrder(p), ConversationStates.ATTENDING,
				"Najpierw dokończ poprzednią dostawę. Powiedz #składniki, a przypomnę ci, czego jeszcze brakuje. Potem porozmawiamy o nowym #zamówieniu zielarskim.", null);
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.YES_MESSAGES,
				(p, s, n) -> !hasLegacyOrder(p) && !active(p) && cooldown(p) == 0 && p.getLevel() >= getMinLevel(),
				ConversationStates.ATTENDING, null, (p, s, n) -> {
					String order = p.isQuestCompleted(JadzkaAntidoteProduction.UNLOCK)
							? ORDERS[ThreadLocalRandom.current().nextInt(ORDERS.length)] : FIRST;
					p.setQuest(SLOT, order);
					n.say("Dziękuję. Przynieś mi " + list(p) + ". Możesz przynosić zioła partiami i podawać mi ich nazwy. Gdy zbierzesz całą dostawę, powiedz #gotowe.");
				});
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES,
				(p, s, n) -> !hasLegacyOrder(p), ConversationStates.ATTENDING,
				"Dobrze. Wróć, jeśli zechcesz pomóc mi uzupełnić zapasy.", null);
		addIngredient(npc, "mięta", "mięta", "mięty", "miętę", "mieta", "mint", "menta");
		addIngredient(npc, "babka lekarska", "babka lekarska", "babkę lekarską", "babki lekarskiej", "babki lekarskie", "babek lekarskich", "babka", "plantain", "llantén");
		addIngredient(npc, "arandula", "arandula", "arandulę", "aranduli", "arandule");
		addIngredient(npc, "pokrzywa", "pokrzywa", "pokrzywę", "pokrzywy", "nettle", "ortiga");
	}

	private String list(final Player player) {
		return Grammar.enumerateCollection(remaining(player).toStringList());
	}

	private void request(final Player player, final EventRaiser npc) {
		if (active(player)) {
			if (ready(player)) {
				for (Map.Entry<String, Integer> item : remaining(player).entrySet()) player.drop(item.getKey(), item.getValue());
				complete(player, npc);
			} else {
				npc.say("Przynieś mi jeszcze " + list(player) + ". Podaj nazwę zioła, które chcesz oddać, albo wróć z całą dostawą i powiedz #gotowe.");
			}
		} else if (player.getLevel() < getMinLevel()) {
			npc.say("Najpierw nabierz trochę doświadczenia. Wróć na 3. poziomie, a powierzę ci pierwsze zamówienie zielarskie.");
		} else if (cooldown(player) > 0) {
			npc.say("Na razie mam dość ziół. Po następne #zamówienie wróć za "
					+ TimeUtil.approxTimeUntil((int) ((cooldown(player) + 999) / 1000)) + ". W tym czasie mogę przygotować dla ciebie #antidotum.");
		} else {
			npc.say(player.isQuestCompleted(JadzkaAntidoteProduction.UNLOCK)
					? "Potrzebuję kolejnej dostawy ziół do lecznicy. Podejmiesz się zamówienia?"
					: "Potrzebuję świeżej mięty, babki lekarskiej i aranduli do lekarstw. Jeśli pomożesz mi uzupełnić zapasy, będę też warzyć dla ciebie antidotum. Przyniesiesz mi zioła?");
			npc.setCurrentState(ConversationStates.QUEST_OFFERED);
		}
	}

	private void addIngredient(final SpeakerNPC npc, final String item, final String... aliases) {
		npc.add(ConversationStates.ATTENDING, Arrays.asList(aliases),
				(p, s, n) -> !hasLegacyOrder(p) && active(p), ConversationStates.ATTENDING, null,
				(p, s, n) -> {
					final ItemCollection items = remaining(p);
					final Integer needed = items.get(item);
					if (needed == null) {
						n.say("Tego zioła nie potrzebuję do obecnej dostawy. Zapytaj o #zamówienie, żeby przypomnieć sobie listę.");
						return;
					}
					final int amount = Math.min(needed, p.getNumberOfEquipped(item));
					if (amount == 0 || !p.drop(item, amount)) {
						n.say("Nie masz przy sobie tego zioła. Wróć, gdy je zdobędziesz.");
						return;
					}
					if (amount == needed) items.remove(item); else items.put(item, needed - amount);
					if (items.isEmpty()) complete(p, n);
					else {
						p.setQuest(SLOT, items.toStringForQuestState());
						n.say("Dziękuję. Przynieś mi jeszcze " + list(p) + ".");
					}
				});
	}

	private void complete(final Player player, final EventRaiser npc) {
		if (!active(player)) return;
		final boolean first = !player.isQuestCompleted(JadzkaAntidoteProduction.UNLOCK);
		player.setQuest(SLOT, "done");
		player.setQuest(LAST, Long.toString(now()));
		player.setQuest(JadzkaAntidoteProduction.UNLOCK, "done");
		new EquipItemAction("money", 300).fire(player, null, npc);
		player.addXP(500); player.addKarma(2);
		npc.say(first
				? "Dziękuję! Te zioła przydadzą się chorym. Oto zapłata. Od teraz mogę warzyć dla ciebie #antidotum. Zapytaj o nie, a wyjaśnię, co przynieść. Kolejne zamówienie zielarskie będę miała jutro."
				: "Dziękuję za kolejną dostawę. Przyjmij zapłatę za swoją pracę. Po nowe zamówienie zielarskie wróć jutro.");
	}

	@Override public List<String> getHistory(final Player player) {
		final List<String> history = new ArrayList<>();
		if (!player.hasQuest(SLOT)) return history;
		history.add("Gaździna Jadźka przyjmuje dostawy ziół w lecznicy w Zakopanem.");
		if (active(player)) {
			history.add("Do oddania zostało: " + list(player) + ".");
			history.add("Możesz oddawać zioła partiami, podając Jadźce ich nazwy. Aby przekazać całą dostawę, powiedz: gotowe.");
			history.add("Zioła możesz zdobyć z łupów lub od innych graczy. Arandula rośnie na północ od Semos.");
		} else {
			history.add("Dostawa ukończona. Jadźka wypłaciła nagrodę. Po kolejne zamówienie można wrócić 24 godziny po oddaniu ziół.");
		}
		if (player.isQuestCompleted(JadzkaAntidoteProduction.UNLOCK)) history.add("Jadźka może przygotowywać dla mnie antidotum. Przepis na jedną porcję: mięta, babka lekarska i buteleczka wody.");
		return history;
	}

	@Override public List<String> getHint(final Player player) {
		return Arrays.asList("Odwiedź Gaździnę Jadźkę w lecznicy w Zakopanem. Zapytaj o zamówienie zielarskie.");
	}

	@Override public QuestMarker getNPCQuestMarker(final Player player, final SpeakerNPC npc) {
		if (!NPC.equals(npc.getName()) || hasLegacyOrder(player)) return QuestMarker.NONE;
		if (active(player)) return ready(player) ? QuestMarker.READY : QuestMarker.IN_PROGRESS;
		return player.getLevel() >= getMinLevel() && cooldown(player) == 0 ? QuestMarker.AVAILABLE : QuestMarker.NONE;
	}

	@Override public NavigationGoal getCompassTarget(final Player player) {
		if (!active(player)) return null;
		final SpeakerNPC npc = npcs.get(NPC);
		if (npc == null || npc.getZone() == null) return null;
		if (!ready(player)) return null;
		return new NavigationGoal(npc.getZone().getName(), npc.getX(), npc.getY(), "Oddaj zioła Jadźce", "jadzka_delivery", "npc:" + NPC);
	}

	@Override public String getCompassHint(final Player player) {
		return active(player) ? "Do oddania zostało: " + list(player) + "." : getHint(player).get(0);
	}
}
