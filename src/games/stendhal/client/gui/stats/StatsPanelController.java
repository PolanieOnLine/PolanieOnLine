/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.stats;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import games.stendhal.client.entity.StatusID;
import games.stendhal.common.Level;
import games.stendhal.common.MathHelper;
import games.stendhal.common.constants.Testing;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Object for listening for various user state changes that should
 * be show.
 */
public final class StatsPanelController {
    private static final String[] MONEY_SLOTS = { "bag", "lhand", "rhand", "pouch", "money" };
    private static final int COPPER_PER_TALAR = 100;
    private static final int COPPER_PER_DUKAT = 10000;
	/**
	 * A string used as a white space at the status labels. This is a
	 * combination of carriage return and no-break space, so that a possible
	 * automatic line break is inserted at the CR, and the next line starts with
	 * a leading space so that it is clear it belongs together with the previous
	 * line. In case the text fits in one line, it looks like a normal space.
	 */
	private static final String SPC = "\r\u00a0";
	private final StatsPanel panel;
	private static	StatsPanelController instance;

	/**
	 * The money objects.
	 * First level keys are the slot name. Second level is the object id.
	 */
	private final HashMap<String, HashMap<String, CoinStack>> money = new HashMap<String, HashMap<String, CoinStack>>();

	private int level;
	private int xp;
	private int hp;
	private int maxhp;
	private int maxhpModified;

	private int atk;
	private int atkxp;
	private int weaponAtk;

	private int def;
	private int defxp;
	private int itemDef;

	private int ratk;
	private int ratkxp;
	private int weaponRatk;

	private int mining;
	private int miningxp;

	private int mana;
	private int baseMana;

	private double capacity;
	private double baseCapacity;

	/**
	 * Create a new <code>StatsPanelController</code>. There
	 * should be only one, so the constructor is hidden.
	 */
	private StatsPanelController() {
		panel = new StatsPanel();
	}

	/**
	 * Get the <code>StatsPanelController</code> instance.
	 *
	 * @return the StatsPanelController instance
	 */
	public static synchronized StatsPanelController get() {
		if (instance == null) {
			instance = new StatsPanelController();
		}
		return instance;
	}

	/**
	 * Get the <code>StatsPanel</code> component this controller
	 * is keeping updated.
	 *
	 * @return StatsPanel
	 */
	public StatsPanel getComponent() {
		return panel;
	}

	/**
	 * Add listeners for all the properties this object follows.
	 *
	 * @param pcs property change support of the user
	 */
	public void registerListeners(PropertyChangeSupport pcs) {
		PropertyChangeListener listener = new HPChangeListener();
		addPropertyChangeListenerWithModifiedSupport(pcs, "base_hp", listener);
		addPropertyChangeListenerWithModifiedSupport(pcs, "hp", listener);

		listener = new ATKChangeListener();
		addPropertyChangeListenerWithModifiedSupport(pcs, "atk", listener);
		pcs.addPropertyChangeListener("atk_xp", listener);

		listener = new DEFChangeListener();
		addPropertyChangeListenerWithModifiedSupport(pcs, "def", listener);
		pcs.addPropertyChangeListener("def_xp", listener);

		listener = new RATKChangeListener();
		addPropertyChangeListenerWithModifiedSupport(pcs, "ratk", listener);
		pcs.addPropertyChangeListener("ratk_xp", listener);

		if (System.getProperty("pol.miningstat") != null) {
			listener = new MININGChangeListener();
			addPropertyChangeListenerWithModifiedSupport(pcs, "mining", listener);
			pcs.addPropertyChangeListener("mining_xp", listener);
		}

		listener = new XPChangeListener();
		pcs.addPropertyChangeListener("xp", listener);

		listener = new LevelChangeListener();
		addPropertyChangeListenerWithModifiedSupport(pcs, "level", listener);

		listener = new WeaponChangeListener();
		pcs.addPropertyChangeListener("atk_item", listener);

		listener = new ArmorChangeListener();
		pcs.addPropertyChangeListener("def_item", listener);

		listener = new RangedWeaponChangeListener();
		if (Testing.COMBAT) {
			pcs.addPropertyChangeListener("ratk_item", listener);
		} else {
			pcs.addPropertyChangeListener("atk_item", listener);
		}

		listener = new MoneyChangeListener();
		for (String slot : MONEY_SLOTS) {
			pcs.addPropertyChangeListener(slot, listener);
		}

		listener = new EatingChangeListener();
		pcs.addPropertyChangeListener("eating", listener);
		pcs.addPropertyChangeListener("choking", listener);

		listener = new StatusChangeListener();
		for (StatusID id : StatusID.values()) {
			pcs.addPropertyChangeListener(id.getAttribute(), listener);
		}

		listener = new AwayChangeListener();
		pcs.addPropertyChangeListener("away", listener);

		listener = new GrumpyChangeListener();
		pcs.addPropertyChangeListener("grumpy", listener);

		listener = new KarmaChangeListener();
		pcs.addPropertyChangeListener("karma", listener);

		listener = new ManaChangeListener();
		addPropertyChangeListenerWithModifiedSupport(pcs, "base_mana", listener);
		addPropertyChangeListenerWithModifiedSupport(pcs, "mana", listener);

		if (Testing.WEIGHT) {
			listener = new CapacityChangeListener();
			addPropertyChangeListenerWithModifiedSupport(pcs, "base_capacity", listener);
			addPropertyChangeListenerWithModifiedSupport(pcs, "capacity", listener);
		}
	}

