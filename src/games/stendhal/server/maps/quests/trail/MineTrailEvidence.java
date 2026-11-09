/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Private clues exist only while their owner needs the corresponding lead. */
final class MineTrailEvidence extends AreaEntity implements UseListener {
	enum Kind {
		ROSTER(
				"rejestr wydobycia",
				"item/documents/book",
				3,
				2,
				53,
				79,
				Step.MINE_LEDGER,
				"Przeglądasz zapisy. Ostatni wózek oddano przed zmrokiem. Ilość węgla zgadza się z dzienną pracą, a nocnej zmiany nie wpisano ani razu. Pokaż to Barbarusowi. Powiedz: nocna zmiana."),
		RATIONS(
				"skrzynka po racjach",
				"item/pot/crate_small",
				0,
				2,
				67,
				64,
				Step.MINE_TRACKS,
				"Na desce są trzy kółka. Ten sam znak był na dostawach Radana. W środku zostały skórki chleba i sznurki po równych porcjach. Ktoś rozdzielał tu jedzenie dla całej grupy."),
		BOOTS(
				"ślady podkutych butów",
				"ground/mieszczanin_tracks",
				4,
				5,
				94,
				62,
				Step.MINE_TRACKS,
				"W pyle odbiły się ciężkie podkute buty. Ślady idą parami, w równych odstępach. Nie widać przy nich kolein po wózkach. Prowadzą dalej na północny wschód."),
		VOICES(
				"stara obudowa szybu",
				"object/wooden_scaffold",
				4,
				4,
				106,
				29,
				Step.MINE_VOICES,
				"Przykładasz ucho do belek. Za skałą słychać równe kroki. Ktoś woła: druga zmiana do bramy, zapasy na dół! Po krótkim sygnale głosy cichną. Opowiedz Zynnowi Iwuhosowi w bibliotece Semos o rozkazach spod ziemi."),
		DISPATCH(
				"pakiet z rozkazem",
				"item/documents/book",
				2,
				2,
				111,
				33,
				Step.MINE_DISPATCH,
				"W pakiecie jest rozkaz odbioru zapasów od Radana i utrzymania warty przy starej bramie. Pod tekstem ktoś odcisnął znak na ciemnym wosku. Przepisujesz treść i rysujesz znak. Pokaż je Phalkowi. Powiedz: rozkaz dla straży.");

		final String name, tileset, text;
		final int tile, columns, x, y;
		final Step step;

		Kind(String name, String tileset, int tile, int columns, int x, int y,
				Step step, String text) {
			this.name = name;
			this.tileset = tileset;
			this.tile = tile;
			this.columns = columns;
			this.x = x;
			this.y = y;
			this.step = step;
			this.text = text;
		}
	}

	static boolean needed(Player player, Kind kind) {
		if (!MineTrailProgress.active(player, kind.step)) {
			return false;
		}
		switch (kind) {
		case RATIONS:
		case BOOTS:
			int bit = kind == Kind.RATIONS ? 1 : 2;
			return player.isQuestInState(kind.step.slot, 0, "clues")
					&& (MineTrailProgress.clues(player) & bit) == 0;
		case ROSTER:
			return player.isQuestInState(kind.step.slot, "register");
		case VOICES:
			return player.isQuestInState(kind.step.slot, "listen");
		case DISPATCH:
			return player.isQuestInState(kind.step.slot, "orders");
		default:
			throw new IllegalStateException("Unknown mine clue");
		}
	}

	final Kind kind;

	MineTrailEvidence(Kind kind) {
		super(1, 1);
		this.kind = kind;
		setRPClass("block");
		put("type", "block");
		put("class", "questuseable");
		put("name", kind.name);
		put("tileset", kind.tileset);
		put("tile_index", kind.tile);
		put("tileset_columns", kind.columns);
		put("z", kind == Kind.BOOTS ? 50 : 8000);
		setResistance(0);
		setPosition(kind.x, kind.y);
		setCursor("ACTIVITY");
		setMenu(kind == Kind.VOICES ? "Posłuchaj|Użyj" : "Sprawdź|Użyj");
		setDescription("Oto " + kind.name + ". Możesz przyjrzeć się bliżej.");
	}

	@Override
	public boolean onUsed(RPEntity user) {
		if (!(user instanceof Player) || getZone() == null
				|| user.getZone() != getZone()
				|| !MineTrailProgress.ZONE.equals(getZone().getName()))
			return false;
		Player player = (Player) user;
		if (!PlayerPrivateScene.isOwnedBy(this, player)) {
			return false;
		}
		if (!player.nextTo(this)) {
			player.sendPrivateText("Musisz podejść bliżej.");
			return false;
		}
		if (!MineTrailProgress.active(player, kind.step)) {
			player.sendPrivateText(
					"Nie znajdujesz tu nic nowego. Sprawdź swój cel w dzienniku.");
			return false;
		}
		boolean changed;
		switch (kind) {
		case RATIONS:
			changed = MineTrailProgress.addClue(player, 1);
			break;
		case BOOTS:
			changed = MineTrailProgress.addClue(player, 2);
			break;
		case ROSTER:
			changed = advance(player, "register", "report");
			break;
		case VOICES:
			changed = advance(player, "listen", "history");
			break;
		case DISPATCH:
			changed = advance(player, "orders", "identified");
			break;
		default:
			throw new IllegalStateException("Unknown mine clue");
		}
		player.sendPrivateText(changed ? kind.text
				: "To miejsce jest już sprawdzone albo trzeba najpierw zrobić coś innego. Sprawdź swój cel w dzienniku.");
		return changed;
	}

	private boolean advance(Player player, String before, String after) {
		if (!player.isQuestInState(kind.step.slot, before))
			return false;
		player.setQuest(kind.step.slot, after);
		return true;
	}

	@Override
	public String getDescriptionName() {
		return kind.name;
	}
}
