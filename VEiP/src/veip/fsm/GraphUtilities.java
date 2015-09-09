package veip.fsm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import veip.synthesis.Verifier;

public final class GraphUtilities {

	private GraphUtilities() {
	}

	public static HashMap<State, HashMap<State, Boolean>> dashedConnectivity(
			FSM fsm) {
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
					dashedConnectivityMap.get(sourceState).put(
							destinationState, true);
				} else {
					dashedConnectivityMap.get(sourceState).put(
							destinationState, false);
				}
			}
		}

		// initialize 1-step dashed transition
		for (HashMap.Entry<String, State> stateEntry : stateMap.entrySet()) {
			State state = stateEntry.getValue();
			for (HashMap.Entry<Event, ArrayList<State>> transitionEntry : state
					.getAllTransitions().entrySet()) {
				if (!transitionEntry.getKey().isInserted()
						|| transitionEntry.getValue().get(0) == state)
					continue;
				ArrayList<State> nextStates = transitionEntry.getValue();
				for (int i = 0; i < nextStates.size(); i++) {
					(dashedConnectivityMap.get(state)).replace(
							nextStates.get(i), true);
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

					boolean sourceToIntermediate = (dashedConnectivityMap
							.get(sourceState)).get(intermediatestate)
							.booleanValue();
					boolean intermediateToDestination = (dashedConnectivityMap
							.get(intermediatestate)).get(destinationState)
							.booleanValue();
					boolean sourceToDestination = (dashedConnectivityMap
							.get(sourceState)).get(destinationState)
							.booleanValue();

					(dashedConnectivityMap.get(sourceState)).replace(
							destinationState, sourceToIntermediate
									&& intermediateToDestination
									|| sourceToDestination);
				}
			}
		}
		return dashedConnectivityMap;
	}

	public static HashMap<State, HashMap<State, String>> shortestDashedPath(
			FSM fsm) {
		// initialization entries to 0
		HashMap<State, HashMap<State, String>> shortestDashedPath = new HashMap<State, HashMap<State, String>>();
		HashMap<String, State> stateMap = fsm.getStateMap();

		for (HashMap.Entry<String, State> sourceStateEntry : stateMap
				.entrySet()) {
			State sourceState = sourceStateEntry.getValue();
			HashMap<State, String> innerMap = new HashMap<State, String>();
			shortestDashedPath.put(sourceState, innerMap);
			for (HashMap.Entry<String, State> destinationStateEntry : stateMap
					.entrySet()) {
				State destinationState = destinationStateEntry.getValue();
				if (sourceStateEntry.getValue().equals(
						destinationStateEntry.getValue())) {
					shortestDashedPath.get(sourceState).put(destinationState,
							new String(""));
				} else {
					shortestDashedPath.get(sourceState).put(destinationState,
							null);
				}
			}
		}

		// initialize 1-step dashed transition
		for (HashMap.Entry<String, State> stateEntry : stateMap.entrySet()) {
			State state = stateEntry.getValue();
			for (HashMap.Entry<Event, ArrayList<State>> transitionEntry : state
					.getAllTransitions().entrySet()) {
				if (!transitionEntry.getKey().isInserted()
						|| transitionEntry.getValue().get(0) == state)
					continue;
				State nextState = transitionEntry.getValue().get(0);
				(shortestDashedPath.get(state)).replace(nextState, new String(
						transitionEntry.getKey().getName()));
			}
		}

		/*
		 * Floyd Warshall If null trace, then skip else, concatenate
		 */
		for (HashMap.Entry<String, State> intermediateStateEntry : stateMap
				.entrySet()) {
			for (HashMap.Entry<String, State> sourceStateEntry : stateMap
					.entrySet()) {
				for (HashMap.Entry<String, State> destinationStateEntry : stateMap
						.entrySet()) {
					State intermediatestate = intermediateStateEntry.getValue();
					State sourceState = sourceStateEntry.getValue();
					State destinationState = destinationStateEntry.getValue();

					String sourceToIntermediate = (shortestDashedPath
							.get(sourceState)).get(intermediatestate);
					String intermediateToDestination = (shortestDashedPath
							.get(intermediatestate)).get(destinationState);
					String sourceToDestination = (shortestDashedPath
							.get(sourceState)).get(destinationState);

					// new trace found, then update if the new one is shorter
					if (sourceToIntermediate != null
							&& intermediateToDestination != null) {
						String newPath = sourceToIntermediate
								+ intermediateToDestination;
						if (sourceToDestination == null
								|| (newPath.length() < sourceToDestination
										.length()))
							(shortestDashedPath.get(sourceState)).replace(
									destinationState, newPath);
					}
				}
			}
		}
		return shortestDashedPath;
	}

	public static void main(String args[]) throws FileNotFoundException {
		FSM estimator = new FSM("testFSM/G.fsm");
		Verifier verifier = new Verifier(estimator);
		System.out.println("======= print safe estimator =======");
		verifier.printSafeEstimator();
		System.out.println("======= print insertion estimator =======");
		verifier.printInsertionEstimator();
		System.out.println("======= print verifier =======");
		verifier.printVerifier();

		HashMap<State, HashMap<State, String>> shortestPath = shortestDashedPath(verifier
				.getVerifierFSM());
		for (HashMap.Entry<State, HashMap<State, String>> shortestPathEntry : shortestPath
				.entrySet()) {
			State state = shortestPathEntry.getKey();
			HashMap<State, String> innerMap = shortestPathEntry.getValue();
			for (HashMap.Entry<State, String> innerEntry : innerMap.entrySet()) {
				System.out.println(state.getName() + "->"
						+ innerEntry.getKey().getName() + " "
						+ innerEntry.getValue());
			}
		}

	}
}
