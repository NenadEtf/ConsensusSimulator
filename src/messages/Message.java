package messages;

import java.util.ArrayList;
import java.util.HashMap;

import util.ConcurrentPrintWriter;
import util.FileHandler;
import util.RandomValuesGenerator;
import consensus.BlockChain;
import consensus.Node.PeerRoundState;
import consensus.Node.RoundState;
import consensus.Node.RoundStep;
import consensus.StatisticsKeeper;
import consensus.StatisticsKeeper.StatisticsInfo;
import simulators.BasicConsensusSimulator;

public abstract class Message implements Cloneable {

	protected int senderID;
	protected int receiverID;
	protected int round;
	protected int height;
	protected long timeStamp;

	protected Message(int senderID, int round, int height, int receiverID, long timeStamp) {
		super();
		this.senderID = senderID;
		this.round = round;
		this.height = height;
		this.timeStamp = timeStamp;
		this.receiverID = receiverID;
	}

	public Message(int round, int height) {
		super();
		this.round = round;
		this.height = height;
	}

	public abstract ArrayList<Message> handle(RoundState roundState, HashMap<Integer, PeerRoundState> peersRoundStates,
			BlockChain blockChain);

	public void setSenderID(int senderID) {
		this.senderID = senderID;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getReceiverID() {
		return receiverID;
	}

	public void setReceiverID(int receiverID) {
		this.receiverID = receiverID;
	}

	protected boolean startNewHeight(RoundState roundState) {
		boolean isProposer = false;
		roundState.step = RoundStep.RoundStepProposalWait;
		roundState.proposal = null;
		roundState.round = 0;
		roundState.height += 1;
		if (BasicConsensusSimulator.checkIfNodeIsProposer(receiverID, roundState.height)) {
			String proposalValue = RandomValuesGenerator.getRandomString();
			roundState.proposal = new ProposalMessage(roundState.round, roundState.height, receiverID, proposalValue);
			isProposer = true;
		}
		if (roundState.height >= BasicConsensusSimulator.heightOfSimulation) {
			BasicConsensusSimulator.endBarrier.release();
		}
		return isProposer;
	}

	protected ArrayList<Message> preVote(RoundState roundState, HashMap<Integer, PeerRoundState> peersRoundStates,
			BlockChain blockChain) {
		ArrayList<Message> newMessages = new ArrayList<>();
		if (roundState.step == RoundStep.RoundStepPrevoteWait
				&& roundState.preVotes.isNumberOfVotesEqualTwoThirdsPlusOne(roundState.height, roundState.round)) {
			roundState.step = RoundStep.RoundStepPrecommitWait;
			Message newRoundStepMessage = new NewRoundStepMessage(roundState.round, roundState.height, roundState.step);
			newMessages.add(newRoundStepMessage);
			int myVote = roundState.preCommits.addMyVote(roundState.height, roundState.round);
			if (myVote != -1) {
				Message newHasVoteMessage = new HasVoteMessage(roundState.round, roundState.height, true, myVote);
				newMessages.add(newHasVoteMessage);
			}
			newMessages.addAll(preCommit(roundState, peersRoundStates, blockChain));
		}
		return newMessages;
	}

	protected ArrayList<Message> preCommit(RoundState roundState, HashMap<Integer, PeerRoundState> peersRoundStates,
			BlockChain blockChain) {
		ArrayList<Message> newMessages = new ArrayList<>();
		if (roundState.step != RoundStep.RoundStepProposalWait
				&& roundState.preCommits.isNumberOfVotesEqualTwoThirdsPlusOne(roundState.height, roundState.round)) {
			ProposalMessage proposalMessage = (ProposalMessage) roundState.proposal;
			String proposalValue = proposalMessage.getProposalValue();
			blockChain.addDecision(roundState.height, roundState.round, proposalValue, timeStamp);
			if (startNewHeight(roundState)) {
				newMessages.addAll(proposal(roundState, peersRoundStates, blockChain));
			} else {
				Message newRoundStepMessage = new NewRoundStepMessage(roundState.round, roundState.height,
						roundState.step);
				newMessages.add(newRoundStepMessage);
			}
		}
		return newMessages;
	}

	protected ArrayList<Message> proposal(RoundState roundState, HashMap<Integer, PeerRoundState> peersRoundStates,
			BlockChain blockChain) {
		ArrayList<Message> newMessages = new ArrayList<>();
		roundState.step = RoundStep.RoundStepPrevoteWait;
		Message newRoundStepMessage = new NewRoundStepMessage(roundState.round, roundState.height, roundState.step);
		newMessages.add(newRoundStepMessage);
		int myVote = roundState.preVotes.addMyVote(roundState.height, roundState.round);
		if (myVote != -1) {
			Message newHasVoteMessage = new HasVoteMessage(roundState.round, roundState.height, false, myVote);
			newMessages.add(newHasVoteMessage);
		}
		ArrayList<Message> prevoteMsg = preVote(roundState, peersRoundStates, blockChain);
		newMessages.addAll(prevoteMsg);
		return newMessages;
	}

	protected boolean checkHeight() {
		if (height >= BasicConsensusSimulator.heightOfSimulation) {
			return false;
		}
		return true;
	}

	protected abstract String getType();

	public abstract Message clone() throws CloneNotSupportedException;

	@Override
	public String toString() {
		return getType() + " [senderID=" + senderID + ", receiverID=" + receiverID + ", round=" + round + ", height="
				+ height + ", timeStamp=" + timeStamp + ", timeStamp(RB)="
				+ (timeStamp - BasicConsensusSimulator.simulationStartTime) + ", deviation="
				+ (System.currentTimeMillis() - timeStamp) + "]";
	}

}
