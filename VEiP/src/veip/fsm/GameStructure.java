package veip.fsm;

import java.awt.event.InputEvent;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ejml.simple.SimpleMatrix;

public class GameStructure extends FSM {

	public GameStructure() {
		super();
		initialStateList = new ArrayList<State>(1);
		weightMatrixMap = new HashMap<Event, SimpleMatrix>();
		stateValueVector = new SimpleMatrix();
		stateList = new ArrayList<State>();
		// eventSet is the event set to the edit function (i.e., the set of
		// system's output event)
		eventSet = new HashSet<Event>();
	}

	public void initiateEventSet(HashMap<String, Event> eventMap) {
		for (Map.Entry<String, Event> entry : eventMap.entrySet())
			eventSet.add(entry.getValue());
	}

	public HashSet<Event> getEventSet() {
		return eventSet;
	}

	public void constructYtoZWeightMatrixMap(
			HashMap<Event, HashMap<StatePair, Double>> ytoZMatrix) {
		// System.out.println("YtoZWeightMatrixMap");
		for (Map.Entry<Event, HashMap<StatePair, Double>> mapEntry : ytoZMatrix
				.entrySet()) {
			Event event = mapEntry.getKey();
			weightMatrixMap.put(event, new SimpleMatrix(numberOfStates,
					numberOfStates));
			for (Map.Entry<StatePair, Double> matrixEntry : mapEntry.getValue()
					.entrySet()) {
				StatePair pair = matrixEntry.getKey();
				weightMatrixMap.get(event).set(pair.first.index,
						pair.second.index, matrixEntry.getValue());
			}
			// ytoZWeightMatrixMap.get(event).print();
		}
	}

	public void constructZtoYWeightMatrixMap(
			HashMap<Event, HashMap<StatePair, Double>> ztoYMatrix) {
		// System.out.println("ZtoYWeightMatrixMap");
		// int i = 0;
		for (Map.Entry<Event, HashMap<StatePair, Double>> mapEntry : ztoYMatrix
				.entrySet()) {
			Event event = mapEntry.getKey();
			/*
			 * System.out.println(i + ":" + event.name); if (i == 20){ double
			 * currentMemory = ((double) ((double) (Runtime.getRuntime()
			 * .totalMemory() / 1024) / 1024)) - ((double) ((double)
			 * (Runtime.getRuntime().freeMemory() / 1024) / 1024));
			 * System.out.println("Memory usage " + currentMemory + " MB"); }
			 */
			SimpleMatrix ematrix = new SimpleMatrix(numberOfStates,
					numberOfStates);
			weightMatrixMap.put(event, ematrix);
			for (Map.Entry<StatePair, Double> matrixEntry : mapEntry.getValue()
					.entrySet()) {
				StatePair pair = matrixEntry.getKey();
				weightMatrixMap.get(event).set(pair.first.index,
						pair.second.index, matrixEntry.getValue());
				/*
				 * if(!matrixEntry.getValue().equals(0))
				 * System.out.println(matrixEntry.getValue());
				 */
			}
			// i++;
		}
	}

	public void setWeightMatrix(Event e, SimpleMatrix weightMatrix) {
		if (weightMatrixMap.containsKey(e))
			System.out.println("weightMatrix for event " + e.getName()
					+ "already exist. OVERRIDE!");
		weightMatrixMap.put(e, weightMatrix);
	}

	@Override
	public State createState(String stateName) {
		if (!stateMap.containsKey(stateName)) {
			stateMap.put(stateName, new State(stateName));
		}
		State state = stateMap.get(stateName);
		if (state.getIndex() == -1) {
			state.setIndex(stateCounter);
			assert (stateCounter == stateList.size());
			stateList.add(state);
			stateCounter++;
			updateNumberOfStates();
		}
		return state;
	}

	@Override
	public State createState(String stateName, boolean isInitial,
			boolean isNonsecret) {
		if (!stateMap.containsKey(stateName)) {
			State state = new State(stateName, isInitial, isNonsecret);
			if (isInitial) {
				initialStateList.add(state);
				updateNumberOfInitialStates();
			}
			stateMap.put(stateName, state);
		}
		State state = stateMap.get(stateName);
		if (state.getIndex() == -1) {
			state.setIndex(stateCounter);
			assert (stateCounter == stateList.size());
			stateList.add(state);
			stateCounter++;
			updateNumberOfStates();
		}
		return state;
	}

	public double getMaxWeight() {
		int Wmax = 0;
		for (Map.Entry<Event, SimpleMatrix> mapEntry : weightMatrixMap
				.entrySet())
			Wmax = (Wmax > (int) mapEntry.getValue().elementMaxAbs()) ? Wmax
					: (int) mapEntry.getValue().elementMaxAbs();
		return Wmax;
	}

	public void setOptimalAction(ArrayList<Event> actions) {
		optimalActoins = actions;
	}

	public void setValueVector(SimpleMatrix values) {
		stateValueVector = values;
	}

	public void setValue(int index, double value) {
		stateValueVector.set(index, value);
	}

	public SimpleMatrix getValueVector() {
		return stateValueVector;
	}

	public Double getValue(State state) {
		return stateValueVector.get(state.getIndex());
	}

	public Double getValue(int index) {
		return stateValueVector.get(index);
	}

	public Event getOptimalAction(State state) {
		return optimalActoins.get(state.getIndex());
	}

	public void setOptimalAction(int i, Event event) {
		optimalActoins.set(i, event);
	}

	public void initializeOptimalActions() {
		optimalActoins = new ArrayList<Event>(numberOfStates);
		for (int i = 0; i < numberOfStates; i++) {
			optimalActoins.add(null);
		}
	}

	public HashMap<Event, SimpleMatrix> getMatrixMap() {
		return weightMatrixMap;
	}

	public SimpleMatrix getMatrix(Event event) {
		return weightMatrixMap.get(event);
	}

	public double getMatrixEntry(Event event, int i, int j) {
		return weightMatrixMap.get(event).get(i, j);
	}

	public void setMatrixEntry(Event event, int i, int j, double value) {
		weightMatrixMap.get(event).set(i, j, value);
	}

	public ArrayList<State> getStateList() {
		return stateList;
	}

	public State getState(int i) {
		return stateList.get(i);
	}

	public int getEditCost(Event e) {
		return (int) editCostMap.get(e);
	}

	public void setEditCost(Event e, int cost) {
		editCostMap.put(e, new Integer(cost));
	}

	private HashMap<Event, Integer> editCostMap = new HashMap<Event, Integer>();
	private HashMap<Event, SimpleMatrix> weightMatrixMap;
	private SimpleMatrix stateValueVector;
	private ArrayList<Event> optimalActoins;
	private ArrayList<State> stateList;
	private HashSet<Event> eventSet;
}
