package games.stendhal.common;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.semos.plains.CreatureInfo;
import games.stendhal.server.maps.zakopane.city.CreatureInfoZ;
import utilities.PlayerTestHelper;
public class CreatureInformationLanguageTest {
 @BeforeClass public static void setup(){MockStendlRPWorld.get();}
 @Test public void semosInformationTranslatesPiecesBeforeJoiningTheParagraph() throws Exception { check(CreatureInfo.class); }
 @Test public void zakopaneInformationTranslatesPiecesBeforeJoiningTheParagraph() throws Exception { check(CreatureInfoZ.class); }
 private void check(Class<?> type) throws Exception {
  Map<Double,String> probabilities=Collections.singletonMap(0.0,"zawsze %s");
  Map<Integer,String> amounts=Collections.singletonMap(1,"%a");
  Map<Double,String> danger=Collections.singletonMap(0.0,"%s nie stanowi dla Ciebie wyzwania.");
  Object info=type.getConstructor(Map.class,Map.class,Map.class,String[].class,String[].class,String[].class,String[].class,String[].class,String[].class)
   .newInstance(probabilities,amounts,danger,new String[]{"O, tak. Znam stwory zwane %s!"},new String[]{"Odradzają się %1 po swojej śmierci"},
    new String[]{"Mają przy sobie %1."},new String[]{"Nie wiem czy coś noszą."},new String[]{"Widziałem ich %1."},
    new String[]{"Nie mam pojęcia, gdzie możesz znaleźć potwora zwanego %1."});
  DefaultCreature rat=null;
  for(DefaultCreature creature:SingletonRepository.getEntityManager().getDefaultCreatures())if("szczur".equals(creature.getCreatureName()))rat=creature;
  assertNotNull(rat);
  Player player=PlayerTestHelper.createPlayer("Zamknij");player.setLanguage("en");
  String method=type==CreatureInfo.class?"getCreatureInfo":"getCreatureInfoZ";
  String english=(String)type.getMethod(method,Player.class,DefaultCreature.class,int.class,int.class,boolean.class).invoke(info,player,rat,1,1,true);
  assertTrue(english,english.startsWith("Oh, yes. I know creatures called rat! Rat is no challenge for you. "));
  assertFalse(english,english.matches(".*[ąćęłńóśźż].*"));
  assertFalse(english,english.contains("%1"));assertFalse(english,english.contains("%s"));
  assertEquals("szczur",rat.getCreatureName());
  player.setLanguage("pl");
  String polish=(String)type.getMethod(method,Player.class,DefaultCreature.class,int.class,int.class,boolean.class).invoke(info,player,rat,0,0,false);
  assertEquals("O, tak. Znam stwory zwane szczur! Szczur nie stanowi dla Ciebie wyzwania. ",polish);
 }
}
