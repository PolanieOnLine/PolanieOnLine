/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.item.ItemDefinitionRefreshService;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Banks;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class ItemDefinitionRefreshTest {
	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void everyEligibleDefinitionKeepsItsRollAffixesAndUpgradeAcrossLogins() throws Exception {
		int checked = 0;
		final ItemRarityService rarity = ItemRarityService.getInstance();
		for (final String itemName : configuredNames()) {
			final Item prototype = definition(itemName);
			if (prototype == null || !rarity.isEligible(prototype)) continue;
			for (final ItemRarity tier : ItemRarity.values()) {
				final Item created = SingletonRepository.getEntityManager().getItem(prototype.getName(),
						ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
								.withRarity(tier).randomizeModifiers(false).withAffixSeed(321L).build());
				created.setID(new ID(800 + checked, "all_definition_refresh"));
				created.setUpgradeLevel(2);
				final Item first = restore(created);
				final Item second = restore(first);
				for (final String attribute : rarity.getSupportedStatistics()) {
					assertEquals(created.getName() + " " + tier + " " + attribute,
							created.has(attribute), second.has(attribute));
					if (created.has(attribute)) {
						assertEquals(created.getName() + " " + tier + " " + attribute,
								created.get(attribute), second.get(attribute));
					}
				}
				assertEquals(created.getName(), 2, second.getUpgradeLevel());
				assertEquals(created.getName(), tier, second.getRarity());
				assertEquals(created.getName(), ItemAffixState.getValues(created), ItemAffixState.getValues(second));
				assertEquals(created.getName(), ItemAffixState.getSeed(created), ItemAffixState.getSeed(second));
				checked++;
			}
		}
		assertTrue("Equipment definitions were not exercised", checked > 100);
	}

	@Test
	public void legacyPolishShieldsRefreshAndNeverLosePaidForUpgrades() throws Exception {
		final String[] names = {"polska płytowa tarcza", "polska tarcza ciężka"};
		final int[] oldDefense = {25, 28};
		final int[] currentDefense = {75, 84};
		for (int i = 0; i < names.length; i++) {
			Item saved = definition(names[i]);
			saved.setID(new ID(700 + i, "definition_refresh"));
			saved.put("def", oldDefense[i]);
			saved.put("improve", 3);
			saved.setPersistent(true);
			saved.setDescription("stary zwykły opis");
			saved.remove(Item.DEFINITION_DESCRIPTION);
			for (int login = 0; login < 3; login++) {
				saved = restore(saved);
				assertEquals(currentDefense[i], saved.getInt("def"));
				assertEquals(3, saved.getUpgradeLevel());
				assertEquals(currentDefense[i] + 3, saved.getDefense());
				assertEquals(definition(names[i]).getDescription(), saved.getDescription());
				assertEquals(Integer.toString(currentDefense[i] + 3),
						saved.getMap(ItemTooltip.ATTRIBUTE).get(ItemTooltip.DEFENSE));
				assertEquals(ItemRarity.COMMON, saved.getRarity());
			}
		}
	}

	@Test
	public void rarityRollAndAffixesBindingSeedAndUpgradeSurviveUpdatedBase() throws Exception {
		Item saved = SingletonRepository.getEntityManager().getItem("pogromca",
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withRarity(ItemRarity.LEGENDARY)
						.withModifiers(ItemRarityModifiers.builder().statMultiplier(1.25).valueMultiplier(2).build())
						.withAffixSeed(12345L).build());
		saved.setID(new ID(710, "definition_refresh"));
		final int expectedAttack = saved.getInt("atk");
		final int expectedMinimum = saved.getInt("damage_min");
		final int expectedMaximum = saved.getInt("damage_max");
		final Map<String, String> affixes = ItemAffixState.getValues(saved);
		final Map<String, Double> modifiers = saved.getRarityModifiers();
		saved.put("atk", 10);
		saved.put("damage_min", 8);
		saved.put("damage_max", 12);
		saved.setUpgradeLevel(2);
		saved.setBoundTo("kokol");
		saved.put("uses", 17);
		saved.put("logid", 123456);
		saved.setPersistent(true);
		for (int login = 0; login < 3; login++) {
			saved = restore(saved);
			assertEquals(expectedAttack, saved.getInt("atk"));
			assertEquals(expectedMinimum + 2, saved.getDamageMin());
			assertEquals(expectedMaximum + 2, saved.getDamageMax());
			assertEquals(2, saved.getUpgradeLevel());
			assertEquals(expectedAttack + 2, saved.getAttack());
			assertEquals(ItemRarity.LEGENDARY, saved.getRarity());
			assertEquals(affixes, ItemAffixState.getValues(saved));
			assertEquals(modifiers, saved.getRarityModifiers());
			assertEquals(Long.valueOf(12345L), ItemAffixState.getSeed(saved));
			assertEquals("kokol", saved.getBoundTo());
			assertEquals(17, saved.getInt("uses"));
			assertEquals(123456, saved.getInt("logid"));
		}
	}

	@Test
	public void savedDefenseMultiplierIsAppliedToNewBaseExactlyOnce() throws Exception {
		Item saved = definition("polska płytowa tarcza");
		saved.setID(new ID(711, "definition_refresh"));
		saved.setRarity(ItemRarity.RARE);
		saved.setRarityModifier("def", 1.10);
		saved.put("def", 28);
		saved.put("improve", 2);
		for (int login = 0; login < 3; login++) {
			saved = restore(saved);
			assertEquals(83, saved.getInt("def"));
			assertEquals(85, saved.getDefense());
			assertEquals(2, saved.getUpgradeLevel());
			assertEquals(Double.valueOf(1.10), saved.getRarityModifier("def"));
		}
	}

	@Test
	public void oldFlatDefenseAffixRemainsAddedOnceToTheUpdatedBase() throws Exception {
		Item saved = definition("polska płytowa tarcza");
		saved.setID(new ID(712, "definition_refresh"));
		saved.setRarity(ItemRarity.RARE);
		saved.setRarityModifier("def", 1.10);
		saved.put("def", 30);
		saved.put("flat_defense_bonus", 2);
		saved.put(ItemAffixState.ATTRIBUTE, "flat_defense_bonus", "2");
		saved.setUpgradeLevel(3);
		for (int login = 0; login < 3; login++) {
			saved = restore(saved);
			assertEquals(85, saved.getInt("def"));
			assertEquals(88, saved.getDefense());
			assertEquals(3, saved.getUpgradeLevel());
			assertEquals("2", saved.getMap(ItemAffixState.ATTRIBUTE).get("flat_defense_bonus"));
		}
	}

	@Test
	public void aLowerOrRemovedUpgradeLimitCannotDeleteAnExistingUpgrade() {
		final Item current = new Item("test", "shield", "test", Collections.singletonMap("def", "75"));
		final Item old = new Item(current);
		old.put("improve", 5);
		current.put("max_improves", 2);
		ItemDefinitionRefreshService.refresh(old, current, new RPObject(old));
		assertEquals(5, old.getUpgradeLevel());
		assertEquals(5, old.getMaxUpgradeLevel());
		assertEquals(80, old.getDefense());
		current.remove("max_improves");
		ItemDefinitionRefreshService.refresh(old, current, new RPObject(old));
		assertEquals(5, old.getUpgradeLevel());
		assertEquals(80, old.getDefense());
	}

	@Test
	public void loginRefreshesEquipmentEveryBankAndNestedContainers() throws Exception {
		final Player player = PlayerTestHelper.createPlayer("refresh-test");
		final java.util.List<String> slots = new java.util.ArrayList<>();
		Collections.addAll(slots, "bag", "lhand", "lhand_set", "magicbag");
		for (final Banks bank : Banks.values()) slots.add(bank.getSlotName());
		for (final String slot : slots) {
			final Item old = definition("polska tarcza ciężka");
			old.put("def", 28);
			old.put("improve", 2);
			player.getSlot(slot).add(old);
		}
		final Item container = definition("rzemyk");
		container.setPersistent(true);
		final Item nested = definition("polska tarcza ciężka");
		nested.put("def", 28);
		nested.put("improve", 3);
		container.getSlot("content").add(nested);
		player.getSlot("bag").add(container);
		new PlayerTransformer().loadItemsIntoSlots(player);
		for (final String slot : slots) {
			final Item updated = (Item) player.getSlot(slot).getFirst();
			assertEquals(slot, 84, updated.getInt("def"));
			assertEquals(slot, 2, updated.getUpgradeLevel());
		}
		Item outer = null;
		for (final RPObject object : player.getSlot("bag")) {
			if ("rzemyk".equals(object.get("name"))) outer = (Item) object;
		}
		assertNotNull(outer);
		assertEquals(1, outer.getSlot("content").size());
		final Item inner = (Item) outer.getSlot("content").getFirst();
		assertEquals(84, inner.getInt("def"));
		assertEquals(3, inner.getUpgradeLevel());
	}

	@Test
	public void consumableStatsRefreshButStackQuantityIsNotLost() throws Exception {
		final Item current = definition("duży eliksir");
		final StackableItem saved = (StackableItem) definition("duży eliksir");
		saved.setID(new ID(720, "definition_refresh"));
		saved.setQuantity(43);
		saved.put("amount", 1);
		saved.put("regen", 1);
		saved.setPersistent(true);
		final StackableItem updated = (StackableItem) restore(saved);
		assertEquals(43, updated.getQuantity());
		assertEquals(current.get("amount"), updated.get("amount"));
		assertEquals(current.get("regen"), updated.get("regen"));
	}

	@Test
	public void normalDescriptionsRefreshWhileExplicitPersonalTextSurvives() throws Exception {
		final Item current = definition("polska płytowa tarcza");
		final Item saved = definition("polska płytowa tarcza");
		saved.setID(new ID(721, "definition_refresh"));
		saved.put(Item.DEFINITION_DESCRIPTION, "stara baza opisu");
		saved.setDescription("stara baza opisu");
		assertEquals(current.getDescription(), restore(saved).getDescription());
		saved.setDescription("Pamiątka dla kokola.");
		assertEquals("Pamiątka dla kokola.", restore(restore(saved)).getDescription());
	}

	@Test
	public void personalScrollDestinationLabelAndStackRemainIntact() throws Exception {
		final StackableItem saved = (StackableItem) definition("zwój zapisany");
		saved.setID(new ID(722, "definition_refresh"));
		saved.setItemData("0_semos_city 10 20");
		saved.setQuantity(7);
		saved.setDescription("Pisze na nim: \"Dom\"");
		saved.remove(Item.DEFINITION_DESCRIPTION);
		final Item updated = restore(restore(saved));
		assertEquals("0_semos_city 10 20", updated.getItemData());
		assertEquals(saved.getDescription(), updated.getDescription());
		assertEquals(7, ((StackableItem) updated).getQuantity());
		assertTrue(updated.get("dest").contains("Dom"));
	}

	@Test
	public void newAccessoryBasePropertiesAreAddedEvenToPreviouslyCommonItems() {
		final Item old = new Item("test", "ring", "test", Collections.singletonMap("def", "1"));
		old.setRarity(ItemRarity.COMMON);
		final Item current = new Item(old);
		current.put("lifesteal", 0.025);
		current.put("critical_chance", 5.0);
		ItemDefinitionRefreshService.refresh(old, current, new RPObject(old));
		assertEquals(0.025, old.getDouble("lifesteal"), 0.0);
		assertEquals(5.0, old.getDouble("critical_chance"), 0.0);
	}

	@Test
	public void futureBaseChangesRemoveOldFieldsAndKeepNonBaseAffixes() {
		final Item old = new Item("test", "shield", "test", Collections.singletonMap("def", "28"));
		old.put("min_level", 1);
		old.put("regen", 9);
		old.setRarityModifier("def", 1.20);
		final Item current = new Item("test", "shield", "test", Collections.singletonMap("def", "90"));
		current.put("min_level", 30);
		final Set<String> base = ItemDefinitionRefreshService.refresh(old, current, new RPObject(old));
		assertEquals(108, old.getInt("def"));
		assertEquals(30, old.getInt("min_level"));
		assertFalse(old.has("regen"));
		assertTrue(base.contains("def"));
	}

	@Test
	public void oldAffixesAreLimitedAcrossRepeatedLoginsWithoutLosingProgress() throws Exception {
		Item saved = definition("miecz chaosu");
		saved.setID(new ID(740, "affix_balance"));
		saved.setRarity(ItemRarity.LEGENDARY);
		saved.setRarityModifier("atk", 1.25);
		saved.setRarityModifier("accuracy_bonus", 1.35);
		saved.put("improve", 3);
		saved.setBoundTo("kokol");
		saved.setPersistent(true);
		final String[] attributes = {"accuracy_bonus", "critical_chance", "critical_damage_bonus", "lifesteal", "parry_chance"};
		final double[] before = {15, 10, 0.30, 0.41, 0.15};
		final double[] after = {8, 6, 0.15, 0.41, 0.15};
		for (int i = 0; i < attributes.length; i++) {
			saved.put(attributes[i], before[i]);
			saved.put(ItemAffixState.ATTRIBUTE, attributes[i], saved.get(attributes[i]));
		}
		ItemAffixState.setSeed(saved, 54321L);
		for (int login = 0; login < 3; login++) {
			saved = restore(saved);
			for (int i = 0; i < attributes.length; i++) {
				assertEquals(attributes[i], after[i], saved.getDouble(attributes[i]), 0.000001);
				assertEquals(saved.get(attributes[i]), ItemAffixState.getValues(saved).get(attributes[i]));
			}
			assertEquals(5, ItemAffixState.getValues(saved).size());
			assertEquals(3, saved.getUpgradeLevel());
			assertEquals(saved.getInt("atk") + 3, saved.getAttack());
			assertEquals("kokol", saved.getBoundTo());
			assertEquals(Long.valueOf(54321L), ItemAffixState.getSeed(saved));
			assertEquals(Double.valueOf(1.25), saved.getRarityModifier("atk"));
			assertEquals(Double.valueOf(1.35), saved.getRarityModifier("accuracy_bonus"));
			assertEquals("8.0", saved.getMap(ItemTooltip.ATTRIBUTE).get(ItemTooltip.ACCURACY_BONUS));
		}
	}

	@Test
	public void flatAttackAndIntrinsicJewelleryBonusesRefreshExactlyOnce() throws Exception {
		Item saved = definition("pierścień magnata");
		saved.setID(new ID(741, "affix_balance"));
		final int baseAttack = saved.has("atk") ? saved.getInt("atk") : 0;
		saved.put("atk", baseAttack + 3);
		saved.put("flat_attack_bonus", 3);
		saved.put(ItemAffixState.ATTRIBUTE, "flat_attack_bonus", "3");
		saved.put("accuracy_bonus", 25.0);
		saved.setPersistent(true);
		saved.put("improve", 2);
		for (int login = 0; login < 3; login++) {
			saved = restore(saved);
			assertEquals(baseAttack + 2, saved.getInt("atk"));
			assertEquals(baseAttack + 4, saved.getAttack());
			assertEquals(2, saved.getUpgradeLevel());
			assertEquals("2", ItemAffixState.getValues(saved).get("flat_attack_bonus"));
		}
		Item intrinsic = definition("pierścień czystości");
		assertNotNull(intrinsic);
		intrinsic.setID(new ID(742, "affix_balance"));
		intrinsic.put("accuracy_bonus", 25.0);
		intrinsic = restore(intrinsic);
		assertEquals(7, intrinsic.getDouble("accuracy_bonus"), 0);
		assertFalse(ItemAffixState.hasAny(intrinsic));
	}

	@Test
	public void affixMigrationReachesBanksEquipmentAndNestedContainers() throws Exception {
		final Player player = PlayerTestHelper.createPlayer("affix-refresh");
		final java.util.List<String> slots = new java.util.ArrayList<>();
		Collections.addAll(slots, "bag", "lhand", "lhand_set", "magicbag");
		for (final Banks bank : Banks.values()) slots.add(bank.getSlotName());
		for (final String slot : slots) player.getSlot(slot).add(oldAccuracyWeapon());
		final Item container = definition("rzemyk");
		container.setPersistent(true);
		container.getSlot("content").add(oldAccuracyWeapon());
		player.getSlot("bag").add(container);
		new PlayerTransformer().loadItemsIntoSlots(player);
		for (final String slot : slots) {
			final Item updated = (Item) player.getSlot(slot).getFirst();
			assertEquals(slot, 8, updated.getDouble("accuracy_bonus"), 0);
			assertEquals(slot, 3, updated.getUpgradeLevel());
		}
		for (final RPObject object : player.getSlot("bag")) {
			if (!"rzemyk".equals(object.get("name"))) continue;
			assertEquals(1, object.getSlot("content").size());
			final Item updated = (Item) object.getSlot("content").getFirst();
			assertEquals(8, updated.getDouble("accuracy_bonus"), 0);
			assertEquals(3, updated.getUpgradeLevel());
		}
	}

	private Item oldAccuracyWeapon() {
		final Item item = definition("miecz chaosu");
		item.put("accuracy_bonus", 15.0);
		item.put(ItemAffixState.ATTRIBUTE, "accuracy_bonus", "15.0");
		item.put("improve", 3);
		return item;
	}

	private Item definition(final String name) {
		return SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.restore());
	}

	private Set<String> configuredNames() throws Exception {
		final Set<String> names = new LinkedHashSet<>();
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		try (Stream<Path> files = Files.walk(Paths.get("data/conf/items"))) {
			final Iterator<Path> iterator = files.filter(path -> path.toString().endsWith(".xml")).iterator();
			while (iterator.hasNext()) {
				final NodeList items = factory.newDocumentBuilder().parse(iterator.next().toFile())
						.getElementsByTagNameNS("stendhal", "item");
				for (int i = 0; i < items.getLength(); i++) {
					names.add(((Element) items.item(i)).getAttribute("name"));
				}
			}
		}
		return names;
	}

	private Item restore(final Item source) throws Exception {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject copy = new RPObject();
		copy.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
		return new ItemTransformer().transform(copy);
	}
}
