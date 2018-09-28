package messages;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import consensus.BlockChain;
import consensus.Node.PeerRoundState;
import consensus.Node.RoundState;
import consensus.Node.RoundStep;
import util.ConcurrentPrintWriter;

public class ProposalMessage extends Message {

	private int proposalID;
	private String proposalValue;

	public ProposalMessage(int senderID, int round, int height, int receiverID, long timeStamp, int proposalID,
			String proposalValue) {
		super(senderID, round, height, receiverID, timeStamp);
		this.proposalID = proposalID;
		this.proposalValue = proposalValue;
	}

	public ProposalMessage(int round, int height, int proposalID, String proposalValue) {
		super(round, height);
		this.proposalID = proposalID;
		this.proposalValue = proposalValue;
	}

	@Override
	public ArrayList<Message> handle(RoundState roundState, HashMap<Integer, PeerRoundState> peersRoundStates,
			BlockChain blockChain) {

		PeerRoundState peerRoundState = peersRoundStates.get(senderID);
		updatePeerState(peerRoundState);

		ArrayList<Message> newMessages = new ArrayList<>();
		if (checkMessage(roundState)) {
			roundState.proposal = this;
			newMessages.addAll(proposal(roundState, peersRoundStates, blockChain));
		}
		return newMessages;
	}

	private void updatePeerState(PeerRoundState peerRoundState) {
		if (height >= peerRoundState.height) {
			peerRoundState.height = height;
			peerRoundState.proposal = true;
		}

	}

	private boolean checkMessage(RoundState roundState) {
		return roundState.step == RoundStep.RoundStepProposalWait && roundState.height == height
				&& roundState.round == round;
	}

	public String getProposalValue() {
		return proposalValue;
	}

	public void setProposalValue(String proposalValue) {
		this.proposalValue = proposalValue;
	}

	public int getProposalID() {
		return proposalID;
	}

	public void setProposalID(int proposalID) {
		this.proposalID = proposalID;
	}

	@Override
	protected String getType() {
		return "ProposalMessage";
	}

	@Override
	public ProposalMessage clone() throws CloneNotSupportedException {
		return new ProposalMessage(senderID, round, height, receiverID, timeStamp, proposalID, proposalValue);
	}

}
