package messages;

import java.util.ArrayList;
import java.util.HashMap;

import consensus.BlockChain;
import consensus.Node.PeerRoundState;
import consensus.Node.RoundState;
import consensus.Node.RoundStep;
import util.ConcurrentPrintWriter;

public class NewRoundStepMessage extends Message {

	private RoundStep newRoundStep;

	public NewRoundStepMessage(int senderID, int round, int height, int receiverID, long timeStamp,
			RoundStep newRoundStep) {
		super(senderID, round, height, receiverID, timeStamp);
		this.newRoundStep = newRoundStep;
	}

	public NewRoundStepMessage(int round, int height, RoundStep newRoundStep) {
		super(round, height);
		this.newRoundStep = newRoundStep;
	}

	@Override
	public ArrayList<Message> handle(RoundState roundState, HashMap<Integer, PeerRoundState> peersRoundStates,
			BlockChain blockChain) {
		ArrayList<Message> newMessages = new ArrayList<>();
		PeerRoundState peerRoundState = peersRoundStates.get(senderID);
		if (peerRoundState.height < height) {
			peerRoundState.decision = false;
			peerRoundState.height = height;
			if (newRoundStep == RoundStep.RoundStepProposalWait) {
				peerRoundState.proposal = false;
			} else {
				peerRoundState.proposal = true;
			}
			peerRoundState.step = newRoundStep;
		} else if (peerRoundState.height == height) {
			if (peerRoundState.step.ordinal() < newRoundStep.ordinal()) {
				peerRoundState.proposal = true;
				peerRoundState.step = newRoundStep;
			}
		}

		return newMessages;
	}

	@Override
	protected String getType() {
		return "NewRoundStepMessage";
	}

	@Override
	public NewRoundStepMessage clone() throws CloneNotSupportedException {
		return new NewRoundStepMessage(senderID, round, height, receiverID, timeStamp, newRoundStep);
	}
}
