package veip.fsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ejml.simple.SimpleMatrix;

import veip.synthesis.StochasticSynthesisUtilities.StatePair;

public class GameStructure extends FSM {

	public GameStructure() {
		super();
		initialStateList = new ArrayList<State>(1);
		ytoZWeightMatrixMap = new HashMap<FSM.Event, SimpleMatrix>();
		ztoYWeightMatrixMap = new HashMap<FSM.Event, SimpleMatrix>();
		stateValueVector = new SimpleMatrix();
		stateList = new ArrayList<State>();
	}

	public void constructTransitionProbabilityMatrixMap(
			HashMap<Event, HashMap<StatePair, Double>> transitionMatrix) {
		// System.out.println("TransitionProbabilityMatrix");
		for (Map.Entry<Event, HashMap<StatePair, Double>> mapEntry : transitionMatrix
				.entrySet()) {
			Event event = mapEntry.getKey();
			// System.out.println(event.name);
			ytoZWeightMatrixMap.put(event, new SimpleMatrix(
					numberOfStates, numberOfStates));
			for (Map.Entry<StatePair, Double> matrixEntry : mapEntry.getValue()
					.entrySet()) {
				StatePair pair = matrixEntry.getKey();
				ytoZWeightMatrixMap.get(event).set(pair.first.index,
						pair.second.index, matrixEntry.getValue());
			}
			// transitionProbabilityMatrixMap.get(event).print();
		}
	}

	public void constructRewardMatrixMap(
			HashMap<Event, HashMap<StatePair, Double>> rewardMatrix) {
		for (Map.Entry<Event, HashMap<StatePair, Double>> mapEntry : rewardMatrix
				.entrySet()) {
			Event event = mapEntry.getKey();
			// System.out.println(event.name);
			ztoYWeightMatrixMap.put(event, new SimpleMatrix(numberOfStates,
					numberOfStates));
			for (Map.Entry<StatePair, Double> matrixEntry : mapEntry.getValue()
					.entrySet()) {
				StatePair pair = matrixEntry.getKey();
				ztoYWeightMatrixMap.get(event).set(pair.first.index,
						pair.second.index, matrixEntry.getValue());
			}
			// rewardMatrixMap.get(event).print();
		}
	}

	@Override
	public State addState(String stateName) {
		if (!stateMap.containsKey(stateName)) {
			stateMap.put(stateName, new State(stateName));
		}
		State state = stateMap.get(stateName);
		if (state.getIndex() == -1) {
			state.setIndex(stateCounter);
			assert (stateCounter == stateList.size());
			stateList.add(state);
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
			state.setIndex(stateCounter);
			assert (stateCounter == stateList.size());
			stateList.add(state);
			stateCounter++;
		}
		return state;
	}

	public void setOptimalAction(ArrayList<Event> actions) {
		optimalActoins = actions;
	}

	public void setValues(SimpleMatrix values) {
		stateValueVector = values;
	}

	public Double getValue(State state) {
		return stateValueVector.get(state.getIndex());
	}

	public Event getOptimalAction(State state) {
		return optimalActoins.get(state.getIndex());
	}
	public void setOptimalAction (int i, Event event){
		optimalActoins.set(i, event);
	}
	public void initializeOptimalActions(int n){
		optimalActoins = new ArrayList<FSM.Event>(n);
		for (int i = 0; i < n; i++){
			optimalActoins.add(null);
		}
	}
	
	public HashMap<Event, SimpleMatrix> getYtoZMatrixMap() {
		return ytoZWeightMatrixMap;
	}

	public SimpleMatrix getZtoYMatrix(Event event) {
		return ztoYWeightMatrixMap.get(event);
	}

	public double getZtoYMatrix(Event event, int i, int j) {
		return ztoYWeightMatrixMap.get(event).get(i, j);
	}

	public ArrayList<State> getStateList() {
		return stateList;
	}
	public State getState(int i){
		return stateList.get(i);
	}

	private HashMap<Event, SimpleMatrix> ytoZWeightMatrixMap;
	private HashMap<Event, SimpleMatrix> ztoYWeightMatrixMap;
	private SimpleMatrix stateValueVector;
	private ArrayList<Event> optimalActoins;
	private ArrayList<State> stateList;
}
