/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import games.stendhal.server.entity.player.Player;
import java.util.ArrayList;
import java.util.List;

/** All story objects are checkpoints, so losing a bag cannot strand this quest. */
final class CompassProgress {
  static final String SLOT = "wanderers_compass", FOREST = "0_koscielisko_e";
  static final String WANDERER = "Wędrowiec Dobromir";
  static final String NOTES =
      "W notatkach czytasz: nie ufaj świeżym nacięciom. Od obozu idź najpierw ku omszałemu"
          + " kamieniowi, potem ku suchemu pniowi. Przy starym rozdrożu wybierz kolejno mech, pień"
          + " i wodę. Błędne światła odciągają od szczątków przewodnika.";

  enum Stage {
    MISSING("Sprawdź ślady na południowej polanie we wschodnim Kościelisku.", 43, 74),
    BAG("Obejrzyj porzuconą torbę dalej na wschód od śladów.", 49, 74),
    MARKS("Sprawdź świeże nacięcia przy ścieżce na wschód od torby.", 57, 74),
    NOTES("Przeczytaj notatki w opuszczonym obozie na wschodzie polany.", 65, 74),
    JUNCTIONS(
        "Przy rozwidleniach od obozu wybierz mech, potem suchy pień; omiń świeże strzałki.",
        60,
        78),
    WOLVES("Pokonaj oba wilki przy rannym Dobromirze, potem powiedz mu: pomoc.", 68, 86),
    BANDAGE(
        "Zbierz miejscowe zioła na zachód od rannego. Przynieś 1 sukno i powiedz Dobromirowi:"
            + " opatrunek.",
        64,
        85),
    FIRE("Dołóż 3 polana do zimnego ogniska obok Dobromira.", 69, 85),
    MEAL("Ugotuj strawę przy ognisku: 1 mięso i 1 buteleczka wody.", 69, 85),
    ESCORT(
        "Odprowadź Dobromira do schronienia na zachodzie polany. Nie oddalaj się od niego; gdy"
            + " zostanie z tyłu, zaczekaj.",
        60,
        86),
    REMAINS(
        "Obejrzyj szczątki dawnego przewodnika pod rozłożystym drzewem na wschodnim skraju polany.",
        82,
        84),
    LETTER("Obejrzyj niedokończony list i uszkodzony kompas obok szczątków.", 83, 84),
    RECOGNITION("Wróć do Sabały w Zakopanem S. Powiedz: stary przewodnik.", 35, 46),
    CHARCOAL(
        "U Smolisława na polanie nad Wisłą w Krakowie wypal nową porcję węgla (2 polana i 5 monet),"
            + " odbierz ją i powiedz: węgiel do kompasu.",
        57,
        58),
    NEEDLE(
        "Przynieś Kowalowi Jackowi w kuźni w Kościelisku SE 1 sztabkę żelaza i 1 węgiel drzewny."
            + " Powiedz: igła kompasu. Wróć po minucie i powtórz te słowa.",
        3,
        5),
    CASE("Pokonaj dwa błędne ogniki we wschodnim Kościelisku i zabierz obudowę kompasu.", 86, 75),
    ASSEMBLY("Złóż kompas przy dawnym ognisku Dobromira na polanie w Kościelisku.", 69, 85),
    CROSSROADS(
        "Przy starym rozdrożu na północ od obozu wybierz kolejno mech, pień i wodę. Notatki w"
            + " dzienniku przypominają kolejność.",
        67,
        70),
    BURIAL(
        "Wróć do szczątków na wschodnim skraju polany i usyp niewielką mogiłę. Uciszy to fałszywe"
            + " światła.",
        82,
        84),
    RETURN(
        "Wróć do Sabały w Zakopanem S, zwolnij miejsce w plecaku i powiedz: kompas wędrowca.",
        35,
        46);
    final String objective;
    final int x, y;

    Stage(String objective, int x, int y) {
      this.objective = objective;
      this.x = x;
      this.y = y;
    }

    String zone() {
      if (this == RECOGNITION || this == RETURN) return "0_zakopane_s";
      if (this == CHARCOAL) return "0_krakow_wisla";
      if (this == NEEDLE) return "int_koscielisko_stones_room";
      return FOREST;
    }
  }

