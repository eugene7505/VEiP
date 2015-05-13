package veip.fsm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import veip.enforcement.Verifier;
import veip.fsm.FSM.Event;

public final class GraphUtilities {

	private GraphUtilities() {}
	
	public static HashMap<State, HashMap<State, Boolean>> dashedConnectivity(FSM fsm) {

		// initialization entries to 0
		HashMap<State, HashMap<State, Boolean>> dashedConnectivityMap = new HashMap<State, HashMap<State, Boolean>>();
		HashMap<String, State> stateMap = fsm.getStateMap();

		for (HashMap.Entry<String, State> sourceStateEntry : stateMap
				.entrySet()) {
			State sourceState = sourceStateEntry.getValue();
			HashMap<State, Boolean> innerMap = new HashMap<State, Boolean>();
			dashedConnectivityMap.put(sourceState, innerMap);
			for (HashMap.Entry<String, State> destinationStateEntry : stateMap
					.entrySet()) {
				State destinationState = destinationStateEntry.getValue();				
				if (sourceStateEntry.getValue().equals(
						destinationStateEntry.getValue())) {	
					dashedConnectivityMap.get(sourceState).put(destinationState, true);
				} else {
					dashedConnectivityMap.get(sourceState).put(destinationState, false);
				}
			}
		}

		// initialize 1-step dashed transition
		for (HashMap.Entry<String, State> stateEntry : stateMap.entrySet()) {
			State state = stateEntry.getValue();
			for (HashMap.Entry<Event, ArrayList<State>> transitionEntry : state
					.getAllTransitions().entrySet()) {
				if (!transitionEntry.getKey().isInserted()) continue;
				ArrayList<State> nextStates = transitionEntry.getValue();
				for (int i = 0; i < nextStates.size(); i++) {
					(dashedConnectivityMap.get(state)).replace(nextStates.get(i), true);
				}
			}
		}

		// Floyd Warshall
		for (HashMap.Entry<String, State> intermediateStateEntry : stateMap
				.entrySet()) {
			for (HashMap.Entry<String, State> sourceStateEntry : stateMap
					.entrySet()) {
				for (HashMap.Entry<String, State> destinationStateEntry : stateMap
						.entrySet()) {
					State intermediatestate = intermediateStateEntry.getValue();
					State sourceState = sourceStateEntry.getValue();
					State destinationState = destinationStateEntry.getValue();
					
					boolean sourceToIntermediate = (dashedConnectivityMap.get(sourceState)).get(intermediatestate).booleanValue();
					boolean intermediateToDestination = (dashedConnectivityMap.get(intermediatestate)).get(destinationState).booleanValue();
					boolean sourceToDestination = (dashedConnectivityMap.get(sourceState)).get(destinationState).booleanValue();
							
					(dashedConnectivityMap.get(sourceState)).replace(destinationState,
							sourceToIntermediate && intermediateToDestination || sourceToDestination);
				}
			}
		}
		return dashedConnectivityMap;
	}

	public static void main(String args[]) throws FileNotFoundException {
		FSM estimator = new FSM("testFSM/test0/obsG.fsm");
		Verifier verifier = new Verifier(estimator);
		verifier.printVerifier();
		
		HashMap<State, HashMap<State, Boolean>> connectivity = dashedConnectivity(verifier.getVerifierFSM());
		for (HashMap.Entry<State, HashMap<State, Boolean>> connectivityEntry : connectivity
				.entrySet()) {
			State state = connectivityEntry.getKey();
			HashMap<State, Boolean> innerMap = connectivityEntry.getValue();
			for (HashMap.Entry<State, Boolean> innerEntry : innerMap.entrySet()){
				System.out.println(state.getName() + "->" + innerEntry.getKey().getName() + " " + innerEntry.getValue());
			}
		}

	}
}
