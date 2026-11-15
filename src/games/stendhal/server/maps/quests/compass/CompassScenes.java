/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.compass.CompassProgress.Stage;
import games.stendhal.server.maps.quests.trail.TrailSceneRegistry;

/** Personal, passable scenes on the existing Kościelisko clearing. */
final class CompassScenes {
	enum Clue {
		TRACKS("ślady zagubionego wędrowca", 1, 24, 44, Stage.MISSING),
		BAG("porzucona torba", 0, 47, 63, Stage.BAG),
		MARKS("fałszywe nacięcia", 1, 70, 43, Stage.MARKS),
		NOTES("notatki w opuszczonym obozie", 2, 84, 78, Stage.NOTES),
		MOSS("omszały kamień", 11, 51, 81, Stage.JUNCTIONS),
		STUMP("suchy pień", 11, 36, 58, Stage.JUNCTIONS),
		FALSE("świeża strzałka", 1, 66, 55, Stage.JUNCTIONS),
		HERBS("miejscowe zioła lecznicze", 3, 35, 78, Stage.BANDAGE),
		FIRE("zimne ognisko", 4, 69, 85, Stage.FIRE),
		MEAL("kociołek nad ogniskiem", 5, 69, 85, Stage.MEAL),
		STOP("postój przy wschodnim wyjściu z lasu", 12, 0),
		SOUTH_STOP("postój przy zaśnieżonym trakcie", 12, 1),
		SHELTER("postój przy drodze przez Zakopane", 12, 2),
		HOME("bezpieczny postój przy Sabale", 12, 3),
		REMAINS("szczątki dawnego przewodnika", 6, 52, 39, Stage.REMAINS),
		LETTER("uszkodzony kompas i niedokończony list", 7, 54, 39, Stage.LETTER),
		CASE("obudowa kompasu", 8, 85, 49, Stage.CASE),
		ASSEMBLY("części kompasu przy ognisku", 9, 69, 85, Stage.ASSEMBLY),
		CROSS_MOSS("omszały kamień przy starym szlaku", 11, 31, 51, Stage.CROSSROADS),
		CROSS_STUMP("suchy pień przy starym szlaku", 11, 75, 79, Stage.CROSSROADS),
		CROSS_WATER("kamienna niecka z wodą", 11, 84, 52, Stage.CROSSROADS),
		BURIAL("miejsce spoczynku Miłorada", 6, 52, 39, Stage.BURIAL),
		CAIRN("mogiła Miłorada", 10, 52, 39, Stage.RETURN);
		final String name, zone;
		final int tile, x, y;
		final Stage stage;

		Clue(String name, int tile, int x, int y, Stage stage) {
			this(name, tile, CompassProgress.FOREST, x, y, stage);
		}

		Clue(String name, int tile, int checkpoint) {
			this(name, tile, CompassEscort.ZONES[checkpoint], CompassEscort.STOPS[checkpoint][0],
					CompassEscort.STOPS[checkpoint][1], Stage.ESCORT);
		}

		Clue(String name, int tile, String zone, int x, int y, Stage stage) {
			this.name = name;
			this.zone = zone;
			this.tile = tile;
			this.x = x;
			this.y = y;
			this.stage = stage;
		}
	}

	private CompassScenes() {}

	static void register(StendhalRPZone zone) {
		for (Clue clue : Clue.values())
			if (clue.zone.equals(zone.getName())) TrailSceneRegistry.prop(
					zone, "compass-" + clue.name(), p -> needed(p, clue), () -> new Prop(clue));
		if (!CompassProgress.FOREST.equals(zone.getName())) return;
		SpeakerNPC wounded =
				new SpeakerNPC(CompassProgress.WANDERER) {
					@Override
					protected void createDialog() {
						addGreeting("Dobrze, że jesteś... Nie mogę stanąć na nodze. Potrzebuję #pomocy.");
						addGoodbye("Nie odchodź daleko.");
					}
				};
		wounded.setEntityClass("woodcutternpc");
		wounded.setGender("M");
		wounded.setDescription(
				"Oto Dobromir. Z podartej nogawki sączy się krew, a dłonie trzęsą mu się z zimna.");
		wounded.setPosition(68, 86);
		wounded.setDirection(Direction.DOWN);
		TrailSceneRegistry.actor(
				zone,
				wounded,
				p -> {
					Stage s = CompassProgress.stage(p);
					return s != null
							&& s.ordinal() >= Stage.WOLVES.ordinal()
							&& s.ordinal() <= Stage.MEAL.ordinal();
				});
		for (int i = 0; i < 4; i++) {
			final int role = i;
			TrailSceneRegistry.encounter(
					zone,
					"compass-guard-" + i,
					CompassGuard.X[i],
					CompassGuard.Y[i],
					p -> CompassGuard.needed(p, role),
					() -> CompassGuard.create(role));
		}
	}

