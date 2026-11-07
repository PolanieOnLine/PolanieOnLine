/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;
import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.quests.trail.DragonTrailProgress.Encounter;

/**
 * Passable personal clues on native maps; no global terrain or ordinary spawn
 * changes.
 */
public final class DragonTrailSites implements ZoneConfigurator {
	enum Kind {
		CRATE(
				"nadpalona skrzynia",
				DragonTrailProgress.SOUTH,
				28,
				119,
				"item/pot/crate_small",
				1,
				2,
				Step.EMBER_CAMP,
				"traces",
				null,
				1,
				"W zwęglonych deskach tkwi duży odłamek skorupy. Od środka przylgnęła do niego słoma i lepkie płótno. Smoki nie przyniosły tu tej skrzyni."),
		ASH(
				"wygasłe palenisko",
				DragonTrailProgress.SOUTH,
				34,
				119,
				"item/campfires",
				0,
				2,
				Step.EMBER_CAMP,
				"traces",
				null,
				2,
				"Pod popiołem zostały ślady kół i przewrócony kociołek. Wóz zatrzymał się przy ognisku, zanim wszystko spłonęło. Radowit siedzi kawałek obok, z ręką owiniętą szmatą."),
		CARGO(
				"porzucony ładunek",
				DragonTrailProgress.NORTH,
				60,
				40,
				"item/pot/crate_small",
				1,
				2,
				Step.EMBER_TRANSPORT,
				"cargo",
				"witness",
				0,
				"Pod płótnem są wygniecione gniazda ze słomy i odłamki skorup. Samych jaj już nie ma. Odcinasz kawałek lepkiej tkaniny. Robercik siedzi na północ stąd, mógł widzieć ludzi przy wozie."),
		MIXTURE(
				"naczynie z zabrudzonym płótnem",
				DragonTrailProgress.TOWER,
				6,
				42,
				"item/pot/clay_pot",
				0,
				1,
				Step.EMBER_SCENT,
				"mixture",
				"report",
				0,
				"Ostrożnie zanurzasz płótno w ciepłej wodzie. Ciemna warstwa puszcza i zbiera się na powierzchni. Spod zapachu żywicy przebija znajoma woń krwi. Wizariusz nachyla się nad naczyniem."),
		EGGS(
				"skrzynia ze smoczymi jajami",
				DragonTrailProgress.NORTH,
				107,
				41,
				"item/pot/crate_small",
				1,
				2,
				Step.EMBER_EGGS,
				"cache",
				"report",
				0,
				"W skrzyni są całe jaja, obłożone słomą. Obok leżą zapiski: podrzucony lęg miał odciągać smoki od gniazd, żeby łatwiej było zabierać następne. Ostrobor powinien to zobaczyć."),
		WASH(
				"naczynie do obmycia jaj",
				DragonTrailProgress.NORTH,
				97,
				43,
				"item/pot/clay_pot",
				0,
				1,
				Step.EMBER_RETURN,
				"wash",
				"traps",
				0,
				"Rozcieńczasz środek od Wizariusza i obmywasz skorupy. Lepka warstwa spływa z wodą. Zmieniasz też zabrudzone płótno na czystą słomę. Teraz trzeba usunąć pułapki przy lęgowisku."),
		TRAPS(
				"sidła przy lęgowisku",
				DragonTrailProgress.NORTH,
				25,
				110,
				"item/ropes",
				2,
				2,
				Step.EMBER_RETURN,
				"traps",
				"nest",
				0,
				"Luzujesz naprężone liny, wyciągasz kołki i zsuwasz pętle. Żaden smok nie zaplącze się w nie przy powrocie do gniazda. Możesz odłożyć jaja na wyściółkę między kamieniami."),
		NEST(
				"wyściółka smoczego gniazda",
				DragonTrailProgress.NORTH,
				27,
				107,
				"item/nest",
				0,
				2,
				Step.EMBER_RETURN,
				"nest",
				"report",
				0,
				"Układasz jaja w zagłębieniu i osłaniasz je suchą wyściółką. Nie ma tu już przynęty ani sideł. Cofasz się spokojnie. Racirad musi poznać prawdę, zanim jego ludzie ruszą z pochodniami.");

