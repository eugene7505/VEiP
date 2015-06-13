package veip.fsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.ejml.simple.SimpleMatrix;

public class HMM {

	// input should be matrices, so, all states are int values. no need to have
	// complicated states
	public HMM(int numberOfStates, int numberOfObservations) {
		this.numberOfStates = numberOfStates;
		this.numberOfObservations = numberOfObservations;
		initialDistribution = new SimpleMatrix(1, numberOfStates);
		stateTransitionMatrix = new SimpleMatrix(numberOfStates, numberOfStates);
		observationMatrix = new SimpleMatrix(numberOfStates,
				numberOfObservations);
		observationList = new ArrayList<String>(numberOfObservations);
	}

	public HMM(int n, int o, SimpleMatrix A, SimpleMatrix B, SimpleMatrix pi,
			ArrayList<String> observations) {
		this.numberOfStates = n;
		this.numberOfObservations = o;
		initialDistribution = pi;
		stateTransitionMatrix = A;
		observationMatrix = B;
		observationList = observations;
	}

	// TODO, what format to follow? ideally, want to use some format output from
	// ML libraries
	public HMM(String fileName) {
		file = fileName;
	}

	public int getNumberOfStates() {
		return numberOfStates;
	}

	public int getNumberOfObservations() {
		return numberOfObservations;
	}

	public SimpleMatrix getInitialDistribution() {
		return initialDistribution;
	}

	public SimpleMatrix getStateTransitionMatrix() {
		return stateTransitionMatrix;
	}

	public SimpleMatrix getObservationMatrix() {
		return observationMatrix;
	}
	
	public void setSecretStates(int stateIndex){
		
	}
	
	public void printHMM(){
		System.out.println("number of states " + numberOfStates);
		System.out.println("number of observations " + numberOfObservations);
		System.out.println("transition matrix");
		stateTransitionMatrix.print();
		System.out.println("observation matrix");
		observationMatrix.print();
		System.out.println("initial distribution");
		initialDistribution.print();
		System.out.println("alphabet");
		for (int i = 0; i < observationList.size(); i++) {
			System.out.print(" " + observationList.get(i));
		}
	}
	

	String file = new String("");
	int numberOfStates;
	int numberOfObservations;
	SimpleMatrix initialDistribution; // 1xn vector
	SimpleMatrix stateTransitionMatrix; // nxn matrix
	SimpleMatrix observationMatrix; // nxo matrix that maps from state to
									// observation vector
	ArrayList<String> observationList;

	public static void main(String[] args) {
		int n = 2;
		int o = 2;
		SimpleMatrix A = SimpleMatrix.identity(2);
		SimpleMatrix B = SimpleMatrix.random(n, o, 0, 1, new Random());
		SimpleMatrix pi = SimpleMatrix.random(1, n, 0, 1, new Random());

		ArrayList<String> observations = new ArrayList<String>();
		observations.add("a");
		observations.add("b");
		HMM hmm = new HMM(n, o, A, B, pi, observations);
		hmm.printHMM();		
	}
}
