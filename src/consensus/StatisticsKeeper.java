package consensus;

import java.util.ArrayList;

import simulators.BasicConsensusSimulator;

public class StatisticsKeeper {

	public static class StatisticsInfo {
		private int numberOfPrevotes;
		private int numberOfDuplicatedPrevotes;
		private int numberOfPrecommits;
		private int numberOfDuplicatedPrecommits;
		private int numberOfProposal;
		private int numberOfDuplicatedProposal;
		private long consensusStartTime;
		private long consensusFinishTime;

		public StatisticsInfo() {
			super();
			this.numberOfPrevotes = 0;
			this.numberOfDuplicatedPrevotes = 0;
			this.numberOfPrecommits = 0;
			this.numberOfDuplicatedPrecommits = 0;
			this.numberOfProposal = 0;
			this.numberOfDuplicatedProposal = 0;
		}

		public void incrementNumberOfPrevotes() {
			numberOfPrevotes++;
		}

		public void incrementNumberOfDuplicatedPrevotes() {
			numberOfDuplicatedPrevotes++;
		}

		public void incrementNumberOfPrecommits() {
			numberOfPrecommits++;
		}

		public void incrementNumberOfDuplicatedPrecommits() {
			numberOfDuplicatedPrecommits++;
		}

		public void incrementNumberOfProposal() {
			numberOfProposal++;
		}

		public void incrementNumberOfDuplicatedProposal() {
			numberOfDuplicatedProposal++;
		}

		public long getConsensusStartTime() {
			return consensusStartTime;
		}

		public long getConsensusFinishTime() {
			return consensusFinishTime;
		}

		public void setConsensusStartTime(long consensusStartTime) {
			this.consensusStartTime = consensusStartTime;
		}

		public void setConsensusFinishTime(long consensusFinishTime) {
			this.consensusFinishTime = consensusFinishTime;
		}

		public int getNumberOfPrevotes() {
			return numberOfPrevotes;
		}

		public int getNumberOfDuplicatedPrevotes() {
			return numberOfDuplicatedPrevotes;
		}

		public int getNumberOfPrecommits() {
			return numberOfPrecommits;
		}

		public int getNumberOfDuplicatedPrecommits() {
			return numberOfDuplicatedPrecommits;
		}

		public int getNumberOfProposal() {
			return numberOfProposal;
		}

		public int getNumberOfDuplicatedProposal() {
			return numberOfDuplicatedProposal;
		}

		@Override
		public String toString() {
			return " [numberOfPrevotes=" + numberOfPrevotes + ", numberOfDuplicatedPrevotes="
					+ numberOfDuplicatedPrevotes + ", numberOfPrecommits=" + numberOfPrecommits
					+ ", numberOfDuplicatedPrecommits=" + numberOfDuplicatedPrecommits + ", numberOfProposal="
					+ numberOfProposal + ", numberOfDuplicatedProposal=" + numberOfDuplicatedProposal
					+ ", consensusStartTime=" + (consensusStartTime - BasicConsensusSimulator.simulationStartTime)
					+ ", consensusFinishTime=" + (consensusFinishTime - BasicConsensusSimulator.simulationStartTime)
					+ "]";
		}

	}

	private ArrayList<StatisticsInfo> statistics;

	public StatisticsKeeper() {
		super();
		statistics = new ArrayList<>();
	}

	public StatisticsInfo getStatistics(int height) {
		try {
			StatisticsInfo info = statistics.get(height);
		} catch (IndexOutOfBoundsException e) {
			this.addStatistics();
		}
		return statistics.get(height);
	}

	public void addStatistics() {
		statistics.add(new StatisticsInfo());
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < BasicConsensusSimulator.heightOfSimulation; i++) {
			stringBuilder.append("ConsensusHeight: " + (i) + "\n");
			stringBuilder.append(statistics.get(i) + "\n");
		}
		return stringBuilder.toString();
	}

}
