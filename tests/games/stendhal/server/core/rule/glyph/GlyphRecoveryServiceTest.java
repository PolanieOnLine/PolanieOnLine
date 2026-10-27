package games.stendhal.server.core.rule.glyph;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Slots;
import utilities.PlayerTestHelper;

public class GlyphRecoveryServiceTest {
    private Player player;
    private Glyph glyph;

    @Before
    public void setUp() {
        player = PlayerTestHelper.createPlayer("recovery");
        player.setBaseHP(1000);
        player.setHP(100);
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("out_of_combat_regen", "1");
        attributes.put("consumable_healing_bonus", "20");
        glyph = new Glyph("recovery glyph", "glyph", "glyph_test", attributes);
        player.getSlot(Slots.GLYPHS.getNames().get(0)).add(glyph);
    }


    @Test
    public void progressionCapsRecoveryAndPreservesSubHealthPulses() {
        player.getSlot(Slots.GLYPHS.getNames().get(0)).clear();
        glyph = new Glyph("glif żywej wody", "glyph", "glyph_water", null);
        player.getSlot(Slots.GLYPHS.getNames().get(0)).add(glyph);
        for (int level = 1; level <= 3; level++) {
            GlyphProgressionService.applyLevel(glyph, level);
            player.setBaseHP(5000); player.setHP(100);
            GlyphRecoveryService.tick(player, 0); GlyphRecoveryService.tick(player, 10000);
            assertEquals(100 + new int[] {8, 14, 20}[level - 1], player.getHP());
        }
        GlyphProgressionService.applyLevel(glyph, 1);
        player.setBaseHP(100); player.setHP(50);
        GlyphRecoveryService.tick(player, 0); GlyphRecoveryService.tick(player, 10000);
        assertEquals(50, player.getHP());
        GlyphRecoveryService.tick(player, 15000);
        assertEquals(51, player.getHP());
        player.setHP(100); GlyphRecoveryService.tick(player, 20000);
        player.setHP(50); GlyphRecoveryService.tick(player, 25000);
        assertEquals(50, player.getHP());
    }

    @Test
    public void waitsTenSecondsThenFiveSecondsBetweenPulses() {
        GlyphRecoveryService.tick(player, 0);
        GlyphRecoveryService.tick(player, 9999);
        assertEquals(100, player.getHP());
        GlyphRecoveryService.tick(player, 10000);
        assertEquals(110, player.getHP());
        GlyphRecoveryService.tick(player, 14999);
        assertEquals(110, player.getHP());
        GlyphRecoveryService.tick(player, 15000);
        assertEquals(120, player.getHP());
    }

    @Test
    public void positiveDamageOnEitherSideRestartsQuietPeriod() {
        final Player other = PlayerTestHelper.createPlayer("other");
        GlyphRecoveryService.tick(player, 0);
        GlyphRecoveryService.recordDamage(other, player, 1, 9000);
        GlyphRecoveryService.tick(player, 10000);
        assertEquals(100, player.getHP());
        GlyphRecoveryService.recordDamage(player, other, 0, 18000);
        GlyphRecoveryService.tick(player, 19000);
        assertEquals(110, player.getHP());
        GlyphRecoveryService.recordDamage(player, other, 1, 20000);
        GlyphRecoveryService.tick(player, 29999);
        assertEquals(110, player.getHP());
        GlyphRecoveryService.tick(player, 30000);
        assertEquals(120, player.getHP());
    }

    @Test
    public void capsAtTwentyAndDoesNotBankPulsesOrOverheal() {
        player.setBaseHP(5000);
        GlyphRecoveryService.tick(player, 0);
        GlyphRecoveryService.tick(player, 1000000);
        assertEquals(120, player.getHP());
        player.setHP(4999);
        GlyphRecoveryService.tick(player, 1005000);
        assertEquals(5000, player.getHP());
    }

    @Test
    public void deathAndEquipmentResetRequireFreshQuietPeriod() {
        GlyphRecoveryService.tick(player, 0);
        player.setHP(0);
        GlyphRecoveryService.tick(player, 10000);
        assertEquals(0, player.getHP());
        player.setHP(100);
        GlyphRecoveryService.tick(player, 20000);
        assertEquals(100, player.getHP());
        glyph.onUnequipped();
        glyph.onEquipped(player, Slots.GLYPHS.getNames().get(0));
        GlyphRecoveryService.tick(player, 29000);
        GlyphRecoveryService.tick(player, 38999);
        assertEquals(100, player.getHP());
        GlyphRecoveryService.tick(player, 39000);
        assertEquals(110, player.getHP());
    }

    @Test
    public void onlyExplicitConsumableHealingGetsBonus() {
        assertEquals(120, GlyphRecoveryService.boostConsumableHealing(player, 100));
        assertEquals(-10, GlyphRecoveryService.boostConsumableHealing(player, -10));
        player.heal(100, false);
        assertEquals(200, player.getHP());
        GlyphRecoveryService.tick(player, 0);
        GlyphRecoveryService.tick(player, 10000);
        assertEquals(210, player.getHP());
    }
    @Test
    public void fractionalConsumableBonusAccumulatesAcrossSmallPulses() {
        glyph.remove("out_of_combat_regen");
        int total = 0;
        for (int i = 0; i < 5; i++) {
            total += GlyphRecoveryService.boostConsumableHealing(player, 1);
            GlyphRecoveryService.tick(player, i * 1000L);
        }
        assertEquals(6, total);
        final Player plain = PlayerTestHelper.createPlayer("plain");
        total = 0;
        for (int i = 0; i < 5; i++) {
            total += GlyphRecoveryService.boostConsumableHealing(plain, 1);
        }
        assertEquals(5, total);
    }

    @Test
    public void equipmentResetDiscardsFractionalConsumableCredit() {
        for (int i = 0; i < 4; i++) {
            assertEquals(1, GlyphRecoveryService.boostConsumableHealing(player, 1));
        }
        GlyphRecoveryService.reset(player);
        assertEquals(1, GlyphRecoveryService.boostConsumableHealing(player, 1));
        final int hp = player.getHP();
        player.heal(1, false);
        assertEquals(hp + 1, player.getHP());
    }

}
