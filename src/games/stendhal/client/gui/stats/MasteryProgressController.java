package games.stendhal.client.gui.stats;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.ScreenController;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.common.Level;
import games.stendhal.common.NotificationType;

/**
 * Keeps mastery state synchronized from user attribute updates.
 */
public final class MasteryProgressController {
	private static MasteryProgressController instance;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private MasteryProgress current = MasteryProgress.lockedDefault();

	private int masteryLevel;
	private long masteryExp;
	private long masteryExpToNext;
	private int resets;
	private int level;
	private boolean unlocked;
	private int requiredResets = MasteryProgress.DEFAULT_REQUIRED_RESETS;
	private int requiredLevel = Level.maxLevel();
	private int maxMasteryLevel = MasteryProgress.DEFAULT_MAX_MASTERY_LEVEL;

	private MasteryProgressController() {
	}

	public static synchronized MasteryProgressController get() {
		if (instance == null) {
			instance = new MasteryProgressController();
		}
		return instance;
	}

	public void registerListeners(final PropertyChangeSupport userPcs) {
		final PropertyChangeListener listener = new MasteryAttributeChangeListener();
		userPcs.addPropertyChangeListener("mastery_level", listener);
		userPcs.addPropertyChangeListener("mastery_exp", listener);
		userPcs.addPropertyChangeListener("mastery_xp", listener);
		userPcs.addPropertyChangeListener("mastery_exp_to_next", listener);
		userPcs.addPropertyChangeListener("mastery_unlocked", listener);
		userPcs.addPropertyChangeListener("mastery_unlocked_at", listener);
		userPcs.addPropertyChangeListener("mastery_required_resets", listener);
		userPcs.addPropertyChangeListener("mastery_required_level", listener);
		userPcs.addPropertyChangeListener("mastery_max_level", listener);
		userPcs.addPropertyChangeListener("resets", listener);
		userPcs.addPropertyChangeListener("reborn", listener);
		userPcs.addPropertyChangeListener("reborns", listener);
		userPcs.addPropertyChangeListener("level", listener);
	}

	public void addProgressListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener("mastery", listener);
		listener.propertyChange(new PropertyChangeEvent(this, "mastery", null, current));
	}

	public MasteryProgress getCurrent() {
		return current;
	}

	private void update() {
		final boolean nowUnlocked = unlocked || (resets >= requiredResets && level >= requiredLevel) || masteryLevel > 0;
		final long expToNext = masteryLevel >= maxMasteryLevel ? 0 : Math.max(0L, masteryExpToNext);

		MasteryProgress old = current;
		current = new MasteryProgress(nowUnlocked, masteryLevel, Math.max(0L, masteryExp), expToNext,
			requiredResets, requiredLevel, maxMasteryLevel, Math.max(0, resets), Math.max(0, level));
		pcs.firePropertyChange("mastery", old, current);

		if (old != null && current.isUnlocked() && current.getMasteryLevel() > old.getMasteryLevel()) {
			onMasteryLevelUp(new MasteryLevelUpEvent(current.getMasteryLevel()));
		}
	}

	private void onMasteryLevelUp(MasteryLevelUpEvent event) {
		final String text = MasteryI18n.text("ui.mastery.levelup", event.getNewLevel());
		ClientSingletonRepository.getUserInterface().addEventLine(new HeaderLessEventLine(text, NotificationType.TUTORIAL));
		if (ScreenController.get() != null) {
			ScreenController.get().addText(0, 0, text, NotificationType.TUTORIAL, false);
		}
	}

	private final class MasteryAttributeChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event == null) {
				return;
			}
			final String key = event.getPropertyName();
			final String value = event.getNewValue() != null ? String.valueOf(event.getNewValue()) : null;
			if ("mastery_level".equals(key)) {
				masteryLevel = parseInt(value, masteryLevel);
			} else if ("mastery_exp".equals(key) || "mastery_xp".equals(key)) {
				masteryExp = parseLong(value, masteryExp);
			} else if ("mastery_exp_to_next".equals(key)) {
				masteryExpToNext = parseLong(value, masteryExpToNext);
			} else if ("mastery_unlocked".equals(key)) {
				unlocked = "1".equals(value) || "true".equalsIgnoreCase(value);
			} else if ("mastery_unlocked_at".equals(key)) {
				unlocked = value != null;
			} else if ("mastery_required_resets".equals(key)) {
				requiredResets = parseInt(value, requiredResets);
			} else if ("mastery_required_level".equals(key)) {
				requiredLevel = parseInt(value, requiredLevel);
			} else if ("mastery_max_level".equals(key)) {
				maxMasteryLevel = parseInt(value, maxMasteryLevel);
			} else if ("resets".equals(key) || "reborn".equals(key) || "reborns".equals(key)) {
				resets = parseInt(value, resets);
			} else if ("level".equals(key)) {
				level = parseInt(value, level);
			}
			update();
		}
	}

	private static int parseInt(String raw, int fallback) {
		if (raw == null) {
			return 0;
		}
		try {
			return Integer.parseInt(raw);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static long parseLong(String raw, long fallback) {
		if (raw == null) {
			return 0;
		}
		try {
			return Long.parseLong(raw);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
