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
 * Resolve the siege without sacrificing the garrison to the steward's theft.
 */
public final class WarsawFortressQuest extends TrailQuest {
	public WarsawFortressQuest(Step step) {
		super(step);
		if (!TrailQuestLine.WARSAW_CHAPTER.equals(step.chapter))
			throw new IllegalArgumentException("Not a Warsaw quest");
	}

	@Override
	protected void addDialogs() {
		switch (step) {
		case WARSAW_GATE:
			offer("Wejdź do głównego budynku zamku, na północ od naszego obozu. Wartownik na dziedzińcu nie przepuści nikogo z zewnątrz. "
					+ "Jeśli zdołasz go pokonać, poszukaj Dobrosławy w lazarecie, w prawej części warowni. Opiekuje się rannymi. Jej uwierzę.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n))
					return;
				request(p, n,
						"Słyszałem, co zrobiono dla poległych pod Krakowem. Tu jeszcze możemy pomóc żywym. "
								+ "Książę szykuje szturm, ale w warowni zostali ranni i służba. Dowiesz się, co z nimi, #'zanim ruszy szturm'?");
			}, "zanim ruszy szturm", "szturm");
			reply(WarsawFortressProgress.HEALER, (p, s, n) -> {
				if (WarsawFortressProgress.advance(p, step, "witness",
						"report"))
					n.say("Powiedz Markowi, że opatruję i jego ludzi, i tych, którzy zamknęli bramę. "
							+ "Żołnierze od miesięcy nie dostają pieniędzy. Radomir mówił im, że książę o nich zapomniał. "
							+ "A potem kazał nie wypuszczać nikogo z warowni. Tu są ludzie, którzy nawet nie mieli broni w ręku.");
				else
					remind(p, n);
			}, "zanim ruszy szturm", "szturm");
			break;
		case WARSAW_PAY:
			offer("Księga wypłat powinna być w kancelarii, po lewej stronie sali dowodzenia. Spis dostaw trzymano w południowo-wschodnim magazynie. "
					+ "Porównaj je. Pieniądze i zapasy przechodziły przez ręce tego samego zarządcy.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n))
					return;
				request(p, n,
						"#'Niewypłacony żołd'? Posłałem pieniądze na całą zimę. Radomir sam pokazał mi pokwitowania. "
								+ "Jeśli mnie oszukał, chcę zobaczyć dowody, zanim odpowiem moim ludziom. Przejrzysz księgi w warowni?");
			}, "niewypłacony żołd", "niewyplacony zold",
					"niewypłaconym żołdzie");
			break;
		case WARSAW_WITNESSES:
			offer("Wszemił został w koszarach, po lewej stronie dziedzińca. Dowodził jedną z wart. Porozmawiaj też z Dobrosławą w lazarecie. "
					+ "Nie obiecuj im niczego w imieniu księcia. Najpierw muszę wiedzieć, co się tam stało.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n))
					return;
				request(p, n,
						"Zapiski wyglądają źle, ale #'ludzie w środku' mogą powiedzieć więcej. Radomir od lat jadł z nami przy jednym stole. "
								+ "Jeśli mam stanąć przeciw niemu, chcę wysłuchać tych, którzy zostali pod jego rozkazami. Pójdziesz do nich?");
			}, "ludzie w środku", "ludzie w srodku", "ludziach w środku");
			reply(WarsawFortressProgress.SOLDIER, (p, s, n) -> {
				if (WarsawFortressProgress.mark(p, step, "witnesses", 1))
					n.say("Miałem otworzyć bramę, kiedy przyszli ludzie księcia. Radomir przyłożył mi nóż do gardła. "
							+ "Powiedział, że jeśli odejdzie choć jeden żołnierz, zawiśniemy wszyscy. Głodni ludzie wierzą w różne rzeczy. "
							+ "Ale nie będę dłużej osłaniał jego kradzieży. Powiedz Markowi, że stanę przed księciem i powtórzę każde słowo.");
				else
					remind(p, n);
			}, "ludzie w środku", "ludzie w srodku", "ludziach w środku");
			reply(WarsawFortressProgress.HEALER, (p, s, n) -> {
				if (WarsawFortressProgress.mark(p, step, "witnesses", 2))
					n.say("Radomir kazał pisarzowi składać podpisy za zmarłych. Rozpoznałam imiona, bo sama zamykałam tym ludziom oczy. "
							+ "Gdy zapytałam o pieniądze dla ich rodzin, zagroził, że wyrzuci rannych z lazaretu. "
							+ "Ostatnio znoszą beczki z olejem do magazynu. Nie podoba mi się to.");
				else
					remind(p, n);
			}, "ludzie w środku", "ludzie w srodku", "ludziach w środku");
			break;
		case WARSAW_FIRE:
			offer("W zbrojowni, po prawej stronie dziedzińca, leży rozkaz dla wart. Zacznij od niego. "
					+ "Jeśli chodzi o podpalenie, unieszkodliw olej w magazynie. Tylko nie wylewaj go przy palenisku. Tuż za murem jest pusty rów.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n))
					return;
				request(p, n,
						"Te #'beczki z olejem' nie służą do lamp. Radomir kazał przynieść ich tyle, że starczyłoby na cały rok. "
								+ "Wczoraj słyszałam, jak mówił, że książę dostanie same popioły. Sprawdzisz, co przygotował? Nie zdołam sama wynieść stąd rannych.");
			}, "beczki z olejem", "beczkach z olejem");
			break;
		case WARSAW_STEWARD:
			offer("Radomir siedzi w sali dowodzenia, na północnym końcu dziedzińca. Pokonaj go i zabierz zapiski z jego stołu. "
					+ "Nie pozwól, żeby zginęły razem z innymi dowodami. Po jego ludziach nie będę posyłał katów, jeśli oddadzą broń.");
			reply(step.npc, (p, s, n) -> {
				if (report(p, n))
					return;
				request(p, n,
						"A więc #'zarządca Radomir' chciał spalić i moje księgi, i moich ludzi. Mark przygotuje miejsce dla rannych. "
								+ "Ty znasz już drogę do środka. Zatrzymasz Radomira, zanim wymyśli coś jeszcze?");
			}, "zarządca Radomir", "zarzadca Radomir", "zarządcy Radomirze");
			break;
		case WARSAW_TRUCE:
			offer("Porozmawiaj z Wszemiłem w koszarach o #'otwartej bramie'. Niech zbierze rannych i ludzi, którzy chcą odejść. "
					+ "Potem zabezpiecz linę przy bramie, żeby nikt jej za nimi nie zatrzasnął. Na koniec poproś księcia o #'ocalenie załogi'.");
			reply(step.npc, (p, s, n) -> {
				if (WarsawFortressProgress.active(p, step)
						&& p.isQuestInState(step.slot, "ready")) {
					reward(p, n);
					return;
				}
				request(p, n,
						"Radomir jest w naszych rękach, ale na murach wciąż stoją przestraszeni żołnierze. "
								+ "Nie chcę, żeby ktoś teraz wypuścił strzałę. Pomożesz mi doprowadzić #'ocalenie załogi' do końca?");
			}, "ocalenie załogi", "ocalenie zalogi", "ocaleniu załogi");
			reply(WarsawFortressProgress.SOLDIER, (p, s, n) -> {
				if (WarsawFortressProgress.advance(p, step, "truce", "gate"))
					n.say("Złożymy broń. Nie mam już siły grzebać kolejnych ludzi. "
							+ "Zabezpiecz linę przy bramie na południu dziedzińca, a wyprowadzę rannych do Marka. "
							+ "Ja pójdę ostatni. Powiedz księciu, że nie uciekam od odpowiedzi za to, co zrobiłem.");
				else
					remind(p, n);
			}, "otwarta brama", "otwartej bramie");
			reply(WarsawFortressProgress.PRINCE, (p, s, n) -> {
				if (WarsawFortressProgress.advance(p, step, "prince", "ready"))
					n.say("Odwołuję szturm. Ci, którzy oddadzą broń, zostaną wysłuchani. "
							+ "Nie powieszę człowieka za to, że chciał dostać obiecaną zapłatę. Radomir odpowie za kradzieże i rozkaz podpalenia. "
							+ "Powiedz Markowi, żeby przyjął rannych. I żeby wydał ci zbroję z mojego składu. Zapracowano dziś na nią uczciwie.");
				else
					remind(p, n);
			}, "ocalenie załogi", "ocalenie zalogi", "ocaleniu załogi");
			break;
		default:
			throw new IllegalArgumentException("Not a Warsaw quest");
		}
	}

	private boolean report(Player player, EventRaiser speaker) {
		if (!WarsawFortressProgress.active(player, step)
				|| !player.isQuestInState(step.slot, "report"))
			return false;
		complete(player, speaker, "report");
		return true;
	}

	private void reward(Player player, EventRaiser speaker) {
		Item armor = SingletonRepository.getEntityManager().getItem(
				"magiczna zbroja płytowa",
				ItemCreationContext.builder(
						ItemCreationContext.Source.QUEST).withQuestRarity(
								ItemRarity.RARE).generateAffixes(true).build());
		armor.setBoundTo(player.getName());
		armor.setDescription(
				"Oto magiczna zbroja płytowa z książęcego składu. Na napierśniku wyryto podziękowanie za ocalenie załogi warszawskiej warowni.");
		if (!player.equip("bag", armor)) {
			speaker.say(
					"Zbroja jest przygotowana, ale nie zmieści się w twoim plecaku. Zostaw trochę miejsca i wróć.");
			return;
		}
		StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"money", ItemCreationContext.quest());
		coins.setQuantity(step.coins);
		// Armor and payment must fit together; the usual reward action may
		// otherwise drop money publicly.
		if (player.getSlotToEquip(coins) == null) {
			player.getSlot("bag").remove(armor.getID());
			speaker.say(
					"Zostaw miejsce na zbroję i monety. Nagroda będzie tu na ciebie czekać.");
			return;
		}
		complete(player, speaker, "ready");
	}

	@Override
	protected void remind(Player player, EventRaiser speaker) {
		if (player.isQuestCompleted(step.slot)) {
			speaker.say("Dziękuję. Twoja pomoc wiele dla nas znaczy.");
			return;
		}
		if (!player.hasQuest(step.slot)) {
			speaker.say(!player.isQuestCompleted(step.previousSlot())
					? "Najpierw doprowadź do końca sprawę, którą już się zajmujesz. Nie wszystko da się zrobić naraz."
					: player.getLevel() < 150
							? "W tej warowni łatwo stracić życie. Nabierz jeszcze doświadczenia, zanim pójdziesz między zwaśnionych żołnierzy."
							: "Porozmawiaj najpierw z osobą, która prosiła cię o pomoc. Musi wiedzieć, że ruszasz w drogę.");
			return;
		}
		switch (step) {
		case WARSAW_GATE:
			speaker.say(
					"Dobrosława jest w lazarecie, po prawej stronie warowni. Jeśli wartownik zastępuje ci drogę, najpierw trzeba się z nim rozprawić.");
			break;
		case WARSAW_PAY:
			speaker.say(
					"Porównaj księgę wypłat z kancelarii i spis zapasów z magazynu. Nie zabieraj niczego, co może przydać się rannym.");
			break;
		case WARSAW_WITNESSES:
			speaker.say(
					"Wszemił czeka w koszarach, a Dobrosława w lazarecie. Zapytaj ich o #'ludzi w środku' i wróć do Marka.");
			break;
		case WARSAW_FIRE:
			speaker.say(
					"Sprawdź rozkaz na stole w zbrojowni, potem zabezpiecz olej w magazynie. Dobrosława czeka na wieści w lazarecie.");
			break;
		case WARSAW_STEWARD:
			speaker.say(
					"Radomir zajął salę dowodzenia. Pokonaj go i przejrzyj zapiski na jego stole. Książę będzie chciał je zobaczyć.");
			break;
		case WARSAW_TRUCE:
			String state = player.getQuest(step.slot, 0);
			speaker.say("truce".equals(state)
					? "Uzgodnij z Wszemiłem wyjście przez #'otwartą bramę'."
					: "gate".equals(state)
							? "Lina przy bramie musi być zamocowana, żeby nikt nie zatrzasnął wyjścia za rannymi."
							: "prince".equals(state)
									? "Porozmawiaj z księciem o #'ocaleniu załogi'. Potrzebujemy jego słowa."
									: "Mark czeka przy obozie. Przekaż mu odpowiedź księcia.");
			break;
		default:
			throw new IllegalArgumentException("Not a Warsaw quest");
		}
	}

	@Override
	protected String completedText() {
		switch (step) {
		case WARSAW_GATE:
			return "Dziękuję. Spróbuję zatrzymać przygotowania do szturmu. Książę musi usłyszeć o #'niewypłaconym żołdzie'. Idź do niego, zanim zwoła dowódców.";
		case WARSAW_PAY:
			return "Są tu podpisy zmarłych i zapłaty dla Radana. Ten sam znak pojawiał się na skradzionych dostawach spod Semos. "
					+ "Radomir utrzymywał swoich ludzi za pieniądze załogi. Mark musi o tym wiedzieć. Zapytaj go o #'ludzi w środku'.";
		case WARSAW_WITNESSES:
			return "Wszemił nie zmyślałby takiej historii. Dopilnuję, żeby książę wysłuchał obojga. "
					+ "A te beczki... Wróć do Dobrosławy i zapytaj o #'beczki z olejem'. Nie możemy z tym czekać.";
		case WARSAW_FIRE:
			return "Dzięki tobie ci ludzie nie spłoną we własnych łóżkach. Zajmę się rannymi. "
					+ "Książę powinien wiedzieć, co przygotował #'zarządca Radomir'. Powiedz mu wszystko.";
		case WARSAW_STEWARD:
			return "Te zapiski wystarczą, żeby postawić mu zarzuty. Ale mury wciąż są obsadzone. "
					+ "Mark będzie wiedział, jak doprowadzić #'ocalenie załogi' do końca. Niech nikt nie ginie już przez Radomira.";
		case WARSAW_TRUCE:
			return "Przyjęliśmy rannych. Wszemił oddał broń i sam stanął przed księciem. Warownia nie spłonęła, a ludzi wysłuchano. "
					+ "Weź tę zbroję i 1700 monet. Gdyby wszyscy zaczynali od szturmu, nie mielibyśmy dziś komu podać wody.";
		default:
			throw new IllegalArgumentException("Not a Warsaw quest");
		}
	}
}
