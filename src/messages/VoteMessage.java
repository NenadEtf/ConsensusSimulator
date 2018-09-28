package messages;

import java.util.ArrayList;
import java.util.HashMap;

import consensus.BlockChain;
import consensus.Node.PeerRoundState;
import consensus.Node.RoundState;
import consensus.Node.RoundStep;
import consensus.StatisticsKeeper.StatisticsInfo;
import consensus.VotesKeeper;
import simulators.BasicConsensusSimulator;
import util.ConcurrentPrintWriter;

public class VoteMessage extends Message {

	private boolean isPreCommit;// govori kog je tipa poruka
	private int voteID;// govori ciji vote ima sused
	private String value;

	public VoteMessage(int senderID, int round, int height, int receiverID, long timeStamp, boolean isPreCommit,
			int voteID, String value) {
		super(senderID, round, height, receiverID, timeStamp);
		this.isPreCommit = isPreCommit;
		this.voteID = voteID;
		this.value = value;
	}

	public VoteMessage(int round, int height, boolean isPreCommit, int voteID, String value) {
		super(round, height);
		this.isPreCommit = isPreCommit;
		this.voteID = voteID;
		this.value = value;
	}

	@Override
	public ArrayList<Message> handle(RoundState roundState, HashMap<Integer, PeerRoundState> peersRoundStates,
			BlockChain blockChain) {

		PeerRoundState peerRoundState = peersRoundStates.get(senderID);
		updatePeer(peerRoundState);

		ArrayList<Message> newMessages = new ArrayList<>();
		if (checkMessage(roundState)) {
			if (addVoteMessage(roundState)) {
				if (isPreCommit) {
					return preCommit(roundState, peersRoundStates, blockChain);
				} else {
					return preVote(roundState, peersRoundStates, blockChain);
				}
			}
		}
		return newMessages;
	}

	private void updatePeer(PeerRoundState peerRoundState) {
		if (isPreCommit) {
			peerRoundState.preCommits.addVote(height, round, voteID);
		} else {
			peerRoundState.preVotes.addVote(height, round, voteID);
		}

	}

	private boolean addVoteMessage(RoundState roundState) {
		if (isPreCommit) {
			VotesKeeper preCommits = roundState.preCommits;
			if (preCommits.addVote(height, round, voteID)) {
				return true;
			}
		} else {
			VotesKeeper preVotes = roundState.preVotes;
			if (preVotes.addVote(height, round, voteID)) {
				return true;
			}
		}
		return false;

	}

	private boolean checkMessage(RoundState roundState) {
		return roundState.height == height && roundState.round == round;
	}

	@Override
	protected String getType() {
		return "VoteMessage[" + isPreCommit + "]";
	}

	@Override
	public VoteMessage clone() throws CloneNotSupportedException {
		return new VoteMessage(senderID, round, height, receiverID, timeStamp, isPreCommit, voteID, value);
	}

}
