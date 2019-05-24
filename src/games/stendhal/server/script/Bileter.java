package games.stendhal.server.script;

import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.core.scripting.ScriptingNPC;
import games.stendhal.server.core.scripting.ScriptingSandbox;
import games.stendhal.server.entity.player.Player;

/**
 * Creates a portable NPC which sell ticket.
 *
 * As admin use /script Bileter.class to summon him right next to you. Please put
 * him back in int_admin_playground after use.
 */
public class Bileter extends ScriptImpl {

	private static Logger logger = Logger.getLogger(Bileter.class);

	@Override
	public void load(Player admin, List<String> args, ScriptingSandbox sandbox) {

		// Create NPC
		ScriptingNPC npc = new ScriptingNPC("Bileter");
		npc.setEntityClass("npckibic");

		// Place NPC in int_admin_playground on server start
		String myZone = "int_admin_playground";
		sandbox.setZone(myZone);
		int x = 13;
		int y = 4;

		// If this script is executed by an admin, Bileter will be placed next to him.
		if (admin != null) {
			sandbox.setZone(sandbox.getZone(admin));
			x = admin.getX() + 1;
			y = admin.getY();
		}

		// Set zone and position
		npc.setPosition(x, y);
		sandbox.add(npc);

		// Create Dialog
		npc.behave("greet", "Witam. W czym mogę pomóc?");
		npc.behave(
				"job",
				"Jestem bileterem. Sprzedaję #'bilety na mecz' oraz #piłki.");
		npc.behave("bilety na mecz",
//			"I have a #coupon for a free beer in Semos' tavern. "+
			"Mecz odbędzie się na stadionie PolskaGra.");
		npc.behave("help",
				"Mogę  zaoferować ( #offer ) bilet na mecz.");
		npc.behave("bye",
				"Dowidzenia. Podziwiaj mecz.");
		try {
			npc.behave("sell", SingletonRepository.getShopList().get("mecz"));
		} catch (NoSuchMethodException e) {
			logger.error(e, e);
		}

	}

}
