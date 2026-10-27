package games.stendhal.server.entity.creature;

import java.util.Collections;
import java.util.List;

import games.stendhal.common.constants.GameTiming;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;

/** A permanent, non-combat companion which matures while accompanying its owner. */
public class Duszek extends Pet {

	private static final String AGE = "companion_turns";
	private static final int GROWTH_TURNS = 10 * 60 * 60 * 1000 / GameTiming.MILLISECONDS_PER_TURN;

	public static void generateRPClass() {
		final RPClass ghost = new RPClass("duszek");
		ghost.isA("pet");
		ghost.addAttribute(AGE, Type.INT, Definition.HIDDEN);
	}

	public Duszek() {
		super();
		setRPClass("duszek");
		put("type", "duszek");
		put(AGE, 0);
		setWeight(0);
		update();
	}

	public Duszek(final RPObject object, final Player owner) {
		super(object, owner);
		setRPClass("duszek");
		put("type", "duszek");
		put(AGE, Math.max(0, Math.min(GROWTH_TURNS, has(AGE) ? getInt(AGE) : 0)));
		setWeight(getInt(AGE) >= GROWTH_TURNS ? 20 : 0);
		update();
	}

	@Override
	void setUp() {
		HP = 100;
		baseSpeed = 0.9;
		setVisibility(65);
		put("active_idle", "");
		setAtk(0);
		setDef(0);
		setXP(0);
		setBaseHP(HP);
		setHP(HP);
	}

	@Override
	protected List<String> getFoodNames() {
		return Collections.emptyList();
	}

	@Override
	protected List<String> getMedicineNames() {
		return Collections.emptyList();
	}

	@Override
	protected void increaseHunger() {
		// A spirit needs companionship rather than food.
	}

	@Override
	protected boolean takesPartInCombat() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public boolean isInvisibleToCreatures() {
		return true;
	}

	@Override
	public void logic() {
		super.logic();
		if (owner != null && !owner.isDisconnected() && getZone() == owner.getZone()
				&& getInt(AGE) < GROWTH_TURNS) {
			put(AGE, getInt(AGE) + 1);
			if (getInt(AGE) == GROWTH_TURNS) {
				setWeight(20);
				owner.sendPrivateText("Twój duszek wyrósł! Wspólne wędrówki dodały mu sił.");
				notifyWorldAboutChanges();
			}
		}
	}

	@Override
	public String describe() {
		if (getInt(AGE) >= GROWTH_TURNS) {
			return "Oto wyrośnięty duszek. Wierny towarzysz wspólnych wędrówek, który nie potrzebuje jedzenia i nie bierze udziału w walce.";
		}
		final int minutes = ((GROWTH_TURNS - getInt(AGE)) * GameTiming.MILLISECONDS_PER_TURN + 59999) / 60000;
		return "Oto mały duszek. Nie potrzebuje jedzenia i nie bierze udziału w walce. Do wyrośnięcia pozostało około "
				+ minutes + " min wspólnego czasu w grze.";
	}
}
