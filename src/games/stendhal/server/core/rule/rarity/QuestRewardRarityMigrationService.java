/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** Safe in-place migration for bound rewards which can be proven by quest state. */
public final class QuestRewardRarityMigrationService {
	private static final List<Reward> REWARDS = Collections.unmodifiableList(
			Arrays.asList(
					new Reward("bring_magic", "magiczny hełm kolczy"),
					new Reward("cloaks_collector", "czarny płaszcz"),
					new Reward("cloaks_collector_2", "buty zabójcy"),
					new Reward("where_dragon", "magiczna tarcza płytowa"),
					new Reward("where_dragon", "tarcza chaosu"),
					new Reward("where_dragon", "tarcza xenocyjska"),
					new Reward("goralski_kolekcjoner1", "korale"),
					new Reward("goralski_kolekcjoner2", "pas zbójnicki"),
					new Reward("goralski_kolekcjoner3", "spinka"),
					new Reward("hunting", "pierścień powrotu"),
					new Reward("kill_dragons", "młot wulkanów"),
					new Reward("belts_collector", "rękawice zabójcy"),
					new Reward("gloves_collector", "pas zabójcy"),
					new Reward("reborn_extra_reward3", "sztylet leworęczny"),
					new Reward("reborn_extra_reward3", "sztylet praworęczny"),
					new Reward("reborn_extra_reward4", "amulecik z mithrilu"),
					new Reward("reborn_extra_reward5", "ekskalibur"),
					new Reward("get_diving_license", "zbroja akwalungowa"),
					new Reward("seven_cherubs", "złote buty"),
					new Reward("seven_cherubs", "złota zbroja"),
					new Reward("seven_cherubs", "złoty hełm"),
					new Reward("seven_cherubs", "miecz ognisty"),
					new Reward("woodcutter_license", "siekierka"),
					new Reward("immortalsword_quest", "miecz nieśmiertelnych"),
					new Reward("mithrilshield_quest", "tarcza z mithrilu"),
					new Reward("weapons_collector", "miecz lodowy"),
					new Reward("weapons_collector2", "miecz leworęczny"),
					new Reward("weapons_collector2", "miecz praworęczny"),
					new Reward("zamowienie_strazy", "srebrny pierścień"),
					new Reward("zloty_pierscien", "złoty pierścień"),
					new Reward("bows_ouchit", "zbroja łuskowa"),
					new Reward("bows_ouchit", "spodnie nabijane ćwiekami"),
					new Reward("antivenom_ring", "pierścień antyjadowy"),
					new Reward("club_thorns", "maczuga cierniowa"),
					new Reward("cloaks_for_bario", "złota tarcza"),
					new Reward("emotion_crystals", "spodnie kamienne"),
					new Reward("find_jefs_mom", "bielikrasa"),
					new Reward("soldier_henry", "buty mainiocyjskie"),
					new Reward("forge_newarms", "tarcza ciemnomithrilowa"),
					new Reward("kill_mountain_elves", "hełm kolczy"),
					new Reward("kill_dark_elves", "pierścień szmaragdowy"),
					new Reward("krolewski_plaszcz", "tarcza cieni"),
					new Reward("mixture_for_ortiv", "sztylet mordercy"),
					new Reward("mithril_cloak", "płaszcz z mithrilu"),
					new Reward("pochorowane_konie", "pas skórzany"),
					new Reward("obsidian_knife", "obsydianowy saks"),
					new Reward("fix_emerald_ring", "pierścień szmaragdowy"),
					new Reward("sad_scientist", "czarne spodnie"),
					new Reward("dragon_amulet", "smocze pazury"),
					new Reward("vs_quest", "krwiopijca"),
					new Reward("supplies_for_phalk", "zbroja krasnoludzka"),
					new Reward("zloty_amulet", "złoty amulet"),
					new Reward("pierscien_mieszczanina", "pierścień mieszczanina"),
					new Reward("pierscien_rycerza", "pierścień rycerza"),
					new Reward("pierscien_barona", "pierścień barona"),
					new Reward("pierscien_magnata", "pierścień magnata"),
					new Reward("zlota_ciupaga", "złota ciupaga"),
					new Reward("zlota_ciupaga_was", "złota ciupaga z wąsem"),
					new Reward("ciupaga_dwa_wasy", "złota ciupaga z dwoma wąsami"),
					new Reward("ciupaga_trzy_wasy", "złota ciupaga z trzema wąsami")));

	private QuestRewardRarityMigrationService() {
		// utility class
	}

	/**
	 * Promotes only currently owned, correctly bound rewards. Missing items are
	 * never recreated, so a completed quest cannot be used to obtain a duplicate.
	 * The operation is intentionally idempotent and may safely run on every login.
	 */
	public static int migrate(final Player player) {
		if (player == null) {
			return 0;
		}
		int migrated = 0;
		for (final RPSlot slot : player.slots()) {
			migrated += migrateSlot(player, slot);
		}
		return migrated;
	}

	private static int migrateSlot(final Player player, final RPSlot slot) {
		int migrated = 0;
		for (final RPObject object : slot) {
			if (object instanceof Item) {
				final Item item = (Item) object;
				if (isProvenReward(player, item.getName())
						&& player.getName().equals(item.getBoundTo())
						&& item.getRarityOrCommon().ordinal()
								< ItemRarity.EPIC.ordinal()) {
					final Item template = SingletonRepository.getEntityManager().getItem(
							item.getName(), ItemCreationContext.questReward());
					if (ItemRarityService.getInstance()
							.promoteToQuestReward(item, template)) {
						migrated++;
					}
				}
			}
			for (final RPSlot child : object.slots()) {
				migrated += migrateSlot(player, child);
			}
		}
		return migrated;
	}

	private static boolean isProvenReward(final Player player,
			final String itemName) {
		for (final Reward reward : REWARDS) {
			if (reward.itemName.equals(itemName)
					&& player.isQuestCompleted(reward.questSlot)) {
				return true;
			}
		}
		return false;
	}

	private static final class Reward {
		private final String questSlot;
		private final String itemName;

		private Reward(final String questSlot, final String itemName) {
			this.questSlot = questSlot;
			this.itemName = itemName;
		}
	}
}
