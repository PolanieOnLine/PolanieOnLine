/**
 * 
 */
package games.stendhal.server.maps.ados.church;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;

import java.util.Arrays;
import java.util.Map;

/**
 * A praying NPC in ados church
 * 
 * @author madmetzger and storyteller
 */
public class VergerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] text = {"... Nie jesteś sam, ponieważ jest tutaj wiele osób, z którymi możesz się zaprzyjaźnić…" , "Nie martw się! Każdy ma czasem gorszy dzień…" , "…Po prostu myśl bardziej pozytywnie, a życie stanie się łatwiejsze…" , "… Pamiętaj: wszystko pójdzie dobrze…" , "Dziękuję, że przyszedłeś tutaj i spędziłeś ze mną trochę czasu." , "Zadbaj o nieszczęśliwych, w sercach smutnych zaszczep nadzieję…"};
		new MonologueBehaviour(buildNPC(zone), text, 3);
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Simon") {

			@Override
			protected void createDialog() {
				addGreeting("*szepcze* Witaj");
				addGoodbye("Żegnaj... I pamiętaj: życie zawsze gdzieś prowadzi... *szepcze* Amen...");
				addOffer("Dla niektórych ludzi pomocna jest modlitwa. Pozostałym trzeba zaoferować #pomoc innych ludzi. A Ty poczuj się wolnym i spytaj o nią. ");
				addHelp("Oh, potrzebujesz pomocy? Hmm... Mogę zasugerować Ci co robić, jeśli czujesz się źle lub jest Ci smutno. Po prostu #porozmawiaj z kimś o tym albo #zapisz swoje myśli. Innym sposobem, na poczucie się szczęśliwszym, jest bycie #miłym w stosunku do innych");
				addJob("Hmm... Obecnie nie pracuję. Dlatego trochę się martwię... Tutaj, w kościele, mogę spokojnie rozmyślać i dlatego przychodzę tu bardzo często. Mam nadzieję, że wpadnie mi do głowy świetny pomysł, jak zdobyć nową pracę. Modlę się za to...");
				addQuest("Nie mam żadnego zadania dla Ciebie, ale jeśli chcesz, możesz usiąść obok mnie i pomodlić się, może być miło.");
				addReply(Arrays.asList("talk", "porozmawiaj"),"Jeśli nie możesz sam rozwiązać problemów lub po prostu nie jesteś w stanie sobie z nimi poradzić, bardzo ważne jest, żebyś pogadał z kimś, komu ufasz. Powiedz mu, na czym polega Twój problem i dlaczego czujesz się źle. Ta osoba może spojrzeć na sytuację z boku i szybko znaleźć rozwiązanie. Czasami kłopotem jest brak kontaktu z drugą osobą, konieczność posiadania kogoś, kto jest po Twojej stronie, poczucie, że ktoś jest obok. Pamiętaj, że nie jesteś sam na tym świecie. ");
				addReply(Arrays.asList("friendly", "miłym"),"To prosta droga do bycia szczęśliwym: po prostu warto być miłym dla każdego! inni ludzie będą Cię lubić, gdy okażesz i sympatię, to samo także okażą Tobie. Uwierz mi, to jest naprawdę miłe uczucie znać ludzi, którzy Ciebie lubią i którzy stają się Twoimi przyjaciółmi. Przyjaciółmi, którzy czują, że jesteś wyjątkowo fajną osobą!");
				addReply(Arrays.asList("write", "zapisz"),"To może pomóc - spisanie wszystkich swoich myśli na papierze. Nie musi to być napisane perfekcyjnie, ponieważ nikt, poza Tobą, tego nie przeczyta. Po prostu napisz, co czujesz i o czym myślisz. Możesz wejrześ w głąb siebie i poznać swoje myśli stojąc na neutralnym gruncie. To pomoże Ci zrozumieć, co wywołuje Twój smutek i dlaczego się martwisz oraz pomoże Ci przeciwdziałać temu. Czasami problem rozwiązuje się sam, gdy podejmiesz jakąś decyzję związaną z nim.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.UP);
			}
		};

		npc.setEntityClass("vergernpc");
		npc.setDescription("Widzisz Simona. Ma przymknięte oczy i cicho się modli, choć czasem możesz usłyszeć pojedyńcze słowa odmawianej modlitwy.");
		npc.setPosition(29, 14);
		npc.setDirection(Direction.UP);
		npc.initHP(100);
		zone.add(npc);
		
		return npc;
	}

}
