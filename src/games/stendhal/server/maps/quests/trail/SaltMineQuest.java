/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * Rescue two miners and close a breached brine reservoir before reopening the
 * route.
 */
public final class SaltMineQuest extends TrailQuest {
	public SaltMineQuest(Step step) {
		super(step);
		if (!TrailQuestLine.SALT_CHAPTER.equals(step.chapter))
			throw new IllegalArgumentException("Not a salt mine quest");
	}

	@Override
	protected void addDialogs() {
		switch (step) {
		case SALT_SHIFT:
			offer("Wejście do kopalni jest w południowo-zachodniej części miasta. Radost czeka zaraz pod szybem. Powiedz mu o #'zaginionej zmianie'. "
					+ "Nie schodź dalej bez rozmowy z nim. Po wstrząsie nawet dobrze znany chodnik potrafi zdradzić człowieka.");
			reply(step.npc, (p, s, n) -> request(p, n,
					"Dobrze, że jesteś. Z Gdańska przyszły wieści o uratowanym statku, a my tutaj też nie możemy doliczyć się ludzi. "
							+ "#'Zaginiona zmiana' to Bogdan i Wojmir. Nie wrócili po wstrząsie. Zejdziesz do Radosta i sprawdzisz, co z nimi?"),
					"zaginiona zmiana", "zaginionej zmianie",
					"zaginioną zmianę");
			reply(SaltMineProgress.FOREMAN, (p, s, n) -> {
				if (report(p, n, "report"))
					return;
				if (SaltMineProgress.advance(p, step, "foreman", "marks;0"))
					n.say("Zbigniew przysłał pomoc? Dobrze. Bogdan i Wojmir zostali za naruszonym przejściem. "
							+ "Zobacz skrzynkę przy podporze, kawałek na południe stąd, i mokre liny na wschód od szybu. "
							+ "Chcę wiedzieć, czy najpierw grozi nam strop, czy woda. Nie będę ryzykował następnych ludzi.");
				else
					remind(p, n);
			}, "zaginiona zmiana", "zaginionej zmianie", "zaginioną zmianę");
			break;
		case SALT_RESCUE:
			offer("Przy skrzynce zostały rozpórki. Podeprzyj nimi belkę, a potem rozpraw się ze szlamem, który zastępuje drogę. "
					+ "Bogdan jest we wschodnim odgałęzieniu niedaleko szybu, Wojmir dalej na północ. Porozmawiaj z oboma i wskaż im zabezpieczone przejście.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n, "report"))
					return;
				request(p, n,
						"#'Odcięci górnicy' nie wrócą, dopóki ta belka wisi im nad głowami. A przez wodę przedostało się jakieś pełzające paskudztwo. "
								+ "Zabezpieczysz przejście i dotrzesz do obu? Ja przygotuję ludzi przy szybie.");
			}, "odcięci górnicy", "odcieci gornicy", "odciętych górnikach");
			miner(SaltMineProgress.BOGDAN, 1,
					"Myślałem, że nikt nas tu nie znajdzie. Jeśli belka jest podparta, dotrę do szybu. "
							+ "Sprawdź jeszcze Wojmira. Poszedł szukać suchej drogi na północ i nie wrócił. Nie zostawiaj go samego.");
			miner(SaltMineProgress.WOJMIR, 2,
					"Usłyszałem, jak coś pełznie w wodzie, i bałem się wracać. Skoro droga jest wolna, pójdę za oznaczonymi podporami. "
							+ "Powiedz Radostowi, że nie zgubiłem kilofa. Będzie się złościł, ale to chyba lepsze niż martwienie się o mnie.");
			break;
		case SALT_SAMPLE:
			offer("W dalszej części chodnika, na południowy wschód od szybu, woda wciąż wypływa spod skały. Nabierz jej do naczynia. "
					+ "Agrypin ma dom w południowo-wschodniej części Wieliczki. Pokaż mu #'próbkę solanki'. Może powie nam, co wlazło do kopalni.");
			reply(step.npc, (p, s, n) -> request(p, n,
					"Ludzie są bezpieczni, ale nie mogę posłać tu następnej zmiany. "
							+ "To paskudztwo wypłynęło razem z wodą. Zaniesiesz Agrypinowi #'próbkę solanki'?"),
					"próbka solanki", "probka solanki", "próbkę solanki",
					"próbce solanki");
			reply(SaltMineProgress.SCHOLAR, (p, s, n) -> {
				if (!report(p, n, "agrypin"))
					remind(p, n);
			}, "próbka solanki", "probka solanki", "próbkę solanki",
					"próbce solanki");
			break;
		case SALT_SOURCE:
			offer("W kopalni obejrzyj odpływ w chodniku na północny wschód od szybu i kamienny próg jeszcze dalej na północ. "
					+ "To pozostałości dawnego odprowadzenia wody. Porównaj, skąd napływa solanka, i powiedz Radostowi o #'starym zbiorniku'. Nie otwieraj jeszcze spustu.");
			reply(step.npc, (p, s, n) -> request(p, n,
					"Te drobiny próbują zebrać się w jedną całość. Wstrząs musiał otworzyć #'stary zbiornik', "
							+ "do którego górnicy dawno nie zaglądali. Ustalisz, którędy woda dostaje się do chodników? Radost zna odpływy, będzie wiedział, co z tym zrobić."),
					"stary zbiornik", "starym zbiorniku");
			reply(SaltMineProgress.FOREMAN, (p, s, n) -> {
				if (!report(p, n, "report"))
					remind(p, n);
			}, "stary zbiornik", "starym zbiorniku");
			break;
		case SALT_CREATURE:
			offer("Szukaj go przy dawnym zbiorniku, dalej na wschód od szybu. Gdy go pokonasz, podejdź do szczeliny obok. "
					+ "Zostały tam kamienie i kliny dawnego zamknięcia. Dopasuj je i zatrzymaj napływ. Wody z chodników pozbędziemy się dopiero potem.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n, "report"))
					return;
				request(p, n,
						"#'Żywiołak solanki'... Nigdy nie myślałem, że przyjdzie mi coś takiego oglądać przy własnym szybie. "
								+ "Nie dopuści nas do szczeliny. Pokonasz go i zamkniesz dopływ? Bez tego woda wróci, choćbyśmy osuszyli każdy chodnik.");
			}, "żywiołak solanki", "zywiolak solanki", "żywiołaku solanki");
			break;
		case SALT_RETURN:
			offer("Przy wypływie na południowy wschód od szybu jest lina spustu awaryjnego. Oczyść wlot i otwórz go. "
					+ "Potem sprawdź belkę przy skrzynce i drogę obok mokrych lin. Kiedy wszystko będzie trzymać, powiedz Zbigniewowi w ratuszu, że mamy #'bezpieczny szyb'.");
			reply(step.npc, (p, s, n) -> request(p, n,
					"Dopływ zamknięty, ale woda sama nie wyjdzie z chodników. "
							+ "Pomożesz mi przywrócić #'bezpieczny szyb'? Otworzymy stary spust i sprawdzimy przejście, zanim znów puszczę tam ludzi."),
					"bezpieczny szyb", "bezpiecznym szybie");
			reply(SaltMineProgress.MAYOR, (p, s, n) -> {
				if (SaltMineProgress.active(p, step)
						&& p.isQuestInState(step.slot, "mayor"))
					reward(p, n);
				else
					remind(p, n);
			}, "bezpieczny szyb", "bezpiecznym szybie");
			break;
		default:
			throw new IllegalArgumentException("Not a salt mine quest");
		}
	}

	private void miner(String name, int bit, String text) {
		reply(name, (p, s, n) -> {
			if (SaltMineProgress.mark(p, Step.SALT_RESCUE, "miners", bit,
					"report"))
				n.say(text);
			else
				remind(p, n);
		}, "odcięci górnicy", "odcieci gornicy", "odciętych górnikach");
	}

	private boolean report(Player player, EventRaiser speaker, String state) {
		if (!SaltMineProgress.active(player, step)
				|| !player.isQuestInState(step.slot, state))
			return false;
		complete(player, speaker, state);
		return true;
	}

	private void reward(Player player, EventRaiser speaker) {
		Item helmet = SingletonRepository.getEntityManager().getItem(
				"hełm wampirzy",
				ItemCreationContext.builder(
						ItemCreationContext.Source.QUEST).withQuestRarity(
								ItemRarity.RARE).generateAffixes(true).build());
		helmet.setBoundTo(player.getName());
		helmet.setDescription(
				"Oto hełm wampirzy podarowany za pomoc górnikom Wieliczki. Wewnątrz wyryto podziękowanie od Bogdana i Wojmira.");
		if (!player.equip("bag", helmet)) {
			speaker.say(
					"Mam dla ciebie hełm, ale nie zmieści się w pełnym plecaku. Zostaw trochę miejsca i wróć.");
			return;
		}
		StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"money", ItemCreationContext.quest());
		coins.setQuantity(step.coins);
		if (player.getSlotToEquip(coins) == null) {
			player.getSlot("bag").remove(helmet.getID());
			speaker.say(
					"Zostaw miejsce również na monety. Hełm i zapłata będą tu czekać.");
			return;
		}
		complete(player, speaker, "mayor");
	}

	@Override
	protected void remind(Player player, EventRaiser speaker) {
		if (player.isQuestCompleted(step.slot)) {
			speaker.say(
					"Dziękuję. Bez twojej pomocy ta historia mogła skończyć się zupełnie inaczej.");
			return;
		}
		if (!player.hasQuest(step.slot)) {
			speaker.say(!player.isQuestCompleted(step.previousSlot())
					? "Dokończ najpierw sprawę, którą już się zajmujesz. Potem pomyślimy o reszcie."
					: player.getLevel() < 250
							? "Po wstrząsie jest tam niebezpiecznie nawet dla doświadczonych górników. Nabierz jeszcze sił, zanim zejdziesz im na pomoc."
							: "Porozmawiaj najpierw z osobą, która prosiła cię o pomoc. Musi wiedzieć, że ruszasz w drogę.");
			return;
		}
		switch (step) {
		case SALT_SHIFT:
			speaker.say(
					"Radost czeka pod szybem. Poprosił o sprawdzenie podpory przy skrzynce i mokrych lin w pobliskim chodniku.");
			break;
		case SALT_RESCUE:
			speaker.say(
					"Najpierw podeprzyj strop i usuń szlam. Potem wskaż bezpieczną drogę Bogdanowi i Wojmirowi. Radost musi wiedzieć, że obaj wrócą.");
			break;
		case SALT_SAMPLE:
			speaker.say(
					"Pobierz wodę z dalszego wypływu i pokaż Agrypinowi #'próbkę solanki'. Bez tego będziemy tylko zgadywać.");
			break;
		case SALT_SOURCE:
			speaker.say(
					"Obejrzyj odpływ i kamienny próg. Powiedz potem Radostowi, jak woda wydostaje się ze #'starego zbiornika'.");
			break;
		case SALT_CREATURE:
			speaker.say(
					"Pokonaj żywiołaka przy zbiorniku, a potem zamknij szczelinę kamieniami i klinami. Radost czeka na wieści pod szybem.");
			break;
		case SALT_RETURN:
			speaker.say("drain".equals(player.getQuest(step.slot, 0))
					? "Otwórz spust liną przy dalszym wypływie. Potem sprawdź belkę i drogę do szybu."
					: "mayor".equals(player.getQuest(step.slot, 0))
							? "Zbigniew czeka w ratuszu. Powiedz mu, że mamy #'bezpieczny szyb'."
							: "Zanim wrócisz do Zbigniewa, sprawdź podpartą belkę i drogę przy mokrych linach.");
			break;
		default:
			throw new IllegalArgumentException("Not a salt mine quest");
		}
	}

	@Override
	protected String completedText() {
		switch (step) {
		case SALT_SHIFT:
			return "Już rozumiem. Woda podmyła przejście, a wstrząs dokończył resztę. Bogdan i Wojmir muszą gdzieś tam czekać. "
					+ "Porozmawiajmy o #'odciętych górnikach'. Da się do nich dotrzeć, jeśli najpierw podeprzemy strop.";
		case SALT_RESCUE:
			return "Obaj żyją? To najlepsza wiadomość, jaką dziś słyszałem. Moi ludzie dopilnują ich powrotu. "
					+ "Ale nie zostawię kopalni z tą wodą. Trzeba pobrać #'próbkę solanki' i zanieść ją Agrypinowi.";
		case SALT_SAMPLE:
			return "Postaw naczynie i spójrz na dno. Te drobiny zbierają się przy ściance, jakby czegoś szukały. "
					+ "To może tłumaczyć stworzenie, które widziano pod ziemią. Porozmawiajmy o #'starym zbiorniku'. Trzeba znaleźć miejsce, skąd to wszystko wypływa.";
		case SALT_SOURCE:
			return "Odpływ zatkany, próg pęknięty... Jeśli ruszymy spust teraz, woda może odciąć nam szyb. "
					+ "Najpierw trzeba zatrzymać to, co wychodzi ze zbiornika. Powiedz mi więcej o #'żywiołaku solanki'.";
		case SALT_CREATURE:
			return "Dobrze. Teraz możemy spokojnie spuścić wodę, bez obawy, że zaraz napłynie następna. "
					+ "Pozostało przywrócić #'bezpieczny szyb'. Nie puszczę tam ludzi, dopóki nie sprawdzimy stropu i drogi.";
		case SALT_RETURN:
			return "Radost przysłał już wieści. Bogdan i Wojmir wrócili, a do zabezpieczonych chodników można znów wejść. "
					+ "Weź ten hełm i 3000 monet z miejskiej kasy. Ludzie chcieli, żeby zostało ci też podziękowanie od nich. W środku wyryli swoje imiona.";
		default:
			throw new IllegalArgumentException("Not a salt mine quest");
		}
	}
}
