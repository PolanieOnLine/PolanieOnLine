package games.stendhal.server.core.rule.rarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StatusResistantItem;

/** Replaces only retired slots; saved replacements are not rolled again. */
public final class RetiredSlowAffixMigration {
	private static final Logger LOGGER = Logger.getLogger(RetiredSlowAffixMigration.class);
	public static final String RESISTANCE = "resist_heavy";
	public static final String BINDING = "legendary_binding_strike";
	private static final String[] RETIRED = {RESISTANCE, BINDING};
	private RetiredSlowAffixMigration() { }

	public static boolean isRetired(final String id) {
		return RESISTANCE.equals(id) || BINDING.equals(id);
	}

	public static boolean migrate(final Item item) {
		if (item == null || item instanceof Glyph) return false;
		final Map<String, String> offer = ItemAffixRerollService.state(item);
		boolean needed = isRetired(offer.get("new_id"));
		for (final String id : RETIRED) needed |= item.has(id) || ItemAffixState.has(item, id);
		if (!needed) return false;
		final Item working = copy(item);
		Long seed = ItemAffixState.getSeed(working);
		if (seed == null) {
			seed = new Random().nextLong();
			ItemAffixState.setSeed(working, seed);
		}
		final Random random = new Random(seed ^ 0x534C4F5752455449L);
		final Set<String> excluded = new HashSet<>();
		if (offer.containsKey("token") && !isRetired(offer.get("new_id"))) excluded.add(offer.get("new_id"));
		final Map<String, String> replacements = new LinkedHashMap<>();
		for (final String id : RETIRED) {
			if (!working.has(id) && !ItemAffixState.has(working, id)) continue;
			if (!ItemAffixState.has(working, id)) working.put(ItemAffixState.ATTRIBUTE, id, working.get(id));
			final Item candidate = replacement(working, id, excluded, random);
			if (candidate == null) {
				LOGGER.warn("No eligible replacement for retired affix " + id + " on " + item.getName());
				return false; // Keep the complete original if no safe replacement exists.
			}
			for (final String key : ItemAffixState.getValues(candidate).keySet()) {
				if (!ItemAffixState.has(working, key)) replacements.put(id, key);
			}
			copyChanges(working, candidate);
		}
		final boolean validOffer = offer.get("fingerprint") != null
				&& offer.get("fingerprint").equals(ItemAffixRerollService.fingerprint(item));
		try {
			ItemAffixRerollService.rebaseRetiredOffer(working, replacements, random, validOffer);
		} catch (final IllegalArgumentException | IllegalStateException exception) {
			LOGGER.warn("Cannot migrate a paid affix offer on " + item.getName() + "; keeping saved item intact", exception);
			return false;
		}
		copyChanges(item, working);
		return true;
	}

	private static Item replacement(final Item original, final String retired,
			final Set<String> excluded, final Random random) {
		final Item stripped = copy(original);
		ItemAffixRerollService.remove(stripped, retired);
		final List<ItemAffixDefinition> pool = new ArrayList<>(BINDING.equals(retired)
				? LegendaryItemAffixRegistry.getInstance().getDefinitions() : ItemAffixRegistry.getInstance().getDefinitions());
		Collections.shuffle(pool, random);
		for (final ItemAffixDefinition definition : pool) {
			if (excluded.contains(definition.getId()) || ItemAffixState.has(stripped, definition.getId())
					|| !definition.isEligible(stripped)) continue;
			final Item candidate = copy(stripped);
			if (!definition.apply(candidate, random)) continue;
			ItemAffixState.record(candidate, definition);
			return candidate;
		}
		return null;
	}

	private static Item copy(final Item item) {
		return item instanceof StatusResistantItem ? new StatusResistantItem((StatusResistantItem) item) : new Item(item);
	}

	private static void copyChanges(final Item target, final Item source) {
		final Map<String, String> affixes = new LinkedHashMap<>(ItemAffixState.getValues(source));
		final Map<String, String> reroll = ItemAffixRerollService.state(source);
		final Set<String> keys = new HashSet<>();
		for (final String key : target) keys.add(key);
		for (final String key : source) keys.add(key);
		for (final String key : keys) {
			if (!source.has(key)) target.remove(key);
			else if (!target.has(key) || !target.get(key).equals(source.get(key))) target.put(key, source.get(key));
		}
		if (target.hasMap(ItemAffixState.ATTRIBUTE)) {
			for (final String id : new HashSet<>(target.getMap(ItemAffixState.ATTRIBUTE).keySet())) target.remove(ItemAffixState.ATTRIBUTE, id);
		}
		for (final Map.Entry<String, String> entry : affixes.entrySet())
			target.put(ItemAffixState.ATTRIBUTE, entry.getKey(), entry.getValue());
		ItemAffixRerollService.replaceState(target, reroll);
	}
}
