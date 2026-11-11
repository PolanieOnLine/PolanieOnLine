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
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.quests.trail.SaltMineProgress.Encounter;

/**
 * Passable evidence and rescue sites in existing, publicly accessible mine
 * corridors.
 */
public final class SaltMineSites implements ZoneConfigurator {
	enum Kind {
		SUPPORT(
				"skrzynka przy naruszonej podporze",
				38,
				89,
				"item/pot/crate_small",
				0,
				2),
		ROPES("mokre liny przy chodniku", 42, 81, "item/ropes", 1, 2),
		SAMPLE("wypływ solanki", 55, 92, "object/trail_investigation_props", 2, 3),
		DRAINAGE("zatkany odpływ", 54, 69, "object/trail_investigation_props", 3, 3),
		CHANNEL("wykruszony próg kanału", 67, 53, "object/trail_investigation_props", 4, 3),
		SEAL("szczelina starego zbiornika", 63, 82, "object/trail_investigation_props", 5, 3),
		RELEASE("lina spustu awaryjnego", 54, 92, "item/ropes", 0, 2);

		final String name, tileset;
		final int x, y, tile, columns;

		Kind(String name, int x, int y, String tileset, int tile, int columns) {
			this.name = name;
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
		if (!SaltMineProgress.MINE.equals(zone.getName()))
			throw new IllegalArgumentException("Unknown salt mine story zone");
		for (Kind kind : Kind.values())
			TrailSceneRegistry.prop(zone, "salt-mine-evidence-" + kind.name(),
					player -> needed(player, kind), () -> new Evidence(kind));
		for (Encounter role : Encounter.values()) {
			TrailSceneRegistry.encounter(zone, "saltmine-" + role.name(),
					role.x, role.y, player -> SaltMineProgress.needs(player, role),
					() -> {
						Creature template = TrailCreatureVariants.create(role.name);
						if (template == null) {
							throw new IllegalStateException(
									"Missing story opponent: " + role.name);
						}
						return new SaltMineGuard(template, role);
					});
		}
		addMiner(zone, SaltMineProgress.FOREMAN, 34, 80,
				"Oto Radost. Sztygar pilnuje wejścia i nasłuchuje odgłosów z chodników.");
		addMiner(zone, SaltMineProgress.BOGDAN, 43, 79,
				"Oto górnik Bogdan. Ma podarte ubranie i mocno ściska trzonek kilofa.");
		addMiner(zone, SaltMineProgress.WOJMIR, 40, 54,
				"Oto górnik Wojmir. Ostrożnie stawia nogi na mokrej posadzce.");
	}

	private void addMiner(StendhalRPZone zone, String name, int x, int y,
			String description) {
		SpeakerNPC miner = new SpeakerNPC(name) {
			@Override
			protected void createDialog() {
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES, null,
						ConversationStates.ATTENDING, null,
						(p, s, n) -> n.say(
								p.isQuestCompleted(Step.SALT_RETURN.slot)
										? "Dobrze cię widzieć. Woda już nie napływa. Teraz sprawdzamy chodniki, zanim wróci tu cała zmiana."
										: SaltMineProgress.FOREMAN.equals(name)
												? "Uważaj pod ziemią. Po wstrząsie nie wszystko trzyma tak, jak powinno. Przysyła cię Zbigniew?"
												: p.isQuestCompleted(
														Step.SALT_RESCUE.slot)
																? "Pomoc już dotarła. Zostałem przy chodniku, żeby nikt znowu nie poszedł w stronę tej wody."
																: "Żyję. Nie wiem tylko, czy droga do szybu jeszcze stoi. Powiedz, że ktoś wie, gdzie nas szukać."));
				addJob(SaltMineProgress.FOREMAN.equals(name)
						? "Jestem sztygarem. Odpowiadam za ludzi, którzy schodzą na zmianę. Muszę ich stąd wyprowadzić."
						: "Pracuję przy wydobyciu soli. Dziś wolałbym już zobaczyć niebo.");
				addHelp(SaltMineProgress.FOREMAN.equals(name)
						? "Jeśli przysyła cię burmistrz, porozmawiajmy o #'zaginionej zmianie'."
						: "Jeśli droga jest bezpieczna, powiedz mi o #'odciętych górnikach'.");
				addGoodbye("Patrz też nad głowę, nie tylko pod nogi.");
			}
		};
		miner.setEntityClass(
				SaltMineProgress.FOREMAN.equals(name) ? "olddwarfnpc"
						: "minernpc");
		miner.setGender("M");
		miner.setPosition(x, y);
		miner.setDirection(Direction.DOWN);
		miner.setDescription(description);
		TrailSceneRegistry.actor(zone, miner, player -> {
			if (SaltMineProgress.FOREMAN.equals(name)) {
				return Step.SALT_SHIFT.availableOrStarted(player)
						&& !player.isQuestCompleted(Step.SALT_RETURN.slot)
						&& !player.isQuestInState(Step.SALT_RETURN.slot, "mayor");
			}
			int bit = SaltMineProgress.BOGDAN.equals(name) ? 1 : 2;
			return unmarked(player, Step.SALT_RESCUE, "miners", bit, "report");
		});
	}

