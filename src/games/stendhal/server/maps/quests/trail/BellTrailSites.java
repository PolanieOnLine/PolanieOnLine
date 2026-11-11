/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;
import games.stendhal.common.Direction;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.SoundEvent;
import games.stendhal.server.maps.quests.trail.BellTrailProgress.Encounter;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

public final class BellTrailSites implements ZoneConfigurator {
	enum Kind {
		LOG("skrzynia z zapiskami", 104, 24, "item/pot/crate_small", 1, 2, 1),
		INSCRIPTION("kamień z napisem", 112, 30, "object/trail_investigation_props", 1, 3, 2),
		BELL_TOP(
				"stary dzwon",
				117,
				33,
				"pol/domki/tower_stone_dzwonnica",
				40,
				3,
				4),
		BELL(
				"stary dzwon",
				117,
				34,
				"pol/domki/tower_stone_dzwonnica",
				43,
				3,
				4),
		WEST_WARD(
				"kamień wiążący dzwon",
				108,
				32,
				"object/trail_investigation_props",
				0,
				3,
				1),
		EAST_WARD(
				"kamień wiążący dzwon",
				116,
				30,
				"object/trail_investigation_props",
				0,
				3,
				2),
		ASSEMBLY("zbiórka rannych", 109, 26, "item/pot/crate_small", 1, 2, 0),
		FIRST_STOP("pierwszy postój", 96, 16, "item/campfires", 0, 2, 1),
		BRIDGE_STOP("postój przy kładce", 76, 12, "item/campfires", 0, 2, 2),
		SAFE_CAMP("zachodnie obozowisko", 50, 12, "item/campfires", 0, 2, 3);

		final String name, tileset;
		final int x, y, tile, columns, bit;

