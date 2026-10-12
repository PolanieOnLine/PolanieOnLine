package games.stendhal.common.grammar;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Polish item names for trade dialogue, independent of command parsing. */
final class PolishItemGrammar {
	private enum Gender { MASCULINE, FEMININE, NEUTER, PLURAL }
	private enum Unit {
		PIECE("sztukę", "sztuki", "sztuk"), PAIR("parę", "pary", "par"),
		PORTION("porcję", "porcje", "porcji"), SET("zestaw", "zestawy", "zestawów"),
		BOUQUET("bukiet", "bukiety", "bukietów");
		private final String one, few, many;
		Unit(String one, String few, String many) { this.one = one; this.few = few; this.many = many; }
		String forAmount(int amount) {
			return amount == 1 ? one : few(amount) ? few : many;
		}
	}
	private static final class Noun {
		private final Gender gender;
		private final String genitive, accusative;
		private final Unit unit;
		private String pluralFew, pluralMany;
		Noun(Gender gender, String genitive, String accusative, Unit unit) {
			this.gender = gender; this.genitive = genitive; this.accusative = accusative; this.unit = unit;
		}
	}
	private static final class Adjective {
		private final String masculine, feminine;
		Adjective(String masculine, String feminine) { this.masculine = masculine; this.feminine = feminine; }
		String genitive(Gender gender) {
			if (gender == Gender.PLURAL) {
				return masculine.substring(0, masculine.length() - 1)
						+ (masculine.endsWith("i") ? "ich" : "ych");
			}
			if (gender == Gender.FEMININE) {
				return feminine.substring(0, feminine.length() - 1)
						+ ((feminine.endsWith("ka") || feminine.endsWith("ga")) ? "iej" : "ej");
			}
			return masculine.substring(0, masculine.length() - 1)
					+ (masculine.endsWith("i") ? "iego" : "ego");
		}
		String accusative(Gender gender, String original) {
			return gender == Gender.FEMININE ? feminine.substring(0, feminine.length() - 1) + "ą" : original;
		}
		String pluralFew() {
			return masculine.substring(0, masculine.length() - 1) + (masculine.endsWith("i") ? "ie" : "e");
		}
	}
	private static final Map<String, Noun> NOUNS = new HashMap<String, Noun>();
	private static final Map<String, Adjective> ADJECTIVES = new HashMap<String, Adjective>();
	static {
		nouns(Gender.MASCULINE, Unit.PIECE,
			"ametyst=ametystu;rubin=rubinu;szafir=szafiru;szmaragd=szmaragdu;diament=diamentu;obsydian=obsydianu;"
			+ "bursztyn=bursztynu;granat=granatu;węgiel=węgla;glif=glifu;kryształ=kryształu;fragment=fragmentu;"
			+ "miecz=miecza;mieczyk=mieczyka;sztylet=sztyletu;sztylecik=sztylecika;hełm=hełmu;pas=pasa;"
			+ "płaszcz=płaszcza;pierścień=pierścienia;topór=topora;toporek=toporka;łuk=łuku;kilof=kilofa;"
			+ "amulet=amuletu;amulecik=amulecika;naszyjnik=naszyjnika;kirys=kirysu;gorset=gorsetu;kapelusz=kapelusza;"
			+ "młot=młota;buzdygan=buzdyganu;kiścień=kiścienia;bełt=bełtu;kropacz=kropacza;berdysz=berdysza;"
			+ "bułat=bułata;chopesz=chopesza;pałasz=pałasza;pyrlik=pyrlika;piernacz=piernacza;puklerz=puklerza;"
			+ "sierp=sierpa;szczerbiec=szczerbca;ekskalibur=ekskalibura;saks=saksa;shuriken=shurikenu;bicz=bicza;"
			+ "trójząb=trójzębu;nóż=noża;nożyk=nożyka;kij=kija;klucz=klucza;kluczyk=kluczyka;wykrywacz=wykrywacza;"
			+ "rzemyk=rzemyka;ząb=zęba;kieł=kła;pazur=pazura;róg=rogu;gruczoł=gruczołu;dziób=dzioba;"
			+ "zwój=zwoju;talizman=talizmanu;eliksir=eliksiru;napój=napoju;antyjad=antyjadu;ekstrakt=ekstraktu;"
			+ "olejek=olejku;bukłak=bukłaka;worek=worka;moździerz=moździerza;młynek=młynka;koszyk=koszyka;"
			+ "puchar=pucharu;krzyżyk=krzyżyka;bilet=biletu;kupon=kuponu;żeton=żetonu;wskaźnik=wskaźnika;"
			+ "balonik=balonika;pionek=pionka;goniec=gońca;hetman=hetmana;król=króla;skoczek=skoczka;"
			+ "bestiariusz=bestiariusza;obraz=obrazu;spis=spisu;prezent=prezentu;posiłek=posiłku;"
			+ "chleb=chleba;ser=sera;szpinak=szpinaku;czosnek=czosnku;sok=soku;ocet=octu;cukier=cukru;"
			+ "miód=miodu;wosk=wosku;mech=mchu;stek=steku;hotdog=hotdoga:hotdoga;shake=shake'a;"
			+ "pączek=pączka:pączka;cukierek=cukierka:cukierka;lizak=lizaka:lizaka;"
			+ "ananas=ananasa:ananasa;arbuz=arbuza:arbuza;banan=banana:banana;pomidor=pomidora:pomidora;"
			+ "brokuł=brokuła:brokuła;kalafior=kalafiora:kalafiora;karczoch=karczocha:karczocha;kokos=kokosa:kokosa;"
			+ "orzech=orzecha:orzecha;borowik=borowika:borowika;muchomor=muchomora:muchomora;oscypek=oscypka:oscypka;"
			+ "karp=karpia:karpia;pstrąg=pstrąga:pstrąga;tuńczyk=tuńczyka:tuńczyka;dorsz=dorsza:dorsza;"
			+ "leszcz=leszcza:leszcza;okoń=okonia:okonia;szczupak=szczupaka:szczupaka;błazenek=błazenka:błazenka;"
			+ "pokolec=pokolca:pokolca;baran=barana:barana;miś=misia:misia;bratek=bratka;żonkil=żonkila;"
			+ "krwiopijca=krwiopijcy:krwiopijcę;pogromca=pogromcy:pogromcę;dukat=dukata;talar=talara;miedziak=miedziaka;cat=kota:kota;"
			+ "kamyczek=kamyczka;kamień=kamienia;środek=środka;por=pora:pora;jabłecznik=jabłecznika;"
			+ "wianek=wianka;klejnot=klejnotu;półtorak=półtoraka");
		nouns(Gender.FEMININE, Unit.PIECE,
			"tarcza=tarczy;zbroja=zbroi;kolczuga=kolczugi;peleryna=peleryny;ciupaga=ciupagi;klinga=klingi;"
			+ "kosa=kosy;kusza=kuszy;różdżka=różdżki;rózga=rózgi;maczuga=maczugi;halabarda=halabardy;"
			+ "katana=katany;proca=procy;siekiera=siekiery;siekierka=siekierki;maczeta=maczety;włócznia=włóczni;"
			+ "strzała=strzały;skóra=skóry;nić=nici;przędza=przędzy;sztabka=sztabki;bryłka=bryłki;bryła=bryły;ruda=rudy;"
			+ "trucizna=trucizny;butla=butli;butelka=butelki;buteleczka=buteleczki;fiolka=fiolki;czara=czary;"
			+ "księga=księgi;magia=magii;skrzynia=skrzyni;skrzynka=skrzynki;koperta=koperty;karteczka=karteczki;"
			+ "misa=misy;piłka=piłki;spinka=spinki;brosza=broszy;igła=igły;łopata=łopaty;wędka=wędki;"
			+ "pułapka=pułapki;szpulka=szpulki;świeca=świecy;latarenka=latarenki;licencja=licencji;mapa=mapy;"
			+ "spódnica=spódnicy;koszula=koszuli;cuha=cuhy;chusta=chusty;misiurka=misiurki;skarpeta=skarpety;"
			+ "perła=perły;kostka=kostki;bila=bili;wieża=wieży;damka=damki;figura=figury;"
			+ "szynka=szynki;mąka=mąki;cebula=cebuli;kapusta=kapusty;papryka=papryki;dynia=dyni;"
			+ "cukinia=cukinii;cytryna=cytryny;pomarańcza=pomarańczy;gruszka=gruszki;gruszeńka=gruszeńki;"
			+ "marchew=marchwi:marchew;rzepa=rzepy;rzodkiewka=rzodkiewki;sałata=sałaty;"
			+ "kiełbasa=kiełbasy;paróweczka=paróweczki;truskawka=truskawki;wisienka=wisienki;"
			+ "kukurydza=kukurydzy;fasola=fasoli;oliwka=oliwki;kanapka=kanapki;pizza=pizzy;tarta=tarty;"
			+ "zupa=zupy;filiżanka=filiżanki;tabliczka=tabliczki;mufinka=mufinki;osełka=osełki;"
			+ "makrela=makreli;palia=palii;płotka=płotki;skrzydlica=skrzydlicy;"
			+ "muszelka=muszelki;śnieżka=śnieżki;mandragora=mandragory;arandula=aranduli;kokuda=kokudy;"
			+ "sclaria=sclarii;grudziarka=grudziarki;bielikrasa=bielikrasy;koniczyna=koniczyny;babka=babki;mięta=mięty;"
			+ "pokrzywa=pokrzywy;przylaszczka=przylaszczki;lilia=lilii;róża=róży;orchidea=orchidei;"
			+ "pieczarka=pieczarki;opieńka=opieńki;lukrecja=lukrecji;laska=laski;trzcina=trzciny;bulwa=bulwy;"
			+ "lina=liny;puciera=puciery;zima=zimy;krew=krwi:krew;sól=soli:sól;kiść=kiści;kość=kości;money=monety:monetę");
		nouns(Gender.NEUTER, Unit.PIECE,
			"polano=polana;mięso=mięsa;pióro=pióra;piórko=piórka;serce=serca;jajo=jaja;jabłko=jabłka;"
			+ "jabłuszko=jabłuszka;sukno=sukna;futro=futra;włosie=włosia;ciasto=ciasta;udko=udka;"
			+ "truchło=truchła;kółko=kółka;zboże=zboża;zaklęcie=zaklęcia;antidotum=antidotum;nihonto=nihonto");
		nouns(Gender.PLURAL, Unit.PAIR,
			"spodnie=spodni;rękawice=rękawic;buty=butów;kierpce=kierpców;portki=portek;nożyczki=nożyczek");
		nouns(Gender.PLURAL, Unit.SET,
			"karty=kart;kości=kości;korale=korali;wytrychy=wytrychów;pazury=pazurów;kamienie=kamieni;kulki=kulek;dwojaki=dwojaków");
		nouns(Gender.PLURAL, Unit.PORTION,
			"lody=lodów;ziemniaki=ziemniaków;naleśniki=naleśników;borówki=borówek;winogrona=winogron;"
			+ "żelki=żelków;nasiona=nasion;fasolki=fasolek;skorupki=skorupek;wymioty=wymiotów");
		nouns(Gender.PLURAL, Unit.BOUQUET, "stokrotki=stokrotek;niezapominajki=niezapominajek");
		nouns(Gender.MASCULINE, Unit.PORTION,
			"cukier=cukru;miód=miodu;sok=soku;ocet=octu;jad=jadu;szpinak=szpinaku;kekik=kekiku");
		nouns(Gender.FEMININE, Unit.PORTION,
			"mąka=mąki;oliwa=oliwy;woda=wody;wełna=wełny;siarka=siarki;mana=many;nalewka=nalewki;żyntyca=żyntycy;krew=krwi:krew;zupa=zupy");
		nouns(Gender.NEUTER, Unit.PORTION, "mleko=mleka");
		adjectives("złoty srebrny brązowy czarny biały czerwony niebieski zielony żółty różowy purpurowy fioletowy "
			+ "skórzany kolczy płytowy elficki kamienny karmazynowy krasnoludzki lazurowy wampirzy żelazny "
			+ "ciemnomithrilowy mainiocyjski monarchistyczny xenocyjski libertyński szmaragdowy "
			+ "magiczny ognisty lodowy smoczy góralski polski drewniany stalowy obsydianowy kościany futrzany olbrzymi "
			+ "wzmocniony twardy zwykły lekki średni ciężki mały duży wielki gigantyczny krótki długi "
			+ "antyczny bojowy dwuręczny jednoręczny obosieczny leworęczny praworęczny treningowy zaczepny "
			+ "krwawy królewski piekielny mroczny mgielny pustynny lwi niedźwiedzi tygrysi wilczy koński "
			+ "nabijany okrągły kwadratowy trójkątny wysoki wąski mocny śmiertelny zabójczy "
			+ "zatruty płonący pordzewiały zardzewiały zdobyczny niezapisany zapisany czyszczący weselny "
			+ "gdański krakowski tatrzański otwarty zaklejony zalakowany pusty jaśniejący "
			+ "ciężki cierniowy ćwiekowy piaskowy łuskowy akwalungowy piracki reniferowy jadowy przędzowy "
			+ "owocowy jabłkowy czekoladowy waniliowy miętowy kwaśny serowy wiejski litworowy leżakowany "
			+ "alpejski pekiński miodowy lekarski czterolistny gotowany grillowany wędzony prasowany "
			+ "obrotowy nakrapiany mityczny wielkanocny straszny smoczy drzewny pszczeli "
			+ "święcony uspokajający zaczarowany zagubiony zaklęty prążkowany egipski cukierkowy cudowny kolorowy "
			+ "bukowy cesarski cukrowy grzybowy jedwabny klejony pitny pluszowy rybny skromny startowy "
			+ "turystyczny wypchany włoski zbójnicki");
		pluralForms("miecz=miecze:mieczy;sztylet=sztylety:sztyletów;sztylecik=sztyleciki:sztylecików;"
			+ "hełm=hełmy:hełmów;pas=pasy:pasów;płaszcz=płaszcze:płaszczy;pierścień=pierścienie:pierścieni;"
			+ "topór=topory:toporów;toporek=toporki:toporków;łuk=łuki:łuków;kilof=kilofy:kilofów;"
			+ "amulet=amulety:amuletów;amulecik=amuleciki:amulecików;naszyjnik=naszyjniki:naszyjników;"
			+ "kirys=kirysy:kirysów;gorset=gorsety:gorsetów;kapelusz=kapelusze:kapeluszy;młot=młoty:młotów;"
			+ "buzdygan=buzdygany:buzdyganów;bełt=bełty:bełtów;zwój=zwoje:zwojów;klucz=klucze:kluczy;"
			+ "tarcza=tarcze:tarcz;zbroja=zbroje:zbroi;kolczuga=kolczugi:kolczug;peleryna=peleryny:peleryn;"
			+ "ciupaga=ciupagi:ciupag;klinga=klingi:kling;kosa=kosy:kos;kusza=kusze:kusz;różdżka=różdżki:różdżek;"
			+ "maczuga=maczugi:maczug;halabarda=halabardy:halabard;siekiera=siekiery:siekier;strzała=strzały:strzał;"
			+ "sztabka=sztabki:sztabek;bryłka=bryłki:bryłek;skóra=skóry:skór;księga=księgi:ksiąg;"
			+ "skrzynia=skrzynie:skrzyń;skrzynka=skrzynki:skrzynek;butelka=butelki:butelek;butla=butle:butli;"
			+ "polano=polana:polan;jabłko=jabłka:jabłek;serce=serca:serc;pióro=pióra:piór;jajo=jaja:jaj;"
			+ "borowik=borowiki:borowików;róża=róże:róż;tabliczka=tabliczki:tabliczek;cat=koty:kotów;money=monety:monet;"
			+ "kamień=kamienie:kamieni;kamyczek=kamyczki:kamyczków;środek=środki:środków;kość=kości:kości;kiść=kiście:kiści");
	}

