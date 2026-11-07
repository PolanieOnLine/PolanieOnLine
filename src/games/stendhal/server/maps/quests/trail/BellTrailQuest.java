/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.common.QuestMarker;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * The final expedition: learn whose voice calls, then bring the living home.
 */
public final class BellTrailQuest extends TrailQuest {
	public BellTrailQuest(Step step) {
		super(step);
		if (!TrailQuestLine.BELL_CHAPTER.equals(step.chapter)) {
			throw new IllegalArgumentException("Not a bell story quest");
		}
	}

	@Override
	protected void addDialogs() {
		switch (step) {
		case BELL_RETURNED:
			offer("Niemir siedzi tuż obok. Daj mu chwilę i zapytaj o #'wyprawę'. Nie krzycz na niego. Chłopak od dwóch nocy nie zmrużył oka.");
			reply(step.npc, (p, s, n) -> {
				if (!report(p, n)) {
					request(p, n,
							"Wysłaliśmy ludzi na wyspy, żeby obejrzeli stary dzwon. Wrócił tylko Niemir. "
									+ "Powtarza, że pozostali nie chcą zejść, choć skończyło im się jedzenie. Pomożesz mi ich sprowadzić?");
				}
			}, "zaginiona wyprawa", "zaginionej wyprawie");
			reply(BellTrailProgress.WITNESS, (p, s, n) -> {
				if (BellTrailProgress.advance(p, step, "witness", "report")) {
					n.say("Słyszałem mamę. Wołała mnie zza kamieni. Tylko że mama nie żyje od sześciu lat. "
							+ "Witosław mówił, żebym się nie bał, bo jego brat też tam jest. Uciekłem. Powiedz Festris, że nie zostawiłem ich dla zabawy.");
				} else {
					remind(p, n);
				}
			}, "wyprawa", "wyprawę", "wyprawe", "wyprawie");
			break;
		case BELL_WOUNDED:
			offer("Borzęta siedzi przy skrzyni, Milan kawałek obok. Zapytaj obu o #'rannych'. "
					+ "Mnie już nie chcą słuchać. Uważają, że powinienem zostawić brata.");
			reply(step.npc, (p, s, n) -> {
				if (!report(p, n)) {
					request(p, n,
							"Festris cię przysłała? Niemir dotarł na dół? Dobrze. "
									+ "Moim ludziom trzeba pomóc, ale nie odejdę, dopóki Bronik mnie woła. Porozmawiasz z nimi? Może powiedzą ci, co ich tak przeraża.");
				}
			}, "ranni", "rannych");
			reply(BellTrailProgress.SCHOLAR, (p, s, n) -> {
				if (BellTrailProgress.mark(p, step, "wounded", 1, 3,
						"report")) {
					n.say("Skręciłem nogę, kiedy próbowałem odciągnąć Witosława od kamieni. Ja słyszę swojego nauczyciela. "
							+ "Umarł przy mnie, więc wiem, że nie stoi za dzwonem. Milan też kogoś słyszał. Zapytaj go.");
				} else {
					remind(p, n);
				}
			}, "ranni", "rannych");
			reply(BellTrailProgress.INJURED, (p, s, n) -> {
				if (BellTrailProgress.mark(p, step, "wounded", 2, 3,
						"report")) {
					n.say("Mnie wołała córka. Jest w domu, cała i zdrowa. Nie powinna wiedzieć, gdzie teraz jestem. "
							+ "Gdy ruszyłem za głosem, coś uderzyło mnie w bark. Powiedz Witosławowi, że chcę wrócić do prawdziwego dziecka.");
				} else {
					remind(p, n);
				}
			}, "ranni", "rannych");
			break;
		case BELL_VOICE:
			offer("Zapiski zostały w skrzyni na zachód od naszego obozu. Przy dzwonie jest kamień z zatartym napisem. "
					+ "Obejrzyj też sam dzwon. Potem porozmawiajmy o #'głosie z mgły'. I proszę, nie odpowiadaj, gdy cię zawoła.");
			reply(step.npc, (p, s, n) -> {
				if (!report(p, n)) {
					request(p, n,
							"Nie przekonamy Witosława samymi słowami. Ma przy sobie obrączkę brata i ściska ją, ilekroć tamten go woła. "
									+ "Potrzebujemy czegoś, czego nie da się pomylić z głosem. Sprawdzisz nasze zapiski i kamienie przy dzwonie?");
				}
			}, "głos z mgły", "glos z mgly", "głosie z mgły", "glosie z mgly");
			break;
		case BELL_GUARD:
			offer("Przy dzwonie leżą dwa kamienie ze znakami. Rozłącz je, zanim podejdziesz do strażnika. "
					+ "Potem opuść dzwon z mocowania. Ja zostanę z rannymi. Wróć do mnie, gdy zrobi się cicho.");
			reply(step.npc, (p, s, n) -> {
				if (!report(p, n)) {
					request(p, n,
							"Pamiętam, jak go pochowałem. Mimo to uwierzyłem, że na mnie czeka. "
									+ "Nie pójdę tam drugi raz. Pomożesz nam uciszyć ten dzwon? Jeśli tylko odejdziemy, przyjdą następni.");
				}
			}, "milczący dzwon", "milczacy dzwon", "milczącym dzwonie");
			break;
		case BELL_ESCAPE:
			offer("Przy skrzyni obok mnie jest #'zbiórka rannych'. Poprowadź ich na zachód, przez trzy postoje przy paleniskach. "
					+ "Nie biegnij. Jeśli coś zagrodzi drogę, pozwól im odpocząć i wróć po nich. Potem powiadom Festris o #'powrocie wyprawy'.");
			reply(step.npc, (p, s, n) -> request(p, n,
					"Spakowaliśmy, co uniesiemy. Borzęta ledwo stawia nogę, a Milan nie utrzyma nawet tarczy. "
							+ "Przeprowadzisz ich do zachodniego obozowiska? Ja pójdę na końcu i dopilnuję zejścia."),
					"powrót wyprawy", "powrot wyprawy", "powrocie wyprawy");
			reply("Festris", (p, s, n) -> {
				if (!report(p, n)) {
					remind(p, n);
				}
			}, "powrót wyprawy", "powrot wyprawy", "powrocie wyprawy");
			break;
		default:
			throw new IllegalArgumentException("Not a bell story quest");
		}
	}

