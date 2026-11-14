package games.stendhal.server.core.events.advent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.events.advent.AdventCalendarRewards.Reward;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.PlayerTestHelper;

public class AdventCalendarRewardsTest {
	@Test
	public void grantsConfiguredEquipmentRarityWithPersistedAffixes() throws Exception {
		PlayerTestHelper.createPlayer("advent_rarity");
		for (final ItemRarity rarity : new ItemRarity[] {ItemRarity.RARE, ItemRarity.EPIC, ItemRarity.LEGENDARY}) {
			final String json = exampleCalendar().replace(
					"\"1\":[{\"name\":\"money\",\"quantity\":1,\"equipment\":false}]",
					"\"1\":[{\"name\":\"drewniana tarcza\",\"quantity\":1,\"rarity\":\"" + rarity.getId() + "\"}]");
			final List<Reward> rewards = AdventCalendarRewards.read(new StringReader(json)).get(0);
			final Item item = AdventCalendarRewards.createItems(rewards).get(0);
			assertEquals(rarity, item.getRarity());
			assertTrue(ItemAffixState.hasAny(item));
			assertTrue(ItemAffixState.hasSeed(item));
			final Item saved = new Item(item);
			assertEquals(rarity, saved.getRarity());
			assertEquals(ItemAffixState.getValues(item), ItemAffixState.getValues(saved));
		}
	}

	@Test
	public void rejectsUnknownRarityRatherThanReplacingItWithCommon() throws Exception {
		for (final String rarity : new String[] {"\"legendery\"", "3", "null"}) {
			try {
				AdventCalendarRewards.read(new StringReader(exampleCalendar().replace(
						"\"equipment\":false", "\"rarity\":" + rarity)));
				fail("Invalid rarity must be rejected");
			} catch (final IllegalArgumentException expected) {
				// Configuration typos must not silently downgrade equipment.
			}
		}
	}

	static String exampleCalendar() {
		final StringBuilder json = new StringBuilder("{\"_instructions\":[\"Example only\"],\"days\":{");
		for (int day = 1; day <= 24; day++) {
			if (day > 1) {
				json.append(',');
			}
			json.append('"').append(day).append("\":[{\"name\":\"money\",\"quantity\":")
					.append(day).append(",\"equipment\":false}]");
		}
		return json.append("}}").toString();
	}

	@Test
	public void readsDaysFromJsonIncludingInstructionsAndEquipment() throws Exception {
		final String json = exampleCalendar().replace(
				"\"24\":[{\"name\":\"money\",\"quantity\":24,\"equipment\":false}]",
				"\"24\":[{\"name\":\"drewniana tarcza\",\"quantity\":1,\"equipment\":true},"
				+ "{\"name\":\"polano\",\"quantity\":2}]");
		final List<List<Reward>> days = AdventCalendarRewards.read(new StringReader(json));
		assertEquals(24, days.size());
		assertEquals(13, days.get(12).get(0).quantity);
		assertFalse(days.get(0).get(0).equipment);
		assertEquals(2, days.get(23).size());
		assertTrue(days.get(23).get(0).equipment);
		assertEquals("polano", days.get(23).get(1).name);
	}

	@Test
	public void rejectsInvalidOrIncompleteRewardConfiguration() throws Exception {
		for (final String json : new String[] {
				"not json", "{}", exampleCalendar().replace("\"24\":", "\"25\":"),
				exampleCalendar().replace("\"quantity\":1,", "\"quantity\":0,"),
				exampleCalendar().replace("\"quantity\":1,", "\"quantity\":1.5,"),
				exampleCalendar().replace("\"equipment\":false", "\"equipment\":\"false\""),
				exampleCalendar().replace("\"name\":\"money\"", "\"name\":\"\""),
				exampleCalendar().replace("\"quantity\":1,", "\"quantity\":2147483648,"),
				exampleCalendar().replace("\"quantity\"", "\"quantitty\"")}) {
			try {
				AdventCalendarRewards.read(new StringReader(json));
				fail("Invalid configuration must not silently use default prizes");
			} catch (final IllegalArgumentException expected) {
				// Incorrect configuration is rejected before any gift is granted.
			}
		}
	}

	@Test
	public void privateRewardsCannotBeFetchedAsWebClientResources() throws Exception {
		for (final String resource : new String[] {
				"/data/conf/advent-calendar-rewards.json", "/data//conf/ADVENT-CALENDAR-REWARDS.JSON",
				"/data/conf/advent-calendar-rewards.json.", "/data/conf/advent-calendar-rewards.json ",
				"/data./conf. /advent-calendar-rewards.json", "/data/./conf/advent-calendar-rewards.json",
				"/data/conf/ADVENT~1.JSO", "/data/conf::$INDEX_ALLOCATION/advent-calendar-rewards.json",
				"/data/conf/advent-calendar-rewards.json::$DATA"}) {
			assertNull(MockStendhalRPRuleProcessor.get().getResource(resource));
		}
		try (InputStream publicResource = MockStendhalRPRuleProcessor.get().getResource("/data/gui/slot.png")) {
			assertNotNull(publicResource);
		}
	}

	@Test
	public void publicExampleIsValidAndCanCreateItsDemonstrationItems() throws Exception {
		PlayerTestHelper.createPlayer("advent_example");
		try (InputStream stream = getClass().getResourceAsStream("/data/conf/advent-calendar-rewards.example.json")) {
			assertNotNull(stream);
			final List<List<Reward>> days = AdventCalendarRewards.read(new InputStreamReader(stream, StandardCharsets.UTF_8));
			assertEquals(24, days.size());
			for (final List<Reward> day : days) {
				assertEquals(day.size(), AdventCalendarRewards.createItems(day).size());
			}
		}
	}
}
