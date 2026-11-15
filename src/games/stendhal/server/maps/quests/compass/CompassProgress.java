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
			"W notesie zapisano: „Nie ufaj świeżym strzałkom ani światłom między drzewami. Z obozu"
					+ " wróć na zachód do omszałego kamienia. Stamtąd ślady prowadzą na północny zachód,"
					+ " do suchego pnia. Gdy naprawiona igła zacznie błądzić, sprawdź ją przy trzech"
					+ " starych znakach: najpierw przy mchu na zachodnim skraju polany, potem przy suchym"
					+ " pniu w jej południowo-wschodniej części, a na końcu przy kamiennej niecce przy"
					+ " wschodnim skraju polany”.";

	enum Stage {
		MISSING("Sprawdź ślady przy zachodnim wejściu na polanę we wschodnim Kościelisku.", 24, 44),
		BAG("Obejrzyj porzuconą torbę przy rozsypanych kamieniach, na południowy wschód od śladów.", 47, 63),
		MARKS("Odszukaj świeże nacięcia przy skraju lasu, na północny wschód od torby.", 70, 43),
		NOTES("Przeczytaj notatki w opuszczonym obozie na południowym wschodzie polany.", 84, 78),
		JUNCTIONS(
				"Odszukaj omszały kamień na zachód od opuszczonego obozu. Kieruj się"
						+ " wskazówkami z notesu, nie świeżymi strzałkami.",
				51,
				81),
		WOLVES("Pokonaj oba wilki przy rannym Dobromirze, potem powiedz mu: pomoc.", 68, 86),
		BANDAGE(
				"Zbierz zioła rosnące na zachód od Dobromira. Przynieś kawałek sukna i zapytaj go o"
						+ " opatrunek.",
				35,
				78),
		FIRE("Dołóż 3 polana do zimnego ogniska obok Dobromira.", 69, 85),
		MEAL(
				"Przygotuj Dobromirowi ciepłą strawę w kociołku. Potrzebujesz kawałka mięsa i buteleczki"
						+ " wody.",
				69,
				85),
		ESCORT(
				"Wyprowadź Dobromira na drogę przy wschodnim wyjściu z lasu. Potem przejdziecie"
						+ " przez zachodnią część Zakopanego i centrum, aż dotrzecie do Sabały na południu.",
				122,
				23),
		REMAINS(
				"Wróć na północny skraj polany we wschodnim Kościelisku. Obejrzyj szczątki dawnego"
						+ " przewodnika w trawie tuż na wschód od rozłożystego drzewa.",
				52,
				39),
		LETTER("Obejrzyj niedokończony list i uszkodzony kompas obok szczątków.", 54, 39),
		RECOGNITION("Wróć do Sabały w Zakopanem S. Powiedz: stary przewodnik.", 35, 46),
		CHARCOAL(
				"Zleć Smolisławowi na polanie nad Wisłą w Krakowie wypalenie porcji węgla drzewnego."
						+ " Potrzeba dwóch polan i pięciu monet. Odbierz węgiel z mielerza, potem zapytaj:"
						+ " węgiel do kompasu.",
				57,
				58),
		NEEDLE(
				"Przynieś Kowalowi Jackowi w kuźni w Kościelisku SE 1 sztabkę żelaza i 1 węgiel drzewny."
						+ " Powiedz: igła kompasu. Wróć po minucie i powtórz te słowa.",
				3,
				5),
		CASE("Pokonaj dwa błędne ogniki we wschodniej części polany w Kościelisku i zabierz obudowę kompasu.", 85, 49),
		ASSEMBLY("Złóż kompas przy dawnym ognisku Dobromira na polanie w Kościelisku.", 69, 85),
		CROSSROADS(
				"Sprawdź naprawiony kompas przy omszałym kamieniu na zachodnim skraju polany."
						+ " Potem odszukaj suchy pień na południowym wschodzie i nieckę na wschodnim"
						+ " skraju polany. Kolejność opisano w odnalezionym notesie.",
				31,
				51),
		BURIAL(
				"Pochowaj Miłorada tam, gdzie znalazłeś jego szczątki, na wschód od rozłożystego drzewa."
						+ " Usyp niewielką mogiłę i przykryj ją kamieniami.",
				52,
				39),
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
					? "Jacek pracuje nad nową igłą. Wróć za chwilę i zapytaj o igłę kompasu."
					: "Igła jest gotowa. Odbierz ją od Kowala Jacka, mówiąc: igła kompasu.";
		if (s == Stage.JUNCTIONS && detail(p) == 1)
			return "Przy omszałym kamieniu znalazł się kolejny ślad. Odszukaj suchy pień na północny zachód od"
								 + " kamienia.";
		if (s == Stage.ESCORT) return CompassEscort.objective(p);
		if (s == Stage.CROSSROADS && detail(p) == 1)
			return "Omszały kamień za tobą. Sprawdź teraz suchy pień przy starym szlaku w południowo-wschodniej części polany.";
		if (s == Stage.CROSSROADS && detail(p) == 2)
			return "Ostatni znak znajdziesz przy kamiennej niecce na wschodnim skraju polany, na północny wschód od suchego pnia.";
		if (s == Stage.WOLVES && detail(p) == 3)
			return "Wilki pokonane. Powiedz rannemu Dobromirowi: pomoc.";
		if (s == Stage.BANDAGE && detail(p) == 1)
			return "Zioła są zebrane. Przynieś Dobromirowi kawałek sukna i zapytaj o opatrunek.";
		if (s == Stage.CASE && detail(p) == 3)
			return "Ogniki zniknęły. Pod świerkiem została obudowa kompasu. Podejdź i zabierz ją.";
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
					"Opatrzyłem Dobromira, rozpaliłem ognisko i nakarmiłem go. Przeprowadziłem go przez"
							+ " las, zachodnią część Zakopanego i centrum, aż dotarliśmy bezpiecznie do Sabały."
							+ " Opowiedział mi o szczątkach dawnego przewodnika na północnym skraju leśnej"
							+ " polany, tuż na wschód od rozłożystego drzewa.");
		if (reached >= 13)
			result.add(
					"Sabała rozpoznał pismo swojego dawnego znajomego, Miłorada. Chce, żeby jego kompas znów"
							+ " służył wędrowcom. Smolisław wypali węgiel, a Jacek spróbuje wykuć nową igłę.");
		if (reached >= 16)
			result.add(
					"Węgiel wypalił Smolisław, a Jacek wykuł nową igłę. Odzyskałem obudowę" + " kompasu.");
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