	private boolean report(Player player, EventRaiser speaker) {
		String npc = step == Step.BELL_ESCAPE ? "Festris" : step.npc;
		String required = step == Step.BELL_ESCAPE ? "return" : "report";
		if (!npc.equals(speaker.getName())
				|| !BellTrailProgress.active(player, step)
				|| !player.isQuestInState(step.slot, required)) {
			return false;
		}
		if (step == Step.BELL_ESCAPE) {
			StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem(
					"money", ItemCreationContext.quest());
			coins.setQuantity(step.coins);
			// A chest may merge into an existing stack without receiving an ID.
			// Reject a full inventory before changing that stack.
			if (player.getSlotToEquip(coins) == null) {
				speaker.say("Zostaw miejsce także na zapłatę. Nagroda nie przepadnie.");
				return true;
			}
			Item chest = SingletonRepository.getEntityManager().getItem(
					"złota skrzynia", ItemCreationContext.quest());
			chest.setBoundTo(player.getName());
			chest.setDescription(
					"Złota skrzynia podarowana przez Festris za sprowadzenie ocalałych z Kikareukin. Otwórz ją.");
			if (!player.equip("bag", chest)) {
				speaker.say(
						"Ta skrzynia jest dla ciebie. Zostaw na nią trochę miejsca w plecaku, zaczekam.");
				return true;
			}
			if (player.getSlotToEquip(coins) == null) {
				player.getSlot("bag").remove(chest.getID());
				speaker.say(
						"Zostaw miejsce także na zapłatę. Nagroda nie przepadnie.");
				return true;
			}
		}
		complete(player, speaker, required);
		return true;
	}

