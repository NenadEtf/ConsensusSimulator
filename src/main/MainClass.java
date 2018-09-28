package main;

import java.lang.reflect.Array;
import java.util.ArrayList;

import simulators.BasicConsensusSimulator;

public class MainClass {

	public static void main(String[] args) {
		// new BasicConsensusSimulator();
		//new BasicConsensusSimulator().startSimulation();
		new BasicConsensusSimulator().startSimulationAndMakeGraphs();
	}

}
