package messages;

import java.util.ArrayList;
import java.util.HashMap;

import consensus.BlockChain;
import consensus.Node.PeerRoundState;
import consensus.Node.RoundState;
import consensus.Node.RoundStep;
import util.ConcurrentPrintWriter;

public class DecisionMessage extends Message {

	private String decisionValue;

	public DecisionMessage(int senderID, int round, int height, int receiverID, long timeStamp, String decisionValue) {
		super(senderID, round, height, receiverID, timeStamp);
		this.decisionValue = decisionValue;
	}

	public DecisionMessage(int round, int height, String decisionValue) {
		super(round, height);
		this.decisionValue = decisionValue;
	}

	@Override
	public ArrayList<Message> handle(RoundState roundState, HashMap<Integer, PeerRoundState> peersRoundStates,
			BlockChain blockChain) {
		
		ArrayList<Message> newMessages = new ArrayList<>();
		if (roundState.height == height) {
			String proposalValue = decisionValue;
			blockChain.addDecision(roundState.height, roundState.round, proposalValue,timeStamp);
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

	@Override
	protected String getType() {
		return "DecisionMessage";
	}

	@Override
	public DecisionMessage clone() throws CloneNotSupportedException {
		return new DecisionMessage(senderID, round, height, receiverID, timeStamp, decisionValue);
	}

}
