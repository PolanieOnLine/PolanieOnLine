/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.compass.CompassProgress.Stage;
import games.stendhal.server.maps.quests.trail.TrailSceneRegistry;

/** Personal, passable scenes on the existing Kościelisko clearing. */
final class CompassScenes {
  enum Clue {
    TRACKS("ślady zagubionego wędrowca", 1, 43, 74, Stage.MISSING),
    BAG("porzucona torba", 0, 49, 74, Stage.BAG),
    MARKS("fałszywe nacięcia", 1, 57, 74, Stage.MARKS),
    NOTES("notatki w opuszczonym obozie", 2, 65, 74, Stage.NOTES),
    MOSS("omszały kamień", 11, 60, 78, Stage.JUNCTIONS),
    STUMP("suchy pień", 11, 60, 84, Stage.JUNCTIONS),
    FALSE("świeża strzałka", 1, 66, 78, Stage.JUNCTIONS),
    HERBS("miejscowe zioła lecznicze", 3, 64, 85, Stage.BANDAGE),
    FIRE("zimne ognisko", 4, 69, 85, Stage.FIRE),
    MEAL("kociołek nad ogniskiem", 5, 69, 85, Stage.MEAL),
    STOP("pierwszy osłonięty postój", 12, 60, 86, Stage.ESCORT),
    SHELTER("bezpieczne schronienie", 12, 51, 86, Stage.ESCORT),
    REMAINS("szczątki dawnego przewodnika", 6, 82, 84, Stage.REMAINS),
    LETTER("uszkodzony kompas i niedokończony list", 7, 83, 84, Stage.LETTER),
    CASE("obudowa kompasu", 8, 86, 75, Stage.CASE),
    ASSEMBLY("miejsce składania kompasu", 9, 69, 85, Stage.ASSEMBLY),
    CROSS_MOSS("znak mchu", 11, 61, 70, Stage.CROSSROADS),
    CROSS_STUMP("znak suchego pnia", 11, 67, 70, Stage.CROSSROADS),
    CROSS_WATER("znak wody", 11, 73, 70, Stage.CROSSROADS),
    BURIAL("miejsce spoczynku Miłorada", 6, 82, 84, Stage.BURIAL),
    CAIRN("mogiła Miłorada", 10, 82, 84, Stage.RETURN);
    final String name;
    final int tile, x, y;
    final Stage stage;

    Clue(String name, int tile, int x, int y, Stage stage) {
      this.name = name;
      this.tile = tile;
      this.x = x;
      this.y = y;
      this.stage = stage;
    }
  }

  private CompassScenes() {}

