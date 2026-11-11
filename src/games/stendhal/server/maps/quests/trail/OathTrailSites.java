/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;
import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.OathTrailProgress.Encounter;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * Passable scenery on existing fields; no map tiles or ordinary spawn markers
 * change.
 */
public final class OathTrailSites implements ZoneConfigurator {
	enum Kind {
		SHIELD(
				"resztki tarczy",
				OathTrailProgress.NORTH,
				53,
				40,
				"item/quest/borzywoj_shield",
				0,
				1),
		NAMES(
				"kamień z imionami",
				OathTrailProgress.NORTH,
				58,
				40,
				"item/gothic/slab",
				0,
				1),
		GRAVE(
				"samotny grób dowódcy",
				OathTrailProgress.WEST,
				92,
				34,
				"item/gothic/grave_marker_2",
				0,
				1),
		ORDER(
				"resztki dawnego posterunku",
				OathTrailProgress.WEST,
				94,
				38,
				"item/gothic/slab",
				0,
				1),
		BANNER(
				"porzucona chorągiew oddziału",
				OathTrailProgress.WEST,
				100,
				85,
				"item/banners/flag",
				4,
				3),
		MOUND(
				"wspólna mogiła oddziału",
				OathTrailProgress.NORTH,
				76,
				28,
				"item/gothic/grave_marker_2",
				0,
				1),
		MEMORIAL(
				"miejsce na chorągiew",
				OathTrailProgress.NORTH,
				78,
				28,
				"item/gothic/slab",
				0,
				1);

		final String name, zone, tileset;
		final int x, y, tile, columns;

