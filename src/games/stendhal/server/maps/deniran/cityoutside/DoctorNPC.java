package games.stendhal.server.maps.deniran.cityoutside;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class DoctorNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("John Smith") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(72, 8));
				nodes.add(new Node(83, 8));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Znakomicie! Lubię ładne ogrodzenie! *niuch* *niuch* Wojna jest nieunikniona. Nie, nie lubię #wojen.");
				addJob("Jestem inspektorem ogrodzeń. Lub coś w tym stylu.");
				addOffer("Mogę zaoferować #poradę, która nawet ona jest bezpłatna.");
				addGoodbye("Bądź zdrów!");
				addReply(Arrays.asList("advice", "doctor", "promise", "kind", "cowardly", "pear", "pears", "porada", "rada", "lekarz", "obietnica", "życzliwy", "tchórzliwy", "gruszka", "gruszki"),
						"Nigdy nie bądź okrutny, nigdy nie bądź tchórzliwy. I nigdy nie jedz gruszek! Pamiętaj: nienawiść jest zawsze głupia, a miłość zawsze mądra.");
				addReply(Arrays.asList("faith", "love", "wedding", "marriage", "wiara", "miłość", "ślub", "małżeństwo"),
						"Coś, w co wierzę: Miłość we wszystkich jej formach jest najpotężniejszą bronią, ponieważ miłość jest formą nadziei.");
				addReply(Arrays.asList("war", "army", "fight", "gun", "attack", "wojna", "armia", "walka", "pistolet", "atak"),
						"Kiedy oddasz pierwszy strzał, bez względu na to, jak dobrze się czujesz, nie masz pojęcia, kto umrze! Nie wiesz, czyje dzieci będą krzyczeć i płonąć! Ile serc zostanie złamanych! Ile istnień zostało rozbitych! Ile krwi się przeleje, dopóki wszyscy nie zrobią tego, co zawsze mieli robić od samego początku! – Usiądź i porozmawiaj!");
				addReply(Arrays.asList("tardis", "blue", "box", "niebieski", "pudełko"),
						"Ukradłem to. Cóż, pożyczyłem to; Zawsze zamierzałem to cofnąć. Och, to pudełko. Duży i mały jednocześnie, zupełnie nowy i stary, i najbardziej niebieski w historii.");
				addReply(Arrays.asList("sonic", "screwdriver", "dźwiękowy", "śrubokręt"),
						"To śrubokręt dźwiękowy. Widzisz? Hałasuje.");
				addReply(Arrays.asList("regeneration", "death", "dying", "end", "change", "regeneracja", "śmierć", "umieranie", "koniec", "zmiana"),
						"Wszyscy się zmieniamy. Wszyscy jesteśmy różnymi ludźmi przez całe życie i to jest w porządku, to jest dobre. Musisz iść dalej. Dopóki pamiętasz wszystkich ludzi, z którymi przebywałeś.");
				addReply(Arrays.asList("good", "bad", "idiot", "mad", "dobry", "zły", "idiota", "szalony"),
						"Czy jestem dobrym człowiekiem?");
				addReply(Arrays.asList("fact", "facts", "fakt", "fakty"),
						"Bardzo potężni i bardzo głupi mają jedną wspólną cechę. Zamiast zmieniać swoje poglądy, by pasowały do ​​faktów, zmieniają fakty, by pasowały do ​​ich poglądów... Co może być bardzo niewygodne, jeśli jesteś jednym z faktów, który wymaga zmiany.");
				addReply(Arrays.asList("9", "nine", "ninth", "Christopher", "Eccleston", "dziewięć", "dziewiąty"),
						"Fantastycznie!");
				addReply(Arrays.asList("10", "ten", "David", "Tennant", "dziesięć"),
						"Chodźmy!");
				addReply(Arrays.asList("11", "eleven", "Matt", "Smith", "jedenaście"),
						"Nadchodzę!");
				addReply(Arrays.asList("13", "thirteen", "Jodie", "Whittaker", "trzynaście"),
						"Wspaniale!");
				addReply(Arrays.asList("bow", "łuk", "muszki"), "Muszki są spoko.");
				addReply(Arrays.asList("mate", "companion", "friend", "kumpel", "towarzysz", "przyjaciel"),
						"Och, przyjaciele! Przyjaciele są wspaniali! Mam wielu przyjaciół... i miałem. Niektórzy mnie zostawili. Niektórzy zostali w tyle. I niektórzy, nie wiele, ale niektórzy niestety zginęli.");
				addReply(Arrays.asList("trap", "pułapka"),
						"Jest jedna rzecz, której nigdy nie wkładasz w pułapkę? Jeśli jesteś mądry, jeśli cenisz sobie ciągłość swojej egzystencji, jeśli masz jakieś plany dotyczące zobaczenia jutra, jest jedna rzecz, której nigdy, przenigdy nie wpadłeś w pułapkę. - Ja.");
				addReply(Arrays.asList("time", "cause", "effect", "wibbly", "wobbly", "linear", "nonlinear", "czas", "przyczyna", "skutek", "wibrujący", "chwiejny", "liniowy", "nieliniowy"),
						"Ludzie zakładają, że czas jest ścisłym ciągiem przyczyny do skutku, ale tak naprawdę, z nieliniowego, niesubiektywnego punktu widzenia, jest to bardziej jak wielka kula chybotliwych, chwiejnych, czasowych... rzeczy.");
				addReply(Arrays.asList("motive", "motywacja"),
						"Robię to, co robię, bo to słuszne! Bo to jest przyzwoite! A przede wszystkim #miłość! Po prostu to. Po prostu miły.");
				addReply(Arrays.asList("childish", "dziecinny"),
						"Nie ma sensu być dorosłym, jeśli czasami nie potrafisz zachowywać się dziecinnie.");
				addReply(Arrays.asList("coincidence", "zbieg", "okoliczności"),
						"Nigdy nie ignoruj ​​zbiegów okoliczności. O ile oczywiście nie jesteś zajęty. W takim przypadku zawsze ignoruj ​​zbiegi okoliczności.");
				addReply(Arrays.asList("important", "ważny"),
						"Na przestrzeni 900 lat nigdy nie spotkałem nikogo, kto nie byłby ważny.");
			}
		};

		npc.setOutfit(0, 14, 0, 0, 0, 0, 32, 0, 0);
		npc.setDescription("Oto John Smith. Człowiek, który wydaje się być bardzo zainteresowany ogrodzeniem.");
		npc.setPosition(83, 8);
		npc.setGender("M");
		npc.initHP(100);
		zone.add(npc);
		return npc;
	}
}
