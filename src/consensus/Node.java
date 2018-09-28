package consensus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import util.FileHandler;

import consensus.Peer.ConnectionType;
import consensus.StatisticsKeeper.StatisticsInfo;
import messages.DecisionMessage;
import messages.Message;
import messages.ProposalMessage;
import messages.VoteMessage;
import simulators.BasicConsensusSimulator;
import util.ConcurrentPrintWriter;
import util.RandomValuesGenerator;
import util.UnboundedMessageBuffer;

public class Node {

	public static final int TIME_OUT_TIME = 2;

	public enum RoundStep {
		RoundStepProposalWait, RoundStepPrevoteWait, RoundStepPrecommitWait
	};

	private static class MessageSentException extends Exception {
	}

	public static class RoundState {
		public int height;
		public int round;
		public RoundStep step;
		public ProposalMessage proposal;
		public VotesKeeper preVotes;
		public VotesKeeper preCommits;

		public RoundState(int nodeID) {
			height = 0;
			round = 0;
			step = RoundStep.RoundStepProposalWait;
			proposal = null;
			preVotes = new VotesKeeper(nodeID);
			preCommits = new VotesKeeper(nodeID);
		}
	}

	public static class PeerRoundState {
		public int height;
		public int round;
		public RoundStep step;
		public boolean proposal;
		public boolean decision;
		public VotesKeeper preVotes;
		public VotesKeeper preCommits;

		public PeerRoundState(int peerID) {
			height = 0;
			round = 0;
			step = RoundStep.RoundStepProposalWait;
			proposal = false;
			decision = false;
			preCommits = new VotesKeeper(peerID);
			preVotes = new VotesKeeper(peerID);
		}
	}

	private int id;
	private boolean isValidator;
	private RoundState roundState;

	private int numberOfPeers;
	private ArrayList<Peer> peers;
	private HashMap<Integer, PeerRoundState> peersRoundStates;

	private BlockChain blockChain;

	private Semaphore stateSemaphore;

	private ArrayList<Message> infoMessages;
	private ReceiveToutine receiveRoutine;
	private GossipRoutine gossipRoutine;

	public Node(boolean isValidator, int id) {
		super();
		this.isValidator = isValidator;
		this.id = id;
		this.peers = new ArrayList<>();
		this.infoMessages = new ArrayList<>();
		this.blockChain = new BlockChain();
	}

	public void init() {
		this.roundState = new RoundState(id);
		this.peersRoundStates = new HashMap<>();
		for (Peer peer : peers) {
			this.peersRoundStates.put(peer.getNode(), new PeerRoundState(peer.getNode()));
		}
		stateSemaphore = new Semaphore(1, true);
		receiveRoutine = new ReceiveToutine();
		gossipRoutine = new GossipRoutine();
	}

	public void startNode() {
		if (BasicConsensusSimulator.checkIfNodeIsProposer(id, roundState.height)) {
			String proposalValue = RandomValuesGenerator.getRandomString();
			roundState.proposal = new ProposalMessage(roundState.round, roundState.height, id, proposalValue);
		}
		receiveRoutine.start();
		gossipRoutine.start();
	}

	public void stopNode() {
		receiveRoutine.finish();
		gossipRoutine.finish();
	}

	public boolean addPeer(Peer newPeer) {
		if (id == newPeer.getNode()) {
			return false;
		}
		for (Peer peer : peers) {
			if (peer.getNode() == newPeer.getNode()) {
				return false;
			}
		}
		peers.add(newPeer);
		return true;
	}

	public class ReceiveToutine extends Thread {
		private boolean finished;

		public void run() {
			finished = false;
			UnboundedMessageBuffer buffer = Network.messageBuffers.get(id);
			while (!finished) {
				try {
					Message message = buffer.get();
					stateSemaphore.acquire();
					ArrayList<Message> newMessages = message.handle(roundState, peersRoundStates, blockChain);
					// ConcurrentPrintWriter.printOnSystemOut("RECEIVE:" + message.toString());
					infoMessages.addAll(newMessages);
					stateSemaphore.release();
				} catch (InterruptedException e) {
					finished = true;
				}
			}
		}

		public void finish() {
			finished = true;
			interrupt();
		}
	}

	public class GossipRoutine extends Thread {
		private boolean finished;

		public void run() {
			boolean doneSomething;
			finished = false;
			while (!finished) {
				doneSomething = false;
				try {
					stateSemaphore.acquire();
					// za svaki peer
					for (Peer peer : peers) {
						doneSomething = sendInfoMessages(peer);
						try {
							sendDecisionMessages(peer);
							sendProposalMessages(peer);
							sendVoteMessages(peer);
						} catch (MessageSentException e) {
							doneSomething = true;
						}
					}
					infoMessages = new ArrayList<>();
					if (!doneSomething) {
						stateSemaphore.release();
						Thread.sleep(TIME_OUT_TIME);
					} else {
						stateSemaphore.release();
					}
				} catch (InterruptedException e1) {
					finished = true;
				}
			}
		}

