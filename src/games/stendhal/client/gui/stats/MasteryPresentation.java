package games.stendhal.client.gui.stats;

final class MasteryPresentation {
	final boolean unlocked;
	final String headline;
	final String detail;
	final int progressMax;
	final int progressValue;

	private MasteryPresentation(boolean unlocked, String headline, String detail, int progressMax, int progressValue) {
		this.unlocked = unlocked;
		this.headline = headline;
		this.detail = detail;
		this.progressMax = progressMax;
		this.progressValue = progressValue;
	}

	static MasteryPresentation from(MasteryProgress progress) {
		if (!progress.isUnlocked()) {
			return new MasteryPresentation(false,
				MasteryI18n.text("ui.mastery.locked.requirements", progress.getRequiredResets()),
				MasteryI18n.text("ui.mastery.locked.progress", progress.getCurrentResets(), progress.getRequiredResets(), progress.getCurrentLevel(), progress.getRequiredLevel()),
				Math.max(1, progress.getRequiredResets() + progress.getRequiredLevel()),
				Math.min(progress.getRequiredResets(), progress.getCurrentResets()) + Math.min(progress.getRequiredLevel(), progress.getCurrentLevel()));
		}

		if (progress.isCapped()) {
			return new MasteryPresentation(true,
				MasteryI18n.text("ui.mastery.level", progress.getMasteryLevel()),
				MasteryI18n.text("ui.mastery.max_reached", progress.getMaxMasteryLevel()),
				1,
				1);
		}

		final long needed = progress.getCurrentExp() + progress.getExpToNext();
		return new MasteryPresentation(true,
			MasteryI18n.text("ui.mastery.level", progress.getMasteryLevel()),
			MasteryI18n.text("ui.mastery.exp_to_next", progress.getExpToNext()),
			(int) Math.max(1L, Math.min(Integer.MAX_VALUE, needed)),
			(int) Math.max(0L, Math.min(Integer.MAX_VALUE, progress.getCurrentExp())));
	}
}