  static void register(StendhalRPZone zone) {
    for (Clue clue : Clue.values())
      TrailSceneRegistry.prop(
          zone, "compass-" + clue.name(), p -> needed(p, clue), () -> new Prop(clue));
    SpeakerNPC wounded =
        new SpeakerNPC(CompassProgress.WANDERER) {
          @Override
          protected void createDialog() {
            addGreeting("Pomocy... Nie mogę stanąć na nodze. Powiedz: #pomoc albo #opatrunek.");
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
    TrailSceneRegistry.prop(
        zone, "compass-escort", p -> CompassProgress.stage(p) == Stage.ESCORT, Escort::new);
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
    if (c == Clue.SHELTER) return CompassProgress.detail(p) == 1;
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
      setDescription("Oto " + clue.name + ". Podejdź bliżej i użyj, aby zbadać to miejsce.");
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
          || !CompassProgress.FOREST.equals(getZone().getName())) return false;
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
              "Ślady schodzą ze szlaku ku wschodowi. Jeden but zostawił głęboki odcisk; wędrowiec"
                  + " dźwigał ciężką torbę.");
          break;
        case BAG:
          p.sendPrivateText(
              "Na skórze wyryto imię Dobromir. Torba jest rozcięta, a chleb rozsypany. Dalej na"
                  + " wschód widać świeże nacięcia.");
          break;
        case MARKS:
          p.sendPrivateText(
              "Strzałki wycięto niedawno i celują prosto w gęstwinę. Pod nimi zachował się stary"
                  + " znak przewodników. Lepiej sprawdzić opuszczony obóz dalej na wschodzie.");
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
                "Ta droga wraca pod fałszywą strzałkę. Wróć myślą do notatek: najpierw mech, potem"
                    + " suchy pień.");
            return false;
          }
          if (expected == 0) {
            CompassProgress.set(p, Stage.JUNCTIONS, 1, 0);
            p.sendPrivateText(
                "Przy mchu znajdujesz ślad buta Dobromira. Teraz poszukaj suchego pnia na południe"
                    + " stąd.");
            return true;
          }
          p.sendPrivateText(
              "Za suchym pniem słychać skowyt. Na południowym wschodzie wilki otoczyły rannego"
                  + " człowieka.");
          break;
        case HERBS:
          CompassProgress.set(p, Stage.BANDAGE, 1, 0);
          p.sendPrivateText(CompassProgress.objective(p));
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
              "Gotujesz mięso w wodzie. Po posiłku Dobromir odzyskuje siły. Idź powoli na zachód,"
                  + " przez pierwszy osłonięty postój do bezpiecznego schronienia.");
          break;
        case STOP:
        case SHELTER:
          return arrive(p, this, clue == Clue.STOP ? 0 : 1);
        case REMAINS:
          p.sendPrivateText(
              "Pod kamieniem leżą ludzkie kości i spróchniały pas przewodnika. Nie ma śladów walki;"
                  + " ktoś zabłądził i nie zdołał wrócić.");
          break;
        case LETTER:
          p.sendPrivateText(
              "W zapiskach Miłorad ostrzega przed błędnymi światłami. List do Sabały urywa się w"
                  + " połowie zdania. Zabezpieczasz list i resztki kompasu; obudowy brakuje. Pokaż"
                  + " Sabale znalezisko, mówiąc: stary przewodnik.");
          break;
        case CASE:
          if (CompassProgress.detail(p) != 3) {
            p.sendPrivateText("Najpierw pokonaj oba ogniki strzegące obudowy.");
            return false;
          }
          p.sendPrivateText(
              "Zabierasz obudowę spod korzeni. Masz już igłę od Jacka i resztki starego kompasu."
                  + " Złóż je przy dawnym ognisku Dobromira.");
          break;
        case ASSEMBLY:
          p.sendPrivateText(
              "Osadzasz igłę w obudowie i zamykasz szkło. Wskazówka wiruje, kiedy zbliżasz kompas"
                  + " do starych znaków. Sprawdź rozdroże na północ od obozu, według notatek"
                  + " Miłorada.");
          break;
        case CROSS_MOSS:
        case CROSS_STUMP:
        case CROSS_WATER:
          int choice = clue == Clue.CROSS_MOSS ? 0 : clue == Clue.CROSS_STUMP ? 1 : 2;
          if (choice != CompassProgress.detail(p)) {
            CompassProgress.set(p, Stage.CROSSROADS, 0, 0);
            p.sendPrivateText(
                "Wskazówka wiruje. To zła kolejność. Zacznij od nowa: mech, pień, woda.");
            return false;
          }
          if (choice < 2) {
            CompassProgress.set(p, Stage.CROSSROADS, choice + 1, 0);
            p.sendPrivateText("Wskazówka na chwilę uspokaja się. Wybierz następny znak z notatek.");
            return true;
          }
          p.sendPrivateText(
              "Kompas wskazuje miejsce spoczynku Miłorada. Błędne światła przygasają, gdy zwracasz"
                  + " się ku szczątkom. Pora godnie je pochować.");
          break;
        case BURIAL:
          p.sendPrivateText(
              "Przysypujesz szczątki ziemią i układasz kamienie w skromną mogiłę. W ciszy"
                  + " wspominasz imię Miłorada. Fałszywe światła znikają, a kompas odzyskuje stały"
                  + " kierunek.");
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

  static boolean arrive(Player p, Entity stop, int checkpoint) {
    if (CompassProgress.stage(p) != Stage.ESCORT || CompassProgress.detail(p) != checkpoint)
      return false;
    Escort escort = null;
    for (Entity e : p.getZone().getEntitiesOfClass(Escort.class))
      if (PlayerPrivateScene.isOwnedBy(e, p)) escort = (Escort) e;
    if (escort == null || !escort.nextTo(stop, 3)) {
      p.sendPrivateText("Zaczekaj na Dobromira. Samo dotarcie do postoju nie wystarczy.");
      return false;
    }
    if (checkpoint == 0) {
      CompassProgress.set(p, Stage.ESCORT, 1, 0);
      p.sendPrivateText("Dobromir odpoczywa chwilę. Bezpieczne schronienie jest dalej na zachód.");
    } else {
      CompassProgress.next(p, Stage.ESCORT);
      p.sendPrivateText(
          "Dobromir: Dalej dam już radę. Pod rozłożystym drzewem na wschodzie znalazłem szczątki z"
              + " kompasem. Może ten człowiek też poszedł za światłami... Sprawdź je, proszę.");
    }
    return true;
  }
  /** Registry-owned escort reconstructs from the last saved stop after relog. */
  static final class Escort extends NPC {
    private int turns;

    Escort() {
      setName("Dobromir pod opieką");
      setEntityClass("woodcutternpc");
      setResistance(0);
      setBaseSpeed(0.35);
      setDescription(
          "Ranny Dobromir idzie powoli. Czeka, jeśli przewodnik walczy lub oddali się za bardzo.");
      setPosition(68, 86);
    }

    private boolean positioned;

    @Override
    public void logic() {
      if (getZone() == null) return;
      Player owner = null;
      for (Player p : getZone().getPlayers()) if (PlayerPrivateScene.isOwnedBy(this, p)) owner = p;
      if (owner == null || owner.isDisconnected() || CompassProgress.stage(owner) != Stage.ESCORT) {
        getZone().remove(this);
        return;
      }
      if (!positioned) {
        positioned = true;
        if (CompassProgress.detail(owner) == 1) setPosition(60, 86);
      }
      if (owner.getAttackTarget() != null
          || !owner.getAttackingRPEntities().isEmpty()
          || !nextTo(owner, 10)) {
        clearPath();
        stop();
        return;
      }
      if (turns++ % 4 == 0) setMovement(owner, 1, 3, 10);
      super.logic();
    }
  }
}
