/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Restore the dead commander's good name and fulfil the promise made to his men. */
public final class OathTrailQuest extends TrailQuest {
    public OathTrailQuest(Step step) {super(step);if (!TrailQuestLine.OATH_CHAPTER.equals(step.chapter)) throw new IllegalArgumentException("Not an oath quest");}
    @Override protected void addDialogs() {
        switch(step) {
        case OATH_WATCH:
            offer("Szukaj ich na polach na północny zachód od Krakowa, tam, gdzie stary trakt skręca na północ. "
                    + "Wartownik stoi po wschodniej stronie drogi. Bądź ostrożny. Najpierw zasłania przejście bronią, dopiero potem patrzy, kto idzie.");
            reply(step.npc,(player,sentence,speaker)->{
                if (report(player,speaker,"report")) return;
                request(player,speaker,"Widywałem już różne rzeczy na drogach, ale ci #'polegli wartownicy' nie dają mi spokoju. "
                        + "Zatrzymują wozy i wypytują o dowódcę. Pod ich zbrojami widać ziemię, jakby byli z mgły. "
                        + "Nie chcę, żeby ktoś rzucił się na nich z widłami. Spróbujesz dowiedzieć się, na kogo czekają?");
            },"polegli wartownicy","poległych wartowników","poleglych wartownikow");
            reply(OathTrailProgress.SENTRY,(player,sentence,speaker)->{
                if (OathTrailProgress.advance(player,step,"witness","report")) speaker.say("Dość. Umiesz utrzymać broń, ale nie jesteś z naszego oddziału. "
                        + "Jestem Wszebor. Dowódca Borzywoj został przy przeprawie, żeby wozy zdążyły odejść. "
                        + "Obiecano nam, że po wszystkim wróci i zabierze nas do domu. Wciąż go nie ma. Znasz go?");
                else remind(player,speaker);
            },"polegli wartownicy","poległych wartowników");break;
        case OATH_NAMES:
            offer("Na północno-zachodnich polach, kawałek na zachód od wartownika, poszukaj resztek tarczy i kamienia z imionami. "
                    + "Odrysuj znak oraz litery. Nie chcę zgadywać, któremu oddziałowi przypisano te wydarzenia.");
            reply(step.npc,(player,sentence,speaker)->{
                if (report(player,speaker,"report")) return;
                request(player,speaker,"Wagabundus przysłał wieść o dawnych wojownikach. Imię Borzywoj coś mi mówi, "
                        + "ale w starych zapiskach ludzie często mieszali nazwiska z przydomkami. Obejrzysz #'znak oddziału' i spis poległych? "
                        + "Wtedy porównam je z tym, co mam w księgach.");
            },"znak oddziału","znak oddzialu","znaku oddziału");break;
        case OATH_GRAVE:
            offer("Idź na zachodnie pola pod Krakowem. Samotny grób jest w północno-wschodniej części tych pól. "
                    + "Pamiętam krzywo osadzony kamień i tarczę pod nim. Przyjrzyj się napisowi, ale jeszcze nie ruszaj szczątków.");
            reply(step.npc,(player,sentence,speaker)->{
                if (report(player,speaker,"report")) return;
                request(player,speaker,"Borzywoj... Widziałem jego #'samotny grób'. Napisano tam, że porzucił swoich ludzi. "
                        + "A przecież Wszebor mówi, że został przy przeprawie. Jedno do drugiego nie pasuje. Sprawdzisz tę mogiłę?");
            },"samotny grób","samotny grob","samotnym grobie");break;
        case OATH_TRUTH:
            offer("Niedaleko samotnego grobu, nieco na południe, zostały kamienie dawnego posterunku. "
                    + "Wartownicy chowali rozkazy w metalowych tubach, między kamieniami. Może pergamin nie rozsypał się jeszcze do końca.");
            reply(step.npc,(player,sentence,speaker)->{
                if (report(player,speaker,"report")) return;
                request(player,speaker,"Pozostał przy przeprawie, a jednak nazwano go zdrajcą... Tak łatwo skrzywdzić człowieka jednym zdaniem. "
                        + "Potrzebujemy czegoś więcej niż kolejnej opowieści. Odszukasz #'ostatni rozkaz', który dostał Borzywoj?");
            },"ostatni rozkaz","ostatnim rozkazie");break;
        case OATH_BANNER:
            offer("Chorągiew została na zachodnich polach, na południe od grobu Borzywoja. Pilnuje jej nasz chorąży. "
                    + "Przysięgał nie oddać jej żywym i słowa nie złamie, nawet teraz. Musisz go pokonać. Dopiero potem zabierz płótno i wróć do mnie.");
            reply(step.npc,(player,sentence,speaker)->{
                if (report(player,speaker,"report")) return;
                request(player,speaker,"Znam te słowa. Borzywoj powtarzał je, kiedy wysyłał nas na trakt. A więc nie odszedł bez nas... "
                        + "To my nie wróciliśmy po niego. Jeśli ma spocząć z oddziałem, potrzebna jest #'chorągiew oddziału'. Przyniesiesz ją?");
            },"chorągiew oddziału","choragiew oddzialu","chorągwi oddziału");break;
        case OATH_BURIAL:
            offer("Wróć do samotnego grobu i przygotuj szczątki Borzywoja do przeniesienia. "
                    + "Wspólna mogiła jest na północno-zachodnich polach, kawałek na północ od Wszebora. "
                    + "Najpierw złóż tam dowódcę, potem umieść chorągiew przy kamieniu obok. Porozmawiaj z Wszeborem, gdy skończysz. "
                    + "Ja zaniosę tarczę Amileuszowi, żeby ją oczyścił. To jedyne, co jeszcze może posłużyć żywym.");
            reply(step.npc,(player,sentence,speaker)->{
                if (player.isQuestInState(step.slot,"ready") && OathTrailProgress.active(player,step)) {reward(player,speaker);return;}
                request(player,speaker,"Znamy ich imiona i wiemy już, co się stało. Na tym jednak nie możemy skończyć. "
                        + "Ludzie dali im słowo, że pochowają ich razem. Doprowadzisz #'wspólny pochówek' do końca? Przygotowałem płótno na szczątki dowódcy.");
            },"wspólny pochówek","wspolny pochowek","wspólnym pochówku");
            reply(OathTrailProgress.SENTRY,(player,sentence,speaker)->{
                if (OathTrailProgress.advance(player,step,"release","ready")) speaker.say("Już nie słyszę, jak nawołują przy drodze. "
                        + "Dowódca jest z nimi. Mogą odejść. Dziękuję ci. Ja zostanę przy mogile, ale z własnej woli. "
                        + "Ktoś musi przypomnieć przechodniom, czyje imiona wyryto w kamieniu. Powiedz Wagabundusowi, że słowo zostało dotrzymane.");
                else remind(player,speaker);
            },"dotrzymane słowo","dotrzymane slowo","dotrzymanym słowie");break;
        default: throw new IllegalArgumentException("Not an oath quest");
        }
    }
    private boolean report(Player player,EventRaiser speaker,String state) {
        if (!OathTrailProgress.active(player,step) || !player.isQuestInState(step.slot,state)) return false;
        complete(player,speaker,state);return true;
    }
    private void reward(Player player,EventRaiser speaker) {
        Item item=SingletonRepository.getEntityManager().getItem("magiczna tarcza płytowa",ItemCreationContext.builder(ItemCreationContext.Source.QUEST)
                .withQuestRarity(ItemRarity.RARE).generateAffixes(true).build());
        item.setBoundTo(player.getName());
        item.setDescription("Oto magiczna tarcza płytowa Borzywoja. Na oczyszczonym obrzeżu wyryto: Pozostał przy przeprawie do końca.");
        if (!player.equip("bag",item)) {speaker.say("Tarcza Borzywoja czeka na ciebie, ale nie zmieści się w pełnym plecaku. Zostaw trochę miejsca i wróć.");return;}
        complete(player,speaker,"ready");
    }
    @Override protected void remind(Player player,EventRaiser speaker) {
        if (player.isQuestCompleted(step.slot)) {speaker.say("Dziękuję. Nie zapomnę twojej pomocy.");return;}
        if (!player.hasQuest(step.slot)) {
            speaker.say(!player.isQuestCompleted(step.previousSlot())?"Nie znam jeszcze całej tej historii. Zajmijmy się najpierw tym, co już zaczęliśmy."
                    :player.getLevel()<100?"Na tym trakcie łatwo stracić życie. Nabierz jeszcze doświadczenia, zanim weźmiesz na siebie sprawę poległych."
                    :"Wróć do tego, kto prosił cię o pomoc. Powinien wiedzieć, że ruszasz w drogę.");return;
        }
        switch(step) {
        case OATH_WATCH: speaker.say("Wszebor czeka na północno-zachodnich polach. Dopóki jego wartownik zastępuje ci drogę, nie uzna cię za kogoś, komu warto zaufać.");break;
        case OATH_NAMES: speaker.say("Potrzebuję znaku z resztek tarczy i imion wyrytych na kamieniu. Oba są na zachód od Wszebora.");break;
        case OATH_GRAVE: speaker.say("Samotny grób leży na zachodnich polach, w ich północno-wschodniej części. Zobacz, co zostało pod późniejszym napisem.");break;
        case OATH_TRUTH: speaker.say("Przy samotnym grobie, nieco na południe, zostały kamienie posterunku. Szukaj między nimi tuby na rozkazy.");break;
        case OATH_BANNER: speaker.say("Chorąży wciąż trzyma wartę na południu zachodnich pól. Pokonaj go, a potem zabierz chorągiew, której bronił.");break;
        case OATH_BURIAL:
            String state=player.getQuest(step.slot,0);
            speaker.say("remains".equals(state)?"Najpierw przygotuj szczątki z samotnego grobu Borzywoja. Jego ludzie spoczywają na północno-zachodnich polach."
                    :"burial".equals(state)?"Dowódca powinien spocząć ze swoimi ludźmi. Dopiero wtedy złóż przy mogile chorągiew."
                    :"release".equals(state)?"Wszebor czeka przy trakcie. Porozmawiaj z nim o #'dotrzymanym słowie'."
                    :"Wagabundus czeka na południe od Krakowa. Powiedz mu, co stało się przy mogile.");break;
        default: throw new IllegalArgumentException("Not an oath quest");
        }
    }
    @Override protected String completedText() {
        switch(step) {
        case OATH_WATCH: return "Borzywoj? Dawno nie słyszałem tego imienia. Amileusz w Krakowie zna dawne znaki i uzbrojenie. "
                + "Zapytaj go o #'znak oddziału'. Może znajdzie coś o tych ludziach.";
        case OATH_NAMES: return "Znak się zgadza. To ludzie, którzy osłaniali dawną przeprawę. Przy nazwisku dowódcy zapisano dezercję, "
                + "ale jego śmierci nie wyjaśniono. Wagabundus zna te trakty. Zapytaj go o #'samotny grób'.";
        case OATH_GRAVE: return "A więc starszy napis mówi coś zupełnie innego. Ktoś oskarżył go później. "
                + "Amileusz powinien o tym usłyszeć. Zapytaj go o #'ostatni rozkaz'.";
        case OATH_TRUTH: return "To rozkaz utrzymania przeprawy do odejścia ostatniego wozu. Borzywoj został tam, gdzie miał zostać. "
                + "Ludzie ocaleli i poszli dalej, a po oddział już nie wrócili. Pokaż to Wszeborowi i zapytaj o #'chorągiew oddziału'.";
        case OATH_BANNER: return "Poznaję to płótno. Pod nim staliśmy przy przeprawie. Teraz może wrócić do Borzywoja. "
                + "Wagabundus zna obyczaje żywych. Poproś go o #'wspólny pochówek'.";
        case OATH_BURIAL: return "Dobrze, że wracasz. Od rana nikt nie słyszał nawoływań na trakcie. "
                + "Dopilnuję, żeby tym razem opowiadano prawdę o Borzywoju. Amileusz oczyścił jego tarczę. "
                + "Weź ją i 600 monet. Niech służy komuś, kto dotrzymuje słowa.";
        default: throw new IllegalArgumentException("Not an oath quest");
        }
    }
}
