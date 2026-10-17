package games.stendhal.server.entity.npc.behaviour.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.core.rule.item.ItemValueService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.OwnedItem;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** A buyer whose catalogue and prices come from item definitions, not a shop list. */
public final class ValueBuyerBehaviour extends BuyerBehaviour {
	private static final String BALDUIN_REQUIREMENT =
			"Czarne przedmioty skupuję po ukończeniu wyzwań Balduina. "
			+ "Jeśli już je ukończyłeś, sprawdź, czy masz przedmiot przy sobie "
			+ "i czy nie jest przypisany do postaci.";

	private final Set<String> questLockedItems;
	private final String requiredQuestSlot;

	public ValueBuyerBehaviour(final Collection<DefaultItem> definitions) {
		this(definitions, Collections.<String>emptySet(), null);
	}

	/** Optional per-item quest gate; the normal catalogue remains unrestricted. */
	public ValueBuyerBehaviour(final Collection<DefaultItem> definitions,
			final Collection<String> questLockedItems, final String requiredQuestSlot) {
		super(createCatalogue(definitions));
		if (questLockedItems == null || (!questLockedItems.isEmpty()
				&& (requiredQuestSlot == null || requiredQuestSlot.isEmpty()))) {
			throw new IllegalArgumentException("Quest-locked items require a quest slot");
		}
		this.questLockedItems = Collections.unmodifiableSet(new HashSet<String>(questLockedItems));
		this.requiredQuestSlot = requiredQuestSlot;
	}

	@Override
	public String getUnavailableOffer(final ItemParserResult res) {
		if (res != null && questLockedItems.contains(res.getChosenItemName())) {
			return BALDUIN_REQUIREMENT;
		}
		return "Nie mogę odkupić tych rzeczy. Sprawdź, czy masz je przy sobie i czy nie są przypisane do postaci.";
	}

	@Override
	public String getErrormessage(final ItemParserResult res, final List<String> userAction, final String npcAction) {
		if (res.getChosenItemName() == null) {
			return "Co chcesz sprzedać? Podaj nazwę przedmiotu.";
		}
		if (res.getMayBeItems() != null && !res.getMayBeItems().isEmpty()) {
			return "Który przedmiot masz na myśli? Podaj jego pełną nazwę.";
		}
		return "Tego nie odkupię. Może znajdziesz innego kupca?";
	}

	private static Map<String, Integer> createCatalogue(final Collection<DefaultItem> definitions) {
		final Map<String, Integer> prices = new TreeMap<String, Integer>();
		for (final DefaultItem item : definitions) {
			if (item.getValue() > 0 && !item.isUnattainable()
					&& !"money".equals(item.getItemClass())
					&& !"key".equals(item.getItemClass())
					&& item.getEquipableSlots() != null && item.getEquipableSlots().contains("trade")
					&& (item.getAttributes() == null || !item.getAttributes().containsKey("autobind"))) {
				prices.put(item.getItemName(), Integer.valueOf(item.getValue()));
			}
		}
		return prices;
	}

	@Override
	public int getCharge(final ItemParserResult res, final Player player) {
		if (res == null || player == null || res.getAmount() <= 0 || res.getAmount() > 1000
				|| res.getChosenItemName() == null || !hasItem(res.getChosenItemName())) {
			return 0;
		}
		// Check at quote time and again at confirmation (transactAgreedDeal
		// invokes getCharge) so an earlier quoted price cannot bypass the gate.
		if (questLockedItems.contains(res.getChosenItemName())
				&& !player.isQuestCompleted(requiredQuestSlot)) {
			return 0;
		}
		int remaining = res.getAmount();
		long total = 0;
		for (final Item item : player.getAllEquipped(res.getChosenItemName())) {
			if (remaining == 0) { break; }
			if (item.isBound() || item.has("autobind") || item instanceof OwnedItem
					|| !item.canBeEquippedIn("trade")) {
				return 0;
			}
			ItemValueService.refresh(item);
			if (item.getValue() <= 0) { return 0; }
			final int quantity = Math.min(remaining, item.getQuantity());
			total += (long) quantity * item.getValue();
			remaining -= quantity;
			if (total > Integer.MAX_VALUE) { return 0; }
		}
		if (remaining != 0) { return 0; }
		long balance = 0;
		for (final Item money : player.getAllEquipped("money")) {
			balance += money.getQuantity();
		}
		return balance + total > Integer.MAX_VALUE ? 0 : (int) total;
	}

	@Override
	public boolean transactAgreedDeal(final ItemParserResult res,
			final EventRaiser seller, final Player player) {
		if (getCharge(res, player) <= 0) {
			seller.say(getUnavailableOffer(res));
			return false;
		}
		return super.transactAgreedDeal(res, seller, player);
	}
}