  private CompassProgress() {}

  static Stage stage(Player p) {
    if (p == null || !p.hasQuest(SLOT) || p.isQuestCompleted(SLOT)) return null;
    try {
      return Stage.valueOf(p.getQuest(SLOT, 0));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  static int detail(Player p) {
    try {
      return Integer.parseInt(p.getQuest(SLOT, 1));
    } catch (RuntimeException e) {
      return 0;
    }
  }

  static long time(Player p) {
    try {
      return Long.parseLong(p.getQuest(SLOT, 2));
    } catch (RuntimeException e) {
      return 0;
    }
  }

  static void set(Player p, Stage stage, int detail, long time) {
    p.setQuest(SLOT, stage.name() + ";" + detail + ";" + time);
  }

  static void enter(Player p, Stage stage) {
    int produced = stage == Stage.CHARCOAL ? p.getQuantityOfProducedItems("węgiel drzewny") : 0;
    set(p, stage, produced, stage == Stage.CHARCOAL ? System.currentTimeMillis() : 0);
  }

  static void next(Player p, Stage expected) {
    if (stage(p) == expected) {
      enter(p, Stage.values()[expected.ordinal() + 1]);
      p.sendPrivateText(objective(p));
    }
  }

  static String objective(Player p) {
    Stage s = stage(p);
    if (s == null)
      return p.isQuestCompleted(SLOT)
          ? "Dobromir jest bezpieczny. Kompas wędrowca został odblokowany na stałe."
          : "Zapytaj Sabałę w Zakopanem S: na manowcach.";
    if (s == Stage.NEEDLE && time(p) > 0)
      return System.currentTimeMillis() < time(p)
          ? "Jacek hartuje igłę. Wróć za "
              + Math.max(1, (time(p) - System.currentTimeMillis() + 999) / 1000)
              + " s i powiedz: igła kompasu."
          : "Igła jest gotowa. Odbierz ją od Kowala Jacka, mówiąc: igła kompasu.";
    if (s == Stage.WOLVES && detail(p) == 3)
      return "Wilki pokonane. Powiedz rannemu Dobromirowi: pomoc.";
    if (s == Stage.BANDAGE && detail(p) == 1)
      return "Masz miejscowe zioła. Przynieś 1 sukno i powiedz Dobromirowi: opatrunek.";
    if (s == Stage.CASE && detail(p) == 3)
      return "Ogniki pokonane. Sprawdź obudowę kompasu przy ich dawnym stanowisku.";
    return s.objective;
  }

  static List<String> history(Player p) {
    List<String> result = new ArrayList<>();
    if (!p.hasQuest(SLOT)) return result;
    result.add(
        "Sabała poprosił mnie o odnalezienie wędrowca, który zszedł ze szlaku w Kościelisku.");
    Stage s = stage(p);
    int reached = p.isQuestCompleted(SLOT) ? 20 : s == null ? 0 : s.ordinal();
    if (reached >= 4) result.add(NOTES);
    if (reached >= 10)
      result.add(
          "Opatrzyłem Dobromira, rozpaliłem ognisko i nakarmiłem go. Doprowadziłem go do"
              + " osłoniętego postoju. Wskazał szczątki dawnego przewodnika pod rozłożystym"
              + " drzewem.");
    if (reached >= 13)
      result.add(
          "Sabała rozpoznał przewodnika Miłorada po niedokończonym liście. Igła jego kompasu"
              + " została zniszczona. Obudowa zaginęła w miejscu, gdzie pojawiają się błędne"
              + " ogniki.");
    if (reached >= 16)
      result.add(
          "Węgiel wypalił Smolisław, a Jacek wykuł i zahartował nową igłę. Odzyskałem obudowę"
              + " kompasu.");
    if (reached >= 19)
      result.add(
          "Ułożyłem kamienie nad szczątkami Miłorada. Błędne światła zgasły, a wskazówka uspokoiła"
              + " się.");
    result.add("Obecny cel: " + objective(p));
    result.add(
        "Nagroda: kompas wędrowca przypisany do właściciela, stałe wskazywanie celów zadań, 5000 PD"
            + " i 100 monet.");
    return result;
  }
}
