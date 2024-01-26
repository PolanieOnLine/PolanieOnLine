package games.stendhal.server.entity.npc.quest;

public class CraftItemQuestHistoryBuilder extends QuestHistoryBuilder {
	private String whenTimeWasNotEnded;
	private String whenTimeWasPassed;

	@Override
	public CraftItemQuestHistoryBuilder whenNpcWasMet(String whenNpcWasMet) {
		super.whenNpcWasMet(whenNpcWasMet);
		return this;
	}

	@Override
	public CraftItemQuestHistoryBuilder whenQuestWasRejected(String whenQuestWasRejected) {
		super.whenQuestWasRejected(whenQuestWasRejected);
		return this;
	}

	@Override
	public CraftItemQuestHistoryBuilder whenQuestWasAccepted(String whenQuestWasAccepted) {
		super.whenQuestWasAccepted(whenQuestWasAccepted);
		return this;
	}

	@Override
	public CraftItemQuestHistoryBuilder whenTaskWasCompleted(String whenTaskWasCompleted) {
		super.whenTaskWasCompleted(whenTaskWasCompleted);
		return this;
	}

	@Override
	public CraftItemQuestHistoryBuilder whenQuestWasCompleted(String whenQuestWasCompleted) {
		super.whenQuestWasCompleted(whenQuestWasCompleted);
		return this;
	}

	@Override
	public CraftItemQuestHistoryBuilder whenQuestCanBeRepeated(String whenQuestCanBeRepeated) {
		super.whenQuestCanBeRepeated(whenQuestCanBeRepeated);
		return this;
	}

	@Override
	public CraftItemQuestHistoryBuilder whenCompletionsShown(String whenCompletionsShown) {
		super.whenCompletionsShown(whenCompletionsShown);
		return this;
	}

	public CraftItemQuestHistoryBuilder whenTimeWasPassed(String whenTimeWasPassed) {
		this.whenTimeWasPassed = whenTimeWasPassed;
		return this;
	}

	public CraftItemQuestHistoryBuilder whenTimeWasNotEnded(String whenTimeWasNotEnded) {
		this.whenTimeWasNotEnded = whenTimeWasNotEnded;
		return this;
	}

	String whenTimeWasPassed() {
		return whenTimeWasPassed;
	}

	String whenTimeWasNotEnded() {
		return whenTimeWasNotEnded;
	}
}
