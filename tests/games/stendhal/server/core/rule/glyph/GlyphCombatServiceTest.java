package games.stendhal.server.core.rule.glyph;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class GlyphCombatServiceTest {
    @Test public void progressionSetsExactHealthLevelsWithoutCompoundingOrHealing() {
        Player p=player("glyph-level-health");
        Glyph glyph = new Glyph(
                "glif daru Mokoszy", "glyph", "glyph_health", null);
        glyph.put("health",500);
        p.getSlot("healing_rune").add(glyph);
        p.setBaseHP(1500); p.setHP(1500);
        assertTrue(GlyphProgressionService.isUpgradeable(glyph));
        GlyphProgressionService.applyLevel(glyph,1);
        assertEquals(100,glyph.getInt("health"));
        assertEquals(1100,p.getBaseHP()); assertEquals(1100,p.getHP());
        GlyphProgressionService.applyLevel(glyph,2);
        assertEquals(250,glyph.getInt("health"));
        assertEquals(1250,p.getBaseHP()); assertEquals(1100,p.getHP());
        GlyphProgressionService.applyLevel(glyph,3);
        GlyphProgressionService.applyLevel(glyph,3);
        assertEquals(500,glyph.getInt("health"));
        assertEquals(1500,p.getBaseHP()); assertEquals(1100,p.getHP());
        assertEquals(3,GlyphProgressionService.getLevel(glyph));
    }
    @Test public void progressionRetainsFractionalEffectsAndDerivesResistanceFromTargetReduction() {
        Player p=player("glyph-level-fractions");
        Glyph wind = new Glyph(
                "glif wichru","glyph","glyph_whirlwind",null);
        GlyphProgressionService.applyLevel(wind,1);
        assertEquals(0.4,wind.getDouble("glyph_attack_rate_reduction"),0.000001);
        assertEquals(2,wind.getDouble("physical_dodge_chance"),0);
        GlyphProgressionService.applyLevel(wind,2);
        assertEquals(0.7,wind.getDouble("glyph_attack_rate_reduction"),0.000001);
        assertEquals(3.5,wind.getDouble("physical_dodge_chance"),0);
        Glyph resistance = new Glyph(
                "glif Jarowita","glyph","glyph_resistance",null);
        for (int level=1;level<=3;level++) {
            GlyphProgressionService.applyLevel(resistance,level);
            assertEquals(new double[]{0.94,0.9,0.85}[level-1],
                    resistance.getSusceptibilities().get(Nature.FIRE),0.000001);
        }
        GlyphProgressionService.applyLevel(resistance,1);
        assertEquals(0.94,resistance.getSusceptibilities().get(Nature.FIRE),0.000001);
    }
    @Test public void knowledgeAndOrdinaryItemsCannotBeLeveledAndInvalidTargetsDoNotChangeItems() {
        player("glyph-level-exclusions");
        Glyph knowledge = new Glyph(
                "glif wiedzy","glyph","glyph_knowledge",null);
        assertFalse(GlyphProgressionService.isUpgradeable(knowledge));
        assertEquals(0,GlyphProgressionService.getLevel(knowledge));
        try { GlyphProgressionService.applyLevel(knowledge,2); fail(); }
        catch (IllegalArgumentException expected) { }
        assertFalse(knowledge.has(GlyphProgressionService.LEVEL_ATTRIBUTE));
        Item ordinary = new Item("glif siły","sword","test",null);
        assertFalse(GlyphProgressionService.isUpgradeable(ordinary));
        Glyph glyph = new Glyph(
                "glif siły","glyph","glyph_strength",null);
        GlyphProgressionService.applyLevel(glyph,2);
        for (int invalid : new int[]{0,4,-1}) {
            try { GlyphProgressionService.applyLevel(glyph,invalid); fail(); }
            catch (IllegalArgumentException expected) { }
            assertEquals(2,GlyphProgressionService.getLevel(glyph));
            assertEquals(3,glyph.getInt("atk"));
        }
    }
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
    @Test public void fractionalDodgeChanceUsesExactBasisPointBoundary() {
        Player p=player("fractional-dodge"); equip(p,"utility_rune","physical_dodge_chance",3.5);
        assertTrue(GlyphCombatService.dodgesOnBasisPointRoll(p,Nature.CUT,false,350));
        assertFalse(GlyphCombatService.dodgesOnBasisPointRoll(p,Nature.CUT,false,351));
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
