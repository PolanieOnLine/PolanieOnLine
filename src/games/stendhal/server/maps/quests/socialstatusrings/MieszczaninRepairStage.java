/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.ZoneEnterExitListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

/**
 * Final village stage: repair the damaged crossing and obtain the community's
 * approval before returning to Marianek.
 */
public final class MieszczaninRepairStage {
	static final int REPAIR_X = 106;
	static final int REPAIR_Y = 59;
	static final int REPAIR_BEAM_Y = REPAIR_Y - 1;

	private static final String TOOLS_RETURN_LABEL = "mieszczanin_repair_tools_return";
	private static final String TOOLS_RETURN_DETAIL_LABEL = "mieszczanin_repair_tools_return_detail";

	private MieszczaninRepairStage() {
		// utility class
	}

	/** Attach the final stage to the already-created settlement NPCs. */
	public static void attach(final StendhalRPZone zone, final SpeakerNPC dobrawa,
			final SpeakerNPC stach, final SpeakerNPC zywia, final SpeakerNPC milost) {
		zone.addZoneEnterExitListener(new RepairZoneListener(stach));
		attachStachAfterRepair(stach);
		attachDobrawa(dobrawa);
		attachWitnessComments(zywia, milost);
	}

	/**
	 * Installs the one authoritative transition that accepts Stach's tools.
	 *
	 * Settlement NPCs are created before quests are loaded. The old main quest
	 * also registers a compatible return-tools greeting, so installing this
	 * transition during zone configuration would make the FSM ambiguous once
	 * the quest system loads. We therefore replace that legacy transition on
	 * the first player entry, after quest loading has finished.
	 */
	static void installToolsReturnTransition(final StendhalRPZone zone, final SpeakerNPC stach) {
		stach.del(TOOLS_RETURN_LABEL);
		stach.del(TOOLS_RETURN_DETAIL_LABEL);
		final AndCondition toolsRecovered = toolsRecoveredCondition();
		stach.removeTransition(ConversationStates.IDLE,
				new ArrayList<String>(ConversationPhrases.GREETING_MESSAGES), toolsRecovered);

		stach.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				toolsRecovered,
				ConversationStates.INFORMATION_9,
				"To moje narzędzia, poznaję wyszczerbienie na dłucie. Radomir wrócił cały, więc została nam #naprawa.",
				null,
				TOOLS_RETURN_LABEL);

