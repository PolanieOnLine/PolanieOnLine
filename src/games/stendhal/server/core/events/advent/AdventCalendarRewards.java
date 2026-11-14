package games.stendhal.server.core.events.advent;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;

/** Server-only reward catalogue. Unopened rewards never enter client payloads. */
public final class AdventCalendarRewards {
	public static final Path CONFIGURATION = Paths.get("data", "conf", "advent-calendar-rewards.json");
	private static List<List<Reward>> configuredDays;

	private AdventCalendarRewards() {
	}

	private static synchronized List<List<Reward>> configuredDays() {
		if (configuredDays == null) {
			try (Reader reader = Files.newBufferedReader(CONFIGURATION, StandardCharsets.UTF_8)) {
				configuredDays = read(reader);
			} catch (final IOException | IllegalArgumentException exception) {
				throw new IllegalStateException("Cannot load private advent rewards from "
						+ CONFIGURATION.toAbsolutePath(), exception);
			}
		}
		return configuredDays;
	}

	public static List<Reward> forDay(final int day) {
		if (day < 1 || day > 24) {
			throw new IllegalArgumentException("Invalid advent day: " + day);
		}
		return configuredDays().get(day - 1);
	}

	public static List<Item> createItems(final int day) {
		return createItems(forDay(day));
	}

	static List<Item> createItems(final List<Reward> rewards) {
		final List<Item> items = new ArrayList<>();
		for (final Reward reward : rewards) {
			final Item item = SingletonRepository.getEntityManager().getItem(reward.name,
					reward.equipment ? ItemCreationContext.questReward() : ItemCreationContext.defaultCreation());
			if (item == null) {
				throw new IllegalStateException("Unknown advent reward: " + reward.name);
			}
			if (item instanceof StackableItem) {
				((StackableItem) item).setQuantity(reward.quantity);
			} else if (reward.quantity != 1) {
				throw new IllegalArgumentException("Non-stackable advent reward must have quantity 1: " + reward.name);
			}
			items.add(item);
		}
		return items;
	}

	static List<List<Reward>> read(final Reader reader) throws IOException {
		final Object document;
		try {
			document = new JSONParser().parse(reader);
		} catch (final ParseException exception) {
			throw new IllegalArgumentException("Invalid advent rewards JSON", exception);
		}
		final Map<?, ?> root = object(document, "root");
		checkKeys(root, "days", "_instructions", "_comment");
		final Map<?, ?> days = object(root.get("days"), "days");
		if (days.size() != 24) {
			throw new IllegalArgumentException("Advent rewards must contain exactly days 1 to 24");
		}
		final List<List<Reward>> result = new ArrayList<>();
		for (int day = 1; day <= 24; day++) {
			final Object entries = days.get(Integer.toString(day));
			if (!(entries instanceof List) || ((List<?>) entries).isEmpty()) {
				throw new IllegalArgumentException("Missing or empty advent rewards for day " + day);
			}
			final List<Reward> rewards = new ArrayList<>();
			for (final Object entry : (List<?>) entries) {
				final Map<?, ?> reward = object(entry, "day " + day);
				checkKeys(reward, "name", "quantity", "equipment", "_comment");
				final Object name = reward.get("name");
				final Object quantity = reward.get("quantity");
				final Object equipment = reward.get("equipment");
				if (!(name instanceof String) || ((String) name).trim().isEmpty()
						|| !(quantity instanceof Long) || (Long) quantity < 1 || (Long) quantity > Integer.MAX_VALUE
						|| (reward.containsKey("equipment") && !(equipment instanceof Boolean))) {
					throw new IllegalArgumentException("Invalid advent reward for day " + day);
				}
				rewards.add(new Reward(((String) name).trim(), ((Long) quantity).intValue(), Boolean.TRUE.equals(equipment)));
			}
			result.add(Collections.unmodifiableList(rewards));
		}
		return Collections.unmodifiableList(result);
	}

	private static Map<?, ?> object(final Object value, final String field) {
		if (!(value instanceof Map)) {
			throw new IllegalArgumentException("Expected JSON object: " + field);
		}
		return (Map<?, ?>) value;
	}

	private static void checkKeys(final Map<?, ?> value, final String... allowed) {
		for (final Object key : value.keySet()) {
			if (!Arrays.asList(allowed).contains(key)) {
				throw new IllegalArgumentException("Unknown advent reward field: " + key);
			}
		}
	}

	public static final class Reward {
		public final String name;
		public final int quantity;
		public final boolean equipment;

		private Reward(final String name, final int quantity, final boolean equipment) {
			this.name = name;
			this.quantity = quantity;
			this.equipment = equipment;
		}
	}
}
