/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * The apparent raiders are fleeing a defender that no longer recognises its own
 * side.
 */
public final class AdosKeepQuest extends TrailQuest {
	public AdosKeepQuest(Step step) {
		super(step);
		if (!TrailQuestLine.KEEP_CHAPTER.equals(step.chapter))
			throw new IllegalArgumentException("Not a keep quest");
	}

	@Override
	protected void addDialogs() {
		switch (step) {
		case KEEP_TRAIL:
			offer("Warownia jest w górach na północny zachód od Ados. Schody znajdziesz na południowym skraju gór, "
					+ "przy zachodniej stronie. Obejrzyj ślady pod wejściem i kamień porzucony kawałek na północny wschód od nich. "
					+ "Wróć, gdy zrozumiesz, co tam zaszło.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (AdosKeepProgress.active(player, step)
						&& player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					request(player, speaker,
							"Drilenun pisał, że można na tobie polegać. Dobrze się składa. "
									+ "Widziano orków schodzących od starej warowni. Kupcy mówią już o napaści, ale nikt jeszcze nie został napadnięty. "
									+ "Sprawdzisz ślady przy wejściu, zanim poślę tam straż?");
			}, "stara warownia", "starej warowni", "warownia");
			break;
		case KEEP_WITNESSES:
			offer("Zogfang przebywa niedaleko wejścia, w południowo-zachodniej części pierwszego piętra. "
					+ "Hagnurk ma sklep po przeciwnej stronie, na północnym wschodzie. Porozmawiaj z oboma o #'hałasach w warowni'. "
					+ "Nie musisz schodzić niżej. Uważaj na pozostałych mieszkańców, nie wszyscy lubią gości.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (AdosKeepProgress.active(player, step)
						&& player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					request(player, speaker,
							"Porzucony bagaż i ciepły kamień... To nie wygląda na przygotowania do najazdu. "
									+ "Znam dwóch mieszkańców warowni, z którymi można się dogadać. Posłuchasz ich, zanim kogokolwiek oskarżymy?");
			}, "uciekinierzy", "uciekinierach");
			witness("Zogfang", 1,
					"Ktoś powiedział, że idziemy na Ados? Bzdura. W nocy coś roztrzaskało beczkę i ruszyło na naszych ludzi. "
							+ "Kamienna postać, wielka jak szafa. Myślałem, że krasnale ją nasłały. Najpierw trzy ciężkie kroki, potem łomot.");
			witness("Hagnurk", 2,
					"Nie zostawię całego towaru tylko dlatego, że komuś zachciało się budzić stare posągi. "
							+ "Ale tragarze boją się tu wracać. W południowych salach są kamienne płyty z takimi znakami jak na twoim rysunku. "
							+ "Jedna wykruszyła się niedawno. Może to wtedy zaczęły się te hałasy.");
			break;
		case KEEP_INSCRIPTIONS:
			offer("Poszukaj dwóch starych płyt w południowych salach pierwszego piętra. Jedna leży w zachodniej sali, "
					+ "niedaleko Zogfanga, druga dalej na wschód. Przepisz wszystkie zachowane litery. "
					+ "W takich rozkazach jedno brakujące słowo potrafi zmienić bardzo wiele.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (AdosKeepProgress.active(player, step)
						&& player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					request(player, speaker,
							"Te znaki... Tak, widziałem podobne w opisie dawnych strażników. "
									+ "Kamień miał strzec załogi nawet po śmierci dowódcy. Tylko z tego rysunku nie odczytam całego polecenia. "
									+ "Przyniesiesz mi odpis pozostałych napisów?");
			}, "stare napisy", "starych napisach", "napisy warowni");
			break;
		case KEEP_GUARDIAN:
			offer("Stoi w środkowej sali na południu pierwszego piętra, na wschód ode mnie. "
					+ "Gdy podejdziesz, uważaj na kamienne ramiona. To nie jest zwykły posąg, a pancerz ma gruby. "
					+ "Ja zostanę przy przejściu, żeby nasi ludzie tam nie weszli.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (AdosKeepProgress.active(player, step)
						&& player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					request(player, speaker,
							"Więc to dawny strażnik? I nikt z nas nie jest dla niego swój? "
									+ "Nie chcę stracić następnych ludzi przez polecenie jakiegoś zmarłego dowódcy. "
									+ "Jeśli bibliotekarz nie umie go zatrzymać, zostaje walka. Podejmiesz się jej?");
			}, "kamienny strażnik", "kamienny straznik", "kamiennym strażniku");
			break;
		case KEEP_RETURN:
			offer("Zanim uspokoję kupców, chcę mieć pewność, że w środku rzeczywiście ucichło. "
					+ "Zajrzyj do Hagnurka i zapytaj, czy droga jest już bezpieczna. Potem wróć do mnie. "
					+ "Straż musi usłyszeć, co naprawdę działo się w warowni.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (AdosKeepProgress.active(player, step)
						&& player.isQuestInState(step.slot, "ready"))
					reward(player, speaker);
				else
					request(player, speaker,
							"Zogfang przysłał wieść o twojej walce. Dobrze cię widzieć. "
									+ "Zostało nam sprawdzić, czy mieszkańcy mogą znów wrócić do swoich zajęć. Odwiedzisz jeszcze Hagnurka?");
			}, "droga do ados", "drogi do ados");
			reply("Hagnurk", (player, sentence, speaker) -> {
				if (!AdosKeepProgress.active(player, step)
						|| !player.isQuestInState(step.slot, "merchant")) {
					remind(player, speaker);
					return;
				}
				player.setQuest(step.slot, "ready");
				speaker.say(
						"Od twojej walki nie słyszałem już tego łomotu. Dzisiaj tragarze przynieśli towar i wrócili bez kłopotów. "
								+ "Powiedz Juliusowi, że nie zamierzamy na nikogo napadać. Chcemy po prostu handlować. I dziękuję za pomoc.");
			}, "bezpieczna droga", "bezpiecznej drodze");
			break;
		default:
			throw new IllegalArgumentException("Not a keep quest");
		}
	}

	private void witness(String name, int bit, String text) {
		reply(name, (player, sentence, speaker) -> {
			boolean changed = AdosKeepProgress.mark(player, step, bit,
					"witnesses");
			if (changed)
				speaker.say(text);
			else if (AdosKeepProgress.active(player, step))
				speaker.say(text
						+ " Przekaż to straży Ados. Niech wiedzą, dlaczego ludzie stąd odchodzili.");
			else
				remind(player, speaker);
		}, "hałasy w warowni", "halasy w warowni", "hałasach w warowni");
	}

	private void reward(Player player, EventRaiser speaker) {
		if (!AdosKeepProgress.active(player, step)
				|| !player.isQuestInState(step.slot, "ready")) {
			remind(player, speaker);
			return;
		}
		Item item = SingletonRepository.getEntityManager().getItem(
				"hełm chaosu",
				ItemCreationContext.builder(
						ItemCreationContext.Source.QUEST).withQuestRarity(
								ItemRarity.RARE).generateAffixes(true).build());
		item.setBoundTo(player.getName());
		if (!player.equip("bag", item)) {
			speaker.say(
					"Mam dla ciebie hełm, ale plecak masz pełny. Zostaw gdzieś część rzeczy, a ja go dla ciebie przechowam.");
			return;
		}
		complete(player, speaker, "ready");
	}

	@Override
	protected void remind(Player player, EventRaiser speaker) {
		if (player.isQuestCompleted(step.slot)) {
			speaker.say(
					"Dobrze, że ta sprawa jest już za nami. Dziękuję za pomoc.");
			return;
		}
		if (!player.isQuestCompleted(step.previousSlot())) {
			speaker.say(
					"Wyjaśnijmy najpierw to, co już wiemy. Nie chcę podejmować decyzji na podstawie pogłosek.");
			return;
		}
		if (!player.hasQuest(step.slot)) {
			speaker.say(player.getLevel() < step.minimumLevel
					? "Nabierz jeszcze doświadczenia w drodze. W warowni łatwo trafić na kogoś znacznie silniejszego."
					: "Tę sprawę prowadzi " + step.npc
							+ ". Porozmawiaj z nim, żeby wiedział o twojej pomocy.");
			return;
		}
		switch (step) {
		case KEEP_TRAIL:
			speaker.say(
					"Przy schodach na południowym skraju gór zostały ślady i odłamek płyty. Przyjrzyj się obu, zanim uznamy to za przygotowania do napadu.");
			break;
		case KEEP_WITNESSES:
			speaker.say("Julius".equals(TrailSceneRegistry.speakerName(speaker.getEntity()))
					? "Zogfang i Hagnurk widzieli więcej niż moi strażnicy. Wysłuchaj obu, a potem wróć."
					: "Przekaż Juliusowi, co tu usłyszysz. Chcę, żeby straż Ados znała prawdę.");
			break;
		case KEEP_INSCRIPTIONS:
			speaker.say(
					"Potrzebuję obu napisów z południowych sal pierwszego piętra. Same urwane znaki nie wystarczą, żeby odczytać polecenie strażnika.");
			break;
		case KEEP_GUARDIAN:
			speaker.say(
					"Kamienna postać stoi w środkowej sali na południu, na wschód ode mnie. Nie zostawimy jej tam, żeby znów ruszyła na naszych ludzi.");
			break;
		case KEEP_RETURN:
			speaker.say("Julius".equals(TrailSceneRegistry.speakerName(speaker.getEntity()))
					? "Hagnurk powinien wiedzieć, czy można już spokojnie przewozić towar. Chcę usłyszeć, co powie."
					: "Powiedz Juliusowi, że w warowni jest już spokojniej. Niech przestanie wypatrywać najazdu na miasto.");
			break;
		default:
			throw new IllegalArgumentException("Not a keep quest");
		}
	}

	@Override
	protected String completedText() {
		switch (step) {
		case KEEP_TRAIL:
			return "Czyli zostawiali bagaże, żeby szybciej odejść. Dobrze, że nie posłałem tam od razu żołnierzy. "
					+ "Trzeba porozmawiać o #uciekinierach z kimś, kto widział, co działo się w środku.";
		case KEEP_WITNESSES:
			return "Kamienna postać, rozbita płyta i hałasy... To nie wygląda na spór między orkami a krasnalami. "
					+ "Wikipedian w bibliotece Ados zna dawne dzieje. Pokaż mu rysunek i zapytaj o #'stare napisy'.";
		case KEEP_INSCRIPTIONS:
			return "Tu jest sedno. Strażnik ma chronić dawną załogę, ale część znaków, po których ją rozpoznawał, odpadła. "
					+ "Dla niego każdy mieszkaniec jest teraz intruzem. Nie umiem naprawić tego mechanizmu, a odtwarzanie rozkazu na ślepo byłoby niebezpieczne. "
					+ "Powiedz Zogfangowi o #'kamiennym strażniku'. Trzeba go zatrzymać, zanim ktoś zginie.";
		case KEEP_GUARDIAN:
			return "Słyszałem, jak runął. Nasi ludzie mogą odetchnąć. "
					+ "Powiedz Juliusowi, że nie szykowaliśmy najazdu. To kamienny strażnik wypędzał nas z domu. Chcemy znów korzystać z #'drogi do Ados'.";
		case KEEP_RETURN:
			return "Dziękuję. Gdybyśmy zaufali samym pogłoskom, moglibyśmy zacząć zupełnie niepotrzebną walkę. "
					+ "Przekażę straży, czego się dowiedzieliśmy. Weź ten hełm i 650 monet. Dobrze mieć przy sobie kogoś, kto najpierw sprawdza, a dopiero potem sięga po broń. "
					+ "Jeśli wrócisz pod Kraków, zajrzyj do Wagabundusa na południowym trakcie. Podróżni mówią o poległych wartownikach, którzy znów stanęli przy drodze.";
		default:
			throw new IllegalArgumentException("Not a keep quest");
		}
	}
}