		Kind(String name, String zone, int x, int y, String tileset, int tile,
				int columns) {
			this.name = name;
			this.zone = zone;
			this.x = x;
			this.y = y;
			this.tileset = tileset;
			this.tile = tile;
			this.columns = columns;
		}
	}

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		if (!OathTrailProgress.NORTH.equals(zone.getName())
				&& !OathTrailProgress.WEST.equals(zone.getName()))
			throw new IllegalArgumentException("Unknown oath zone");
		for (Kind kind : Kind.values())
			if (kind.zone.equals(zone.getName()))
				TrailSceneRegistry.prop(zone, "oath-evidence-" + kind.name(),
						player -> needed(player, kind), () -> new Evidence(kind));
		for (Encounter role : Encounter.values())
			if (role.zone.equals(zone.getName())) {
				TrailSceneRegistry.encounter(zone, "oath-" + role.name(),
						role.x, role.y,
						player -> OathTrailProgress.needs(player, role),
						() -> {
							Creature template = TrailCreatureVariants.create(role.name);
							if (template == null) {
								throw new IllegalStateException(
										"Missing story opponent: " + role.name);
							}
							return new OathTrailGuard(template, role);
						});
			}
		if (OathTrailProgress.NORTH.equals(zone.getName())) {
			SpeakerNPC witness = new SpeakerNPC(OathTrailProgress.SENTRY) {
				@Override
				protected void createDialog() {
					add(ConversationStates.IDLE,
							ConversationPhrases.GREETING_MESSAGES, null,
							ConversationStates.ATTENDING, null,
							(player, sentence, speaker) -> speaker.say(
									OathTrailProgress.released(player)
											? "Moi towarzysze odeszli. Dziękuję za pomoc. Zostaję tu z własnej woli, żeby strzec ich mogiły."
											: "Stój. Dalej idą tylko nasi. #Dowódca nie zwolnił nas z warty."));
					add(ConversationStates.ATTENDING, "dowódca", null,
							ConversationStates.ATTENDING, null,
							(player, sentence, speaker) -> speaker.say(
									OathTrailProgress.released(player)
											? "Borzywoj spoczywa ze swoimi ludźmi. Teraz przechodnie poznają jego prawdziwą historię."
											: "Borzywoj. Miał wrócić po ostatnim wozie. Czekamy na niego."));
					addGoodbye("Uważaj na trakcie.");
				}
			};
			witness.setEntityClass("youngsoldiernpc");
			witness.setGender("M");
			witness.setVisibility(45);
			witness.setResistance(0);
			witness.put("no_shadow", "");
			witness.setDirection(Direction.DOWN);
			witness.setPosition(70, 38);
			witness.setDescription(
					"Oto Wszebor. Przez jego bladą postać widać trawę. Wciąż wypatruje kogoś na drodze.");
			TrailSceneRegistry.actor(zone, witness,
					player -> Step.OATH_WATCH.availableOrStarted(player)
							&& !OathTrailProgress.released(player));
		}
	}

	static boolean needed(Player player, Kind kind) {
		switch (kind) {
		case SHIELD:
		case NAMES:
			int bit = kind == Kind.SHIELD ? 1 : 2;
			return OathTrailProgress.active(player, Step.OATH_NAMES)
					&& player.isQuestInState(Step.OATH_NAMES.slot, 0, "clues")
					&& (OathTrailProgress.mask(player) & bit) == 0;
		case GRAVE:
			return at(player, Step.OATH_GRAVE, "grave")
					|| at(player, Step.OATH_BURIAL, "remains");
		case ORDER:
			return at(player, Step.OATH_TRUTH, "proof");
		case BANNER:
			return at(player, Step.OATH_BANNER, "banner");
		case MOUND:
			return at(player, Step.OATH_BURIAL, "burial;0");
		case MEMORIAL:
			return at(player, Step.OATH_BURIAL, "burial;1");
		default:
			throw new IllegalStateException("Unknown oath evidence");
		}
	}

	private static boolean at(Player player, Step step, String state) {
		return OathTrailProgress.active(player, step)
				&& player.isQuestInState(step.slot, state);
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
			setCursor("ACTIVITY");
			setMenu("Sprawdź|Użyj");
			setDescription("Oto " + kind.name + ".");
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
			boolean changed = false;
			String text = "";
			switch (kind) {
			case SHIELD:
				changed = OathTrailProgress.mark(player, 1);
				text = "Spod rdzy wyłania się grot włóczni, a pod nim trzy nacięcia. Odrysowujesz znak. Na obrzeżu widać imię Borzywoja.";
				break;
			case NAMES:
				changed = OathTrailProgress.mark(player, 2);
				text = "Kamień wymienia poległych z tego samego oddziału. Przy Borzywoju ktoś dopisał: uciekł. Odrysowujesz imiona dla Amileusza.";
				break;
			case GRAVE:
				changed = OathTrailProgress.advance(player, Step.OATH_GRAVE,
						"grave", "report");
				text = "Pod oskarżeniem o zdradę zachował się starszy napis: pozostał przy przeprawie. Przy nagrobku leży tarcza ze znakiem oddziału. Zostawiasz grób nienaruszony.";
				if (!changed) {
					changed = OathTrailProgress.advance(player,
							Step.OATH_BURIAL, "remains", "burial;0");
					text = "Ostrożnie odsłaniasz pochówek. Owijasz szczątki Borzywoja w płótno, żeby przenieść je do jego ludzi. Tarcza zostanie przy grobie do czasu zakończenia pochówku.";
				}
				break;
			case ORDER:
				changed = OathTrailProgress.advance(player, Step.OATH_TRUTH,
						"proof", "report");
				text = "W szczelinie kamienia znajdujesz resztki metalowej tuby i pergamin. Rozkaz nakazuje Borzywojowi utrzymać przeprawę, aż odejdzie ostatni wóz z mieszkańcami. Starannie przepisujesz zachowane zdania.";
				break;
			case BANNER:
				changed = OathTrailProgress.advance(player, Step.OATH_BANNER,
						"banner", "report");
				text = "Zwijasz wyblakłą chorągiew. Mimo postrzępionych brzegów widać ten sam grot i trzy nacięcia. Zatrzymujesz ją na wspólny pochówek oddziału.";
				break;
			case MOUND:
				changed = OathTrailProgress.advance(player, Step.OATH_BURIAL,
						"burial;0", "burial;1");
				text = "Składasz szczątki Borzywoja obok jego wojowników i zasypujesz grób. Dowódca wrócił do oddziału. Pozostało oddać im chorągiew.";
				break;
			case MEMORIAL:
				changed = OathTrailProgress.advance(player, Step.OATH_BURIAL,
						"burial;1", "release");
				text = "Rozwijasz chorągiew nad wspólną mogiłą. Gdy wypowiadasz imię Borzywoja, milknie szczęk niewidocznej broni. Wszebor czeka przy drodze.";
				break;
			default:
				throw new IllegalStateException("Unknown evidence");
			}
			player.sendPrivateText(changed ? text
					: "Nie znajdujesz tu niczego nowego. Dziennik przypomni ci, co pozostało do zrobienia.");
			return changed;
		}
	}
}