	private PolishItemGrammar() { }

	private static void nouns(Gender gender, Unit unit, String entries) {
		for (String entry : entries.split(";")) {
			String[] pair = entry.split("=", 2);
			String[] forms = pair[1].split(":", 2);
			String accusative = forms.length > 1 ? forms[1]
					: gender == Gender.FEMININE && pair[0].endsWith("a")
					? pair[0].substring(0, pair[0].length() - 1) + "ę" : pair[0];
			NOUNS.put(pair[0], new Noun(gender, forms[0], accusative, unit));
		}
	}

	private static void adjectives(String entries) {
		for (String masculine : entries.split(" ")) {
			String stem = masculine.substring(0, masculine.length() - 1);
			boolean soft = masculine.endsWith("i") && !masculine.endsWith("ki") && !masculine.endsWith("gi");
			String feminine = stem + (soft ? "ia" : "a");
			Adjective adjective = new Adjective(masculine, feminine);
			ADJECTIVES.put(masculine, adjective);
			ADJECTIVES.put(feminine, adjective);
			ADJECTIVES.put(stem + (masculine.endsWith("i") ? "ie" : "e"), adjective);
		}
	}

	private static void pluralForms(String entries) {
		for (String entry : entries.split(";")) {
			String[] pair = entry.split("=", 2), forms = pair[1].split(":", 2);
			Noun noun = NOUNS.get(pair[0]);
			noun.pluralFew = forms[0]; noun.pluralMany = forms[1];
		}
	}