		final String name, zone, tileset, state, next, text;
		final int x, y, tile, columns, bit;
		final Step step;

		Kind(String name, String zone, int x, int y, String tileset, int tile,
				int columns, Step step, String state, String next, int bit,
				String text) {
			this.name = name;
			this.zone = zone;
			this.x = x;
			this.y = y;
			this.tileset = tileset;
			this.tile = tile;
			this.columns = columns;
			this.step = step;
			this.state = state;
			this.next = next;
			this.bit = bit;
			this.text = text;
		}
	}

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		String name = zone.getName();
		if (!DragonTrailProgress.SOUTH.equals(name)
				&& !DragonTrailProgress.NORTH.equals(name)
				&& !DragonTrailProgress.TOWER.equals(name))
			throw new IllegalArgumentException("Unknown dragon story zone");
		for (Kind kind : Kind.values())
			if (kind.zone.equals(name))
				zone.add(new Evidence(kind));
		if (DragonTrailProgress.NORTH.equals(name)) {
			for (Encounter role : Encounter.values()) {
				Creature template = TrailCreatureVariants.create(
						role.name);
				if (template == null)
					throw new IllegalStateException(
							"Missing dragon story opponent: " + role.name);
				zone.add(new CreatureRespawnPoint(zone, role.x, role.y,
						new DragonTrailGuard(template, role), 1));
			}
			addWitness(zone, DragonTrailProgress.GUIDE, 99, 43,
					"Oto Ostrobor. Na jego nadgarstkach widać otarcia po linie.");
		} else if (DragonTrailProgress.SOUTH.equals(name))
			addWitness(zone, DragonTrailProgress.SURVIVOR, 31, 118,
					"Oto Radowit. Ma nadpalone ubranie i zabandażowaną rękę.");
	}

	private void addWitness(StendhalRPZone zone, String name, int x, int y,
			String description) {
		SpeakerNPC npc = new SpeakerNPC(name) {
			@Override
			protected void createDialog() {
				boolean guide = DragonTrailProgress.GUIDE.equals(name);
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES, null,
						ConversationStates.ATTENDING, null,
						(p, s, n) -> n.say(
								p.isQuestCompleted(Step.EMBER_RETURN.slot)
										? "Dobrze cię widzieć. Racirad dotrzymał słowa. Tym razem nikt nie pójdzie palić gniazd."
										: guide ? (p.isQuestCompleted(
												Step.EMBER_GUIDE.slot)
														? "Wciąż myślę o tych jajach. Nie zostawimy ich handlarzom, prawda?"
														: "Ciszej. Jeśli strażnik usłyszy, że rozmawiamy, oboje będziemy mieli kłopoty.")
												: "Udało mi się odczołgać od ognia. Resztę widzisz. Jeśli Racirad cię przysłał, powiem, co pamiętam."));
				addJob(guide
						? "Prowadzę ludzi przez te ziemie. Zwykle takich, którzy chcą wrócić z obiema nogami. Z tymi tutaj nie powinienem był w ogóle iść."
						: "Woziłem zapasy ludziom na trakcie. Nigdy nie sądziłem, że przy naszym ognisku wyląduje smoczy łeb.");
				addHelp(guide
						? "Jeśli przysyła cię Racirad, powiedz o #'pojmanym przewodniku'."
						: "Porozmawiajmy o #'spalonym obozie'. Nie chcę, żeby zginęli następni.");
				addGoodbye("Uważaj na siebie.");
			}
		};
		npc.setEntityClass("woodcutternpc");
		npc.setGender("M");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(x, y);
		npc.setDescription(description);
		zone.add(npc);
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
					|| !kind.zone.equals(getZone().getName()))
				return false;
			Player player = (Player) user;
			if (!player.nextTo(this)) {
				player.sendPrivateText("Musisz podejść bliżej.");
				return false;
			}
			boolean changed = kind.bit != 0
					? DragonTrailProgress.trace(player, kind.bit)
					: DragonTrailProgress.advance(player, kind.step, kind.state,
							kind.next);
			player.sendPrivateText(changed ? kind.text
					: "Nie znajdujesz tu niczego nowego. Zajrzyj do dziennika, jeśli nie wiesz, co pozostało do zrobienia.");
			return changed;
		}
	}
}
