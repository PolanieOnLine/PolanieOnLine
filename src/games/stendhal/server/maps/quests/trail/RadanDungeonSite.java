/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;
import org.apache.log4j.Logger;
import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import marauroa.server.game.rp.InstanceScope;
import marauroa.server.game.rp.InstanceZoneManager;

/**
 * Public entrance. Each visitor receives a separate, checkpointed expedition.
 */
public final class RadanDungeonSite implements ZoneConfigurator {
	static final String PUBLIC_ZONE = "-1_semos_dungeon";
	static final int RETURN_X = 27, RETURN_Y = 37;

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		TrailSceneRegistry.prop(zone, "radan-tools", player ->
				player.isQuestCompleted(Step.SEMOS_PICKUP.slot)
						&& player.isQuestInState(Step.RADAN_PASSAGE.slot, "search"), () -> {
			final PublicProp tools = new PublicProp(false);
			tools.setPosition(25, 38);
			return tools;
		});
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 2; x++) {
				final int tile = y * 2 + x;
				final int entryX = 26 + x;
				final int entryY = 38 + y;
				TrailSceneRegistry.prop(zone, "radan-entrance-" + tile, player ->
						player.isQuestCompleted(Step.SEMOS_PICKUP.slot)
								&& player.hasQuest(Step.RADAN_PASSAGE.slot)
								&& !player.isQuestCompleted(Step.RADAN_FINALE.slot), () -> {
					final PublicProp entrance = new PublicProp(true, tile);
					entrance.setPosition(entryX, entryY);
					return entrance;
				});
			}
		}
	}

	static final class PublicProp extends AreaEntity implements UseListener {
		private static final Logger LOG = Logger.getLogger(PublicProp.class);
		private final boolean entrance;

		PublicProp(boolean entrance) {
			this(entrance, 0);
		}

		PublicProp(boolean entrance, int tile) {
			super(1, 1);
			this.entrance = entrance;
			setRPClass("block");
			put("type", "block");
			put("class", "questuseable");
			put("name", entrance ? "boczny chodnik" : "porzucone narzędzia");
			put("tileset", entrance ? "ground/ladder/wooden_ladder_down"
					: "item/dropped_tools");
			put("tile_index", tile);
			put("tileset_columns", entrance ? 2 : 1);
			put("z", 8000);
			setResistance(0);
			setCursor("ACTIVITY");
			setMenu(entrance ? "Wejdź|Użyj" : "Sprawdź|Użyj");
			setDescription(entrance
					? "Oto boczny chodnik. Belka przy wejściu jest poluzowana."
					: "Oto porzucone narzędzia. Obok belki leży drewniany klin.");
		}

		@Override
		public boolean onUsed(RPEntity user) {
			if (!(user instanceof Player) || getZone() == null
					|| user.getZone() != getZone()
					|| !PUBLIC_ZONE.equals(getZone().getName()))
				return false;
			Player player = (Player) user;
			if (!PlayerPrivateScene.isOwnedBy(this, player)) {
				return false;
			}
			if (!player.nextTo(this)) {
				player.sendPrivateText("Musisz podejść bliżej.");
				return false;
			}
			if (!entrance) {
				if (!player.isQuestInState(Step.RADAN_PASSAGE.slot, "search")) {
					player.sendPrivateText(
							"Przy belce leżą narzędzia. Jeśli szukasz Radana, porozmawiaj z Drilenunem o chodniku.");
					return false;
				}
				player.setQuest(Step.RADAN_PASSAGE.slot, "brace");
				player.sendPrivateText(
						"Na belce widać świeże nacięcie. Ktoś chciał ją przewrócić za ostatnim wózkiem. "
								+ "Wsuwasz znaleziony klin i podpierasz belkę. Możesz teraz wejść do bocznego chodnika.");
				return true;
			}
			if (!RadanDungeonProgress.mayEnter(player)) {
				player.sendPrivateText(
						player.isQuestCompleted(Step.RADAN_FINALE.slot)
								? "Straż zajęła już kryjówkę. Nie musisz tam wracać."
								: "Najpierw porozmawiaj z Drilenunem o chodniku i sprawdź narzędzia przy wejściu.");
				return false;
			}
			InstanceZoneManager manager = SingletonRepository.getRPWorld().getInstanceZoneManager();
			StendhalRPZone dungeon = null;
			try {
				dungeon = (StendhalRPZone) manager.acquire(
						RadanDungeonFactory.TEMPLATE, "radan",
						InstanceScope.player(player.getName()),
						player.getName(), new RadanDungeonFactory(player));
				if (!player.teleport(dungeon, 6, 23, Direction.UP, player)) {
					manager.release(dungeon.getID(), player.getName());
					player.sendPrivateText(
							"Nie udało się wejść. Spróbuj jeszcze raz.");
					return false;
				}
				RadanDungeonProgress.entered(player);
				player.sendPrivateText(TrailQuestLine.nextObjective(player));
				return true;
			} catch (Exception exception) {
				LOG.error(
						"Cannot enter Radan's hideout for " + player.getName(),
						exception);
				if (dungeon != null && player.getZone() != dungeon) {
					try {
						manager.release(dungeon.getID(), player.getName());
					} catch (Exception cleanup) {
						LOG.error("Cannot release failed Radan entry", cleanup);
					}
				}
				player.sendPrivateText(
						"Nie udało się wejść do chodnika. Spróbuj ponownie później.");
				return false;
			}
		}

		@Override
		public String getDescriptionName() {
			return entrance ? "boczny chodnik" : "porzucone narzędzia";
		}
	}
}