	private static boolean few(int amount) {
		int last = amount % 10, lastTwo = amount % 100;
		return last >= 2 && last <= 4 && (lastTwo < 12 || lastTwo > 14);
	}

	static String accusativeQuantity(int amount, String name) {
		if (name == null || name.trim().isEmpty()) return amount == 1 ? "przedmiot" : amount + " przedmiotów";
		String[] words = name.trim().split("\\s+");
		int head = -1, stop = words.length;
		for (int i = 0; i < words.length; i++) {
			String word = words[i].toLowerCase(Locale.ROOT);
			if (word.matches("z|ze|do|od|na|dla|bez|w|we")) { stop = i; break; }
			if (head == -1 && NOUNS.containsKey(word)) head = i;
		}
		if (head == -1) return amount == 1 ? name.trim() : amount + " " + Unit.PIECE.forAmount(amount) + " " + name.trim();
		String headName = words[head].toLowerCase(Locale.ROOT);
		Noun noun = NOUNS.get(headName);
		boolean counted = amount != 1 && noun.pluralFew != null;
		boolean genitive = !counted && (amount != 1 || noun.unit != Unit.PIECE);
		for (int i = 0; i < stop; i++) {
			String word = words[i].toLowerCase(Locale.ROOT), changed = words[i];
			if (i == head) changed = counted ? (few(amount) ? noun.pluralFew : noun.pluralMany)
					: genitive ? noun.genitive : noun.accusative;
			else if (ADJECTIVES.containsKey(word)) {
				// In "sztabka złota", złota names a material, not a golden adjective.
				if (i > head && word.equals("złota") && headName.matches("sztabka|bryłka|bryła|ruda|kryształ")) continue;
				Adjective adjective = ADJECTIVES.get(word);
				changed = counted ? (few(amount) ? adjective.pluralFew() : adjective.genitive(Gender.PLURAL))
						: genitive ? adjective.genitive(noun.gender) : adjective.accusative(noun.gender, words[i]);
			}
			if (Character.isUpperCase(words[i].charAt(0))) changed = Grammar.capitalize(changed);
			words[i] = changed;
		}
		String result = String.join(" ", words);
		if (counted) return amount + " " + result;
		if (!genitive) return result;
		return (amount == 1 ? "" : amount + " ") + noun.unit.forAmount(amount) + " " + result;
	}
}
