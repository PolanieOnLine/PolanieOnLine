package games.stendhal.server.core.rule.glyph;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.common.Rand;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;

/** Server-side tactical glyph effects. Timed effects never alter saved stats. */
public final class GlyphCombatService {
    private static final Map<RPEntity, Long> NIGHTMARE_COOLDOWNS =
            Collections.synchronizedMap(new WeakHashMap<RPEntity, Long>());
    private static final Map<RPEntity, Nightmare> NIGHTMARES =
            Collections.synchronizedMap(new WeakHashMap<RPEntity, Nightmare>());

    private GlyphCombatService() { }

    private static double percent(final RPEntity entity, final String key) {
        double value = 0;
        if (entity != null) {
            for (Item glyph : entity.getAllEquippedGlyphs()) {
                if (glyph != null && glyph.has(key)) {
                    double amount = glyph.getDouble(key);
                    if (Double.isFinite(amount)) { value = Math.max(value, amount); }
                }
            }
        }
        return Math.max(0, Math.min(100, value)) / 100;
    }

    /** Oak softens only the extra portion, never the ordinary base hit. */
    public static int reduceCriticalBonus(final RPEntity defender,
            final int baseDamage, final int criticalDamage) {
        if (baseDamage <= 0 || criticalDamage <= baseDamage) { return criticalDamage; }
        return baseDamage + (int) Math.round((criticalDamage - baseDamage)
                * (1 - percent(defender, "critical_bonus_reduction")));
    }

    public static boolean dodges(final RPEntity defender, final Nature nature,
            final boolean critical, final int roll) {
        return roll >= 1 && roll <= 100 && dodgesOnBasisPointRoll(defender, nature, critical, roll * 100);
    }

    static boolean dodgesOnBasisPointRoll(final RPEntity defender, final Nature nature,
            final boolean critical, final int roll) {
        return !critical && nature == Nature.CUT && roll >= 1 && roll <= 10000
                && roll <= 10000 * percent(defender, "physical_dodge_chance");
    }

    /** Direct-hit-only effects; callers apply endurance at the damage sink. */
    public static int resolveDirectHit(final RPEntity defender, final Nature nature,
            final boolean critical, final int baseDamage, final int damage) {
        if (damage <= 0) { return damage; }
        if (!critical && nature == Nature.CUT
                && percent(defender, "physical_dodge_chance") > 0
                && dodgesOnBasisPointRoll(defender, nature, false, Rand.rand(10000) + 1)) { return 0; }
        return critical ? reduceCriticalBonus(defender, baseDamage, damage) : damage;
    }

    /** Strictly below 35%; the hit that crosses the threshold is not reduced. */
    public static int reduceIncomingDamage(final RPEntity defender, final int damage) {
        if (damage <= 0 || defender == null || defender.getHP() <= 0
                || defender.getBaseHP() <= 0
                || (long) defender.getHP() * 100 >= (long) defender.getBaseHP() * 35) {
            return damage;
        }
        return Math.max(0, (int) Math.round(damage
                * (1 - percent(defender, "low_health_damage_reduction"))));
    }

    public static void onHit(final RPEntity source, final RPEntity target,
            final int damage) {
        if (damage <= 0 || percent(source, "nightmare_chance") <= 0) { return; }
        tryNightmare(source, target, damage, Rand.roll1D100(), System.currentTimeMillis());
    }

    /** Death/logout discard temporary effects; source cooldown stays in force. */
    public static void clearEffects(final RPEntity entity) {
        NIGHTMARES.remove(entity);
        synchronized (NIGHTMARES) {
            NIGHTMARES.entrySet().removeIf(entry -> entry.getValue().source.get() == entity);
        }
    }

    static boolean tryNightmare(final RPEntity source, final RPEntity target,
            final int damage, final int roll, final long now) {
        if (source == null || target == null || source == target || damage <= 0
                || source.getHP() <= 0 || target.getHP() <= 0
                || source.getZone() != target.getZone()
                || (target instanceof Creature && ((Creature) target).isImmortal())
                || roll < 1 || roll > 100
                || roll > 100 * percent(source, "nightmare_chance")) { return false; }
        final Long ready = NIGHTMARE_COOLDOWNS.get(source);
        if (ready != null && now < ready) { return false; }
        if (outgoingAttackMultiplier(target, now) < 1) { return false; }
        NIGHTMARE_COOLDOWNS.put(source, now + 12000);
        NIGHTMARES.put(target, new Nightmare(source, now + 6000));
        return true;
    }

    public static double outgoingAttackMultiplier(final RPEntity entity) {
        return outgoingAttackMultiplier(entity, System.currentTimeMillis());
    }

    static double outgoingAttackMultiplier(final RPEntity entity, final long now) {
        Nightmare effect = NIGHTMARES.get(entity);
        if (effect == null) { return 1; }
        RPEntity source = effect.source.get();
        if (source == null || now >= effect.expiresAt || entity.getHP() <= 0
                || source.getHP() <= 0 || source.getZone() != entity.getZone()) {
            NIGHTMARES.remove(entity);
            return 1;
        }
        return .9;
    }

    private static final class Nightmare {
        final WeakReference<RPEntity> source;
        final long expiresAt;
        Nightmare(RPEntity source, long expiresAt) {
            this.source = new WeakReference<>(source); this.expiresAt = expiresAt;
        }
    }
}
