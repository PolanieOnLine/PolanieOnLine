package games.stendhal.server.actions.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.json.simple.JSONValue;
import games.stendhal.common.constants.Testing;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.RebornSystem;
import games.stendhal.server.maps.quests.IQuest;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** Data-only administrator snapshot; strings are rendered literally by both clients. */
final class AdminInspectionSnapshot {
    private AdminInspectionSnapshot() { }
    static String title(final Entity target) {
        String name=target.getName();
        if(name==null||name.isEmpty())name=target.getTitle();
        return name==null||name.isEmpty()?"Obiekt":name;
    }
    static String request(final RPAction original, final Entity target, final Player viewer) {
        Map<String,Object> request=new LinkedHashMap<>(); request.put("type","inspect"); request.put("visual","1");
        if(target instanceof Player || target instanceof SpeakerNPC) {
            request.put("target",target.getName());return JSONValue.toJSONString(request);
        }
        if(viewer.getZone()!=null) request.put("inspection_zone",viewer.getZone().getName());
        for(String key:new String[]{"target","baseobject","baseslot","baseitem"}) {
            if(original.has(key)) request.put(key,original.get(key));
        }
        if(original.has("target_path")) request.put("target_path",original.getList("target_path"));
        return JSONValue.toJSONString(request);
    }
    static String create(final Entity target) {
        List<Object> sections=new ArrayList<>(); List<List<String>> summary=new ArrayList<>();
        row(summary,"Nazwa",title(target)); row(summary,"Typ",target.has("type")?target.get("type"):"");
        row(summary,"ID",target.has("id")?target.getInt("id"):"");
        row(summary,"Klasa",target.getClass().getName());
        row(summary,"Lokalizacja",target.getZone()==null?"W ekwipunku":target.getZone().getName()+" ("+target.getX()+", "+target.getY()+")");
        if(target instanceof RPEntity) {
            RPEntity actor=(RPEntity)target;
            row(summary,"PZ",actor.getHP()+" / "+actor.getBaseHP());
            row(summary,"Opancerzenie",actor.getArmorHP()+" / "+actor.getBaseArmorHP());
            row(summary,"Poziom",actor.getLevel()); row(summary,"PD",actor.getXP());
            row(summary,"ATK",actor.getAtk()); row(summary,"PD ataku",actor.getAtkXP());
            row(summary,"OBR",actor.getDef()); row(summary,"PD obrony",actor.getDefXP());
            if(Testing.COMBAT) {row(summary,"Atak dystansowy",actor.getRatk());row(summary,"PD ataku dystansowego",actor.getRatkXP());}
            row(summary,"Górnictwo",actor.getMining());row(summary,"PD górnictwa",actor.getMiningXP());
            row(summary,"Mana",actor.getMana()+" / "+actor.getBaseMana()); row(summary,"Karma",actor.getKarma());
            if(Testing.WEIGHT)row(summary,"Udźwig",actor.getCapacity()+" / "+actor.getBaseCapacity()+" kg");
            row(summary,"Płeć",actor.getGender()); row(summary,"Widoczność",actor.getVisibility());
            if(actor instanceof Player) {
                Player player=(Player)actor; int reborn=RebornSystem.getRebornCount(player);
                row(summary,"Odrodzenia",reborn);row(summary,"Premia do limitów umiejętności",RebornSystem.getSkillCapBonus(player));
                row(summary,"Premia zdrowia z odrodzeń",RebornSystem.getHealthBonus(player));
                row(summary,"Doświadczenie z potworów",RebornSystem.getCombatExperiencePercentForRebornCount(reborn)+"%");
                row(summary,"Nagrody odrodzeń",RebornSystem.getClaimedRewardCount(player)+" / 3");
            }
            if(actor instanceof SpeakerNPC) row(summary,"Stan rozmowy",((SpeakerNPC)actor).getEngine().getCurrentState());
        }
        sections.add(section("summary","Podsumowanie",Arrays.asList("Parametr","Wartość"),summary,null));
        if(target instanceof RPEntity) {
            List<List<String>> resistance=new ArrayList<>(), appearance=new ArrayList<>();
            row(resistance,"Odporności",((RPEntity)target).getResistance());
            for(String key:new TreeSet<String>(attributes(target))) {
                if(key.startsWith("resist_")) row(resistance,key,target.get(key));
                if(key.startsWith("outfit")||key.equals("class")||key.equals("age")) row(appearance,key,target.get(key));
            }
            sections.add(section("resistance","Odporności",Arrays.asList("Parametr","Wartość"),resistance,null));
            if(!appearance.isEmpty()) sections.add(section("appearance","Wygląd",Arrays.asList("Parametr","Wartość"),appearance,null));
            List<List<String>> inventory=new ArrayList<>(); List<String> details=new ArrayList<>();
            for(RPSlot slot:target.slots()) {
                if(slot.getName().startsWith("!")||slot.getName().equals("skills")||slot.getName().equals("spells")) continue;
                for(RPObject object:slot) if(object instanceof Item) {
                    Item item=(Item)object;
                    inventory.add(Arrays.asList(slot.getName(),item.getName(),Integer.toString(item.getQuantity()),
                        item.getRarity()==null?"":item.getRarity().getPolishDisplayName(),item.has("improve")?item.get("improve"):"0"));
                    details.add(ItemInspectionFormatter.format(item));
                }
            }
            sections.add(section("inventory","Wyposażenie",Arrays.asList("Slot","Przedmiot","Ilość","Rzadkość","Ulepszenie"),inventory,details));
            if(target instanceof Player) {
                Player player=(Player)target;List<List<String>> quests=new ArrayList<>();
                for(String key:new TreeSet<String>(player.getQuests())) {
                    IQuest quest=SingletonRepository.getStendhalQuestSystem().getQuestFromSlot(key);
                    quests.add(Arrays.asList(quest==null?key:quest.getName(),key,player.getQuest(key)));
                }
                sections.add(section("quests","Zadania",Arrays.asList("Zadanie","Klucz","Stan"),quests,null));
            }
        } else {
            List<List<String>> attributes=new ArrayList<>();
            for(String key:new TreeSet<String>(attributes(target))) row(attributes,key,target.get(key));
            sections.add(section("attributes","Atrybuty",Arrays.asList("Atrybut","Wartość"),attributes,null));
            if(target instanceof Item) {
                for(String key:new String[]{ItemAffixState.ATTRIBUTE,Item.RARITY_MODIFIERS,ItemTooltip.ATTRIBUTE}) {
                    if(target.hasMap(key)) {
                        List<List<String>> values=new ArrayList<>();
                        for(String name:new TreeSet<String>(target.getMap(key).keySet())) row(values,name,target.getMap(key).get(name));
                        sections.add(section(key,key,Arrays.asList("Parametr","Wartość"),values,null));
                    }
                }
            }
        }
        Map<String,Object> snapshot=new LinkedHashMap<>();snapshot.put("title",title(target));snapshot.put("sections",sections);
        return JSONValue.toJSONString(snapshot);
    }
    private static List<String> attributes(RPObject object) {
        List<String> keys=new ArrayList<>(); for(String key:object) keys.add(key);return keys;
    }
    private static void row(List<List<String>> rows,String key,Object value) {rows.add(Arrays.asList(key,value==null?"":value.toString()));}
    private static Map<String,Object> section(String id,String title,List<String> columns,List<List<String>> rows,List<String> details) {
        Map<String,Object> section=new LinkedHashMap<>();section.put("id",id);section.put("title",title);section.put("columns",columns);
        section.put("rows",rows);section.put("details",details==null?Collections.emptyList():details);return section;
    }
}
