package consensus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import simulators.BasicConsensusSimulator;

public class BlockChain {

	private static class BlockChainValue {
		private String value;
		private long startTime;
		private long finishTime;

		public BlockChainValue(String value, long startTime, long finishTime) {
			super();
			this.value = value;
			this.startTime = startTime;
			this.finishTime = finishTime;
		}

		public String getValue() {
			return value;
		}

		public long getConsensusDuration() {
			return finishTime - startTime;
		}

		@Override
		public String toString() {
			return "[value=" + value + ", duration=" + (finishTime - startTime) + "]";
		}

	}

	private HashMap<Integer, HashMap<Integer, BlockChainValue>> chain;

	public BlockChain() {
		chain = new HashMap<>();
	}

	public void addDecision(int height, int round, String value, long finishTime) {
		if (chain.get(height) == null) {
			chain.put(height, new HashMap<>());
		}
		long startTime;
		try {
			startTime = chain.get(height - 1).get(round).finishTime + 1;
		} catch (NullPointerException e) {
			startTime = BasicConsensusSimulator.simulationStartTime;
		}

		BlockChainValue newDecision = new BlockChainValue(value, startTime, finishTime);
		chain.get(height).put(round, newDecision);
	}

	public String getDecision(int height, int round) {
		try {
			return chain.get(height).get(round).getValue();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Blockchain:\n");
		Iterator iteratorHeight = chain.entrySet().iterator();
		while (iteratorHeight.hasNext()) {
			Map.Entry<Integer, HashMap<Integer, BlockChainValue>> pair = (Map.Entry<Integer, HashMap<Integer, BlockChainValue>>) iteratorHeight
					.next();
			Iterator iteratorRound = pair.getValue().entrySet().iterator();
			while (iteratorRound.hasNext()) {
				Map.Entry<Integer, BlockChainValue> pairRound = (Map.Entry<Integer, BlockChainValue>) iteratorRound
						.next();
				stringBuilder.append(
						"[" + pair.getKey() + "," + pairRound.getKey() + "]-" + pairRound.getValue().toString() + "; ");
			}
		}
		return stringBuilder.toString();
	}

	public double getAverageConsensusTime() {
		double time = 0;
		int numberOfConsensus = 0;
		Iterator iteratorHeight = chain.entrySet().iterator();
		while (iteratorHeight.hasNext()) {
			Map.Entry<Integer, HashMap<Integer, BlockChainValue>> pair = (Map.Entry<Integer, HashMap<Integer, BlockChainValue>>) iteratorHeight
					.next();
			Iterator iteratorRound = pair.getValue().entrySet().iterator();
			while (iteratorRound.hasNext()) {
				Map.Entry<Integer, BlockChainValue> pairRound = (Map.Entry<Integer, BlockChainValue>) iteratorRound
						.next();
				time += pairRound.getValue().getConsensusDuration();
				numberOfConsensus++;
			}
		}
		return time / numberOfConsensus;
	}

}
