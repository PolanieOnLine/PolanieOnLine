package games.stendhal.server.core.rule.glyph;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Killer;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.spell.effect.DamageEffect;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

/** Exercises real damage sinks and the spell callback, rather than effect helpers. */
public class GlyphCombatIntegrationTest {
    private StendhalRPZone zone;

    @Before
    public void setUp() {
        MockStendlRPWorld.get();
        MockStendhalRPRuleProcessor.get();
        ItemTestHelper.generateRPClasses();
        PlayerTestHelper.generatePlayerRPClasses();
        zone = new StendhalRPZone("glyph-integration");
    }

    private Player target(final int maximum, final int current) {
        final Player player = PlayerTestHelper.createPlayer("endurance-target");
        player.setBaseHP(maximum);
        player.setHP(current);
        final Item glyph = new Item("endurance", "glyph", "test", null);
        glyph.put("low_health_damage_reduction", 10.0);
        player.getSlot("utility_rune").add(glyph);
        zone.add(player);
        return player;
    }

    private Player caster() {
        // Only replace the random damage roll; all spell and damage hooks remain real.
        final Player caster = new Player(PlayerTestHelper.createPlayer("spell-caster")) {
            @Override
            public int damageDone(final RPEntity defender, final double weapon,
                    final Nature nature) {
                return 100;
            }
        };
        caster.setBaseHP(1000);
        caster.setHP(100);
        zone.add(caster);
        return caster;
    }

    @Test
    public void lethalHitIsReducedBeforeCurrentHealthCap() {
        final Player target = target(100, 30);
        target.onDamaged(caster(), 100);
        assertEquals(0, target.getHP());
    }

    @Test
    public void environmentalDamageSinkReducesOnlyOnceAndReturnsActualLoss() {
        final Player target = target(1000, 300);
        assertEquals(90, target.damage(100, (Killer) caster()));
        assertEquals(210, target.getHP());
    }

    @Test
    public void environmentalOverkillReturnsRemainingHealthAndCallsDeathHook() {
        final boolean[] died = { false };
        final Player target = new Player(target(100, 30)) {
            @Override
            public void onDead(final Killer killer, final boolean remove) {
                died[0] = true;
            }
        };
        assertEquals(30, target.damage(100, (Killer) caster()));
        assertEquals(0, target.getHP());
        org.junit.Assert.assertTrue(died[0]);
    }

    @Test
    public void spellLifestealUsesEnduranceReducedDamage() {
        final Player target = target(1000, 300);
        final Player caster = caster();
        final DamageEffect effect = new DamageEffect(Nature.FIRE, 1, 100, 0,
                .5, 1, 0, 1);
        effect.act(caster, target);
        effect.onTurnReached(0);
        assertEquals(210, target.getHP());
        assertEquals(145, caster.getHP());
    }

    @Test
    public void spellOverkillLifestealIsCappedToActualRemainingHealth() {
        final Player target = target(100, 30);
        final Player caster = caster();
        final DamageEffect effect = new DamageEffect(Nature.FIRE, 1, 100, 0,
                .5, 1, 0, 1);
        effect.act(caster, target);
        effect.onTurnReached(0);
        assertEquals(0, target.getHP());
        assertEquals(115, caster.getHP());
    }
}
