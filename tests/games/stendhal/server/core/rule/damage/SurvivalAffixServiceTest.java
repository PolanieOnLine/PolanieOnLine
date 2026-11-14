package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.mapstuff.area.DamagingArea;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.PlayerTestHelper;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

public class SurvivalAffixServiceTest {
	@BeforeClass public static void setup() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Test public void vigilanceProtectsOnlyTheFirstDirectHitFromEachAttacker() {
		final Player player = player("vigilance");
		player.getSlot("head").add(affix("helmet", "vigilance", 0.12));
		final Creature attacker = attacker(player);
		assertEquals(88, player.receiveCombatDamage(attacker, 100).getHealthDamage());
		assertEquals(100, player.receiveCombatDamage(attacker, 100).getHealthDamage());
		assertEquals(88, player.receiveCombatDamage(attacker(player), 100).getHealthDamage());
		player.onHealthDamaged(attacker, 100);
		assertEquals(624, player.getHP());
	}

	@Test public void ironResolvePreservesOneHealthAndStoresItsCooldownOnThePlayer() {
		final Player player = player("iron-resolve");
		player.getSlot("armor").add(affix("armor", "legendary_iron_resolve", 1));
		assertEquals(999, player.receiveCombatDamage(attacker(player), 5000).getHealthDamage());
		assertEquals(1, player.getHP());
		assertTrue(player.has("iron_resolve_ready_at"));
		assertTrue(Long.parseLong(player.get("iron_resolve_ready_at")) > System.currentTimeMillis());
	}

	@Test public void vigilanceRefreshesOnlyAfterTenSecondsWithoutThatAttackersHitAndCapsAtTwentyFivePercent() {
		final Player player = player("vigilance-timer");
		player.getSlot("head").add(affix("helmet", "vigilance", 0.50));
		player.getSlot("cloak").add(affix("cloak", "vigilance", 0.50));
		final Creature source = attacker(player);
		assertEquals(75, SurvivalAffixService.reduceDirectHit(player, source, 100, 1000));
		assertEquals(100, SurvivalAffixService.reduceDirectHit(player, source, 100, 10999));
		assertEquals(100, SurvivalAffixService.reduceDirectHit(player, source, 100, 11000));
		assertEquals(75, SurvivalAffixService.reduceDirectHit(player, source, 100, 21000));
	}

	@Test public void ironResolveCooldownSurvivesGearChangesAndReloadingSavedCharacterState() throws Exception {
		final Player player = player("iron-cooldown");
		player.getSlot("armor").add(affix("armor", "legendary_iron_resolve", 1));
		final ArmorPoolService.DamageResult lethal = ArmorPoolService.resolve(player, 5000, 1.0, false);
		assertEquals(999, SurvivalAffixService.guardFatalHit(player, lethal, 1000).getHealthDamage());
		player.getSlot("armor").clear();
		player.getSlot("pas").add(affix("belts", "legendary_iron_resolve", 1));
		assertEquals(1000, SurvivalAffixService.guardFatalHit(player, lethal, 2000).getHealthDamage());
		final Player restored = player("iron-reloaded");
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		player.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject saved = (RPObject) new InputSerializer(new ByteArrayInputStream(bytes.toByteArray()))
				.readObject(new RPObject());
		restored.put("iron_resolve_ready_at", saved.get("iron_resolve_ready_at"));
		restored.getSlot("armor").add(affix("armor", "legendary_iron_resolve", 1));
		assertEquals(1000, SurvivalAffixService.guardFatalHit(restored, lethal, 120999).getHealthDamage());
		assertEquals(999, SurvivalAffixService.guardFatalHit(restored, lethal, 121000).getHealthDamage());
	}

	@Test public void environmentalTicksAndSelfDamageDoNotTriggerIronResolve() {
		final Player victim = player("iron-environment");
		victim.getSlot("armor").add(affix("armor", "legendary_iron_resolve", 1));
		final DamagingArea fire = new DamagingArea(1, 1, 1, 5000, 1.0);
		victim.getZone().add(fire);
		victim.receiveCombatDamage(fire, 5000);
		assertFalse(victim.has("iron_resolve_ready_at"));
		final Player self = player("iron-self");
		self.getSlot("armor").add(affix("armor", "legendary_iron_resolve", 1));
		self.receiveCombatDamage(self, 5000);
		assertFalse(self.has("iron_resolve_ready_at"));
	}

	@Test public void woundsBypassIronResolveAndExistingArmorDamageIsPreserved() {
		final Player player = player("iron-wounds");
		player.getSlot("armor").add(affix("armor", "legendary_iron_resolve", 1));
		player.setBaseArmorHP(50);
		player.setArmorHP(50);
		final ArmorPoolService.DamageResult lethal = ArmorPoolService.resolve(player, 5000, 1.0, false);
		final ArmorPoolService.DamageResult saved = SurvivalAffixService.guardFatalHit(player, lethal, 1000);
		assertEquals(50, saved.getArmorDamage());
		assertEquals(999, saved.getHealthDamage());
		player.remove("iron_resolve_ready_at");
		player.onHealthDamaged(attacker(player), 5000);
		assertFalse(player.has("iron_resolve_ready_at"));
	}

	private Player player(final String name) {
		final Player player = PlayerTestHelper.createPlayer(name);
		player.initHP(1000);
		new StendhalRPZone("survival_" + name, 20, 20).add(player);
		player.setPosition(5, 5);
		return player;
	}

	private Creature attacker(final Player player) {
		final Creature creature = new Creature();
		creature.setGender("M");
		creature.put("name", "test attacker");
		creature.initHP(100);
		player.getZone().add(creature);
		creature.setPosition(6, 5);
		return creature;
	}

	private Item affix(final String type, final String id, final double value) {
		final Item item = new Item("survival equipment", type, "test", new HashMap<String, String>());
		item.put(ItemAffixState.ATTRIBUTE, id, Double.toString(value));
		return item;
	}
}
