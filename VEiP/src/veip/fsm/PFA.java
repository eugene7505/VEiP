package veip.fsm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.ejml.simple.SimpleMatrix;

/*
 * stateList is added to index states
 */
public class PFA extends FSM {

	public PFA(int n) {
		super();
		numberOfStates = n;
		initialDistribution = new SimpleMatrix(1, numberOfStates);
		eventMatrixMap = new HashMap<Event, SimpleMatrix>();
		stateList = new ArrayList<State>(numberOfStates);
	}

	/*
	 * This constructor builds a PFA from a given FSM. By default, initial
	 * probability and transition probability are equally probable among all
	 * states or all outgoing transitions
	 */
	public PFA(FSM fsm) {
		super(fsm);
		// TODO every outgoing transition is equally likely
		initialDistribution = new SimpleMatrix(1, numberOfStates);
		numberOfInitialState = fsm.numberOfInitialState;
		eventMatrixMap = new HashMap<Event, SimpleMatrix>();
		stateList = new ArrayList<State>(numberOfStates);
		for (Map.Entry<String, Event> eventEntry : localEventMap.entrySet()) {
			Event event = eventEntry.getValue();
			eventMatrixMap.put(event, new SimpleMatrix(numberOfStates,
					numberOfStates));
		}
		for (Map.Entry<String, State> stateEntry : stateMap.entrySet()) {
			State state = stateEntry.getValue();
			stateList.add(state.index, state);
		}
		for (int i = 0; i < fsm.initialStateList.size(); i++) {
			int stateIndex = fsm.initialStateList.get(i).getIndex();
			initialDistribution.set(stateIndex, 1 / numberOfInitialState);
		}
	}

	public PFA(String pfaFile) throws FileNotFoundException {
		super();
		file = pfaFile;
		eventMatrixMap = new HashMap<Event, SimpleMatrix>();
		Scanner s = new Scanner(new FileInputStream(file));
		numberOfStates = s.nextInt();
		stateList = new ArrayList<State>(numberOfStates);
		initialDistribution = new SimpleMatrix(1, numberOfStates);
		double stateProbSum = 0;

		for (int i = 0; i < numberOfStates; i++) {
			String stateName = s.next();
			State state = addState(stateName);
			int sindex = state.getIndex();
			assert (sindex >= 0 && sindex < numberOfStates);
			state.nonsecret = (s.nextInt() == 1);
			state.numberOfTransitions = s.nextInt();
			double d = s.nextDouble();
			if (d > 0) {
				addInitialState(state);
				initialDistribution.set(0, state.index, d);
				stateProbSum += d;
			}
			double transitionProbSum = 0;
			for (int j = 0; j < state.numberOfTransitions; j++) {
				String eventName = s.next();
				String nextStateName = s.next();
				State nextState = addState(nextStateName);
				int nsindex = nextState.getIndex();
				assert (nsindex >= 0 && nsindex < numberOfStates);
				String o = s.next();
				Event event = addEvent(eventName, (o.compareTo("o") == 0));
				addObsUnobsEventMap(event);
				state.addTransition(event, nextState);
				double p = s.nextDouble();
				setEventMatrix(event, sindex, nsindex, p);
				transitionProbSum += p;
			}
			if (!(transitionProbSum == 1 || transitionProbSum == 0)) {
				System.out.println("transition probability of state"
						+ state.name + " is invalid");
			}
		}
		if (Math.abs(stateProbSum - 1) > epsilon) {
			System.out.println("initial distribution is invalid");
		}
		updateNumberOfInitialStates();
		s.close();
	}

