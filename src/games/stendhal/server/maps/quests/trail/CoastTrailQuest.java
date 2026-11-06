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

/** Follow a wreck's survivors and bring the next ship safely through the shoals. */
public final class CoastTrailQuest extends TrailQuest {
    public CoastTrailQuest(Step step) {
        super(step);if (!TrailQuestLine.COAST_CHAPTER.equals(step.chapter)) throw new IllegalArgumentException("Not a coast quest");
    }
    @Override protected void addDialogs() {
        switch(step) {
        case COAST_WRECK:
            offer("Idź plażą na wschód od przystani. Woda wyrzuciła tam skrzynię i strzępy olinowania. Obejrzyj je, a potem rozejrzyj się za ludźmi. "
                    + "Podobno ktoś siedział na piasku, kawałek dalej. Jeśli to człowiek z Mewy, nie zostawiaj go samego.");
            reply(step.npc,(p,s,n)->{
                if (report(p,n)) return;
                request(p,n,"Mark pisał, że można na tobie polegać. Przyda się ktoś taki. #'Zaginiona Mewa' miała przywieźć między innymi papiery kupca, który handlował z Radomirem. "
                        + "Ale papiery mogą poczekać. Na tym statku byli ludzie. Sprawdzisz, co wyrzuciło morze i czy ktoś ocalał?");
            },"zaginiona Mewa","zaginioną Mewę","zaginionej Mewie");
            reply(CoastTrailProgress.SAILOR,(p,s,n)->{
                if (CoastTrailProgress.advance(p,step,"survivor","report")) n.say("Tak, płynąłem Mewą. Zobaczyliśmy światło tam, gdzie miało być wejście do portu. "
                        + "Potem było szarpnięcie i deski pękały pod nogami. Ktoś już kręcił się przy naszych skrzyniach, zanim ucichły krzyki. "
                        + "Dowlokłem się tutaj o świcie. Powiedz Staszkowi, że żyję. Niech pośle ludzi wzdłuż brzegu. Może nie tylko ja wyszedłem z wody.");
                else remind(p,n);
            },"zaginiona Mewa","zaginioną Mewę","zaginionej Mewie");break;
        case COAST_LIGHTS:
            offer("Idź dalej na wschód, aż miniesz ujście rzeki i zabudowania portu. Na dalekim końcu plaży leży palenisko z blaszanymi osłonami. "
                    + "Obejrzyj je. Jeśli kręcą się tam rabusie, nie licz na spokojną rozmowę.");
            reply(step.npc,(p,s,n)->{
                if (report(p,n)) return;
                request(p,n,"To było #'fałszywe światło'. Znam ten brzeg. Nikt rozsądny nie pokazałby statkowi wejścia od tamtej strony. "
                        + "Nie zdołam tam teraz dojść, ledwo stoję na nogach. Sprawdzisz miejsce, z którego świecili?");
            },"fałszywe światło","falszywe swiatlo","fałszywym świetle");break;
        case COAST_CARGO:
            offer("Skład jest niedaleko muzeum, przy północnej części miasta. Zejdź do piwnicy i rozejrzyj się wśród beczek. "
                    + "Jeśli znajdziesz coś z Mewy, zapytaj Rosę w tawernie, kto to przynosił. Ona widzi więcej niż moi ludzie na warcie.");
            reply(step.npc,(p,s,n)->{
                if (report(p,n)) return;
                request(p,n,"Lubor przysłał mi wieść o palenisku. Jeśli to rabusie, muszą gdzieś sprzedawać #'ładunek z Mewy'. "
                        + "Ludzie widzieli ostatnio mokre skrzynie noszone do składu. Sprawdzisz je, zanim znikną między uczciwym towarem?");
            },"ładunek z Mewy","ladunek z Mewy","ładunku z Mewy");
            reply(CoastTrailProgress.BARMAID,(p,s,n)->{
                if (CoastTrailProgress.advance(p,step,"rosa","report")) n.say("Widziałam ich. Jednemu kapała woda z rękawa, a już pytał, kto kupi srebrne zapięcie i czyjś płaszcz. "
                        + "Mówili, że morze oddaje to, co zabierze. Ich herszt wyniósł potem skrzynię do składu i nie wrócił na salę. "
                        + "Podobno trzyma pieniądze w głębi piwnicy. Powiedz Malteisowi, że sama rozpoznam tych ludzi, jeśli trzeba.");
                else remind(p,n);
            },"ładunek z Mewy","ladunek z Mewy","ładunku z Mewy");break;
        case COAST_WRECKERS:
            offer("Zejdź znowu do piwnicy składu. Herszt zaszył się w jej najgłębszej części. Pokonaj go i zabezpiecz zapiski przy beczkach. "
                    + "Moich ludzi poślę na brzeg po rannych. Nie chcę, żeby następny statek wpadł w tę samą pułapkę.");
            reply(step.npc,(p,s,n)->{
                if (report(p,n)) return;
                request(p,n,"A więc ci #'rabusie wybrzeża' czekali na rozbicie. Dlatego tak szybko znaleźli się przy skrzyniach. "
                        + "Ich przywódca musi wiedzieć, na jakie statki polują. Zatrzymasz go i sprawdzisz, co przygotowali dalej?");
            },"rabusie wybrzeża","rabusie wybrzeza","rabusiów wybrzeża");break;
        case COAST_LAMPS:
            offer("Na północno-wschodniej plaży zostały mocowania dawnych lamp. Najpierw napraw linki pośrodku brzegu. "
                    + "Potem ustaw czerwoną lampę po zachodniej stronie, a na końcu zieloną po wschodniej. "
                    + "Tak umawialiśmy się z pilotem Jaskółki. Ostatni błysk damy dopiero wtedy, gdy droga będzie bezpieczna.");
            reply(step.npc,(p,s,n)->{
                if (report(p,n)) return;
                request(p,n,"Jaskółka wiezie pasażerów. Kapitan poczeka na pilota, ale gdy zobaczy światła rabusiów, może uznać, że trzeba zmienić kurs. "
                        + "Musimy pokazać mu #'bezpieczny brzeg'. Odzyskano osłony i liny. Pomożesz mi przywrócić nasz sygnał?");
            },"bezpieczny brzeg","bezpiecznym brzegu");break;
        case COAST_RETURN:
            offer("Widziano jeszcze jednego rabusia przy lampach w północno-wschodniej części brzegu. Pokonaj go, a potem daj błysk latarnią pilota. "
                    + "Lubor będzie patrzył na morze. Zapytaj go o #'bezpieczny kurs', zanim wrócisz do mnie.");
            reply(step.npc,(p,s,n)->{
                if (CoastTrailProgress.active(p,step)&&p.isQuestInState(step.slot,"ready")) {reward(p,n);return;}
                request(p,n,"Ludzie Malteisa zabezpieczyli skład. Ci, których znaleźliśmy na brzegu, dostali wodę i suche ubrania. "
                        + "Został jeszcze #'ratunek dla Jaskółki'. Dopilnujesz, żeby nikt nie zniszczył lamp, zanim statek wejdzie do portu?");
            },"ratunek dla Jaskółki","ratunek dla Jaskolki","ratunku dla Jaskółki");
            reply(CoastTrailProgress.SAILOR,(p,s,n)->{
                if (CoastTrailProgress.advance(p,step,"lookout","ready")) n.say("Widzę ją. Minęła mieliznę i skręca za pilotem. Płyną dobrym kursem. "
                        + "Tym razem nikt nie będzie wyławiał ludzi spomiędzy desek. Idź do Staszka i powiedz mu, że Jaskółka wraca. "
                        + "Ja tu jeszcze posiedzę. Dobrze znowu popatrzeć na statek, który dopływa do domu.");
                else remind(p,n);
            },"bezpieczny kurs","bezpiecznym kursie");break;
        default: throw new IllegalArgumentException("Not a coast quest");
        }
    }
    private boolean report(Player player,EventRaiser speaker) {
        if (!CoastTrailProgress.active(player,step)||!player.isQuestInState(step.slot,"report")) return false;
        complete(player,speaker,"report");return true;
    }
    private void reward(Player player,EventRaiser speaker) {
        Item cloak=SingletonRepository.getEntityManager().getItem("czarny płaszcz",
                ItemCreationContext.builder(ItemCreationContext.Source.QUEST).withQuestRarity(ItemRarity.RARE).generateAffixes(true).build());
        cloak.setBoundTo(player.getName());
        cloak.setDescription("Oto czarny płaszcz podarowany za ocalenie Jaskółki. Pod kołnierzem wyszyto niewielką mewę, ku pamięci ludzi uratowanych na gdańskim wybrzeżu.");
        if (!player.equip("bag",cloak)) {speaker.say("Mam dla ciebie płaszcz, ale twój plecak jest pełny. Zostaw trochę miejsca i wróć.");return;}
        StackableItem coins=(StackableItem)SingletonRepository.getEntityManager().getItem("money",ItemCreationContext.quest());coins.setQuantity(step.coins);
        if (player.getSlotToEquip(coins)==null) {
            player.getSlot("bag").remove(cloak.getID());speaker.say("Zostaw miejsce także na monety. Płaszcz i zapłata poczekają tutaj na ciebie.");return;
        }
        complete(player,speaker,"ready");
    }
    @Override protected void remind(Player player,EventRaiser speaker) {
        if (player.isQuestCompleted(step.slot)) {speaker.say("Dziękuję. Nie zapomnimy twojej pomocy.");return;}
        if (!player.hasQuest(step.slot)) {
            speaker.say(!player.isQuestCompleted(step.previousSlot())?"Najpierw dokończ sprawę, którą już się zajmujesz. Potem pomyślimy o reszcie."
                    :player.getLevel()<200?"Na tym brzegu niebezpieczni są nie tylko ludzie. Nabierz jeszcze doświadczenia, zanim ruszysz tropem rabusiów."
                    :"Porozmawiaj najpierw z osobą, która prosiła cię o pomoc. Powinna wiedzieć, że ruszasz w drogę.");return;
        }
        switch(step) {
        case COAST_WRECK: speaker.say("Obejrzyj skrzynię i strzępy lin na plaży na wschód od przystani. Kawałek dalej odpoczywa rozbitek. Zapytaj go o #'zaginioną Mewę'.");break;
        case COAST_LIGHTS: speaker.say("Fałszywe palenisko jest na dalekim końcu północno-wschodniej plaży. Rozejrzyj się przy nim i uważaj na ludzi, którzy go pilnują.");break;
        case COAST_CARGO: speaker.say("Ładunku szukaj w piwnicy składu, niedaleko muzeum. Rosa w tawernie może wiedzieć, kto wnosił tam skrzynie.");break;
        case COAST_WRECKERS: speaker.say("Herszt ukrywa się w głębi piwnicy. Zabezpiecz jego zapiski i wróć do Malteisa.");break;
        case COAST_LAMPS:
            speaker.say("report".equals(player.getQuest(step.slot,0))?"Lubor czeka na plaży przy przystani promu. Powiedz mu, że przygotowano #'bezpieczny brzeg'."
                    :"Najpierw linki pośrodku brzegu, potem czerwona lampa na zachodzie i zielona na wschodzie. Nie dawaj jeszcze ostatniego błysku.");break;
        case COAST_RETURN:
            String state=player.getQuest(step.slot,0);
            speaker.say("fight".equals(state)?"Ktoś nadal kręci się przy lampach. Dopilnuj, żeby ich nie zniszczył."
                    :"signal".equals(state)?"Lampy są bezpieczne. Daj błysk latarnią pilota, pośrodku północno-wschodniej plaży."
                    :"lookout".equals(state)?"Lubor patrzy na morze. Zapytaj go o #'bezpieczny kurs'."
                    :"Staszek czeka na przystani. Powiedz mu o #'ratunku dla Jaskółki'.");break;
        default: throw new IllegalArgumentException("Not a coast quest");
        }
    }
    @Override protected String completedText() {
        switch(step) {
        case COAST_WRECK: return "Poślę ludzi po Lubora i każę przeszukać brzeg. Dobrze, że ktoś ocalał. "
                + "Gdy odpocznie, zapytaj go o #'fałszywe światło'. Jeśli ktoś zwabił Mewę na mieliznę, nie wolno nam czekać na następny wrak.";
        case COAST_LIGHTS: return "Znak składu? A więc nie siedzą tylko na plaży. Wnoszą łup do miasta i udają kupców. "
                + "Malteis pilnuje Gdańska. Znajdziesz go w siedzibie rycerzy, w północno-wschodniej części miasta. Powiedz mu o #'ładunku z Mewy'.";
        case COAST_CARGO: return "Oznaczenia kupca Radomira... Mewa rzeczywiście wiozła te towary, ale nikt z jej załogi ich tu nie dostarczył. "
                + "Rosa wskazała nam ludzi, nie tylko skrzynie. Teraz trzeba zatrzymać tych #'rabusiów wybrzeża', zanim znów wyjdą na brzeg.";
        case COAST_WRECKERS: return "Jaskółka ma wejść do portu z następnym przypływem. Posyłam ludzi po resztę bandy. "
                + "Zanieś Luborowi odzyskane osłony i liny. Wie, jak pokazać statkowi #'bezpieczny brzeg'.";
        case COAST_LAMPS: return "Tak, o to chodziło. Pilot rozpozna obie lampy. Został ostatni sygnał, ale najpierw brzeg musi być spokojny. "
                + "Porozmawiaj ze Staszkiem o #'ratunku dla Jaskółki'. Z przystani łatwiej wypatrzyć, czy ktoś znowu idzie do lamp.";
        case COAST_RETURN: return "Jaskółka zacumowała. Widziałem, jak pasażerowie schodzili na pomost, i dopiero wtedy odetchnąłem. "
                + "Weź ten płaszcz i 2500 monet od uratowanych kupców. Niech dobrze ci służy. "
                + "A o Mewie będziemy pamiętać. Nie wszyscy doczekali pomocy.";
        default: throw new IllegalArgumentException("Not a coast quest");
        }
    }
}