	private void addPropertyChangeListenerWithModifiedSupport(PropertyChangeSupport pcs, String attribute, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(attribute, listener);
		pcs.addPropertyChangeListener("modified_" + attribute, listener);
	}

	/**
	 * Called when xp or level has changed.
	 */
	private void updateLevel() {
		final int next = Level.getXP(level + 1) - xp;
		// Show "em-dash" for max level players rather than
		// a confusing negative required xp.
		final String nextS = (next < 0) ? "\u2014" : Integer.toString(next);

		final String text = "Poziom:" + SPC + level + SPC + "(" + nextS + ")";
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				panel.setLevel(text);
			}
		});
	}

	/**
	 * Called when HP or max HP has changed.
	 */
	private void updateHP() {
		final int maxhpvalue;
		if (maxhpModified != 0) {
			maxhpvalue = maxhpModified;
		} else {
			maxhpvalue = maxhp;
		}

		final String text = "PZ:" + SPC + hp + "/" + maxhpvalue;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// FIXME: this seems to be run twice at level up
				panel.setHP(text);
				if (maxhpvalue > 0) {
					panel.setHPBar(maxhpvalue, hp);
				}
			}
		});
	}

	/**
	 * Called when atk, atkxp, or weaponAtk changes.
	 */
	private void updateAtk() {
		// atk uses 10 levels shifted starting point
		final int next = Level.getXP(atk - 9) - atkxp;
		final String text = "ATK:" + SPC + atk + "×" + (1 + weaponAtk) + SPC + "(" + next + ")";
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				panel.setAtk(text);
			}
		});
	}

	/**
	 * Called when def, defxp, or itemDef changes.
	 */
	private void updateDef() {
		// def uses 10 levels shifted starting point
		final int next = Level.getXP(def - 9) - defxp;
		final String text = "OBR:" + SPC + def + "×" + (1 + itemDef) + SPC + "(" + next + ")";
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				panel.setDef(text);
			}
		});
	}

	/**
	 * Called when ratk, ratkxp, or weaponRatk changes.
	 */
	private void updateRatk() {
		if (!Testing.COMBAT) {
			return;
		}

		// ratk uses 10 levels shifted starting point
		final int next = Level.getXP(ratk - 9) - ratkxp;
		final String text = "STR:" + SPC + ratk + "×" + (1 + weaponRatk) + SPC + "(" + next + ")";
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				panel.setRatk(text);
			}
		});
	}

	/**
	 * Called when mining xp has changed.
	 */
	private void updateMining() {
		if (System.getProperty("pol.miningstat") != null) {
			return;
		}

		final long next = Level.getXP(mining - 9) - miningxp;
		final String text = "Górnictwo:" + SPC + mining + SPC + "(" + next + ")";
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				panel.setMining(text);
			}
		});
	}

	/**
	 * Show/Hide HP bar.
	 *
	 * @param show
	 * 		If <code>true</code>, HP bar will be visible.
	 */
	public void toggleHPBar(final boolean show) {
		panel.toggleHPBar(show);
	}

	/**
	 * Listener for HP and base_hp changes.
	 */
	private class HPChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}

			String newValue = (String) event.getNewValue();
			if (event.getPropertyName().equals("base_hp")) {
				maxhp = Integer.parseInt(newValue);
			} else if (event.getPropertyName().equals("base_hp_modified")) {
				if (newValue != null) {
					maxhpModified = Integer.parseInt(newValue);
				} else {
					maxhpModified = 0;
				}
			} else if (event.getPropertyName().equals("hp")) {
				hp = Integer.parseInt(newValue);
			}
			updateHP();
		}
	}

	/**
	 * Called when there are additions to a potential money slot.
	 *
	 * @param slot
	 * @param object
	 */
	private void addMoney(String slot, RPObject object) {
		HashMap<String, CoinStack> set = money.get(slot);
		boolean knownSet = set != null;
		if (!knownSet) {
			set = new HashMap<String, CoinStack>();
		}

		String id = object.get("id");
		if (id == null) {
			return;
		}

		CoinStack existing = set.get(id);
		CoinDescriptor descriptor = identifyCoin(object, existing != null ? existing.descriptor : null);
		if (descriptor == null) {
			if (existing != null) {
				set.remove(id);
				if (set.isEmpty()) {
					money.remove(slot);
				}
				updateMoney();
			}
			return;
		}

		int quantity = object.has("quantity") ? object.getInt("quantity") : (existing != null ? existing.quantity : 0);

		if (existing == null) {
			set.put(id, new CoinStack(descriptor, quantity));
			if (!knownSet) {
				money.put(slot, set);
			}
		} else {
			existing.descriptor = descriptor;
			existing.quantity = quantity;
		}

		updateMoney();
	}

	/**
	 * Remove all the money objects. Called at zone change.
	 */
	private void clearMoney() {
		money.clear();
		updateMoney();
	}

	/**
	 * Called when items are removed from a potential money slot.
	 *
	 * @param slot
	 * @param obj
	 */
	private void removeMoney(String slot, RPObject obj) {
		HashMap<String, CoinStack> set = money.get(slot);
		String id = obj.get("id");
		if (set != null && id != null && set.remove(id) != null) {
			if (set.isEmpty()) {
				money.remove(slot);
			}
			updateMoney();
		}
	}

	/**
	 * Count the money, and update the label text.
	 */
	private void updateMoney() {
		int dukaty = 0;
		int talary = 0;
		int miedziaki = 0;
		int totalCopper = 0;

		for (HashMap<String, CoinStack> stack : money.values()) {
			for (CoinStack coin : stack.values()) {
				int quantity = coin.quantity;
				if (quantity <= 0) {
					continue;
				}

				CoinDescriptor descriptor = coin.descriptor;

				switch (descriptor.bucket) {
				case DUKAT:
					dukaty += quantity;
					break;
				case TALAR:
					talary += quantity;
					break;
				case COPPER:
				default:
					miedziaki += quantity;
					break;
				}

				totalCopper += quantity * descriptor.copperValue;
			}
		}

		final int finalDukaty = dukaty;
		final int finalTalary = talary;
		final int finalMiedziaki = miedziaki;
		final int finalTotalCopper = totalCopper;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				panel.setMoney(finalDukaty, finalTalary, finalMiedziaki, finalTotalCopper);
			}
		});
	}

	private CoinDescriptor identifyCoin(RPObject obj, CoinDescriptor fallback) {
		String name = resolveCoinName(obj);
		if (name != null) {
			name = name.toLowerCase(Locale.ROOT);

			if ("dukat".equals(name)) {
				return CoinDescriptor.dukat();
			}
			if ("talar".equals(name)) {
				return CoinDescriptor.talar();
			}
			if ("miedziak".equals(name) || "money".equals(name)) {
				return CoinDescriptor.copper();
			}
			if ("gold".equals(name)) {
				// spróbuj rozróżnić dukata od starego "money" po wartości
				if (obj.has("value") && obj.getInt("value") == COPPER_PER_DUKAT) {
					return CoinDescriptor.dukat();
				}
				return CoinDescriptor.copper();
			}
			if ("silver".equals(name)) {
				return CoinDescriptor.talar();
			}
			if ("copper".equals(name)) {
				return CoinDescriptor.copper();
			}
			return null;
		}

		if (obj.has("value")) {
			int value = obj.getInt("value");
			if (value == COPPER_PER_DUKAT) {
				return CoinDescriptor.dukat();
			}
			if (value == COPPER_PER_TALAR) {
				return CoinDescriptor.talar();
			}
			if (value > 0) {
				return CoinDescriptor.customCopper(value);
			}
		}

		return fallback;
	}

        private String resolveCoinName(RPObject obj) {
                String name = obj.get("name");
                if (name != null) {
                        return name;
                }
                name = obj.get("subclass");
                if (name != null) {
                        return name;
                }
                return obj.get("class");
        }

	private static class CoinDescriptor {
		private enum Bucket {
			DUKAT,
			TALAR,
			COPPER
		}

		private static final CoinDescriptor DUKAT = new CoinDescriptor(Bucket.DUKAT, COPPER_PER_DUKAT);
		private static final CoinDescriptor TALAR = new CoinDescriptor(Bucket.TALAR, COPPER_PER_TALAR);
		private static final CoinDescriptor COPPER = new CoinDescriptor(Bucket.COPPER, 1);

		final Bucket bucket;
		final int copperValue;

		private CoinDescriptor(Bucket bucket, int copperValue) {
			this.bucket = bucket;
			this.copperValue = copperValue;
		}

		static CoinDescriptor dukat() {
			return DUKAT;
		}

		static CoinDescriptor talar() {
			return TALAR;
		}

		static CoinDescriptor copper() {
			return COPPER;
		}

		static CoinDescriptor customCopper(int copperValue) {
			return new CoinDescriptor(Bucket.COPPER, copperValue);
		}
	}

	private static class CoinStack {
		private CoinDescriptor descriptor;
		private int quantity;

		CoinStack(CoinDescriptor descriptor, int quantity) {
			this.descriptor = descriptor;
			this.quantity = quantity;
		}
	}

	/**
	 * Called when capacity or baseCapacity changes.
	 */
	private void updateCapacity() {
		final String text = "Udźwig:" + SPC + capacity + SPC + "/" + SPC + baseCapacity + "kg";
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				panel.setCapacity(text);
			}
		});
	}

	/**
	 * Listener for atk and atk_xp changes.
	 */
	private class ATKChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}

			if ("atk_xp".equals(event.getPropertyName())) {
				atkxp = Integer.parseInt((String) event.getNewValue());
			} else if ("atk".equals(event.getPropertyName())) {
				atk = Integer.parseInt((String) event.getNewValue());
			}
			updateAtk();
		}
	}

	/**
	 * Listener for def and def_xp changes.
	 */
	private class DEFChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}

			if ("def_xp".equals(event.getPropertyName())) {
				defxp = Integer.parseInt((String) event.getNewValue());
			} else if ("def".equals(event.getPropertyName())) {
				def =  Integer.parseInt((String) event.getNewValue());
			}
			updateDef();
		}
	}

	/**
	 * Listener for ratk and ratk_xp changes.
	 */
	private class RATKChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}

			if ("ratk_xp".equals(event.getPropertyName())) {
				ratkxp = Integer.parseInt((String) event.getNewValue());
			} else if ("ratk".equals(event.getPropertyName())) {
				ratk = Integer.parseInt((String) event.getNewValue());
			}
			updateRatk();
		}
	}

	/**
	 * Listener for mining and mining_xp changes.
	 */
	private class MININGChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}

			if (event.getPropertyName().equals("mining_xp")) {
				miningxp = Integer.parseInt((String) event.getNewValue());
			} else if (event.getPropertyName().equals("mining")) {
				mining =  Integer.parseInt((String) event.getNewValue());
			}
			updateMining();
		}
	}

	/**
	 * Listener for capacity and baseCapacity changes.
	 */
	private class CapacityChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}

			if (event.getPropertyName().equals("base_capacity")) {
				baseCapacity = Double.parseDouble((String) event.getNewValue());
			} else if (event.getPropertyName().equals("capacity")) {
				capacity =  Double.parseDouble((String) event.getNewValue());
			}
			updateCapacity();
		}
	}

	/**
	 * Listener for xp changes.
	 */
	private class XPChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}
			xp = Integer.parseInt((String) event.getNewValue());
			updateLevel();
			final String text = "PD:" + SPC + xp;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					panel.setXP(text);
				}
			});
		}
	}

	/**
	 * Listener for level changes.
	 */
	private class LevelChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}
			if (event.getPropertyName().equals("level")) {
				level = Integer.parseInt((String) event.getNewValue());
			}
			updateLevel();
		}
	}

	/**
	 * Listener for weapon atk changes.
	 */
	private class WeaponChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}
			weaponAtk = Integer.parseInt((String) event.getNewValue());
			updateAtk();
		}
	}

	/**
	 * Listener for armor def changes.
	 */
	private class ArmorChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}
			itemDef = Integer.parseInt((String) event.getNewValue());
			updateDef();
		}
	}

	/**
	 * Listener for ranged weapon atk changes.
	 */
	private class RangedWeaponChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}

			if (Testing.COMBAT) {
				weaponRatk = Integer.parseInt((String) event.getNewValue());
				updateRatk();
			} else {
				weaponAtk = Integer.parseInt((String) event.getNewValue());
				updateAtk();
			}
		}
	}

	/**
	 * Listener for eating and choking changes.
	 */
	private class EatingChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			// Deleted attribute can raise a null event
			if (event == null) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						panel.setEating(false);
						panel.setChoking(false);
					}
				});
				return;
			}

			final boolean newStatus = event.getNewValue() != null;
			if ("eating".equals(event.getPropertyName())) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						panel.setEating(newStatus);
					}
				});
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						panel.setChoking(newStatus);
					}
				});
			}
		}
	}

	/**
	 * Listener for status changes.
	 */
	private class StatusChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				// User deleted. Reset all states.
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						panel.resetStatuses();
					}
				});
				return;
			}

			Object value = event.getNewValue();
	        final StatusID ID = StatusID.getStatusID(event.getPropertyName());
	        final boolean enabled = value != null;
	        SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                panel.setStatus(ID, enabled);
	            }
	        });
	    }
	}

	/**
	 * Listener for away status changes.
	 */
	private class AwayChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			// Deleted attribute can raise a null event
			String value = null;
			if (event != null) {
				Object obj = event.getNewValue();
				if (obj != null) {
					value = obj.toString();
				}
			}
			final String message = value;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					panel.setAway(message);
				}
			});
		}
	}

	/**
	 * Listener for karma changes.
	 */
	private class KarmaChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}

			panel.setKarma(MathHelper.parseDouble((String) event.getNewValue()));
		}
	}

	/**
	 * Listener for mana changes.
	 */
	private class ManaChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				return;
			}

			try {
				if (event.getPropertyName().endsWith("base_mana")) {
					baseMana = Integer.parseInt((String) event.getNewValue());
					panel.setBaseMana(baseMana);
				} else {
					mana = Integer.parseInt((String) event.getNewValue());
					panel.setMana(mana);
				}
			} catch (NumberFormatException e) {
				Logger.getLogger(ManaChangeListener.class).error("Invalid mana value", e);
			}
		}
	}

	/**
	 * Listener for grumpy status changes.
	 */
	private class GrumpyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			// Deleted attribute can raise a null event
			String value = null;
			if (event != null) {
				Object obj = event.getNewValue();
				if (obj != null) {
					value = obj.toString();
				}
			}
			final String message = value;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					panel.setGrumpy(message);
				}
			});
		}
	}

	/**
	 * Listener for money changes.
	 * Due to there being no "money" property for the player, this
	 * need to listen to all the slots where it's possible to store money.
	 */
	private class MoneyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (event == null) {
				/*
				 * We get a null event when the player object is deleted. Clear
				 * the money. For the situations where a zone change is
				 * combined with a complete removal of a money stack.
				 */
				clearMoney();
				return;
			}

			RPSlot slot = (RPSlot) event.getOldValue();
			if (slot != null) {
				for (final RPObject object : slot) {
					removeMoney(slot.getName(), object);
				}
			}

			slot = (RPSlot) event.getNewValue();
			if (slot != null) {
				for (final RPObject object : slot) {
					// add everything. let the panel figure out if it's money
					addMoney(slot.getName(), object);
				}
			}
		}
	}
}