	// TODO
	/*
	 * This constructor builds an equivalent PFA from an HMM The transformation
	 * is done using the algorithm in Pupont et al 2005 But the transformation
	 * does not seem to preserve causality between observations and transitions
	 * in HMM and PFA. Can be modified
	 */
	public PFA(HMM hmm) {
		initialDistribution = hmm.getInitialDistribution();
		numberOfStates = hmm.numberOfStates;
		stateMap = new HashMap<String, State>(numberOfStates);
		stateList = new ArrayList<State>(numberOfStates);
		for (int i = 0; i < numberOfStates; i++) {
			addState(String.valueOf(i));
		}
		initialStateList = new ArrayList<State>(numberOfStates);

		localEventMap = new HashMap<String, Event>(hmm.numberOfObservations);
		eventMatrixMap = new HashMap<Event, SimpleMatrix>();
		for (int i = 0; i < hmm.numberOfObservations; i++) {
			String eventName = hmm.observationList.get(i);
			Event event = addEvent(eventName, true);
			eventMatrixMap.put(event, new SimpleMatrix(numberOfStates,
					numberOfStates));
		}

		for (int i = 0; i < numberOfStates; i++) {
			State state = stateList.get(i);
			if (initialDistribution.get(i) > 0)
				addInitialState(state);
			for (int j = 0; j < numberOfStates; j++) {
				if (hmm.stateTransitionMatrix.get(i, j) > 0) {
					for (int k = 0; k < hmm.numberOfObservations; k++) {
						Event event = localEventMap.get(hmm.observationList
								.get(k));
						if (hmm.observationMatrix.get(i, k) > 0) {
							state.addTransition(event, stateList.get(j));
							setEventMatrix(
									event,
									i,
									j,
									hmm.observationMatrix.get(i, k)
											* hmm.stateTransitionMatrix.get(i,
													j));
						}
					}
				}
			}
			state.updateNumberOfTransitions();
		}
		updateNumberOfInitialStates();
	}

	/*
	 * Old private void parseInitialDistribution(String initial){ String[]
	 * tokens = initial.split("[ ]+"); if (tokens.length!= numberOfStates)
	 * System
	 * .out.println("initial distribution is inconsistent with the number of states"
	 * ); else { double sum = 0; for (int i = 0; i < tokens.length; i++){ double
	 * pi = double.parsefloat(tokens[i]); if (pi > 0){
	 * initialDistribution.addEntry(0, i, pi); numberOfInitialState++; sum +=
	 * pi; } } if (1-sum != 0)
	 * System.out.println("initial distribution is not valid"); } }
	 */

	@Override
	/*
	 * almost the same implementation except that we also add the new state to
	 * stateList (non-Javadoc)
	 * 
	 * @see veip.fsm.FSM#addState(java.lang.String)
	 */
	public State addState(String stateName) {
		if (!stateMap.containsKey(stateName)) {
			stateMap.put(stateName, new State(stateName));
		}
		State state = stateMap.get(stateName);
		if (state.getIndex() == -1) {
			stateList.add(stateCounter, state);
			state.setIndex(stateCounter);
			stateCounter++;
		}
		return state;
	}

	@Override
	public State addState(String stateName, boolean isInitial,
			boolean isNonsecret) {
		if (!stateMap.containsKey(stateName)) {
			State state = new State(stateName, isInitial, isNonsecret);
			if (isInitial)
				initialStateList.add(state);
			stateMap.put(stateName, state);
		}
		State state = stateMap.get(stateName);
		if (state.getIndex() == -1) {
			stateList.add(stateCounter, state);
			state.setIndex(stateCounter);
			stateCounter++;
		}
		return state;
	}

	public State getState(int index) {
		return stateList.get(index);
	}

	public void setEventMatrix(Event e, int i, int j, double p) {
		if (!eventMatrixMap.containsKey(e))
			eventMatrixMap.put(e, new SimpleMatrix(numberOfStates,
					numberOfStates));
		SimpleMatrix matrix = eventMatrixMap.get(e);
		matrix.set(i, j, p);
	}

