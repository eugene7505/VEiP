package veip.fsm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;


public class FSM {

	public class Event {
		String name;
		boolean observable = false;
		boolean controllable = false;

		public Event(String eventName) {
			name = eventName;
		}

		public Event(String eventName, boolean isObservable,
				boolean isControllable) {
			name = eventName;
			observable = isObservable;
			controllable = isControllable;
		}
	}

	

	protected String file = new String("");
	protected int numberOfStates = 0;
	protected int numberOfInitialState = 0;
	protected ArrayList<State> initialStateList;
	// private ArrayList<Event> eventList = new ArrayList<Event>();
	// private ArrayList<Event> obsEventList = new ArrayList<Event>();

	protected HashMap<String, State> stateMap;
	protected HashMap<String, Event> localEventMap;

	static HashMap<String, Event> globalEventMap = new HashMap<String, Event>();
	static HashMap<String, Event> globalObsEventMap = new HashMap<String, Event>();
	
	public FSM(){};
	
	@SuppressWarnings("unchecked")
	public FSM (FSM fsm){
		numberOfStates = fsm.numberOfStates;
		numberOfInitialState = fsm.numberOfInitialState;
		initialStateList = (ArrayList<State>)fsm.initialStateList.clone();
		stateMap = (HashMap<String, State>)fsm.stateMap.clone();
		localEventMap = (HashMap<String, Event>)fsm.localEventMap.clone();		
	} 
	
	public FSM(String fileName) throws FileNotFoundException {
		file = fileName;
		initialStateList = new ArrayList<State>();
		stateMap = new HashMap<String, State> ();
		localEventMap = new HashMap<String, Event> ();
		
		Scanner s = new Scanner(new FileInputStream(fileName));
		numberOfStates = s.nextInt();
		numberOfInitialState = s.nextInt();
		for (int i = 0; i < numberOfStates; i++) {
			String stateName = s.next();
			State state = addState(stateName);
			state.nonsecret = (s.nextInt() == 1);
			state.numberOfTransitions = s.nextInt();
			for (int j = 0; j < state.numberOfTransitions; j++) {

				String eventName = s.next();
				Event event = addEvent(eventName);
				String nextStateName = s.next();
				State nextState = addState(nextStateName);
				event.controllable = (s.next() == "c");
				event.observable = (s.next() == "o");
				state.addTransition(event, nextState);
			}
			// currently, support single initial state
			if (i < numberOfInitialState){
				state.initial = true;
				initialStateList.add(state);				
			}
		}
		s.close();
	}
	/*
	 * This function create and return a new state and add to stateList if no
	 * state named stateName already exists If there is already a state with the
	 * same name, then return the State object of that name
	 */
	public State addState(String stateName) {
		if (!stateMap.containsKey(stateName))
			stateMap.put(stateName, new State(stateName));
		
		State state = stateMap.get(stateName);
		return state;
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
			globalObsEventMap.put(eventName, new Event(eventName));
			event = globalEventMap.get(eventName);
			localEventMap.put(eventName, event);
		}

		else {
			event = globalEventMap.get(eventName);
			if (!localEventMap.containsKey(eventName)) {
				localEventMap.put(eventName, event);
			}
		}
		return event;
	}

	public int getNumberOfStates(){
		return numberOfStates;
	}

	public int getNumberOfInitialState(){
		return numberOfInitialState;
	}
	public ArrayList<State> getInitialStateList(){
		return initialStateList;
	}
	public HashMap<String, State> getStateMap(){
		return stateMap;
	}
	public HashMap<String, Event> getLocalEventMap(){
		return localEventMap;
	}
	
	/* This method prints the FSM 
	 * Initial states are printed first. Then other states are printed by iterating over the map datastructure
	 */
	public void printFSM() {
		System.out.println(numberOfStates + " " + numberOfInitialState);
		for (int i = 0; i < numberOfInitialState; i++) {
			State state = initialStateList.get(i);
			System.out.println();
			System.out.println(state.name + " " + ((state.nonsecret) ? 1 : 0)
					+ " " + state.numberOfTransitions);
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : state.transitions
					.entrySet()) {
				Event event = transitionEntry.getKey();
				ArrayList<State> nextStateList = transitionEntry.getValue();
				for (int j = 0; j < nextStateList.size(); j++) {
					System.out.println(event.name + " "
							+ nextStateList.get(j).name + " "
							+ ((event.controllable) ? 1 : 0) + ""
							+ ((event.observable) ? 1 : 0));
				}
			}
		}
		for (Map.Entry<String, State> entry : stateMap.entrySet()){
			State state = entry.getValue();
			if (state.initial)
				continue;
			else {
				System.out.println();
				System.out.println(state.name + " " + ((state.nonsecret) ? 1 : 0)
						+ " " + state.numberOfTransitions);
				for (Map.Entry<Event, ArrayList<State>> transitionEntry : state.transitions
						.entrySet()) {
					Event event = transitionEntry.getKey();
					ArrayList<State> nextStateList = transitionEntry.getValue();
					for (int j = 0; j < nextStateList.size(); j++) {
						System.out.println(event.name + " "
								+ nextStateList.get(j).name + " "
								+ ((event.controllable) ? 1 : 0) + ""
								+ ((event.observable) ? 1 : 0));
					}
				}
				
			}
			
		}
		
	}
	
//	public static void main(String[] args) throws FileNotFoundException{
//		
//		String file = "testFSM/G.fsm";
//		FSM fsm = new FSM(file);
//		fsm.printFSM();
//	}

}
