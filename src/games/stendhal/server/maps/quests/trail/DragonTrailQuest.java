/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Uncover the egg hunters' provocation before a second wave of retaliation. */
public final class DragonTrailQuest extends TrailQuest {
	public DragonTrailQuest(Step step) {
		super(step);
		if (!TrailQuestLine.DRAGON_CHAPTER.equals(step.chapter))
			throw new IllegalArgumentException("Not a dragon story quest");
	}

	@Override
	protected void addDialogs() {
		switch (step) {
		case EMBER_CAMP:
			offer("Obóz był kawałek na wschód stąd. Obejrzyj nadpaloną skrzynię i palenisko, potem porozmawiaj z Radowitem. "
					+ "Powiedz mu o #'spalonym obozie'. Ocalał z napadu, ale ledwo trzyma się na nogach. Wróć, gdy dowiesz się, co tam zaszło.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n))
					return;
				request(p, n,
						"Słyszałem o górnikach z Wieliczki. Dobrze, że ktoś potrafi jeszcze ratować ludzi, zamiast tylko liczyć trupy. "
								+ "U nas został #'spalony obóz' i kilku rannych. Zbieram ludzi, żeby odgonić smoki od traktu, ale najpierw chcę wiedzieć, co je tu sprowadziło. Rozejrzysz się?");
			}, "spalony obóz", "spalony oboz", "spalonym obozie");
			reply(DragonTrailProgress.SURVIVOR, (p, s, n) -> {
				if (DragonTrailProgress.advance(p, step, "witness", "report"))
					n.say("Przyjechał wóz. Woźnica zrzucił skrzynię przy ogniu i nawet nie zapytał o zapłatę za nocleg. "
							+ "Potem zobaczyliśmy smoki. On już wtedy biegł. Mówił, że wiezie garnki, ale ja żadnego nie widziałem. Powiedz Raciradowi, żeby najpierw znalazł tego człowieka.");
				else
					remind(p, n);
			}, "spalony obóz", "spalony oboz", "spalonym obozie");
			break;
		case EMBER_TRANSPORT:
			offer("Drugi ładunek został przy drodze w północnej części tych ziem, na południe od ławki Robercika. "
					+ "Sprawdź go i zapytaj chłopaka o #'porzucony transport'. Kręci się tam od rana. Może widział więcej niż moja straż.");
			reply(step.npc, (p, s, n) -> request(p, n,
					"Skorupa w skrzyni? Więc ktoś przywiózł nam pod nos smoczy lęg. "
							+ "Wartownik widział jeszcze #'porzucony transport' przy północnej osadzie. Sprawdzisz go, zanim wrócą po niego ci ludzie?"),
					"porzucony transport", "porzuconym transporcie");
			reply("Robercik", (p, s, n) -> {
				if (DragonTrailProgress.active(p, step)
						&& p.isQuestInState(step.slot, "witness"))
					complete(p, n, "witness");
				else
					remind(p, n);
			}, "porzucony transport", "porzuconym transporcie");
			break;
		case EMBER_SCENT:
			npcs.get(step.npc).add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					(p, s, n) -> TrailQuestLine.dragonScholarNeeded(p),
					ConversationStates.ATTENDING,
					"Przynosisz coś od Racirada? Czuję stąd żywicę. Podejdź i powiedz, co się stało.",
					null);
			offer("Połóż kawałek płótna przy glinianym naczyniu obok mnie i zanurz go w ciepłej wodzie. "
					+ "Rób to powoli. Potem powiedz mi o #'zapachu jaj'. Zobaczymy, co tak naprawdę przewozili.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n))
					return;
				request(p, n,
						"#'Zapach jaj'... Same jaja nie tłumaczą takiego pościgu. Na tym płótnie jest coś jeszcze. "
								+ "Zbadamy je razem? Nie chcę, żeby Racirad wyprawił ludzi przeciw smokom, zanim dowiemy się, co zaszło.");
			}, "zapach jaj", "zapachu jaj");
			break;
		case EMBER_GUIDE:
			offer("Ślady prowadzą do obozowiska na północnym wschodzie, w pobliżu kamiennej wieży. Ostrobor jest tam pod strażą. "
					+ "Pokonaj pilnującego go człowieka i zapytaj o #'pojmanego przewodnika'. Musimy wiedzieć, gdzie zabrano resztę lęgu.");
			reply(step.npc, (p, s, n) -> request(p, n,
					"Robercik opisał mi człowieka, który prowadził tamten wóz. To Ostrobor. "
							+ "Moi ludzie widzieli go potem z rękami związanymi za plecami. #'Pojmany przewodnik' wie coś, czego handlarze nie chcą puścić w świat. Wydostaniesz go?"),
					"pojmany przewodnik", "pojmanym przewodniku",
					"pojmanego przewodnika");
			reply(DragonTrailProgress.GUIDE, (p, s, n) -> {
				if (!DragonTrailProgress.active(p, step)
						|| !p.isQuestInState(step.slot, "guide")) {
					remind(p, n);
					return;
				}
				complete(p, n, "guide");
			}, "pojmany przewodnik", "pojmanym przewodniku",
					"pojmanego przewodnika");
			break;
		case EMBER_EGGS:
			offer("Herszt pilnuje skrzyni kawałek na północny wschód stąd. Pokonaj go i sprawdź ładunek obok. "
					+ "Uważaj na skorupy. Jeśli ocalały, doniesiemy je razem do gniazda. Potem porozmawiajmy o #'skradzionym lęgu'.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n))
					return;
				request(p, n,
						"Wiedziałem, że handlują smoczymi rzeczami, ale nie że poślą bestie na ludzi. Gdy odmówiłem dalszej drogi, związali mnie. "
								+ "#'Skradziony lęg' jest u herszta. Odzyskasz go? Nie zostawimy jaj na następną przynętę.");
			}, "skradziony lęg", "skradziony leg", "skradzionym lęgu");
			break;
		case EMBER_RETURN:
			offer("Przy obozie stoi naczynie z wodą. Użyj środka od Wizariusza, żeby obmyć skorupy i zmienić zabrudzone opakowanie. "
					+ "Gniazdo jest między kamieniami w południowo-zachodniej części północnych ziem. Najpierw rozbrój zostawione tam sidła, potem odłóż lęg. "
					+ "Wróć do Racirada i powiedz o #'powrocie do gniazda'. Musi zatrzymać ludzi z pochodniami.");
			reply(step.npc, (p, s, n) -> request(p, n,
					"Jaja są całe. Jeszcze zdążymy. #'Powrót do gniazda' to jedyna szansa, żeby smoki przestały szukać ich przy ludzkich domach. "
							+ "Pomożesz mi doprowadzić tę sprawę do końca?"),
					"powrót do gniazda", "powrot do gniazda",
					"powrocie do gniazda");
			reply("Racirad", (p, s, n) -> {
				if (DragonTrailProgress.active(p, step)
						&& p.isQuestInState(step.slot, "report"))
					reward(p, n);
				else
					remind(p, n);
			}, "powrót do gniazda", "powrot do gniazda", "powrocie do gniazda");
			break;
		default:
			throw new IllegalArgumentException("Not a dragon story quest");
		}
	}

	private boolean report(Player player, EventRaiser speaker) {
		if (!DragonTrailProgress.active(player, step)
				|| !player.isQuestInState(step.slot, "report"))
			return false;
		complete(player, speaker, "report");
		return true;
	}

	private void reward(Player player, EventRaiser speaker) {
		Item chest = SingletonRepository.getEntityManager().getItem(
				"złota skrzynia", ItemCreationContext.quest());
		chest.setBoundTo(player.getName());
		chest.put("itemdata", step.slot);
		chest.setDescription(
				"Oto złota skrzynia podarowana przez Racirada za powstrzymanie handlarzy smoczych jaj. Otwórz ją.");
		if (!player.equip("bag", chest)) {
			speaker.say(
					"Skrzynia jest twoja, ale nie zmieści się w pełnym plecaku. Zostaw trochę miejsca i wróć.");
			return;
		}
		StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"money", ItemCreationContext.quest());
		coins.setQuantity(step.coins);
		if (player.getSlotToEquip(coins) == null) {
			player.getSlot("bag").remove(chest.getID());
			speaker.say(
					"Zostaw miejsce także na monety. Skrzynia i zapłata będą tutaj czekać.");
			return;
		}
		complete(player, speaker, "report");
	}

	@Override
	protected void remind(Player player, EventRaiser speaker) {
		if (player.isQuestCompleted(step.slot)) {
			speaker.say(
					"Dziękuję. Gdybyśmy poszli za gniewem, dziś pewnie płonęłyby następne domy.");
			return;
		}
		if (!player.hasQuest(step.slot)) {
			speaker.say(!player.isQuestCompleted(step.previousSlot())
					? "Najpierw dokończ sprawę, którą już się zajmujesz. Nie chcę dokładać ci następnej, zanim poznasz prawdę o tamtej."
					: player.getLevel() < 400
							? "Nie poślę cię jeszcze między te smoki. Nabierz sił i doświadczenia. Tu jeden zły krok potrafi kosztować życie."
							: "Porozmawiaj najpierw z osobą, która prosiła cię o pomoc. Powinna wiedzieć, że ruszasz w drogę.");
			return;
		}
		switch (step) {
		case EMBER_CAMP:
			speaker.say(
					"Obejrzyj skrzynię i palenisko przy południowym wejściu, a potem wysłuchaj Radowita. Racirad musi poznać całą historię obozu.");
			break;
		case EMBER_TRANSPORT:
			speaker.say(
					"Sprawdź porzucony ładunek przy północnej osadzie i zapytaj Robercika, co widział. Powiedz mu o #'porzuconym transporcie'.");
			break;
		case EMBER_SCENT:
			speaker.say(
					"Zanurz płótno w naczyniu obok mnie, a potem porozmawiajmy o #'zapachu jaj'. Nie dotykaj tej mazi gołą ręką.");
			break;
		case EMBER_GUIDE:
			speaker.say(
					"Ostrobor jest pod strażą przy obozowisku na północnym wschodzie. Najpierw uwolnij go od strażnika. Potem zapytaj o #'pojmanego przewodnika'.");
			break;
		case EMBER_EGGS:
			speaker.say(
					"Herszt pilnuje skrzyni z jajami kawałek na północny wschód od Ostrobora. Pokonaj go, odzyskaj ładunek i wróć z wieściami o #'skradzionym lęgu'.");
			break;
		case EMBER_RETURN:
			speaker.say(
					"Obmyj skorupy w obozie, usuń sidła przy dawnym lęgowisku i odłóż jaja. Racirad czeka na wieści przy południowym wejściu. Powiedz mu o #'powrocie do gniazda'.");
			break;
		default:
			throw new IllegalArgumentException("Not a dragon story quest");
		}
	}

	@Override
	protected String completedText() {
		switch (step) {
		case EMBER_CAMP:
			return "Więc woźnica zostawił ładunek i uciekł, zanim smoki nadleciały. To nie wygląda na przypadek. "
					+ "Trzeba sprawdzić #'porzucony transport' przy północnej osadzie. Nie chcę karać smoków za to, co urządził nam jakiś człowiek.";
		case EMBER_TRANSPORT:
			return "Widziałem go. Smarował płótno czymś czarnym, potem zakrył nim skrzynie. Nie pozwolił mi podejść. "
					+ "Zanieś ten kawałek Wizariuszowi. Mieszka na szczycie wieży na północnym zachodzie. Zapytaj go o #'zapach jaj'. Ja się na tym nie znam, ale on powinien.";
		case EMBER_SCENT:
			return "Żywica i smocza krew. Na słońcu zapach roznosi się daleko. Ktoś chciał, żeby smoki znalazły ten wóz, nawet gdyby jaja leżały już gdzie indziej. "
					+ "Weź środek do obmycia skorup. Wystarczy dodać go do wody. Zanieś Raciradowi te wieści i zapytaj o #'pojmanego przewodnika'. Może wie, kto prowadził handlarzy.";
		case EMBER_GUIDE:
			return "Dziękuję. Kazali mi znaleźć stare lęgowisko. Mówili, że zabiorą tylko puste skorupy. "
					+ "Kiedy zobaczyłem całe jaja, chciałem wrócić. Wtedy mnie związali. Herszt siedzi niedaleko, przy skrzyni. Porozmawiajmy o #'skradzionym lęgu'. Nie zostawię go tutaj.";
		case EMBER_EGGS:
			return "Chcieli odciągać dorosłe smoki od gniazd, a potem zabierać kolejne jaja. Ludzkie osady miały za to płacić. "
					+ "Mamy dowód i mamy lęg. Pozostał #'powrót do gniazda', zanim Racirad zrobi coś, czego nie zdołamy odwrócić.";
		case EMBER_RETURN:
			return "Odłożę pochodnię. Ludzie zostaną przy trakcie, żeby pilnować podróżnych. Gniazd nie ruszymy. "
					+ "Dobrze, że nie poszliśmy tam w ciemno. Weź złotą skrzynię i 7000 monet. Zasłużyłeś na zapłatę za coś więcej niż zabicie następnego smoka.";
		default:
			throw new IllegalArgumentException("Not a dragon story quest");
		}
	}
}
