package games.stendhal.server.actions.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.RPClass.CreatureTestHelper;
import utilities.RPClass.ItemTestHelper;

public class LookActionItemTest {
	@BeforeClass
	public static void setUpClass() {
		ItemTestHelper.generateRPClasses();
		MockStendlRPWorld.get();
		PlayerTestHelper.generatePlayerRPClasses();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void itemLookShowsOnlyConfiguredDescription() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("description", "Stary miecz z wyszczerbionym ostrzem.");
		attributes.put("atk", "99");
		attributes.put("def", "12");
		final Item item = new Item("miecz testowy", "sword", "test", attributes);
		item.setRarity(ItemRarity.LEGENDARY);
		item.setValue(12345);

		assertEquals("Stary miecz z wyszczerbionym ostrzem.",
				LookAction.getLookDescription(item));
	}

	@Test
	public void boundItemLookShowsBindingAfterDescription() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("description", "Stary miecz z wyszczerbionym ostrzem.");
		final Item item = new Item("miecz testowy", "sword", "test", attributes);
		item.setBoundTo("Ala");

		assertEquals("Stary miecz z wyszczerbionym ostrzem. "
				+ "Oto specjalna nagroda dla Ala, która nie może być wykorzystana przez innych.",
				LookAction.getLookDescription(item));
	}

	@Test
	public void itemWithoutDescriptionGetsNeutralFallbackOnly() {
		final Item item = new Item("miecz testowy", "sword", "test", null);
		item.setRarity(ItemRarity.RARE);
		item.setValue(999);

		assertEquals("Oto §'miecz testowy'.", LookAction.getLookDescription(item));
	}

	@Test
	public void translatedLookKeepsSavedItemDataAndBoundNickname() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("description", "Oto drewniany łuk. Z tą bronią w jednej ręce i strzałami w drugiej, bedziesz mógł atakować z dystansu.");
		attributes.put("atk", "99");
		final Item item = new Item("drewniany łuk", "bow", "wooden_bow", attributes);
		item.setRarity(ItemRarity.LEGENDARY);
		item.setValue(12345);
		item.setBoundTo("Zamknij");
		final String saved = item.toString();
		assertEquals("You see a §'wooden bow'. With this weapon in one hand and arrows in the other, you can attack from a distance. "
				+ "This is a special reward for Zamknij and cannot be used by anyone else.",
				LookAction.getLookDescription(item, "en"));
		assertEquals(saved, item.toString());
		assertEquals(LookAction.getLookDescription(item), LookAction.getLookDescription(item, "pl"));
	}

	@Test
	public void translatedFallbackUsesDisplayNameWithoutExposingStats() {
		final Item item = new Item("polano", "wood", "wood", null);
		item.setValue(999);
		item.setRarity(ItemRarity.EPIC);
		assertEquals("You see §'wood'.", LookAction.getLookDescription(item, "en"));
		assertEquals("polano", item.getName());
	}

	@Test
	public void polishDescriptionUnderlinesOnlyTheItemNameNotItsOwner() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("description", "Oto drewniany łuk. Broń do walki na dystans.");
		final Item item = new Item("drewniany łuk", "bow", "wooden_bow", attributes);
		item.setBoundTo("drewniany łuk");
		assertEquals("Oto §'drewniany łuk'. Broń do walki na dystans. "
				+ "Oto specjalna nagroda dla drewniany łuk, która nie może być wykorzystana przez innych.",
				LookAction.getLookDescription(item, "pl"));
		assertEquals("Oto drewniany łuk. Broń do walki na dystans.", item.getDescription());
	}

	@Test
	public void creatureLookTranslatesLevelAndArmorSeparately() {
		final Creature creature = new Creature();
		creature.setName("serafin");
		creature.setLevel(123);
		creature.setArmorType("heavy");
		assertEquals("You see seraph. Level 123. Heavy armor.", LookAction.getLookDescription(creature, "en"));
		assertEquals("Oto serafin. Poziom 123. Pancerz ciężki.", creature.describe());
	}

	@Test
	public void unarmoredCreatureLookOmitsArmorNoticeInBothLanguages() {
		final Creature creature = new Creature();
		creature.setName("serafin");
		creature.setLevel(123);
		assertEquals("Oto serafin. Poziom 123.", LookAction.getLookDescription(creature, "pl"));
		assertEquals("You see seraph. Level 123.", LookAction.getLookDescription(creature, "en"));
	}

	@Test
	public void authoredPlayerDescriptionAndNicknameAreNotTranslated() {
		final Player player = PlayerTestHelper.createPlayer("polano");
		assertTrue(LookAction.getLookDescription(player, "en").contains("polano"));
		assertFalse(LookAction.getLookDescription(player, "en").contains("wood"));
		player.setDescription("Zamknij");
		assertEquals("Zamknij", LookAction.getLookDescription(player, "en"));
	}

	@Test
	public void playerLookTranslatesOnlySystemFieldsAndKeepsPersonalMessagesVerbatim() {
		final Player player = PlayerTestHelper.createPlayer("Zamknij");
		player.setAge(125);
		player.setSentence("Zamknij\nWitaj. $5 \\opis");
		assertEquals("You see farmhand Zamknij.\nZamknij is level 0. Age: 2 hours and 5 minutes.\n"
				+ "Zamknij's description is: \"Zamknij\nWitaj. $5 \\opis\"", LookAction.getLookDescription(player, "en"));
		player.setAwayMessage("Zamknij\nWitaj.");
		player.setGrumpyMessage("polano");
		assertEquals("You see farmhand Zamknij.\nZamknij is level 0. Age: 2 hours and 5 minutes.\n"
				+ "Zamknij is away and has left a message: Zamknij\nWitaj.\n"
				+ "Zamknij is keeping to themselves and has left a message: polano", LookAction.getLookDescription(player, "en"));
		assertEquals("Zamknij\nWitaj.", player.getAwayMessage());
		assertEquals("polano", player.getGrumpyMessage());
	}

	@Test
	public void femaleRanksAndPostmanUseTheExistingPlayerDescriptionRules() {
		final Player player = PlayerTestHelper.createPlayer("polano");
		player.setGender("F");
		player.setLevel(350);
		player.setAge(60);
		assertEquals("You see baroness polano.\npolano is level 350. Age: 1 hour and 0 minutes.",
				LookAction.getLookDescription(player, "en"));
		assertTrue(player.describe().startsWith("Oto baronessa polano."));
		final Player postman = PlayerTestHelper.createPlayer("postman");
		assertEquals("You see postman, the postman who makes sure every letter reaches its recipient. Age: 0 hours and 0 minutes.",
				LookAction.getLookDescription(postman, "en"));
	}
}