	public double getEventMatrix(Event e, int i, int j) {
		if (!eventMatrixMap.containsKey(e))
			System.out.println("PFA does not have Event " + e + ". Wrong!");
		SimpleMatrix matrix = eventMatrixMap.get(e);
		return matrix.get(i, j);
	}

	public SimpleMatrix getEventMatrix(Event e) {
		if (!eventMatrixMap.containsKey(e))
			System.out.println("PFA does not have Event " + e + ". Wrong!");
		return eventMatrixMap.get(e);
	}

	public SimpleMatrix getTransitionMatrix() {
		SimpleMatrix transitionMatrix = new SimpleMatrix(numberOfStates,
				numberOfStates);
		for (Map.Entry<Event, SimpleMatrix> matrixEntry : eventMatrixMap
				.entrySet()) {
			transitionMatrix = transitionMatrix.plus(matrixEntry.getValue());
		}
		return transitionMatrix;
	}

	public SimpleMatrix getInitialDistribution() {
		return initialDistribution;
	}

	public void updateSize() {
		initialDistribution.reshape(1, numberOfStates);
		stateList = new ArrayList<State>(stateList.subList(0, numberOfStates));
		for (Map.Entry<Event, SimpleMatrix> matrixEntry : eventMatrixMap
				.entrySet()) {
			SimpleMatrix matrix = matrixEntry.getValue();
			SimpleMatrix resizedMatrix = matrix.extractMatrix(0,
					numberOfStates, 0, numberOfStates);
			matrixEntry.setValue(resizedMatrix);
		}
	}

	public void printPFA() {
		System.out.println(numberOfStates);

		for (int i = 0; i < numberOfStates; i++) {
			State state = stateList.get(i);
			System.out.println();
			System.out.println(state.name + "\t" + ((state.nonsecret) ? 1 : 0)
					+ "\t" + state.numberOfTransitions + "\t"
					+ initialDistribution.get(0, i));
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : state.transitions
					.entrySet()) {
				Event event = transitionEntry.getKey();
				ArrayList<State> nextStateList = transitionEntry.getValue();
				for (int j = 0; j < nextStateList.size(); j++) {
					State nState = nextStateList.get(j);
					System.out.println(event.name + "\t" + nState.name + "\t"
							+ ((event.isObservable()) ? "o" : "uo") + "\t"
							+ (getEventMatrix(event, i, nState.getIndex())));
				}
			}
		}
	}

	SimpleMatrix initialDistribution;
	private HashMap<Event, SimpleMatrix> eventMatrixMap;
	private ArrayList<State> stateList;
	private static double epsilon = 5.96e-8;

	public static void main(String[] args) throws FileNotFoundException {

		int n = 4;
		int o = 2;
		SimpleMatrix A = new SimpleMatrix(n, n);
		A.set(0, 1, 0.9);
		A.set(0, 0, 0.1);
		A.set(1, 2, 0.7);
		A.set(1, 3, 0.3);
		A.set(2, 0, 0.1);
		A.set(2, 1, 0.9);
		A.set(3, 2, 0.7);
		A.set(3, 3, 0.3);
		SimpleMatrix B = new SimpleMatrix(n, o);
		B.set(0, 0, 0.2);
		B.set(0, 1, 0.8);
		B.set(1, 0, 0.3);
		B.set(1, 1, 0.7);
		B.set(2, 0, 0.8);
		B.set(2, 1, 0.2);
		B.set(3, 0, 0.9);
		B.set(3, 1, 0.1);
		SimpleMatrix pi = new SimpleMatrix(1, n);
		pi.set(0, 0.04);
		pi.set(1, 0.36);
		pi.set(2, 0.42);
		pi.set(3, 0.18);

		ArrayList<String> observations = new ArrayList<String>();
		observations.add("a");
		observations.add("b");
		HMM hmm = new HMM(n, o, A, B, pi, observations);
		hmm.printHMM();
		System.out.println();

		PFA pfa = new PFA(hmm);
		pfa.printPFA();
	}
}
