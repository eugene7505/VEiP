package veip.fsm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import veip.verification.CurrentStateEstimator;
import veip.verification.EstimatorState;

public class FSM {

	protected int stateCounter = 0;
	protected String file = new String("");
	protected int numberOfStates;
	protected int numberOfInitialState;
	protected ArrayList<State> initialStateList;

	protected HashMap<String, State> stateMap;
	protected HashMap<String, Event> localEventMap;

	public static HashMap<String, Event> globalEventMap = new HashMap<String, Event>();
	public static HashMap<String, Event> globalObsEventMap = new HashMap<String, Event>();
	public static HashMap<String, Event> globalUnobsEventMap = new HashMap<String, Event>();
	public static int numberOfGlobalEvents;

	public FSM() {
		numberOfStates = 0;
		numberOfInitialState = 0;
		initialStateList = new ArrayList<State>();
		stateMap = new HashMap<String, State>();
		localEventMap = new HashMap<String, Event>();
	};

	// TODO use stateList?

	@SuppressWarnings("unchecked")
	public FSM(FSM fsm) {
		numberOfStates = fsm.numberOfStates;
		numberOfInitialState = fsm.numberOfInitialState;

		initialStateList = new ArrayList<State>(fsm.numberOfInitialState);

		// deep copy of the stateMap
		stateMap = new HashMap<String, State>(fsm.getStateMap().size());
		for (Map.Entry<String, State> stateEntry : fsm.getStateMap().entrySet()) {
			State state = addState(stateEntry.getValue().getName(), stateEntry
					.getValue().isInitial(), stateEntry.getValue()
					.isNonsecret());
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : stateEntry
					.getValue().getAllTransitions().entrySet()) {
				Event event = transitionEntry.getKey();
				ArrayList<State> nextStates = transitionEntry.getValue();
				for (int i = 0; i < nextStates.size(); i++) {
					State nextState = addState(nextStates.get(i).getName(),
							nextStates.get(i).isInitial(), nextStates.get(i)
									.isNonsecret());
					state.createTransition(event, nextState);
				}
			}
		}
		// shallow copy of the localEventMap because event should have global
		// attributes
		localEventMap = (HashMap<String, Event>) fsm.localEventMap.clone();
	}

	public FSM(String fileName) throws FileNotFoundException {
		file = fileName;
		localEventMap = new HashMap<String, Event>();

		Scanner s = new Scanner(new FileInputStream(fileName));
		numberOfStates = s.nextInt();
		numberOfInitialState = s.nextInt();
		stateMap = new HashMap<String, State>(numberOfStates);
		initialStateList = new ArrayList<State>(numberOfInitialState);

		for (int i = 0; i < numberOfStates; i++) {
			String stateName = s.next();
			State state = addState(stateName);
			state.nonsecret = (s.nextInt() == 1);
			state.numberOfTransitions = s.nextInt();
			for (int j = 0; j < state.numberOfTransitions; j++) {
				String eventName = s.next();
				String nextStateName = s.next();
				State nextState = addState(nextStateName);
				String o = s.next();
				Event event = addEvent(eventName, (o.compareTo("o") == 0));
				addObsUnobsEventMap(event);
				state.addTransition(event, nextState);
			}
			// currently, support single initial state
			if (i < numberOfInitialState) {
				addInitialState(state);
			}
		}
		s.close();
	}

	/*
	 * This constructor builds an fsm from a current state estimator
	 * 
	 * @param currentStateEstimator the cse that the fsm is built from
	 * 
	 * @param renameWithNumbers whether names are renamed with numbers
	 * 
	 * @return corresponding fsm TODO: need to update globalEventMap? TODO:
	 * perhaps we want a choice to concatenate state names or rename to numbers
	 * (perhaps using a function called rename states?)
	 */
	public FSM(CurrentStateEstimator currentStateEstimator) {
		numberOfStates = currentStateEstimator.getNumberOfStates();
		numberOfInitialState = 1;
		numberOfGlobalEvents = 0;
		initialStateList = new ArrayList<State>(numberOfInitialState);
		initialStateList
				.add((State) currentStateEstimator.getInitialEstimate());

		stateMap = new HashMap<String, State>(numberOfStates);
		for (Map.Entry<String, EstimatorState> stateEntry : currentStateEstimator
				.getEstimatorStateMap().entrySet()) {
			String stateName = stateEntry.getKey();
			State state = (State) stateEntry.getValue();
			stateMap.put(stateName, state);
		}

		localEventMap = new HashMap<String, Event>();
		for (Map.Entry<String, Event> eventEntry : currentStateEstimator
				.getLocalEventMap().entrySet()) {
			String eventName = eventEntry.getKey();
			Event event = eventEntry.getValue();
			localEventMap.put(eventName, event);
		}
	}

	/*
	 * This function create and return a new state and add to stateList if no
	 * state named stateName already exists If there is already a state with the
	 * same name, then return the State object of that name
	 */

	public State createState(String stateName) {
		if (!stateMap.containsKey(stateName)) {
			stateMap.put(stateName, new State(stateName));
		}
		State state = stateMap.get(stateName);
		if (state.getIndex() == -1) {
			state.setIndex(stateCounter);
			stateCounter++;
			updateNumberOfStates();
		}
		return state;
	}
	public State addState(String stateName) {
		if (!stateMap.containsKey(stateName)) {
			stateMap.put(stateName, new State(stateName));
		}
		State state = stateMap.get(stateName);
		if (state.getIndex() == -1) {
			state.setIndex(stateCounter);
			stateCounter++;
		}
		return state;
	}

	public State createState(String stateName, boolean isInitial,
			boolean isNonsecret) {
		if (!stateMap.containsKey(stateName)) {
			State state = new State(stateName, isInitial, isNonsecret);
			if (isInitial){
				initialStateList.add(state);
				updateNumberOfInitialStates();
			}
			stateMap.put(stateName, state);
		}
		State state = stateMap.get(stateName);
		if (state.getIndex() == -1) {
			state.setIndex(stateCounter);
			stateCounter++;
			updateNumberOfStates();
		}
		return state;
	}
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
			stateCounter++;
		}
		return state;
	}
	/*
	 * This function make an existing state initial
	 * 
	 * @param state If state already exists and was not in the initialStateList,
	 * then the function sets state.initial = true and adds it to
	 * initialStateList
	 */
	public void addInitialState(State state) {
		if (!stateMap.containsValue(state))
			System.out.println("State " + state.getName()
					+ "does not exists. WRONG!");
		else if (!initialStateList.contains(state)) {
			state.setInitial(true);
			initialStateList.add(state);
			updateNumberOfInitialStates();
		}
	}

	/*
	 * This function remove an existing state from the initialStateList
	 * 
	 * @param state If state is previously initial, the function sets
	 * state.initial = false and remove it from initialStateList
	 */

	public void removeInitialState(State state) {
		if (!stateMap.containsValue(state))
			System.out.println("State " + state.getName()
					+ "does not exists. WRONG!");
		else {
			if (!initialStateList.contains(state))
				System.out
						.println("State "
								+ state.getName()
								+ "is not an initial state and cannot be removed from the initialStateList. WRONG!");
			else {
				initialStateList.remove(state);
				state.setInitial(false);
				updateNumberOfInitialStates();
			}
		}
	}

	public State getState(String stateName) {
		if (!stateMap.containsKey(stateName)) {
			System.out
					.println("State " + stateName + "does not exists. WRONG!");
			return null;
		} else
			return stateMap.get(stateName);
	}

	public void addSecretState(String stateName) {
		if (!stateMap.containsKey(stateName)) {
			System.out
					.println("State " + stateName + "does not exists. WRONG!");
		} else
			stateMap.get(stateName).setNonsecret(false);
		;
	}

	public void resetAllSecretStates() {
		for (Map.Entry<String, State> stateEntry : stateMap.entrySet()) {
			stateEntry.getValue().setNonsecret(true);
		}
	}

	/*
	 * First check if such an event exists in any of the automata if no, then
	 * create a new event and add to both globalEventMap and localEventMap if
	 * yes, the event may exists in other automata but not in this current local
	 * automaton add to localEventMap if it exists in other automata but not in
	 * this current automata return the event if it exists in this current
	 * automaton
	 */
	public Event addEvent(String eventName) {
		Event event;
		if (!globalEventMap.containsKey(eventName)) {
			globalEventMap.put(eventName, new Event(eventName));
			// globalObsEventMap.put(eventName, new Event(eventName));
			event = globalEventMap.get(eventName);
			localEventMap.put(eventName, event);
		} else {
			event = globalEventMap.get(eventName);
			if (!localEventMap.containsKey(eventName)) {
				localEventMap.put(eventName, event);
			}
		}
		return event;
	}

	/*
	 * TODO: can we throw an error when the con/obs attributes are inconsistent?
	 */
	public Event addEvent(String eventName, boolean observable) {
		Event event;
		if (!globalEventMap.containsKey(eventName)) {
			globalEventMap.put(eventName, new Event(eventName, observable));
			event = globalEventMap.get(eventName);
			addObsUnobsEventMap(event);
			localEventMap.put(eventName, event);
		} else {
			event = globalEventMap.get(eventName);
			if (event.isObservable() != observable) {
				System.out
						.println("event "
								+ event.getName()
								+ " has inconsistent controllable/observable attributes. Overwrite the attributes to "
								+ (observable ? "o" : "uo"));
				event.setObservable(observable);
				addObsUnobsEventMap(event);
			}
			if (!localEventMap.containsKey(eventName)) {
				localEventMap.put(eventName, event);
			}
		}
		return event;
	}

	public void addObsUnobsEventMap(Event event) {
		if (event.isObservable())
			globalObsEventMap.put(event.name, event);
		else
			globalUnobsEventMap.put(event.name, event);
	}

	public void removeState(String stateName) {
		if (stateMap.containsKey(stateName)) {
			stateMap.remove(stateName);
			updateNumberOfStates();
		}else System.out.println("State" + stateName + "does not exist. Wrong!");
	}

	public int getNumberOfStates() {
		return numberOfStates;
	}

	public int getNumberOfInitialState() {
		return numberOfInitialState;
	}

	public ArrayList<State> getInitialStateList() {
		return initialStateList;
	}

	public HashMap<String, State> getStateMap() {
		return stateMap;
	}

	public HashMap<String, Event> getLocalEventMap() {
		return localEventMap;
	}

	@SuppressWarnings("unchecked")
	public void setLocalEventMap(HashMap<String, Event> map) {
		localEventMap = (HashMap<String, Event>) map.clone();
	}

	public void updateNumberOfStates() {
		numberOfStates = stateMap.size();
	}

	public void updateNumberOfInitialStates() {
		numberOfInitialState = initialStateList.size();
	}

	public void clearFlags() {
		for (Map.Entry<String, State> stateEntry : stateMap.entrySet()) {
			stateEntry.getValue().flagged = false;
		}
	}

	public boolean isEmptyFSM() {
		return (stateMap.size() == 0);
	}

	/*
	 * This method prints the FSM Initial states are printed first. Then other
	 * states are printed by iterating over the map data structure
	 */
	public void printFSM() {
		System.out.println(numberOfStates + "\t" + numberOfInitialState);
		for (int i = 0; i < numberOfInitialState; i++) {
			State state = initialStateList.get(i);
			System.out.println();
			System.out.println(state.name + "\t" + ((state.nonsecret) ? 1 : 0)
					+ "\t" + state.numberOfTransitions);
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : state.transitions
					.entrySet()) {
				Event event = transitionEntry.getKey();
				ArrayList<State> nextStateList = transitionEntry.getValue();
				for (int j = 0; j < nextStateList.size(); j++) {
					System.out.println(event.name + "\t"
							+ nextStateList.get(j).name + "\t"
							+ ((event.isObservable()) ? "o" : "uo"));
				}
			}
		}
		for (Map.Entry<String, State> entry : stateMap.entrySet()) {
			State state = entry.getValue();
			if (state.initial)
				continue;
			else {
				System.out.println();
				System.out.println(state.name + "\t"
						+ ((state.nonsecret) ? 1 : 0) + "\t"
						+ state.numberOfTransitions);
				for (Map.Entry<Event, ArrayList<State>> transitionEntry : state.transitions
						.entrySet()) {
					Event event = transitionEntry.getKey();
					ArrayList<State> nextStateList = transitionEntry.getValue();
					for (int j = 0; j < nextStateList.size(); j++) {
						System.out.println(event.name + "\t"
								+ nextStateList.get(j).name + "\t"
								+ ((event.isObservable()) ? "o" : "uo"));
					}
				}
			}
		}
	}

	public void exportFSM(String outFileName, boolean renumberStates)
			throws FileNotFoundException {
		PrintWriter fileWriter = new PrintWriter(outFileName);
		int pos = outFileName.indexOf(".fsm");
		String mappingFileName = outFileName.substring(0, pos) + "-mapping.txt";
		exportStateIndexMapping(mappingFileName);
		fileWriter.println(numberOfStates + "\t" + numberOfInitialState);
		for (int i = 0; i < numberOfInitialState; i++) {
			State state = initialStateList.get(i);
			fileWriter.println();
			if (renumberStates)
				fileWriter.println(state.index + "\t"
						+ ((state.nonsecret) ? 1 : 0) + "\t"
						+ state.numberOfTransitions);
			else
				fileWriter.println(state.name + "\t"
						+ ((state.nonsecret) ? 1 : 0) + "\t"
						+ state.numberOfTransitions);
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : state.transitions
					.entrySet()) {
				Event event = transitionEntry.getKey();
				ArrayList<State> nextStateList = transitionEntry.getValue();
				if (renumberStates) {
					for (int j = 0; j < nextStateList.size(); j++) {
						fileWriter.println(event.name + "\t"
								+ nextStateList.get(j).index + "\t"
								+ ((event.isObservable()) ? "o" : "uo"));
					}
				} else {
					for (int j = 0; j < nextStateList.size(); j++) {
						fileWriter.println(event.name + "\t"
								+ nextStateList.get(j).name + "\t"
								+ ((event.isObservable()) ? "o" : "uo"));
					}
				}
			}
		}
		for (Map.Entry<String, State> entry : stateMap.entrySet()) {
			State state = entry.getValue();
			if (state.initial)
				continue;
			else {
				fileWriter.println();
				if (renumberStates)
					fileWriter.println(state.index + "\t"
							+ ((state.nonsecret) ? 1 : 0) + "\t"
							+ state.numberOfTransitions);
				else
					fileWriter.println(state.name + "\t"
							+ ((state.nonsecret) ? 1 : 0) + "\t"
							+ state.numberOfTransitions);
				for (Map.Entry<Event, ArrayList<State>> transitionEntry : state.transitions
						.entrySet()) {
					Event event = transitionEntry.getKey();
					ArrayList<State> nextStateList = transitionEntry.getValue();
					if (renumberStates) {
						for (int j = 0; j < nextStateList.size(); j++) {
							fileWriter.println(event.name + "\t"
									+ nextStateList.get(j).index + "\t"
									+ ((event.isObservable()) ? "o" : "uo"));
						}
					} else {
						for (int j = 0; j < nextStateList.size(); j++) {
							fileWriter.println(event.name + "\t"
									+ nextStateList.get(j).name + "\t"
									+ ((event.isObservable()) ? "o" : "uo"));
						}
					}
				}
			}
		}
		fileWriter.close();
	}

	public void exportStateIndexMapping(String stateMappingFile)
			throws FileNotFoundException {
		PrintWriter fileWriter = new PrintWriter(stateMappingFile);
		fileWriter.println("StateIndex \t StateName");
		for (Map.Entry<String, State> entry : stateMap.entrySet()) {
			fileWriter.println(entry.getValue().getIndex() + "\t"
					+ entry.getValue().getName());
		}
		fileWriter.close();
	}

	public static void main(String[] args) throws FileNotFoundException {
		FSM fsm2 = new FSM();
		State s1 = fsm2.createState("s1");
		State s2 = fsm2.createState("s2");
		fsm2.addInitialState(s2);
		fsm2.addInitialState(s1);
		fsm2.removeInitialState(s1);
		fsm2.printFSM();
		
	}

}