	private static boolean needed(Player player, Kind kind) {
		switch (kind) {
		case SUPPORT:
			return unmarked(player, Step.SALT_SHIFT, "marks", 1, "report")
					|| at(player, Step.SALT_RESCUE, "brace")
					|| unmarked(player, Step.SALT_RETURN, "verify", 1, "mayor");
		case ROPES:
			return unmarked(player, Step.SALT_SHIFT, "marks", 2, "report")
					|| unmarked(player, Step.SALT_RETURN, "verify", 2, "mayor");
		case SAMPLE:
			return at(player, Step.SALT_SAMPLE, "sample");
		case DRAINAGE:
			return unmarked(player, Step.SALT_SOURCE, "channels", 1, "report");
		case CHANNEL:
			return unmarked(player, Step.SALT_SOURCE, "channels", 2, "report");
		case SEAL:
			return at(player, Step.SALT_CREATURE, "seal");
		case RELEASE:
			return at(player, Step.SALT_RETURN, "drain");
		default:
			throw new IllegalStateException("Unknown salt mine evidence");
		}
	}

	private static boolean at(Player player, Step step, String state) {
		return SaltMineProgress.active(player, step)
				&& player.isQuestInState(step.slot, state);
	}

	private static boolean unmarked(Player player, Step step, String state,
			int bit, String terminal) {
		return SaltMineProgress.active(player, step)
				&& player.isQuestInState(step.slot, 0, state)
				&& (SaltMineProgress.mask(player, step, terminal) & bit) == 0;
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
					|| !SaltMineProgress.MINE.equals(getZone().getName()))
				return false;
			Player p = (Player) user;
			if (!PlayerPrivateScene.isOwnedBy(this, p)) {
				return false;
			}
			if (!p.nextTo(this)) {
				p.sendPrivateText("Musisz podejść bliżej.");
				return false;
			}
			boolean changed = false;
			String text = "";
			switch (kind) {
			case SUPPORT:
				if (SaltMineProgress.mark(p, Step.SALT_SHIFT, "marks", 1,
						"report")) {
					changed = true;
					text = "Podpora odsunęła się od stropu. Obok leżą wyrwane kliny, a w skrzynce zostało kilka rozpórek. Ktoś próbował zabezpieczyć przejście po wstrząsie.";
				} else if (SaltMineProgress.advance(p, Step.SALT_RESCUE,
						"brace", "fight")) {
					changed = true;
					text = "Wyciągasz rozpórki ze skrzynki i wsuwasz je pod belkę. Po dobiciu klinów strop przestaje trzeszczeć. W dalszej części chodnika wciąż porusza się solny szlam.";
				} else if (SaltMineProgress.mark(p, Step.SALT_RETURN, "verify",
						1, "mayor")) {
					changed = true;
					text = "Sprawdzasz kliny i rozpórki. Belka trzyma, a ze stropu nie osypują się już kamienie. Tędy można bezpiecznie dojść do szybu.";
				}
				break;
			case ROPES:
				if (SaltMineProgress.mark(p, Step.SALT_SHIFT, "marks", 2,
						"report")) {
					changed = true;
					text = "Liny są mokre i sztywne od soli. Ślady napływu prowadzą z dalszych chodników. To nie woda rozlana z górniczego wiadra.";
				} else if (SaltMineProgress.mark(p, Step.SALT_RETURN, "verify",
						2, "mayor")) {
					changed = true;
					text = "Woda opadła poniżej drogi do szybu. Na kamieniach został tylko mokry osad. Tędy górnicy mogą bezpiecznie wrócić na powierzchnię.";
				}
				break;
			case SAMPLE:
				changed = SaltMineProgress.advance(p, Step.SALT_SAMPLE,
						"sample", "agrypin");
				text = "Pobierasz trochę solanki do szczelnego naczynia. Na dnie porusza się jasna zawiesina, choć ręka już się nie trzęsie. Trzeba pokazać to Agrypinowi.";
				break;
			case DRAINAGE:
				changed = SaltMineProgress.mark(p, Step.SALT_SOURCE, "channels",
						1, "report");
				text = "Odpływ jest zasypany odłamkami soli. Woda zamiast odpływać, rozlewa się po chodnikach. Nie ruszasz jeszcze spustu, żeby nie skierować strumienia na drogę do szybu.";
				break;
			case CHANNEL:
				changed = SaltMineProgress.mark(p, Step.SALT_SOURCE, "channels",
						2, "report");
				text = "Próg kanału pękł przy samym zbiorniku. Ze szczeliny wypływa gęsta solanka. Nad wodą formuje się postać. Radost musi o tym usłyszeć, zanim ktokolwiek spróbuje oczyścić odpływ.";
				break;
			case SEAL:
				changed = SaltMineProgress.advance(p, Step.SALT_CREATURE,
						"seal", "report");
				text = "Usuwasz luźne odłamki i dopasowujesz płaskie kamienie do szczeliny. Dobijasz przygotowane kliny. Strumień słabnie, aż wreszcie ustaje. Zbiornik nie zalewa już chodników.";
				break;
			case RELEASE:
				changed = SaltMineProgress.advance(p, Step.SALT_RETURN, "drain",
						"verify;0");
				text = "Oczyszczasz wlot spustu i ciągniesz za linę. Nagromadzona woda rusza dawnym odpływem, z dala od szybu. Pozostało sprawdzić podpartą belkę i drogę przy mokrych linach.";
				break;
			default:
				throw new IllegalStateException("Unknown mine evidence");
			}
			p.sendPrivateText(changed ? text
					: "Nie znajdujesz tu niczego nowego. Zajrzyj do dziennika, jeśli nie wiesz, co pozostało do zrobienia.");
			return changed;
		}
	}
}