		stach.add(ConversationStates.INFORMATION_9,
				"naprawa",
				toolsRecoveredCondition(),
				ConversationStates.ATTENDING,
				"Przygotowałem dwie belki, klamry i deski. Uszkodzenie jest na trakcie na północny wschód stąd. Wzmocnij przejazd i wróć do mnie.",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.setQuest(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR);
						MieszczaninHideoutProgress.clear(player);
						MieszczaninRepairProgress.clear(player);
						MieszczaninRoadScene.removeTrackProps(zone, player);
						syncRepairSite(zone, player);
					}
				},
				TOOLS_RETURN_DETAIL_LABEL);
	}

	private static AndCondition toolsRecoveredCondition() {
		return new AndCondition(
				new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
						PierscienMieszczanina.STATE_TRACKS),
				new QuestInStateCondition(MieszczaninHideoutProgress.SLOT,
						MieszczaninHideoutProgress.TOOLS_RECOVERED));
	}

	private static void attachStachAfterRepair(final SpeakerNPC stach) {
		stach.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.REPAIRED)),
				ConversationStates.INFORMATION_9,
				"Byłem już przy przejeździe. Na pierwszy rzut oka wygląda dobrze, ale najwięcej powiedzą same #belki.",
				null);

		stach.add(ConversationStates.INFORMATION_9,
				"belki",
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.REPAIRED)),
				ConversationStates.ATTENDING,
				"Siedzą równo, klamry trzymają, a pełny wóz powinien przejechać bezpiecznie. Powiedz Dobrawie, że trakt jest gotowy.",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						MieszczaninRepairProgress.markStachConfirmed(player);
					}
				});

		stach.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.STACH_CONFIRMED)),
				ConversationStates.ATTENDING,
				"Przejazd jest bezpieczny. Powiedz Dobrawie, że sprawdziłem belki i klamry.",
				null);

		stach.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.COMMUNITY_APPROVED)),
				ConversationStates.ATTENDING,
				"Tu już nie ma nic do poprawiania. Dobrawa wysłała wiadomość do Marianka, wróć do niego.",
				null);
	}

	private static void attachDobrawa(final SpeakerNPC dobrawa) {
		dobrawa.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.REPAIRED)),
				ConversationStates.ATTENDING,
				"Widzę, że pracowałeś przy przejeździe. Zanim puszczę tamtędy wóz, niech Stach obejrzy belki.",
				null);

		dobrawa.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.STACH_CONFIRMED)),
				ConversationStates.INFORMATION_8,
				"Stach potwierdził naprawę. Chcę jeszcze podsumować twoją #pomoc.",
				null);

		dobrawa.add(ConversationStates.INFORMATION_8,
				"pomoc",
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.STACH_CONFIRMED)),
				ConversationStates.INFORMATION_9,
				"Żywia dostała lekarstwo, Radomir wrócił, a zasadzki się skończyły. Został tylko #Marianek.",
				null);

		dobrawa.add(ConversationStates.INFORMATION_9,
				"Marianek",
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.STACH_CONFIRMED)),
				ConversationStates.ATTENDING,
				"Nie wiem, po co skierował cię właśnie tutaj. Kiedy zapyta, powiem mu jedno, można było na tobie polegać. Wyślę mu wiadomość jeszcze dziś.",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						MieszczaninRepairProgress.markCommunityApproved(player);
					}
				});

		dobrawa.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.COMMUNITY_APPROVED)),
				ConversationStates.ATTENDING,
				"Wiadomość do Marianka już poszła. Tutaj doprowadziłeś sprawę do końca.",
				null);
	}

	private static void attachWitnessComments(final SpeakerNPC zywia, final SpeakerNPC milost) {
		final AndCondition approved = new AndCondition(
				new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
						PierscienMieszczanina.STATE_REPAIR),
				new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
						MieszczaninRepairProgress.COMMUNITY_APPROVED));

		zywia.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				approved,
				ConversationStates.ATTENDING,
				"Ranny dochodzi do siebie, a Radomir wrócił do swoich. Dobrawa dobrze zrobiła, że napisała Mariankowi.",
				null);

		milost.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				approved,
				ConversationStates.ATTENDING,
				"Dziś przy ogniu nikt nie zastanawia się, który wóz zniknie w lesie. Jedź do Marianka.",
				null);
	}

	static void syncRepairSite(final StendhalRPZone zone, final Player player) {
		if (zone == null || !MieszczaninRoadScene.ZONE_NAME.equals(zone.getName())) {
			return;
		}
		if (!player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_REPAIR)
				|| MieszczaninRepairProgress.isRepaired(player)) {
			removeRepairSite(zone, player);
			return;
		}

		if (findRepairBeam(zone, player) == null) {
			final MieszczaninRepairBeam beam = new MieszczaninRepairBeam(player);
			beam.setPosition(REPAIR_X, REPAIR_BEAM_Y);
			zone.add(beam);
		}
		if (findRepairSite(zone, player) == null) {
			final MieszczaninRepairSite site = new MieszczaninRepairSite(player);
			site.setPosition(REPAIR_X, REPAIR_Y);
			zone.add(site);
		}
	}

	static void removeRepairSite(final StendhalRPZone zone, final Player player) {
		final List<Entity> toRemove = new ArrayList<Entity>();
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninRepairSite.class)) {
			final MieszczaninRepairSite site = (MieszczaninRepairSite) entity;
			if (site.isOwnedBy(player)) {
				toRemove.add(site);
			}
		}
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninRepairBeam.class)) {
			final MieszczaninRepairBeam beam = (MieszczaninRepairBeam) entity;
			if (beam.isOwnedBy(player)) {
				toRemove.add(beam);
			}
		}
		for (final Entity entity : toRemove) {
			zone.remove(entity.getID());
		}
	}

	private static MieszczaninRepairSite findRepairSite(final StendhalRPZone zone,
			final Player player) {
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninRepairSite.class)) {
			final MieszczaninRepairSite site = (MieszczaninRepairSite) entity;
			if (site.isOwnedBy(player)) {
				return site;
			}
		}
		return null;
	}

	private static MieszczaninRepairBeam findRepairBeam(final StendhalRPZone zone,
			final Player player) {
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninRepairBeam.class)) {
			final MieszczaninRepairBeam beam = (MieszczaninRepairBeam) entity;
			if (beam.isOwnedBy(player)) {
				return beam;
			}
		}
		return null;
	}

	private static final class RepairZoneListener implements ZoneEnterExitListener {
		private final SpeakerNPC stach;
		private boolean toolsTransitionInstalled;

		RepairZoneListener(final SpeakerNPC stach) {
			this.stach = stach;
		}

		@Override
		public void onEntered(final RPObject object, final StendhalRPZone zone) {
			if (!(object instanceof Player)) {
				return;
			}
			if (!toolsTransitionInstalled) {
				installToolsReturnTransition(zone, stach);
				toolsTransitionInstalled = true;
			}
			syncRepairSite(zone, (Player) object);
		}

		@Override
		public void onExited(final RPObject object, final StendhalRPZone zone) {
			if (object instanceof Player) {
				removeRepairSite(zone, (Player) object);
			}
		}
	}
}
