package games.stendhal.server.maps.quests.captureflag;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;

final class CaptureFlagSupport {

	private static final String PLAYING_ATTRIBUTE = "ctf_playing";
	private static final int DEFAULT_SUPPLY_AMOUNT = 100;

	private static final Map<String, String> PROJECTILE_EFFECTS;
	private static final List<String> CTF_ITEM_NAMES = Collections.unmodifiableList(Arrays.asList(
		"flag",
		"łuk zf",
		"ctf bow",
		"strzała pogrzebania",
		"strzała spowolnienia",
		"strzała przyspieszenia",
		"fumble arrow",
		"slowdown arrow",
		"speedup arrow",
		"śnieżka pogrzebania",
		"śnieżka spowolnienia",
		"śnieżka przyspieszenia"
	));

	static {
		Map<String, String> effects = new HashMap<String, String>();
		effects.put("strzała pogrzebania", "drop");
		effects.put("strzała spowolnienia", "slowdown");
		effects.put("strzała przyspieszenia", "speedup");
		effects.put("śnieżka pogrzebania", "drop");
		effects.put("śnieżka spowolnienia", "slowdown");
		effects.put("śnieżka przyspieszenia", "speedup");
		effects.put("fumble arrow", "drop");
		effects.put("slowdown arrow", "slowdown");
		effects.put("speedup arrow", "speedup");
		PROJECTILE_EFFECTS = Collections.unmodifiableMap(effects);
	}

	private CaptureFlagSupport() {
	}

	static void markPlaying(Player player) {
		player.put(PLAYING_ATTRIBUTE, 1);
	}

	static void clearPlaying(Player player) {
		player.remove(PLAYING_ATTRIBUTE);
	}

	static boolean isPlaying(Player player) {
		return player.has(PLAYING_ATTRIBUTE);
	}

	static boolean supplyIfMissing(Player player, Sentence sentence, EventRaiser raiser, String itemName, int amount) {
		if (player.getNumberOfEquipped(itemName) > 0) {
			return false;
		}
		new EquipItemAction(itemName, amount).fire(player, sentence, raiser);
		return true;
	}

	static ChatAction supplyAmmoAction(final String... itemNames) {
		return new ChatAction() {
			@Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
				boolean supplied = false;
				for (String itemName : itemNames) {
					supplied |= supplyIfMissing(player, sentence, npc, itemName, DEFAULT_SUPPLY_AMOUNT);
				}
				if (!supplied && npc != null) {
					npc.say("Masz już pełny zapas testowej amunicji.");
				}
			}
		};
	}

	static void dropAllCaptureFlagItems(Player player) {
		for (Item droppable : player.getDroppables()) {
			player.dropDroppableItem(droppable);
		}

		Set<Item> processed = new HashSet<Item>();
		for (String name : CTF_ITEM_NAMES) {
			for (Item item : player.getAllEquipped(name)) {
				if (processed.add(item)) {
					player.drop(item);
				}
			}
		}
	}

	static String resolveProjectileEffect(StackableItem projectiles) {
		String effect = PROJECTILE_EFFECTS.get(projectiles.getName());
		if (effect != null) {
			return effect;
		}
		String subclass = PROJECTILE_EFFECTS.get(projectiles.getItemSubclass());
		if (subclass != null) {
			return subclass;
		}
		return null;
	}

	static boolean isRecognizedEffect(String effect) {
		return PROJECTILE_EFFECTS.containsValue(effect);
	}
}