	@Override
	protected void remind(Player player, EventRaiser speaker) {
		if (player.isQuestCompleted(step.slot)) {
			speaker.say("Nie zapomnę, że wróciłeś po żywych. Dziękuję.");
		} else if (!step.available(player) && !player.hasQuest(step.slot)) {
			speaker.say(
					"Droga na wyspy jest niebezpieczna. Dokończ najpierw to, czym już się zajmujesz. Nie chcę stracić kolejnej osoby.");
		} else {
			switch (step) {
			case BELL_RETURNED:
				speaker.say(
						"Niemir siedzi obok Festris. Wysłuchaj go, a potem wróć do kapłanki. Chłopak nie powinien zostać sam z tym, co słyszał.");
				break;
			case BELL_WOUNDED:
				speaker.say(
						"Borzęta i Milan są przy skrzyniach w obozie. Porozmawiaj z oboma. Każdy słyszy tu kogoś innego.");
				break;
			case BELL_VOICE:
				speaker.say(
						"Zapiski zostały w skrzyni, a napis jest na kamieniu przy dzwonie. Zajrzyj też do jego wnętrza. Chcę wiedzieć, co naprawdę wydaje ten dźwięk.");
				break;
			case BELL_GUARD:
				speaker.say(
						"Rozłącz kamienie ze znakami, pokonaj strażnika i opuść dzwon. Dopiero wtedy uwierzę, że ten głos już nie wróci.");
				break;
			case BELL_ESCAPE:
				speaker.say(player.isQuestInState(step.slot, "return")
						? "Festris czeka na czwartym piętrze. Powiedz jej, że obaj ranni przeżyli. O Broniku też powinna wiedzieć."
						: "Zbierz rannych przy skrzyni obok Witosława. Prowadź ich od postoju do postoju. Gdy stracą cię z oczu, zatrzymają się i zaczekają.");
				break;
			default:
				break;
			}
		}
	}

	@Override
	public QuestMarker getNPCQuestMarker(Player player, SpeakerNPC npc) {
		if (BellTrailProgress.active(player, step)) {
			String recipient = step == Step.BELL_ESCAPE ? "Festris" : step.npc;
			String required = step == Step.BELL_ESCAPE ? "return" : "report";
			return recipient.equals(npc.getName())
					&& player.isQuestInState(step.slot, required)
							? QuestMarker.READY
							: QuestMarker.IN_PROGRESS;
		}
		return super.getNPCQuestMarker(player, npc);
	}

	@Override
	protected String completedText() {
		switch (step) {
		case BELL_RETURNED:
			return "Nie mam mu tego za złe. Czasem ucieczka ratuje więcej ludzi niż upór. "
					+ "Witosław rozbił obóz na północno-wschodniej wyspie. Odszukaj go i zapytaj o #'rannych'. Ja przygotuję miejsce na dole.";
		case BELL_WOUNDED:
			return "Każdy słyszy kogoś innego? A ja byłem pewien, że to Bronik. "
					+ "Borzęta chciał ci coś jeszcze powiedzieć. Zapytaj go o #'głos z mgły'. Muszę to sobie poukładać.";
		case BELL_VOICE:
			return "Więc Witosław wiedział, że jego brat nie żyje. Tylko nie potrafił odejść, kiedy znowu go usłyszał. "
					+ "Pokaż mu ten wpis. Porozmawiajcie o #'milczącym dzwonie'. Teraz może nas wysłucha.";
		case BELL_GUARD:
			return "Słyszę wiatr. Tylko wiatr. Nigdy nie sądziłem, że tak mnie to ucieszy. "
					+ "Borzęta może już stanąć. Milan też. Porozmawiajmy o #'powrocie wyprawy', zanim pogoda się zmieni.";
		case BELL_ESCAPE:
			return "Obaj żyją... Dobrze. Naprawdę dobrze. O Broniku powiem rodzinie sama. "
					+ "Kiedyś wystarczyło poprosić cię o drobną pomoc, a dziś przyprowadzasz ludzi z miejsca, z którego bali się wrócić. "
					+ "Weź skrzynię i zapłatę. A potem odpocznij. Przez jedną noc kraina poradzi sobie bez ciebie.";
		default:
			throw new IllegalArgumentException("Not a bell story quest");
		}
	}
}
