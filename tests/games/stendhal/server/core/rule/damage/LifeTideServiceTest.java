package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.*;

import java.util.Random;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.rarity.ItemAffixDefinition;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.rarity.LegendaryItemAffixRegistry;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Potion;
import games.stendhal.server.entity.item.consumption.FeederFactory;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.PlayerTestHelper;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

public class LifeTideServiceTest {
	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Test
	public void legendarySignatureIsAvailableOnArmourAndPersistsAsAMarker() {
		final ItemAffixDefinition definition = LegendaryItemAffixRegistry.getInstance()
				.get("legendary_life_tide");
		assertNotNull(definition);
		final Item armour = new Item("tide armour", "armor", "test", null);
		assertTrue(definition.apply(armour, new Random(1)));
		assertEquals(1.0, armour.getDouble("legendary_life_tide"), 0);
		assertFalse(definition.isEligible(new Item("sword", "sword", "test", null)));
	}

	@Test
	public void potionOverflowUsesItsRealHealingAndDoesNotRaiseMaximumHealth() {
		final Player player = player("tide-overflow", 10000);
		player.setHP(9500);
		assertEquals(1000, LifeTideService.healFromPotion(player, 1000, 0));
		assertEquals(10500, player.getHP());
		assertEquals(10000, player.getBaseHP());
		LifeTideService.healFromPotion(player, 9000, 1);
		assertEquals(12000, player.getHP());
	}

	@Test
	public void capacityScalesPastTwoThousandAndSupportsHpAboveTheOldShortLimit() {
		final Player player = player("tide-capacity", 32767);
		LifeTideService.healFromPotion(player, 100000, 0);
		assertEquals(39320, player.getHP());
		assertEquals(32767, player.getInt("hp"));
		assertEquals(39320, player.getInt("modified_hp"));
		assertEquals(32767, player.getBaseHP());
	}

	@Test
	public void savedHealthKeepsItsLegacyFormatAndDoesNotStoreTemporaryOverflow() throws Exception {
		final Player player = player("tide-save", 32767);
		LifeTideService.healFromPotion(player, 10000, 0);
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		player.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final InputSerializer input = new InputSerializer(new ByteArrayInputStream(bytes.toByteArray()));
		final RPObject saved = (RPObject) input.readObject(new RPObject());
		assertEquals(32767, saved.getInt("hp"));
		assertFalse(saved.has("modified_hp"));
		assertEquals(32767, new Player(saved).getHP());
	}

	@Test
	public void actualPotionConsumptionCanHealAFullHealthPlayerAboveMaximum() {
		final Player player = player("tide-potion", 10000);
		final Potion potion = (Potion) SingletonRepository.getEntityManager().getItem("duży eliksir");
		potion.setQuantity(2);
		player.getSlot("bag").add(potion);
		assertTrue(FeederFactory.get(potion).feed(potion, player));
		assertEquals(10000 + Math.min(2000, potion.getAmount()), player.getHP());
		assertEquals(1, potion.getQuantity());
	}

	@Test
	public void ordinaryHealingAndFullHealingPreserveSurplusWithoutAddingToIt() {
		final Player player = player("tide-normal-heal", 10000);
		LifeTideService.healFromPotion(player, 1000, 0);
		assertEquals(0, player.heal(500, true));
		assertEquals(0, player.heal());
		assertEquals(11000, player.getHP());
		player.onHealed(player, 500);
		assertEquals(11000, player.getHP());
	}

	@Test
	public void decayStartsAfterTenSecondsAndRemovesTwentyHpEveryTwoSeconds() {
		final Player player = player("tide-decay", 10000);
		LifeTideService.healFromPotion(player, 1000, 1000);
		LifeTideService.tick(player, 10999);
		assertEquals(11000, player.getHP());
		LifeTideService.tick(player, 11000);
		assertEquals(10980, player.getHP());
		LifeTideService.tick(player, 12999);
		assertEquals(10980, player.getHP());
		LifeTideService.tick(player, 13000);
		assertEquals(10960, player.getHP());
		LifeTideService.tick(player, 1000000);
		assertEquals(10000, player.getHP());
	}

	@Test
	public void refillingAnExistingSurplusDoesNotPostponeItsDecay() {
		final Player player = player("tide-refill", 10000);
		LifeTideService.healFromPotion(player, 1000, 0);
		LifeTideService.healFromPotion(player, 1000, 9000);
		LifeTideService.tick(player, 10000);
		assertEquals(11980, player.getHP());
	}

	@Test
	public void damageConsumesSurplusAndDecayNeverHurtsOrdinaryHealth() {
		final Player player = player("tide-damage", 10000);
		new StendhalRPZone("life_tide_damage", 10, 10).add(player);
		LifeTideService.healFromPotion(player, 1000, 0);
		player.onDamaged(player, 400);
		assertEquals(10600, player.getHP());
		player.onDamaged(player, 800);
		assertEquals(9800, player.getHP());
		LifeTideService.tick(player, 10000);
		assertEquals(9800, player.getHP());
	}

	@Test
	public void duplicateSignaturesDoNotStackAndInventoryItemsCannotEnableIt() {
		final Player player = player("tide-duplicate", 10000);
		player.getSlot("cloak").add(affixItem("cloak"));
		LifeTideService.healFromPotion(player, 5000, 0);
		assertEquals(12000, player.getHP());
		final Player without = PlayerTestHelper.createPlayer("tide-bag");
		without.initHP(10000);
		without.getSlot("bag").add(affixItem("armor"));
		LifeTideService.healFromPotion(without, 5000, 0);
		assertEquals(10000, without.getHP());
	}

	@Test
	public void unequippingTheAffixClearsSurplusImmediately() {
		final Player player = player("tide-unequip", 10000);
		LifeTideService.healFromPotion(player, 1000, 0);
		final Item armour = (Item) player.getSlot("armor").iterator().next();
		armour.onUnequipped();
		assertEquals(10000, player.getHP());
	}

	@Test
	public void resetAndReloadDiscardTemporarySurplus() {
		final Player player = player("tide-reset", 10000);
		LifeTideService.healFromPotion(player, 1000, 0);
		LifeTideService.reset(player);
		assertEquals(10000, player.getHP());
		LifeTideService.healFromPotion(player, 1000, 1);
		final Player reloaded = new Player(player);
		assertTrue(reloaded.getHP() <= reloaded.getBaseHP());
	}

	private Player player(final String name, final int maximumHP) {
		final Player player = PlayerTestHelper.createPlayer(name);
		player.initHP(maximumHP);
		player.getSlot("armor").add(affixItem("armor"));
		return player;
	}

	private Item affixItem(final String itemClass) {
		final Item item = new Item("tide gear", itemClass, "test", null);
		item.put("legendary_life_tide", 1.0);
		return item;
	}
}