		private boolean sendInfoMessages(Peer peer) {
			boolean messageSent = false;
			if (!infoMessages.isEmpty()) {
				for (int i = 0; i < infoMessages.size(); i++) {
					try {
						Message msg = infoMessages.get(i).clone();
						sendMessage(id, peer.getNode(), peer.getConnectionType(), msg);
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
				}
				messageSent = true;
			}
			return messageSent;
		}

		private void sendProposalMessages(Peer peer) throws MessageSentException {
			if (roundState.proposal != null) {
				PeerRoundState peerRoundState = peersRoundStates.get(peer.getNode());
				int peerRound = peerRoundState.round;
				int peerHeight = peerRoundState.height;
				if (!peerRoundState.proposal && peerRound == roundState.round && peerHeight == roundState.height) {
					int proposalID = roundState.proposal.getProposalID();
					String proposalValue = roundState.proposal.getProposalValue();
					Message message = new ProposalMessage(peerRound, peerHeight, proposalID, proposalValue);
					sendMessage(id, peer.getNode(), peer.getConnectionType(), message);
					peerRoundState.proposal = true;
					throw new MessageSentException();
				}
			}
		}

		private void sendDecisionMessages(Peer peer) throws MessageSentException {
			PeerRoundState peerRoundState = peersRoundStates.get(peer.getNode());
			int peerRound = peerRoundState.round;
			int peerHeight = peerRoundState.height;
			if (roundState.height > peerHeight && !peerRoundState.decision) {
				peerRoundState.decision = true;
				String decisionValue = blockChain.getDecision(peerHeight, peerRound);
				Message message = new DecisionMessage(peerRound, peerHeight, decisionValue);
				sendMessage(id, peer.getNode(), peer.getConnectionType(), message);
				throw new MessageSentException();
			}
		}

		private void sendMessage(int senderID, int receiverID, ConnectionType connectionType, Message message) {
			message.setSenderID(senderID);
			message.setReceiverID(receiverID);
			long timeStamp = System.currentTimeMillis()
					+ RandomValuesGenerator.getRandomMessageTripDuration(connectionType);
			message.setTimeStamp(timeStamp);
			// ConcurrentPrintWriter.printOnSystemOut("SENT:" + message);
			BasicConsensusSimulator.network.addMessage(message);
		}

		private ProposalMessage makeProposalMessage() {
			if (roundState.proposal == null
					&& roundState.height % BasicConsensusSimulator.numberOfValidatorNodes == id) {
				String proposalValue = RandomValuesGenerator.getRandomString();
				roundState.proposal = new ProposalMessage(roundState.round, roundState.height, id, proposalValue);
			}
			return roundState.proposal;
		}

		private void sendVoteMessages(Peer peer) throws MessageSentException { // zasto saljemo prevotes ako smo
																				// poslali decision?
			PeerRoundState peerRoundState = peersRoundStates.get(peer.getNode());
			int peerHeight = peerRoundState.height;
			int peerRound = peerRoundState.round;
			if (roundState.height == peerHeight) {
				if (peerRoundState.step.ordinal() <= RoundStep.RoundStepPrevoteWait.ordinal()
						&& peerRound <= roundState.round) {
					// saljem mu prevote iz njegove runde
					VotesKeeper peerPrevotes = peerRoundState.preVotes;
					int newVoteID = peerPrevotes.addRandomVote(roundState.preVotes, peerHeight, peerRound);
					if (newVoteID != -1) { // znaci da je nasao nesto sto sused nema
						Message message = new VoteMessage(peerRound, peerHeight, false, newVoteID, null);
						sendMessage(id, peer.getNode(), peer.getConnectionType(), message);
						throw new MessageSentException();
					}
				}
				if (peerRoundState.step.ordinal() <= RoundStep.RoundStepPrecommitWait.ordinal()
						&& peerRound <= roundState.round) {
					VotesKeeper peerPrecommits = peerRoundState.preCommits;
					int newVoteID = peerPrecommits.addRandomVote(roundState.preCommits, peerHeight, peerRound);
					if (newVoteID != -1) { // znaci da je nasao nesto sto sused nema
						Message message = new VoteMessage(peerRound, peerHeight, true, newVoteID, null);
						sendMessage(id, peer.getNode(), peer.getConnectionType(), message);
						throw new MessageSentException();
					}
				}
			}
		}

		public void finish() {
			finished = true;
			interrupt();
		}
	}

	public int getNumberOfPeers() {
		return numberOfPeers;
	}

	public ArrayList<Peer> getPeers() {
		return peers;
	}

	public void setPeers(ArrayList<Peer> peers) {
		this.peers = peers;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node {\n");
		builder.append("id: " + id + "\n");
		builder.append("isValidator: " + isValidator + "\n");
		builder.append("numOfPeers: " + numberOfPeers + "\n");

		// builder.append(" Peers {\n");
		// for (Link peer : peers) {
		// builder.append(" peerID: " + peer.getNode() + "\n");
		// builder.append(" connectionType: " + peer.getConnectionType() + "\n");
		// }
		// builder.append(" }\n");

		builder.append("Prevotes:\n");
		builder.append(roundState.preVotes + "\n");
		builder.append("Precommits:\n");
		builder.append(roundState.preCommits + "\n");
		builder.append(blockChain);
		builder.append("}\n");
		FileHandler.putTextToFile("simulationReview.txt", builder.toString());
		return builder.toString();
	}

	public int getNodeId() {
		return id;
	}

	public void setNumberOfPeers(int numberOfPeers) {
		this.numberOfPeers = numberOfPeers;
	}

	public BlockChain getBlockChain() {
		return blockChain;
	}

	public static int getTimeOutTime() {
		return TIME_OUT_TIME;
	}

	public RoundState getRoundState() {
		return roundState;
	}
	
	

}
