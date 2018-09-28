package simulators;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import consensus.Peer;
import consensus.Peer.ConnectionType;
import consensus.Network;
import consensus.Node;
import util.ConcurrentPrintWriter;
import util.FileHandler;
import util.GraphMaker;
import util.UnboundedMessageBuffer;

public class BasicConsensusSimulator {

	public static int numberOfNodes;
	public static int numberOfValidatorNodes;
	public static int heightOfSimulation;
	public static int numberOfOutgoingConnections;
	public static long simulationStartTime;
	public static long simulationEndTime;
	public static Semaphore endBarrier;
	public static Network network;

	private ArrayList<Node> nodes;

	private ArrayList<Double> xValues = new ArrayList<>();
	private ArrayList<Double> yValues = new ArrayList<>();
	private ArrayList<Double> yValuesPrecommit = new ArrayList<>();
	private ArrayList<Double> yValuesPrevotes = new ArrayList<>();

	public void startSimulation() {
		while (true) {
			getSimulationParameters();
			if (initNet()) {
				refreshPeersNumbers();
				initNodes();
				simulationStartTime = System.currentTimeMillis();
				network = new Network();
				startNodes();
				network.start();
				waitForTheEnd();
				network.stopNetwork();
				stopNodes();
				printNodes();
				simulationEndTime = System.currentTimeMillis();
				ConcurrentPrintWriter.printOnSystemOut("Simulation has finished!");
				if (startNewSimulation()) {
					collectStatistics();
				} else {
					makeDelayGraphs();
				}

			}
		}
	}

	public void startSimulationAndMakeGraphs() {
		heightOfSimulation = 100;
		numberOfValidatorNodes = 100;
		numberOfOutgoingConnections = 10;
		for (int i = 0; i <= 100; i += 10) {
			numberOfNodes = numberOfValidatorNodes + i;
			if (initNet()) {
				refreshPeersNumbers();
				initNodes();
				simulationStartTime = System.currentTimeMillis();
				network = new Network();
				startNodes();
				network.start();
				waitForTheEnd();
				network.stopNetwork();
				stopNodes();
				printNodes();
				simulationEndTime = System.currentTimeMillis();
				ConcurrentPrintWriter.printOnSystemOut("Simulation has finished!");
				collectStatistics();
			}
		}
		makeDelayGraphs();
		makePrecommitOverflowGraphs();
		makePrevoteOverflowGraphs();

	}

	private boolean startNewSimulation() {
		System.out.print("New simulation(1-yes,0-no)? ");
		Scanner scanner = new Scanner(System.in);
		int answer = scanner.nextInt();
		return answer == 1;
	}

	private void startNodes() {
		for (Node node : nodes) {
			node.startNode();
		}
	}

	private void stopNodes() {
		for (Node node : nodes) {
			node.stopNode();
		}
	}

	private void printNodes() {
		for (Node node : nodes) {
			System.out.println(node);
		}
	}

	private void waitForTheEnd() {
		endBarrier = new Semaphore(0);
		endBarrier.acquireUninterruptibly(numberOfNodes);
	}

	private boolean initNet() {
		nodes = new ArrayList<>();
		for (int i = 0; i < numberOfNodes; i++) {
			if (i < numberOfValidatorNodes) {
				nodes.add(new Node(true, i));
			} else {
				nodes.add(new Node(false, i));
			}
			Network.addNodeBuffer(i);
		}
		// postavljanje suseda
		for (Node node : nodes) {
			for (int i = 0; i < numberOfOutgoingConnections; i++) {
				if (!addRandomPeer(node)) {
					break;
				}
			}
		}
		if (checkNetConnectivity()) {
			System.out.println("Net connected properly!");
			return true;
		} else {
			error("Net has not been connected properly!");
			return false;
		}
	}

	private void initNodes() {
		for (Node node : nodes) {
			node.init();
		}
	}

	private void refreshPeersNumbers() {
		for (Node node : nodes) {
			node.setNumberOfPeers(node.getPeers().size());
		}
	}

	private boolean addRandomPeer(Node node) {
		ConnectionType connectionType;
		int randType = ThreadLocalRandom.current().nextInt(0, 100);
		if (randType < 20) {
			connectionType = ConnectionType.LAN;
		} else {
			connectionType = ConnectionType.WAN;
		}
		int i = 0;
		int nodeID = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
		while (!node.addPeer(new Peer(connectionType, nodeID))) {
			nodeID = ThreadLocalRandom.current().nextInt(0, numberOfNodes);
			if (i++ > 10) {
				return false;
			}
		}
		nodes.get(nodeID).addPeer(new Peer(connectionType, node.getNodeId()));
		return true;
	}

