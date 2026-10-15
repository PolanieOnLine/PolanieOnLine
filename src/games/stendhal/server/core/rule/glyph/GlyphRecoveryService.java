package games.stendhal.server.core.rule.glyph;

import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** Transient recovery timers. Bonuses are read from equipped glyphs only. */
public final class GlyphRecoveryService {
    private static final long QUIET_MS = 10000L;
    private static final long PULSE_MS = 5000L;
    private static final Map<Player, Recovery> STATES = new WeakHashMap<Player, Recovery>();
    private static final Map<Player, Double> CONSUMABLE_REMAINDERS = new WeakHashMap<Player, Double>();

    private static final class Recovery {
        private long quietSince;
        private long nextPulse;
        Recovery(final long now) { quietSince = now; nextPulse = now + QUIET_MS; }
    }

    private GlyphRecoveryService() { }

    public static void reset(final Player player) {
        STATES.remove(player);
        CONSUMABLE_REMAINDERS.remove(player);
    }

    public static void recordDamage(final RPEntity victim, final Entity attacker, final int damage) {
        recordDamage(victim, attacker, damage, System.currentTimeMillis());
    }

    static void recordDamage(final RPEntity victim, final Entity attacker, final int damage, final long now) {
        if (damage <= 0) { return; }
        if (victim instanceof Player) { STATES.put((Player) victim, new Recovery(now)); }
        if (attacker instanceof Player) { STATES.put((Player) attacker, new Recovery(now)); }
    }

    public static void tick(final Player player) { tick(player, System.currentTimeMillis()); }

    static void tick(final Player player, final long now) {
        final double percent = bonus(player, "out_of_combat_regen");
        if (player.getHP() <= 0 || player.has("dead") || player.isDisconnected()) {
            reset(player);
            return;
        }
        if (bonus(player, "consumable_healing_bonus") <= 0.0) {
            CONSUMABLE_REMAINDERS.remove(player);
        }
        if (percent <= 0.0) {
            STATES.remove(player);
            return;
        }
        Recovery state = STATES.get(player);
        if (state == null || now < state.quietSince) {
            STATES.put(player, new Recovery(now));
            return;
        }
        if (now < state.nextPulse) { return; }
        // One pulse at most: a paused server or offline interval cannot bank healing.
        state.nextPulse = now + PULSE_MS;
        final int healing = (int) Math.min(20.0, Math.floor(player.getBaseHP() * percent / 100.0));
        if (healing > 0) { player.heal(healing, true); }
    }

    /** Only food/potion callers use this; spells, lifesteal and regeneration do not. */
    public static int boostConsumableHealing(final RPEntity entity, final int amount) {
        if (amount <= 0) { return amount; }
        final double percent = bonus(entity, "consumable_healing_bonus");
        if (!(entity instanceof Player)) { return amount; }
        final Player player = (Player) entity;
        if (percent <= 0.0) {
            CONSUMABLE_REMAINDERS.remove(player);
            return amount;
        }
        final Double previous = CONSUMABLE_REMAINDERS.get(player);
        final double extra = amount * percent / 100.0 + (previous == null ? 0.0 : previous);
        // Keep sub-HP bonuses so small food ticks receive the advertised total.
        final double wholeExtra = Math.floor(extra + 1.0e-9);
        CONSUMABLE_REMAINDERS.put(player, Math.max(0.0, extra - wholeExtra));
        return (int) Math.min(Integer.MAX_VALUE, amount + wholeExtra);
    }

    private static double bonus(final RPEntity entity, final String attribute) {
        if (!(entity instanceof Player)) { return 0.0; }
        double result = 0.0;
        for (final Item glyph : entity.getAllEquippedGlyphs()) {
            if (glyph != null && glyph.has(attribute)) {
                final double value = glyph.getDouble(attribute);
                if (Double.isFinite(value) && value > 0.0) { result += value; }
            }
        }
        return result;
    }
}
