package consensus;

import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import simulators.BasicConsensusSimulator;
import util.ConcurrentPrintWriter;

public class VotesKeeper {

	private static class Votes {
		private ArrayList<Boolean> votes;
		private ArrayList<Integer> votesIDS;
		private int numberOfVotes;
		private int numberOfTries;

		public Votes() {
			super();
			this.votesIDS = new ArrayList<>();
			this.votes = new ArrayList<>(BasicConsensusSimulator.numberOfValidatorNodes);
			for (int i = 0; i < BasicConsensusSimulator.numberOfValidatorNodes; i++) {
				this.votes.add(false);
			}
			this.numberOfVotes = 0;
			this.numberOfTries = 0;
		}

		public int getNumberOfVotes() {
			return numberOfVotes;
		}

		public int getNumberOfTries() {
			return numberOfTries;
		}

		public ArrayList<Integer> getVotesIDS() {
			Collections.shuffle(this.votesIDS);
			return votesIDS;
		}

		public boolean addVote(int index) {
			if (this.votes.get(index)) {
				numberOfTries++;
				return false;
			} else {
				numberOfVotes++;
				this.votesIDS.add(index);
				this.votes.remove(index);
				this.votes.add(index, true);
				return true;
			}
		}

		public Boolean getVote(int index) {
			return this.votes.get(index);
		}

		public int getRandomVote() {
			int index = ThreadLocalRandom.current().nextInt(0, votesIDS.size());
			return votesIDS.get(index);
		}

		@Override
		public String toString() {
			return "Votes [votes=" + votes + ", numberOfVotes=" + numberOfVotes + "]";
		}

	}

	private HashMap<Integer, HashMap<Integer, Votes>> votes;

	private int nodeID;

	// HashMap<Height, HashMap< Round, HashMap<NodeId,Boolean>>>>)
	public VotesKeeper(int nodeID) {
		votes = new HashMap<>();
		this.nodeID = nodeID;
	}

	public boolean addVote(int height, int round, int voteID) {
		if (votes.get(height) == null) {
			votes.put(height, new HashMap<>());
		}
		if (votes.get(height).get(round) == null) {
			votes.get(height).put(round, new Votes());
		}
		return votes.get(height).get(round).addVote(voteID);
	}

	public int addMyVote(int height, int round) {
		if (nodeID < BasicConsensusSimulator.numberOfValidatorNodes) {
			addVote(height, round, nodeID);
			return nodeID;
		} else {
			return -1;
		}

	}

	public boolean isNumberOfVotesEqualTwoThirdsPlusOne(int height, int round) {
		try {
			int twoThirdsPlusOne = BasicConsensusSimulator.numberOfValidatorNodes * 2 / 3 + 1;
			int voteSize = votes.get(height).get(round).getNumberOfVotes();
			return voteSize >= twoThirdsPlusOne;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public ArrayList<Integer> getAllVotes(int height, int round) {
		try {
			return votes.get(height).get(round).getVotesIDS();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public boolean hasVote(int height, int round, int voteID) {
		try {
			return votes.get(height).get(round).getVote(voteID);
		} catch (NullPointerException e) {
			return false;
		}
	}
	

	public int getRandomVote(int height, int round) {
		return votes.get(height).get(round).getRandomVote();
	}

	public int addRandomVote(VotesKeeper from, int height, int round) {
		try {
			for (Integer voteID : from.getAllVotes(height, round)) {
				if (!this.hasVote(height, round, voteID)) {
					this.addVote(height, round, voteID);
					return voteID;
				}
			}
			return -1;
		} catch (NullPointerException e) {
			return -1;
		}
	}

	public int getNodeID() {
		return nodeID;
	}

	public double getAverageNumberOfDuplicats() {
		double sum = 0;
		int i = 0;
		Iterator iteratorHeight = votes.entrySet().iterator();
		while (iteratorHeight.hasNext()) {
			Map.Entry<Integer, HashMap<Integer, Votes>> pair = (Map.Entry<Integer, HashMap<Integer, Votes>>) iteratorHeight
					.next();
			Iterator iteratorRound = pair.getValue().entrySet().iterator();
			while (iteratorRound.hasNext()) {
				Map.Entry<Integer, Votes> pairRound = (Map.Entry<Integer, Votes>) iteratorRound.next();
				sum += pairRound.getValue().numberOfTries;
				i++;
			}
		}
		return sum / i;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		Iterator iteratorHeight = votes.entrySet().iterator();
		while (iteratorHeight.hasNext()) {
			Map.Entry<Integer, HashMap<Integer, Votes>> pair = (Map.Entry<Integer, HashMap<Integer, Votes>>) iteratorHeight
					.next();
			Iterator iteratorRound = pair.getValue().entrySet().iterator();
			while (iteratorRound.hasNext()) {
				Map.Entry<Integer, Votes> pairRound = (Map.Entry<Integer, Votes>) iteratorRound.next();
				stringBuilder.append("[" + pair.getKey() + "," + pairRound.getKey() + "]-["
						+ pairRound.getValue().numberOfVotes + "-" + pairRound.getValue().numberOfTries + "]; ");
			}
		}
		return stringBuilder.toString();
	}

	public static void main(String args[]) {
		VotesKeeper votes = new VotesKeeper(2);
		votes.addMyVote(0, 0);
		votes.addVote(0, 0, 1);
		votes.addVote(0, 0, 3);
		votes.addVote(0, 0, 6);
		votes.addVote(0, 0, 1);
		votes.addVote(1, 0, 1);
		votes.addVote(1, 2, 2);
		votes.addVote(4, 0, 1);
		votes.hasVote(0, 0, 1);
		System.out.println(votes.getAllVotes(0, 0));
		System.out.println(votes.getAllVotes(1, 0));
		System.out.println(votes.getAllVotes(1, 2));
		System.out.println(votes.getAllVotes(4, 0));
		VotesKeeper fromVotes = new VotesKeeper(3);
		fromVotes.addVote(0, 0, 1);
		fromVotes.addVote(0, 0, 3);
		fromVotes.addVote(2, 0, 5);
		System.out.println(votes.addRandomVote(fromVotes, 2, 0));
		System.out.println(votes.getAllVotes(0, 0));
		System.out.println(votes.getAllVotes(2, 0));

	}

}