	static boolean needed(Player p, Clue c) {
		Stage s = CompassProgress.stage(p);
		if (c == Clue.NOTES)
			return s != null
					&& s.ordinal() >= Stage.NOTES.ordinal()
					&& s.ordinal() <= Stage.CROSSROADS.ordinal();
		if (s != c.stage) return false;
		if (c == Clue.HERBS) return CompassProgress.detail(p) == 0;
		if (c == Clue.STOP) return CompassProgress.detail(p) == 0;
		if (c == Clue.SOUTH_STOP) return CompassProgress.detail(p) == 1;
		if (c == Clue.SHELTER) return CompassProgress.detail(p) == 2;
		if (c == Clue.HOME) return CompassProgress.detail(p) == 3;
		return true;
	}

	static final class Prop extends AreaEntity implements UseListener {
		final Clue clue;

		Prop(Clue clue) {
			super(1, 1);
			this.clue = clue;
			setRPClass("block");
			put("type", "block");
			put("class", "questuseable");
			put("name", clue.name);
			put("tileset", "object/wanderers_compass_props");
			put("tile_index", clue.tile);
			if (clue == Clue.MOSS || clue == Clue.CROSS_MOSS) {
				put("tileset", "object/wanderers_compass_waymarks");
				put("tile_index", 0);
			} else if (clue == Clue.STUMP || clue == Clue.CROSS_STUMP) {
				put("tileset", "object/wanderers_compass_waymarks");
				put("tile_index", 1);
			} else if (clue == Clue.CROSS_WATER) {
				put("tileset", "object/wanderers_compass_waymarks");
				put("tile_index", 2);
			} else if (clue == Clue.TRACKS) {
				put("tileset", "object/wanderers_compass_waymarks");
				put("tile_index", 3);
			}
			put("tileset_columns", 4);
			put("z", 8000);
			setPosition(clue.x, clue.y);
			setResistance(0);
			setCursor("ACTIVITY");
			setMenu("Sprawdź|Użyj");
			setDescription("Oto " + clue.name + ". Możesz przyjrzeć się bliżej.");
		}

		@Override
		public String getDescriptionName() {
			return clue.name;
		}

