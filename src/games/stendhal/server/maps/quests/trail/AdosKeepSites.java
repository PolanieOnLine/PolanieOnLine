/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * Existing first-floor rooms and the unchanged mountain entrance host this
 * chapter.
 */
public final class AdosKeepSites implements ZoneConfigurator {
	enum Kind {
		TRACKS(
				"ciężkie ślady",
				AdosKeepProgress.OUTSIDE,
				13,
				122,
				"ground/mieszczanin_tracks",
				4,
				5,
				Step.KEEP_TRAIL,
				1,
				"clues",
				"Ślady prowadzą od schodów w stronę drogi. Ktoś porzucił część bagażu i ruszył dalej pośpiesznie. Nie ma krwi ani śladów starcia. To bardziej przypomina ucieczkę niż marsz na miasto."),
		FRAGMENT(
				"odłamek kamiennej płyty",
				AdosKeepProgress.OUTSIDE,
				20,
				119,
				"object/trail_investigation_props",
				0,
				3,
				Step.KEEP_TRAIL,
				2,
				"clues",
				"Kamień jest ciepły, choć w pobliżu nie palono ogniska. Na odłamku widać resztki liter. Odrysowujesz jego kształt i znaki, żeby pokazać je komuś, kto zna dawne pismo."),
		WEST(
				"stary napis w zachodniej sali",
				AdosKeepProgress.KEEP,
				22,
				105,
				"object/trail_investigation_props",
				1,
				3,
				Step.KEEP_INSCRIPTIONS,
				1,
				"inscriptions",
				"Oczyszczasz płytę z kurzu. Odczytujesz polecenie ochrony załogi i zatrzymywania intruzów. Przepisujesz zachowane słowa oraz znak kamiennego wartownika."),
		EAST(
				"stary napis we wschodniej sali",
				AdosKeepProgress.KEEP,
				65,
				105,
				"object/trail_investigation_props",
				1,
				3,
				Step.KEEP_INSCRIPTIONS,
				2,
				"inscriptions",
				"Na płycie wyryto znaki dawnej załogi. Środek napisu jest wykruszony. Rysunek odłamka spod wejścia pasuje do ubytku. Przerysowujesz całość dla Wikipediana.");

		final String name, zone, tileset, state, text;
		final int x, y, tile, columns, bit;
		final Step step;

		Kind(String name, String zone, int x, int y, String tileset, int tile,
				int columns, Step step, int bit, String state, String text) {
			this.name = name;
			this.zone = zone;
			this.x = x;
			this.y = y;
			this.tileset = tileset;
			this.tile = tile;
			this.columns = columns;
			this.step = step;
			this.bit = bit;
			this.state = state;
			this.text = text;
		}
	}

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		if (!AdosKeepProgress.OUTSIDE.equals(zone.getName())
				&& !AdosKeepProgress.KEEP.equals(zone.getName()))
			throw new IllegalArgumentException("Unknown keep story zone");
		for (Kind kind : Kind.values())
			if (kind.zone.equals(zone.getName()))
				TrailSceneRegistry.prop(zone, "keep-evidence-" + kind.name(),
						player -> needed(player, kind), () -> new Evidence(kind));
		if (AdosKeepProgress.KEEP.equals(zone.getName())) {
			TrailSceneRegistry.encounter(zone, "keep-guardian",
					AdosKeepProgress.GUARDIAN_X,
					AdosKeepProgress.GUARDIAN_Y,
					AdosKeepProgress::needsGuardian, () -> {
						Creature template = TrailCreatureVariants.create(
								AdosKeepProgress.GUARDIAN);
						if (template == null) {
							throw new IllegalStateException(
									"Missing keep guardian");
						}
						return new AdosKeepGuardian(template);
					});
		}
	}

	private static boolean needed(Player player, Kind kind) {
		return AdosKeepProgress.active(player, kind.step)
				&& player.isQuestInState(kind.step.slot, 0, kind.state)
				&& (AdosKeepProgress.mask(player, kind.step) & kind.bit) == 0;
	}

	static final class Evidence extends AreaEntity implements UseListener {
		final Kind kind;

		Evidence(Kind kind) {
			super(1, 1);
			this.kind = kind;
			setRPClass("block");
			put("type", "block");
			put("class", "questuseable");
			put("name", kind.name);
			put("tileset", kind.tileset);
			put("tile_index", kind.tile);
			put("tileset_columns", kind.columns);
			put("z", kind == Kind.TRACKS ? 50 : 8000);
			setPosition(kind.x, kind.y);
			setMenu("Sprawdź|Użyj");
			setCursor("ACTIVITY");
			setDescription(
					"Oto " + kind.name + ". Możesz przyjrzeć się bliżej.");
		}

		@Override
		public boolean onUsed(RPEntity user) {
			if (!(user instanceof Player) || getZone() == null
					|| user.getZone() != getZone()
					|| !kind.zone.equals(getZone().getName()))
				return false;
			Player player = (Player) user;
			if (!PlayerPrivateScene.isOwnedBy(this, player)) {
				return false;
			}
			if (!player.nextTo(this)) {
				player.sendPrivateText("Musisz podejść bliżej.");
				return false;
			}
			boolean changed = AdosKeepProgress.mark(player, kind.step, kind.bit,
					kind.state);
			player.sendPrivateText(changed ? kind.text
					: "Nie znajdujesz tu niczego nowego. Jeśli nie wiesz, co robić dalej, zajrzyj do dziennika.");
			return changed;
		}
	}
}