	private boolean checkNetConnectivity() {
		ArrayList<Integer> nodeIDS = new ArrayList<>();
		nodeIDS.add(0);
		for (int i = 0; i < nodeIDS.size(); i++) {
			for (Peer peer : nodes.get(nodeIDS.get(i)).getPeers()) {
				if (!nodeIDS.contains(peer.getNode())) {
					nodeIDS.add(peer.getNode());
				}
			}
		}
		if (nodeIDS.size() != numberOfNodes) {
			return false;
		} else {
			return true;
		}
	}

	private void getSimulationParameters() {
		Scanner in = new Scanner(System.in);
		int temp = 0;
		System.out.println("*****BASIC CONSENSUS SIMULATOR*****");
		System.out.print("Enter number of nodes: ");
		temp = in.nextInt();
		while (temp < 4) {
			error("Number must be greater then 4!");
			System.out.print("Enter again: ");
			temp = in.nextInt();
		}
		numberOfNodes = temp;

		System.out.print("Enter number of validator-node: ");
		temp = in.nextInt();
		while (temp < 4 || temp > numberOfNodes) {
			error("Number must be greater then 4, and it must not be greater than number of nodes!");
			System.out.print("Enter again: ");
			temp = in.nextInt();
		}
		numberOfValidatorNodes = temp;

		System.out.print("Enter number of outgoing-connections: ");
		temp = in.nextInt();
		while (temp < 1) {
			error("Number must be greater than 0!");
			System.out.print("Enter again: ");
			temp = in.nextInt();
		}
		numberOfOutgoingConnections = temp;

		System.out.print("Enter simulation height: ");
		temp = in.nextInt();
		while (temp < 1) {
			error("Number must be greater than 0!");
			System.out.print("Enter again: ");
			temp = in.nextInt();
		}
		heightOfSimulation = temp;

	}

	private void makeDelayGraphs() {
		XYSeries series = GraphMaker.makeSeries("Number of processes = 100", xValues, yValues);
		XYDataset dataset = GraphMaker.makeDataset(series);
		JFreeChart chart = GraphMaker.makeChart("Average consensus block time", "Non-validator nodes number", "Block time [ms]", dataset);
		GraphMaker.customizeChart(chart);
		GraphMaker.saveChartAsJPEG("blocktime2.jpeg", chart, 1000, 1000);
	}

	private void makePrevoteOverflowGraphs() {
		XYSeries series = GraphMaker.makeSeries("Number of processes = 100", xValues, yValuesPrevotes);
		XYDataset dataset = GraphMaker.makeDataset(series);
		JFreeChart chart = GraphMaker.makeChart("Prevote messages overhead", "Non-validator nodes number", "Message overhead", dataset);
		GraphMaker.customizeChart(chart);
		GraphMaker.saveChartAsJPEG("prevote2.jpeg", chart, 1000, 1000);
	}

	private void makePrecommitOverflowGraphs() {
		XYSeries series = GraphMaker.makeSeries("Number of processes = 100", xValues, yValuesPrecommit);
		XYDataset dataset = GraphMaker.makeDataset(series);
		JFreeChart chart = GraphMaker.makeChart("Precommit message overhead", "Non-validator nodes number", "Message overhead", dataset);
		GraphMaker.customizeChart(chart);
		GraphMaker.saveChartAsJPEG("precommit2.jpeg", chart, 1000, 1000);
	}

	private void collectStatistics() {
		double sumBlockTime = 0;
		double sumPrevotes = 0;
		double sumPrecommits = 0;
		int i = 0;
		for (Node node : nodes) {
			sumBlockTime += node.getBlockChain().getAverageConsensusTime();
			sumPrevotes += node.getRoundState().preVotes.getAverageNumberOfDuplicats();
			sumPrecommits += node.getRoundState().preCommits.getAverageNumberOfDuplicats();
			i++;
		}
		double averageBlockTime = sumBlockTime / i;
		double averagePrevotes = sumPrevotes / i;
		double averagePrecommits = sumPrecommits / i;
		xValues.add((double) numberOfNodes - numberOfValidatorNodes);
		yValues.add(averageBlockTime);
		yValuesPrevotes.add(averagePrevotes / numberOfValidatorNodes);
		yValuesPrecommit.add(averagePrecommits / numberOfValidatorNodes);
	}

	private void error(String message) {
		System.out.println("ERROR: " + message);
	}

	public static boolean checkIfNodeIsProposer(int nodeID, int height) {
		if (height % BasicConsensusSimulator.numberOfValidatorNodes == nodeID) {
			return true;
		}
		return false;
	}

}