		@Override
		public boolean onUsed(RPEntity user) {
			if (!(user instanceof Player)
					|| getZone() == null
					|| user.getZone() != getZone()
					|| !clue.zone.equals(getZone().getName())) return false;
			Player p = (Player) user;
			if (!PlayerPrivateScene.isOwnedBy(this, p)) return false;
			if (!p.nextTo(this)) {
				p.sendPrivateText("Musisz podejść bliżej.");
				return false;
			}
			if (!needed(p, clue)) {
				p.sendPrivateText(CompassProgress.objective(p));
				return false;
			}
			Stage s = CompassProgress.stage(p);
			if (clue == Clue.NOTES && s != Stage.NOTES) {
				p.sendPrivateText(CompassProgress.NOTES);
				return true;
			}
			switch (clue) {
				case TRACKS:
					p.sendPrivateText(
							"Jeden but zostawił głęboki odcisk. Ślady skręcają na południowy wschód, ku"
									+ " kamieniom rozsypanym na środku polany. Wędrowiec musiał dźwigać ciężką torbę.");
					break;
				case BAG:
					p.sendPrivateText(
							"Na skórze wyryto imię Dobromir. Torba jest rozcięta, a chleb rozsypany. Ślady"
									+ " prowadzą teraz na północny wschód, ku nacięciom przy skraju lasu.");
					break;
				case MARKS:
					p.sendPrivateText(
							"Strzałki wycięto niedawno i celują prosto w gęstwinę. Pod nimi zachował się stary"
									+ " znak przewodników. Na południowym wschodzie widać opuszczony obóz."
									+ " Warto poszukać tam wyjaśnienia.");
					break;
				case NOTES:
					p.sendPrivateText(CompassProgress.NOTES);
					break;
				case MOSS:
				case STUMP:
				case FALSE:
					int expected = clue == Clue.MOSS ? 0 : clue == Clue.STUMP ? 1 : -1;
					if (expected != CompassProgress.detail(p)) {
						CompassProgress.set(p, Stage.JUNCTIONS, 0, 0);
						p.sendPrivateText(
								"Tutaj ślad się urywa. Wracasz do zapisków z obozu: najpierw omszały kamień,"
										+ " potem suchy pień. Trzeba jeszcze raz przyjrzeć się tym miejscom.");
						return false;
					}
					if (expected == 0) {
						CompassProgress.set(p, Stage.JUNCTIONS, 1, 0);
						p.sendPrivateText(
								"Przy mchu znajdujesz ślad buta Dobromira. Teraz poszukaj suchego pnia na północny zachód"
										+ " stąd.");
						return true;
					}
					p.sendPrivateText(
							"Z głębi polany dochodzi skowyt. Na południowym wschodzie ktoś odpędza wilki."
									+ " Przedzierasz się wzrokiem przez zarośla, ale stąd nie widać jeszcze, co się dzieje.");
					break;
				case HERBS:
					CompassProgress.set(p, Stage.BANDAGE, 1, 0);
					p.sendPrivateText(
							"Zrywasz kilka leczniczych liści. Z kawałkiem sukna nadadzą się na opatrunek dla"
									+ " Dobromira.");
					return true;
				case FIRE:
					if (!WanderersCompassQuest.consume(p, null, new String[] {"polano"}, new int[] {3}))
						return false;
					p.sendPrivateText(
							"Układasz suche polana i rozpalasz ogień. Dobromir przestaje drżeć. Teraz przygotuj"
									+ " mu ciepłą strawę.");
					break;
				case MEAL:
					if (!WanderersCompassQuest.consume(
							p, null, new String[] {"mięso", "buteleczka wody"}, new int[] {1, 1})) return false;
					p.sendPrivateText(
							"Podajesz Dobromirowi gorącą strawę. Po posiłku próbuje stanąć na nogach."
									+ " „Muszę wrócić między ludzi. Wyprowadź mnie na drogę przy wschodnim"
									+ " wyjściu z lasu. Przejdziemy przez zachodnią część Zakopanego i centrum,"
									+ " a potem na południe, do Sabały. Sam jeszcze nie dam rady”.");
					break;
				case STOP:
					return CompassEscort.arrive(p, this, 0);
				case SOUTH_STOP:
					return CompassEscort.arrive(p, this, 1);
				case SHELTER:
					return CompassEscort.arrive(p, this, 2);
				case HOME:
					return CompassEscort.arrive(p, this, 3);
				case REMAINS:
					p.sendPrivateText(
							"W trawie leżą stare ludzkie kości. Przy spróchniałym pasie zachował się"
									+ " znak podobny do tego z notesu. Obok widać złożoną kartkę i rozbity kompas.");
					break;
				case LETTER:
					p.sendPrivateText(
							"„Sabało, szukałem tych, którzy poszli za światłami...” Dalej pismo jest nieczytelne."
									+ " Pod listem widnieje imię Miłorad. Zabierasz kartkę i resztki kompasu. Obudowy"
									+ " brakuje; ślady po wleczonym pasku ciągną się ku wschodniej części polany,"
									+ " gdzie migoczą ogniki. Najpierw pokaż list Sabale i zapytaj o"
									+ " starego przewodnika.");
					break;
				case CASE:
					if (CompassProgress.detail(p) != 3) {
						p.sendPrivateText("Ogniki krążą tuż nad obudową. Najpierw musisz się ich pozbyć.");
						return false;
					}
					p.sendPrivateText(
							"Zabierasz obudowę z trawy. Masz już igłę od Jacka i resztki starego kompasu."
									+ " Złóż je przy dawnym ognisku Dobromira.");
					break;
				case ASSEMBLY:
					p.sendPrivateText(
							"Osadzasz igłę i zamykasz szkło, lecz wskazówka wciąż krąży bez ładu."
									+ " W notesie Miłorad opisał, co robić, gdy igła błądzi. Trzeba sprawdzić kompas"
									+ " przy trzech starych znakach: na zachodnim skraju polany, przy suchym pniu"
									+ " na południowym wschodzie i przy niecce na wschodnim skraju polany.");
					break;
				case CROSS_MOSS:
				case CROSS_STUMP:
				case CROSS_WATER:
					int choice = clue == Clue.CROSS_MOSS ? 0 : clue == Clue.CROSS_STUMP ? 1 : 2;
					if (choice != CompassProgress.detail(p)) {
						CompassProgress.set(p, Stage.CROSSROADS, 0, 0);
						p.sendPrivateText(
								"Igła znów krąży bez ładu. Wracasz do początku drogi opisanej w notesie:"
										+ " mech, pień, woda.");
						return false;
					}
					if (choice < 2) {
						CompassProgress.set(p, Stage.CROSSROADS, choice + 1, 0);
						p.sendPrivateText(choice == 0
								? "Igła na chwilę się uspokaja. Teraz idź na południowy wschód, do suchego pnia przy starym szlaku."
								: "Igła przestaje drżeć. Ostatni znak znajdziesz przy kamiennej niecce na wschodnim skraju polany, na północny wschód stąd.");
						return true;
					}
					p.sendPrivateText(
							"Igła przestaje krążyć i wskazuje miejsce, w którym znalazłeś szczątki Miłorada."
									+ " Wśród gałęzi na chwilę rysuje się sylwetka wędrowca. Nie ma już dokąd iść."
									+ " Trzeba go pochować.");
					break;
				case BURIAL:
					p.sendPrivateText(
							"Przysypujesz szczątki ziemią i układasz kamienie w skromną mogiłę. W ciszy"
									+ " wspominasz imię Miłorada. Światła między drzewami gasną. Igła wreszcie"
									+ " wskazuje północ.");
					break;
				case CAIRN:
					p.sendPrivateText(
							"Tu spoczywa Miłorad, przewodnik, który szukał zagubionych. Na polanie panuje"
									+ " spokój.");
					return true;
				default:
					return false;
			}
			CompassProgress.next(p, s);
			return true;
		}
	}

}
