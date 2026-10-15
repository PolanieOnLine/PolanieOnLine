package games.stendhal.server.core.rule.glyph;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class GlyphCombatServiceTest {
    @BeforeClass public static void setup() {
        ItemTestHelper.generateRPClasses();
        PlayerTestHelper.generatePlayerRPClasses();
    }
    private Player player(String name) {
        Player p = PlayerTestHelper.createPlayer(name);
        p.setBaseHP(1000); p.setHP(1000); return p;
    }
    private void equip(Player p, String slot, String key, double value) {
        Item g = new Item("test glyph", "glyph", "test", null);
        g.put(key, value); p.getSlot(slot).add(g);
    }
    @Test public void oakReducesOnlyCriticalBonus() {
        Player p=player("oak"); equip(p,"defensive_rune","critical_bonus_reduction",25);
        assertEquals(175, GlyphCombatService.reduceCriticalBonus(p,100,200));
        assertEquals(100, GlyphCombatService.reduceCriticalBonus(p,100,100));
        assertEquals(194, GlyphCombatService.reduceCriticalBonus(p,100,225));
    }
    @Test public void whirlwindDodgesOnlyNormalPhysicalHits() {
        Player p=player("wind"); equip(p,"utility_rune","physical_dodge_chance",5);
        assertTrue(GlyphCombatService.dodges(p,Nature.CUT,false,5));
        assertFalse(GlyphCombatService.dodges(p,Nature.CUT,false,6));
        assertFalse(GlyphCombatService.dodges(p,Nature.CUT,true,1));
        assertFalse(GlyphCombatService.dodges(p,Nature.FIRE,false,1));
    }
    @Test public void enduranceHasStrictThresholdAndNoEffectOnHealing() {
        Player p=player("endurance"); equip(p,"utility_rune","low_health_damage_reduction",10);
        p.setHP(350); assertEquals(100,GlyphCombatService.reduceIncomingDamage(p,100));
        p.setHP(349); assertEquals(90,GlyphCombatService.reduceIncomingDamage(p,100));
        assertEquals(0,GlyphCombatService.reduceIncomingDamage(p,0));
        assertEquals(-5,GlyphCombatService.reduceIncomingDamage(p,-5));
        p.setHP(0); assertEquals(100,GlyphCombatService.reduceIncomingDamage(p,100));
    }
    @Test public void nightmareExpiresAndUsesPerAttackerCooldownWithoutStacking() {
        Player source=player("nightmare"), target=player("victim"), other=player("other");
        equip(source,"control_rune","nightmare_chance",10);
        equip(other,"control_rune","nightmare_chance",10);
        assertTrue(GlyphCombatService.tryNightmare(source,target,100,10,1000));
        assertEquals(.9,GlyphCombatService.outgoingAttackMultiplier(target,1001),.00001);
        assertFalse(GlyphCombatService.tryNightmare(source,other,100,1,2000));
        assertFalse(GlyphCombatService.tryNightmare(other,target,100,1,2000));
        assertEquals(1,GlyphCombatService.outgoingAttackMultiplier(target,7000),.00001);
        assertFalse(GlyphCombatService.tryNightmare(source,target,100,1,12999));
        assertTrue(GlyphCombatService.tryNightmare(source,target,100,1,13000));
    }
    @Test public void nightmareNeedsActualDamageLivingTargetsAndEquippedGlyph() {
        Player source=player("no-proc"), target=player("no-victim");
        assertFalse(GlyphCombatService.tryNightmare(source,target,100,1,1000));
        equip(source,"control_rune","nightmare_chance",10);
        assertFalse(GlyphCombatService.tryNightmare(source,target,0,1,1000));
        assertFalse(GlyphCombatService.tryNightmare(source,target,100,11,1000));
        target.setHP(0);
        assertFalse(GlyphCombatService.tryNightmare(source,target,100,1,1000));
    }

    @Test public void lifecycleCleanupRemovesBothIncomingAndSourcedEffects() {
        Player source=player("cleanup-source"), target=player("cleanup-target");
        equip(source,"control_rune","nightmare_chance",10);
        assertTrue(GlyphCombatService.tryNightmare(source,target,100,1,1000));
        GlyphCombatService.clearEffects(source);
        assertEquals(1,GlyphCombatService.outgoingAttackMultiplier(target,1001),.00001);
        assertFalse(GlyphCombatService.tryNightmare(source,target,100,1,1002));
        assertTrue(GlyphCombatService.tryNightmare(source,target,100,1,13000));
        GlyphCombatService.clearEffects(target);
        assertEquals(1,GlyphCombatService.outgoingAttackMultiplier(target,13001),.00001);
    }
}
