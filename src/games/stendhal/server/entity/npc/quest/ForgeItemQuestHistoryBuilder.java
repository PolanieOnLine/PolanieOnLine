package games.stendhal.server.entity.npc.quest;

public class ForgeItemQuestHistoryBuilder extends QuestHistoryBuilder {
	private String whenTimeWasNotEnded;
	private String whenTimeWasPassed;

	@Override
	public ForgeItemQuestHistoryBuilder whenNpcWasMet(String whenNpcWasMet) {
		super.whenNpcWasMet(whenNpcWasMet);
		return this;
	}

	@Override
	public ForgeItemQuestHistoryBuilder whenQuestWasRejected(String whenQuestWasRejected) {
		super.whenQuestWasRejected(whenQuestWasRejected);
		return this;
	}

	@Override
	public ForgeItemQuestHistoryBuilder whenQuestWasAccepted(String whenQuestWasAccepted) {
		super.whenQuestWasAccepted(whenQuestWasAccepted);
		return this;
	}

	@Override
	public ForgeItemQuestHistoryBuilder whenTaskWasCompleted(String whenTaskWasCompleted) {
		super.whenTaskWasCompleted(whenTaskWasCompleted);
		return this;
	}

	@Override
	public ForgeItemQuestHistoryBuilder whenQuestWasCompleted(String whenQuestWasCompleted) {
		super.whenQuestWasCompleted(whenQuestWasCompleted);
		return this;
	}

	@Override
	public ForgeItemQuestHistoryBuilder whenQuestCanBeRepeated(String whenQuestCanBeRepeated) {
		super.whenQuestCanBeRepeated(whenQuestCanBeRepeated);
		return this;
	}

	@Override
	public ForgeItemQuestHistoryBuilder whenCompletionsShown(String whenCompletionsShown) {
		super.whenCompletionsShown(whenCompletionsShown);
		return this;
	}

	public ForgeItemQuestHistoryBuilder whenTimeWasPassed(String whenTimeWasPassed) {
		this.whenTimeWasPassed = whenTimeWasPassed;
		return this;
	}

	public ForgeItemQuestHistoryBuilder whenTimeWasNotEnded(String whenTimeWasNotEnded) {
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
