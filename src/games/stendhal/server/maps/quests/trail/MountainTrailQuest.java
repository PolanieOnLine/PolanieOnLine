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
 * Six linked investigations, rescues and encounters across the three existing
 * regions.
 */
public final class MountainTrailQuest extends TrailQuest {
	public MountainTrailQuest(Step step) {
		super(step);
		if (!TrailQuestLine.MOUNTAIN_CHAPTER.equals(step.chapter))
			throw new IllegalArgumentException("Not a mountain quest");
	}

	@Override
	protected void addDialogs() {
		switch (step) {
		case MOUNTAIN_MINERS:
			offer("Borzym i Oskar sprawdzali boczny tunel na północny wschód stąd. Poszukaj ich tam, przy zakręcie. "
					+ "Borzym nosi stary roboczy kaftan. Jeśli są ranni, nie każ im iść samym. Przyślę po nich ludzi.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					request(player, speaker,
							"Borzym i Oskar powinni już tu być. Poszli tylko obejrzeć boczny tunel, nawet kilofów nie zabrali. "
									+ "Po tym, co odkryliśmy z duergarami, nie chcę czekać do jutra. Poszukasz ich?");
			}, "zaginieni górnicy", "zaginieni gornicy");
			reply(MountainTrailProgress.MINER, (player, sentence, speaker) -> {
				if (MountainTrailProgress.active(player, step)
						&& player.isQuestInState(step.slot, "search")) {
					player.setQuest(step.slot, "report");
					speaker.say(
							"Barbarus cię przysyła? Oskar wyrwał się pierwszy, ja skręciłem nogę przy ucieczce. "
									+ "Dopadli nas duergarowie. Kazali nosić skrzynie, a jednego wieczoru przyszedł do nich człowiek. "
									+ "Mówili o Kościelisku i o kupcach, którzy za dużo nagadali. Powiedz Barbarusowi, gdzie jestem. Dalej sam nie zajdę.");
				} else
					remind(player, speaker);
			}, "górnicy", "gornicy", "górników", "gornikow", "towarzysze",
					"pomoc");
			break;
		case MOUNTAIN_THREATS:
			offer("Odwiedź Staska w sklepie obok mostu i Jagnę w karczmie na północnym wschodzie miasta. "
					+ "Niech powiedzą ci wszystko, nawet jeśli uznali to za zwykłą kłótnię. Potem wróć, zdecydujemy co dalej.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					request(player, speaker,
							"Stasek dostał pogróżki za to, że poznał swoje worki z węglem. "
									+ "Jagna też widziała kogoś podejrzanego, ale nie chciała robić zamieszania. "
									+ "Popytasz ich? Może to ci sami ludzie, o których mówił górnik.");
			}, "pogróżki", "pogrozki");
			witness("Stasek", 1,
					"Przyszedł ten, który kiedyś pomagał ładować wóz Radana. Mówił, że za długi język drogo się płaci. "
							+ "Poznałem go od razu, choć zarósł i brudny był jak nieboskie stworzenie. Nie oddam im sklepu ze strachu.");
			witness("Jagna", 2,
					"Byli tu we dwóch. Jeden jadł, drugi stał przy drzwiach i wypytywał, kiedy Brzezdom wraca z polany. "
							+ "Nie powiedziałam im. Po sprawie z woźnicą wolę już uważać na takich gości.");
			break;
		case MOUNTAIN_MEETING:
			offer("Idź na wschód ode mnie, w stronę polany za drzewami. Miłosz jest przy skrzyni, "
					+ "a tamci nie spuszczają z niego oka. Rozpraw się z oboma, zanim spróbujesz przeciąć #więzy.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					request(player, speaker,
							"Widziałem ich. Człowiek i duergar pokłócili się przy skrzyni, a potem przywlekli woźnicę. "
									+ "To Miłosz, znam go z drogi. Skrępowali mu ręce. Sam ich nie pokonam. Pomożesz mu?");
			}, "obcy na polanie", "obcych na polanie", "obcy");
			reply(MountainTrailProgress.DRIVER, (player, sentence, speaker) -> {
				if (MountainTrailProgress.active(player, step)
						&& player.isQuestInState(step.slot, "rescue")) {
					player.setQuest(step.slot, "report");
					speaker.say(
							"Dzięki... Jeszcze chwila i nie czułbym palców. Miałem wieźć stare żelastwo. "
									+ "Ale kiedy płótno się zsunęło, zobaczyłem ostrza i groty. Powiedziałem, że tego nie ruszę, i tak się skończyło. "
									+ "Kazali jechać do północnej części polany. Powiedz Brzezdomowi, że zaczekam tutaj na jego ludzi.");
				} else if (MountainTrailProgress.active(player, step)
						&& player.isQuestInState(step.slot, 0, "guards")) {
					speaker.say(
							"Najpierw zajmij się tymi dwoma. Jeśli mnie teraz rozwiążesz, złapią nas obu.");
				} else
					remind(player, speaker);
			}, "więzy", "wiezy", "uwolnij", "woźnica", "woznica");
			break;
		case MOUNTAIN_CARGO:
			offer("Obejrzyj skrzynię obok Miłosza we wschodnim Kościelisku. Jeśli znajdziesz znak na ostrzach, "
					+ "przerysuj go dla mnie. Miałem wielu dostawców, może poznam, od kogo pochodzi ta #'ukryta broń'.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (MountainTrailProgress.active(player, step)
						&& player.isQuestInState(step.slot, "stasek")) {
					player.setQuest(step.slot, "briefing");
					speaker.say(
							"Znam to nacięcie. Radan skupował takie ostrza jeszcze przed pierwszym zaginionym transportem. "
									+ "Więc od początku chodziło o broń... Nie będę udawał, że to tylko sprawa mojego węgla. "
									+ "Powiedz Drilenunowi w koszarach w Ados o #'transporcie broni'. On będzie wiedział, co z tym zrobić.");
				} else
					request(player, speaker,
							"Miłosz miał wieźć broń? Nic dziwnego, że nie chcieli puścić go wolno. "
									+ "Mogę pomóc ustalić, skąd ją wzięli. Sprawdzisz dla mnie zawartość skrzyni?");
			}, "ukryta broń", "ukryta bron", "ukrytą broń");
			reply("Lieutenant Drilenun", (player, sentence, speaker) -> {
				if (MountainTrailProgress.active(player, step)
						&& player.isQuestInState(step.slot, "briefing"))
					complete(player, speaker, "briefing");
				else
					remind(player, speaker);
			}, "transport broni", "transporcie broni");
			break;
		case MOUNTAIN_BATTLE:
			offer("Idź na północ od miejsca, gdzie trzymali Miłosza. Dwaj ludzie stoją przy skrzyni, "
					+ "a dowódca kawałek dalej na północny zachód. Uważaj na jego ciężką zbroję. Ja zostanę przy drodze, żeby nikt nie wpadł na nich przypadkiem.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (player.isQuestInState(step.slot, "report"))
					complete(player, speaker, "report");
				else
					request(player, speaker,
							"Drilenun uprzedził mnie, że to oddział do napadania na transporty. "
									+ "Wypatrzyłem ich dowódcę. Jeśli ruszą z tą bronią, nie dogonimy ich w lesie. Zatrzymasz ich tutaj?");
			}, "zbiórka oddziału", "zbiorka oddzialu", "zbiórkę oddziału");
			break;
		case MOUNTAIN_RETURN:
			offer("Chcę znać wszystko z pierwszej ręki. Powiedz mi, jak skończyła się walka, a potem zajrzyj do Drilenuna i Barbarusa. "
					+ "Porucznik musi wiedzieć, co zostało z oddziału, a Barbarus wciąż martwi się o swoich ludzi.");
			reply(step.npc, (player, sentence, speaker) -> {
				if (player.isQuestInState(step.slot, "ready")) {
					reward(player, speaker);
					return;
				}
				if (MountainTrailProgress.active(player, step)) {
					report(player, speaker, 1);
					if (player.isQuestInState(step.slot, "ready"))
						reward(player, speaker);
				} else
					request(player, speaker,
							"Brzezdom przysłał już słowo, że walka skończona. Dobrze cię widzieć w całości. "
									+ "Opowiesz nam, co stało się na polanie? Drilenun i Barbarus też czekają na wiadomość.");
			}, "wieści z lasu", "wiesci z lasu");
			reply("Lieutenant Drilenun", (player, sentence, speaker) -> {
				if (MountainTrailProgress.active(player, step)
						&& player.isQuestInState(step.slot, 0, "reports"))
					report(player, speaker, 2);
				else
					remind(player, speaker);
			}, "wieści z lasu", "wiesci z lasu");
			reply("Barbarus", (player, sentence, speaker) -> {
				if (MountainTrailProgress.active(player, step)
						&& player.isQuestInState(step.slot, 0, "reports"))
					report(player, speaker, 4);
				else
					remind(player, speaker);
			}, "wieści z lasu", "wiesci z lasu");
			break;
		default:
			throw new IllegalArgumentException("Not a mountain quest");
		}
	}

	private void witness(String name, int bit, String statement) {
		reply(name, (player, sentence, speaker) -> {
			if (!MountainTrailProgress.active(player, step)
					|| !player.isQuestInState(step.slot, 0, "witnesses")) {
				remind(player, speaker);
				return;
			}
			MountainTrailProgress.mark(player, step, bit, "witnesses", "report",
					3);
			speaker.say(statement
					+ " Przekaż Wojtkowi, co tu usłyszysz. Wolę, żeby wiedział, z kim ma do czynienia.");
		}, "pogróżki", "pogrozki");
	}

	private void report(Player player, EventRaiser speaker, int bit) {
		int known = MountainTrailProgress.mark(player, step, bit, "reports",
				"ready", 7);
		String text = bit == 1
				? "Dobrze się spisujesz. Przestaną wreszcie straszyć ludzi i wywozić nam zapasy. "
						+ "Pamiętaj jeszcze o Drilenunie i Barbarusie."
				: bit == 2
						? "Dziękuję. Dopilnuję reszty wspólników Radana. Jego dostawy i ten oddział nie będą już zagrażać drodze."
						: "Oskar wrócił cały, a po Borzyma idą moi ludzie. Nie mógł już chodzić po tej nodze. "
								+ "Uratowałeś mu życie. Dziękuję.";
		if (known == 7 && bit != 1)
			text += " Wojtek powinien już znać całą historię. Wróć do niego do ratusza w centrum Zakopanego. Czeka z podziękowaniem.";
		speaker.say(text);
	}

	private void reward(Player player, EventRaiser speaker) {
		if (!MountainTrailProgress.active(player, step)
				|| !player.isQuestInState(step.slot, "ready")) {
			remind(player, speaker);
			return;
		}
		Item item = SingletonRepository.getEntityManager().getItem(
				"cuha góralska",
				ItemCreationContext.builder(
						ItemCreationContext.Source.QUEST).withQuestRarity(
								ItemRarity.RARE).generateAffixes(true).build());
		item.setBoundTo(player.getName());
		if (!player.equip("bag", item)) {
			speaker.say(
					"Mam dla ciebie porządną cuhę, ale nie zmieści się w plecaku. Zostaw gdzieś część rzeczy. Poczekam z nią na ciebie.");
			return;
		}
		complete(player, speaker, "ready");
	}

	@Override
	protected void remind(Player player, EventRaiser speaker) {
		if (player.isQuestCompleted(step.slot)) {
			speaker.say(
					"Dobrze, że udało się to zakończyć. Ludzie wreszcie odetchną.");
			return;
		}
		if (!player.isQuestCompleted(step.previousSlot())) {
			speaker.say(
					"Najpierw trzeba wyjaśnić to, co już wiemy o ludziach Radana. Nie chcę wyciągać wniosków z samych pogłosek.");
			return;
		}
		if (!player.hasQuest(step.slot)) {
			if (player.getLevel() < step.minimumLevel)
				speaker.say(
						"Nabierz jeszcze doświadczenia w drodze. Ci ludzie są niebezpieczniejsi od zwykłych złodziei.");
			else
				speaker.say("Tę sprawę prowadzi " + step.npc
						+ ". Warto najpierw z nim porozmawiać, żeby wiedział, co zamierzasz.");
			return;
		}
		switch (step) {
		case MOUNTAIN_MINERS:
			speaker.say(MountainTrailProgress.MINER.equals(TrailSceneRegistry.speakerName(speaker.getEntity()))
					? "Zaczekam tu. Powiedz Barbarusowi, gdzie mnie znaleźć. Może przyśle kogoś, kto pomoże mi wrócić."
					: "Borzym powinien być przy zakręcie na północny wschód stąd. Nie zostawiaj go tam samego, jeśli jest ranny.");
			break;
		case MOUNTAIN_THREATS:
			speaker.say("Gazda Wojtek".equals(TrailSceneRegistry.speakerName(speaker.getEntity()))
					? "Stasek i Jagna widzieli tych ludzi z bliska. Dowiedz się, co pamiętają. Muszę wiedzieć, kto zastrasza mieszkańców."
					: "Wojtek musi usłyszeć, co tu się dzieje. Przekaż mu wszystko, czego uda ci się dowiedzieć.");
			break;
		case MOUNTAIN_MEETING:
			speaker.say(MountainTrailProgress.DRIVER.equals(TrailSceneRegistry.speakerName(speaker.getEntity()))
					? "Jeszcze nie ruszę w drogę. Brzezdom wie, gdzie jestem? Zaczekam, aż przyjdzie pomoc."
					: "Miłosz jest przy skrzyni na wschód stąd. Nie zostawiaj go tym dwóm. Sam nie da rady się uwolnić.");
			break;
		case MOUNTAIN_CARGO:
			speaker.say(player.isQuestInState(step.slot, "briefing")
					? "Drilenun w koszarach w Ados musi usłyszeć o tym transporcie broni. To już nie jest zwykła kradzież."
					: "Stasek".equals(TrailSceneRegistry.speakerName(speaker.getEntity()))
							? "Obejrzyj ostrza w skrzyni obok Miłosza. Jeśli mają znak dostawcy, spróbuję go rozpoznać."
							: "Ostrza w skrzyni obok Miłosza powinny mieć znak dostawcy. Stasek handluje tu od lat, może go pozna.");
			break;
		case MOUNTAIN_BATTLE:
			speaker.say(
					"Dowódca jest ze swoimi ludźmi w północnej części polany, na północ od Miłosza. Musimy zatrzymać całą trójkę, zanim wyruszą.");
			break;
		case MOUNTAIN_RETURN:
			speaker.say("Gazda Wojtek".equals(TrailSceneRegistry.speakerName(speaker.getEntity()))
					? "Ja już znam wynik wyprawy. Dopilnuj, żeby Drilenun i Barbarus też usłyszeli wieści."
					: "Wojtek czeka na ciebie w ratuszu w centrum Zakopanego. Niech usłyszy, jak skończyła się wyprawa.");
			break;
		default:
			throw new IllegalArgumentException("Not a mountain quest");
		}
	}

	@Override
	protected String completedText() {
		switch (step) {
		case MOUNTAIN_MINERS:
			return "Żyją... To najważniejsze. Po Borzyma poślę ludzi z noszami. "
					+ "A ty ostrzeż Wojtka w Zakopanem. Niech zapyta kupców o #pogróżki, zanim ktoś jeszcze ucierpi.";
		case MOUNTAIN_THREATS:
			return "Zostawię ludzi przy sklepie i karczmie. Stasek i Jagna nie będą sami. "
					+ "Skoro wypytywali o Brzezdoma, pewnie chcieli przejść przez jego polanę niezauważeni. Zapytaj go o #'obcych na polanie'.";
		case MOUNTAIN_MEETING:
			return "Miłosz wolny? Dobrze, przypilnuję, żeby go znów nie dopadli. "
					+ "Zawartość skrzyni trzeba pokazać komuś, kto zna się na handlu bronią. Stasek powinien poznać tę #'ukrytą broń'.";
		case MOUNTAIN_CARGO:
			return "Broń, prowiant i ludzie w lesie. Przygotowują napady na transporty, nie na sam ratusz. "
					+ "Jeśli odetną drogę, ucierpią oba miasta. Uprzedzę Wojtka. Brzezdom obserwuje polanę, "
					+ "zapytaj go o #'zbiórkę oddziału'. Zatrzymajmy ich, zanim ruszą.";
		case MOUNTAIN_BATTLE:
			return "Słyszałem walkę. Dobrze, że wracasz o własnych siłach. Zostanę przy polanie do przybycia straży. "
					+ "Zanieś Wojtkowi #'wieści z lasu'. Drilenun też musi je usłyszeć, a Barbarus wiedzieć, co z jego górnikami.";
		case MOUNTAIN_RETURN:
			return "Teraz wszystko jasne. Radan siedzi, jego wspólnicy stracili broń i dowódcę, a górnicy wrócą do domów. "
					+ "Dziękuję ci w imieniu mieszkańców. Weź tę cuhę i 300 monet. Zasługujesz na coś porządnego na dalszą drogę. "
					+ "Jeśli ruszysz kiedyś do Ados, zajrzyj do Juliusa przy głównym wejściu. Drilenun polecił cię tamtejszej straży.";
		default:
			throw new IllegalArgumentException("Not a mountain quest");
		}
	}
}
