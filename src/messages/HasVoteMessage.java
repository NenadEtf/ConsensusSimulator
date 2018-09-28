package messages;

import java.util.ArrayList;
import java.util.HashMap;

import consensus.BlockChain;
import consensus.Node.PeerRoundState;
import consensus.Node.RoundState;
import util.ConcurrentPrintWriter;

public class HasVoteMessage extends Message {

	private boolean isPreCommit;// govori kog je tipa poruka
	private int voteID;// govori ciji vote ima sused

	public HasVoteMessage(int senderID, int round, int height, int receiverID, long timeStamp, boolean isPreCommit,
			int voteID) {
		super(senderID, round, height, receiverID, timeStamp);
		this.isPreCommit = isPreCommit;
		this.voteID = voteID;
	}

	public HasVoteMessage(int round, int height, boolean isPreCommit, int voteID) {
		super(round, height);
		this.isPreCommit = isPreCommit;
		this.voteID = voteID;
	}

	@Override
	public ArrayList<Message> handle(RoundState roundState, HashMap<Integer, PeerRoundState> peersRoundStates,
			BlockChain blockChain) {
		ArrayList<Message> newMessages = new ArrayList<>();
		PeerRoundState peerRoundState = peersRoundStates.get(senderID);
		if (isPreCommit) {
			peerRoundState.preCommits.addVote(height, round, voteID);
		} else {
			peerRoundState.preVotes.addVote(height, round, voteID);
		}
		return newMessages;
	}

	@Override
	protected String getType() {
		return "HasVoteMessage";
	}

	@Override
	public HasVoteMessage clone() throws CloneNotSupportedException {
		return new HasVoteMessage(senderID, round, height, receiverID, timeStamp, isPreCommit, voteID);
	}

}
