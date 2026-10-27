package games.stendhal.common;
import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.nalwor.hell.HellTimer;
import utilities.PlayerTestHelper;
public class HellSceneLanguageTest {
 @BeforeClass public static void setup(){MockStendlRPWorld.get();}
 @Test public void authoredSceneSettingUsesThePlayersLanguage() throws Exception {
  runStage("TimerStage3",new Class<?>[]{String.class},new Object[]{"Wskakujesz do dołu, aby uciec od sprzedawcy."},
   "You jump into the pit to escape the salesperson.");
 }
 @Test public void recaughtGuardUsesThePlayersLanguage() throws Exception {
  runStage("TimerStage2",new Class<?>[]{boolean.class},new Object[]{true},
   "The guardian of Hell shouts: Ha, caught you. The reapers warned me that you might try to return.");
 }
 @Test public void sceneTemplateSubstitutionPreservesDictionaryLikeNamesAndFee() throws Exception {
  Player player=PlayerTestHelper.createPlayer("Jan$FEE");player.setLanguage("en");
  Field messages=HellTimer.class.getDeclaredField("MESSAGES");messages.setAccessible(true);
  String[][] templates=(String[][])messages.get(null);
  Method format=HellTimer.class.getDeclaredMethod("formatMessage",Player.class,String.class,int.class);format.setAccessible(true);
  String greeting=(String)format.invoke(null,player,templates[9][0],123);
  assertTrue(greeting,greeting.startsWith("The saleswoman of Hell shouts: Hello, friend!"));
  assertEquals(2,greeting.split(java.util.regex.Pattern.quote("Jan$FEE"),-1).length-1);
  String lawyer=(String)format.invoke(null,player,templates[6][0],123);
  assertTrue(lawyer,lawyer.contains("123 coins"));assertFalse(lawyer,lawyer.contains("$FEE"));
 }
 private void runStage(String name,Class<?>[] types,Object[] args,String expected) throws Exception {
  Player player=PlayerTestHelper.createPlayer("Witaj.$2");player.setLanguage("en");player.setClientVersion("1.0");
  HellTimer owner=new HellTimer();
  Class<?> inner=Class.forName(HellTimer.class.getName()+"$"+name);
  Class<?>[] signature=new Class<?>[types.length+2];signature[0]=HellTimer.class;signature[1]=Player.class;System.arraycopy(types,0,signature,2,types.length);
  Object[] parameters=new Object[args.length+2];parameters[0]=owner;parameters[1]=player;System.arraycopy(args,0,parameters,2,args.length);
  Constructor<?> constructor=inner.getDeclaredConstructor(signature);constructor.setAccessible(true);
  try {
   ((TurnListener)constructor.newInstance(parameters)).onTurnReached(0);
   assertEquals(expected,player.events().get(0).get("text"));
  } finally {
   Field timers=HellTimer.class.getDeclaredField("runningTimers");timers.setAccessible(true);
   for(Object timer:((Map<?,?>)timers.get(owner)).values())SingletonRepository.getTurnNotifier().dontNotify((TurnListener)timer);
  }
 }
}