		Kind(String name, int x, int y, String tileset, int tile, int columns,
				int bit) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.tileset = tileset;
			this.tile = tile;
			this.columns = columns;
			this.bit = bit;
		}
	}

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		if (BellTrailProgress.CAVE.equals(zone.getName())) {
			addNpc(zone, BellTrailProgress.WITNESS, 110, 76, "youngsoldiernpc",
					"Niemir siedzi pod ścianą. Na widok przejścia w górę odwraca wzrok.");
		} else if (BellTrailProgress.ISLANDS.equals(zone.getName())) {
			addNpc(zone, BellTrailProgress.LEADER, 108, 26, "youngsoldiernpc",
					"Witosław dowodzi wyprawą. Wciąż nasłuchuje, choć nikt obok nic nie mówi.");
			addNpc(zone, BellTrailProgress.SCHOLAR, 111, 26, "woodcutternpc",
					"Borzęta podpiera się kosturem. Podartą nogawkę ma przesiąkniętą krwią.");
			addNpc(zone, BellTrailProgress.INJURED, 113, 26, "youngsoldiernpc",
					"Milan ma zabandażowany bark. Co chwilę sprawdza, czy ktoś stoi za nim.");
			for (Kind kind : Kind.values()) {
				TrailSceneRegistry.prop(zone, "bell-evidence-" + kind.name(),
						player -> needed(player, kind), () -> new Evidence(kind));
			}
			for (Encounter role : Encounter.values()) {
				TrailSceneRegistry.encounter(zone, "bell-" + role.name(),
						role.x, role.y,
						player -> BellTrailProgress.needs(player, role),
						() -> new BellTrailGuard(
								TrailCreatureVariants.create(role.name), role));
			}
		} else {
			throw new IllegalArgumentException("Unknown bell story zone");
		}
	}

	private void addNpc(StendhalRPZone zone, String name, int x, int y,
			String sprite, String description) {
		SpeakerNPC npc = new SpeakerNPC(name) {
			@Override
			protected void createDialog() {
				addGreeting(BellTrailProgress.WITNESS.equals(name)
						? "Jeśli pytasz o #'wyprawę', powiem ci, co pamiętam. Tylko daj mi chwilę."
						: BellTrailProgress.SCHOLAR.equals(name)
								? "Możemy porozmawiać o #'rannych' albo o #'głosie z mgły'."
								: "Dobrze, że jesteś. Zapytaj o #'rannych', jeśli przysyła cię Festris.");
				addJob("Przyszliśmy tu zbadać stary dzwon. Teraz chcemy po prostu wrócić do domu.");
				addGoodbye("Nie odchodź daleko.");
			}
		};
		npc.setEntityClass(sprite);
		npc.setGender("M");
		npc.setPosition(x, y);
		npc.setDirection(Direction.DOWN);
		npc.setDescription(description);
		TrailSceneRegistry.actor(zone, npc, player -> {
			if (BellTrailProgress.WITNESS.equals(name)) {
				return at(player, Step.BELL_RETURNED, "witness");
			}
			if (BellTrailProgress.LEADER.equals(name)) {
				return Step.BELL_WOUNDED.availableOrStarted(player)
						&& !player.isQuestCompleted(Step.BELL_ESCAPE.slot)
						&& !player.isQuestInState(Step.BELL_ESCAPE.slot, "return");
			}
			if (BellTrailProgress.SCHOLAR.equals(name)) {
				return BellTrailProgress.active(player, Step.BELL_WOUNDED)
						|| Step.BELL_VOICE.availableOrStarted(player)
								&& !player.isQuestCompleted(Step.BELL_VOICE.slot);
			}
			return BellTrailProgress.active(player, Step.BELL_WOUNDED);
		});
	}

	static boolean needed(Player player, Kind kind) {
		switch (kind) {
		case LOG:
		case INSCRIPTION:
			return unmarked(player, Step.BELL_VOICE, "clues", kind.bit);
		case BELL:
		case BELL_TOP:
			return unmarked(player, Step.BELL_VOICE, "clues", kind.bit)
					|| at(player, Step.BELL_GUARD, "silence");
		case WEST_WARD:
		case EAST_WARD:
			return unmarked(player, Step.BELL_GUARD, "wards", kind.bit);
		case ASSEMBLY:
			return BellTrailProgress.active(player, Step.BELL_ESCAPE)
					&& (player.isQuestInState(Step.BELL_ESCAPE.slot, 0, "escort")
							|| player.isQuestInState(Step.BELL_ESCAPE.slot, 0, "ambush"));
		default:
			return at(player, Step.BELL_ESCAPE, "escort;" + (kind.bit - 1));
		}
	}

	private static boolean at(Player player, Step step, String state) {
		return BellTrailProgress.active(player, step)
				&& player.isQuestInState(step.slot, state);
	}

	private static boolean unmarked(Player player, Step step, String state,
			int bit) {
		return BellTrailProgress.active(player, step)
				&& player.isQuestInState(step.slot, 0, state)
				&& (BellTrailProgress.counter(player, step) & bit) == 0;
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
			put("z", 8000);
			setPosition(kind.x, kind.y);
			setResistance(0);
			setMenu("Sprawdź|Użyj");
			setCursor("ACTIVITY");
			setDescription("Oto " + kind.name + ".");
		}

		@Override
		public boolean onUsed(RPEntity user) {
			if (!(user instanceof Player) || getZone() == null
					|| user.getZone() != getZone()
					|| !BellTrailProgress.ISLANDS.equals(getZone().getName())) {
				return false;
			}
			Player player = (Player) user;
			if (!PlayerPrivateScene.isOwnedBy(this, player)) {
				return false;
			}
			if (!player.nextTo(this)) {
				player.sendPrivateText("Musisz podejść bliżej.");
				return false;
			}
			boolean changed = false;
			String text = "Nie znajdujesz niczego nowego. Sprawdź w dzienniku, czego jeszcze potrzeba wyprawie.";
			switch (kind) {
			case LOG:
			case INSCRIPTION:
			case BELL:
			case BELL_TOP:
				changed = BellTrailProgress.mark(player, Step.BELL_VOICE,
						"clues", kind.bit, 7, "report");
				text = kind == Kind.LOG
						? "Pod ostatnim wpisem widnieje imię Bronika. Zginął przy podejściu, zanim reszta dotarła do dzwonu. Witosław sam dopisał datę."
						: kind == Kind.INSCRIPTION
								? "Pod mchem czytasz: Nie odpowiadaj temu, kto woła cię głosem twoich zmarłych. Pamięć nie jest drogą powrotną."
								: "W środku nie ma serca dzwonu. Mimo to słyszysz uderzenie, a potem znajomy głos. Milknie, gdy odwracasz wzrok.";
				if (!changed && (kind == Kind.BELL || kind == Kind.BELL_TOP)) {
					changed = BellTrailProgress.advance(player, Step.BELL_GUARD,
							"silence", "report");
					text = "Luzujesz mocowanie i ostrożnie opuszczasz dzwon na ziemię. Po raz pierwszy słychać sam wiatr. Witosław powinien już wiedzieć, że możecie odejść.";
				} else if (changed
						&& (kind == Kind.BELL || kind == Kind.BELL_TOP)) {
					player.addEvent(
							new SoundEvent("bell-1", SoundLayer.AMBIENT_SOUND));
				}
				break;
			case WEST_WARD:
			case EAST_WARD:
				changed = BellTrailProgress.mark(player, Step.BELL_GUARD,
						"wards", kind.bit, 3, "fight");
				text = "Podważasz kamień i odgarniasz ziemię ze znaku. Głos urywa się w połowie słowa. Przy dzwonie porusza się skrzydlata postać.";
				break;
			case ASSEMBLY:
				return BellTrailEscort.start(player);
			default:
				changed = BellTrailEscort.arrive(player, this, kind.bit);
				text = kind == Kind.FIRST_STOP
						? "Ranni siadają przy palenisku. Znów słychać głos. Każ im zaczekać i sprawdź kładkę na zachodzie."
						: kind == Kind.BRIDGE_STOP
								? "Obaj przeszli przez kładkę. Został ostatni odcinek do obozowiska na zachodzie."
								: "Obaj ranni są bezpieczni. Witosław zajmie się zejściem. Wróć do Festris i powiedz jej, kto ocalał.";
				break;
			}
			player.sendPrivateText(changed ? text
					: "Nie możesz jeszcze tego zrobić. Zajrzyj do dziennika wyprawy.");
			return changed;
		}
	}
}
