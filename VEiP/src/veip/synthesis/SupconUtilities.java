package veip.synthesis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import veip.fsm.FSM;
import veip.fsm.FSM.Event;
import veip.fsm.State;

public final class SupconUtilities {
	private SupconUtilities() {
	}

	private static HashSet<State> illegalStates;
	private static FSM supconFSM;

	public static FSM stateBasedSupconNonblocking(FSM fsm) {
		if (fsm.getNumberOfInitialState() != 1)
			System.out.println("There are " + fsm.getNumberOfInitialState()
					+ " != 1 initial states. WRONG!");
		supconFSM = new FSM(fsm);
		illegalStates = new HashSet<State>();
		// initialize illegal states with all deadlocked states
		for (Map.Entry<String, State> stateEntry : supconFSM.getStateMap()
				.entrySet()) {
			State state = stateEntry.getValue();
			if (!state.isMarked() && (state.getNumberOfTransitions() == 0))
				illegalStates.add(state);
		}
		computeIllegalStates();
		FSM resultFSM = trimIllegalStates();
		return resultFSM;
	}

	// ideally we'd want to recursively prune, but we don't have a tree
	// structure that we can easily recursive on
	// private static void recursivePruningIllegalStates() {}

	private static void computeIllegalStates() {
		supconFSM.clearFlags();

		// find the DFS traversal order
		ArrayList<State> DFSArray = new ArrayList<State>();
		DFSArray.add(supconFSM.getInitialStateList().get(0));
		supconFSM.getInitialStateList().get(0).flagged = true;
		for (int i = 0; i < DFSArray.size(); i++) {
			State state = DFSArray.get(i);
			for (Map.Entry<Event, ArrayList<State>> transitinEntry : state
					.getAllTransitions().entrySet()) {
				if (!transitinEntry.getValue().get(0).flagged) {
					DFSArray.add(transitinEntry.getValue().get(0));
					transitinEntry.getValue().get(0).flagged = true;
				}
			}
		}

		// backward iteratively mark illegal states
		for (int i = DFSArray.size() - 1; i >= 0; i--) {
			State state = DFSArray.get(i);
			if (illegalStates.contains(state))
				continue;
			else if (state.isMarked()) { // illegal if one child node is
				// illegal
				boolean illegal = false;
				for (Map.Entry<Event, ArrayList<State>> transitinEntry : state
						.getAllTransitions().entrySet()) {
					if (illegalStates.contains(transitinEntry.getValue()
							.get(0)))
						illegal = true;
				}
				if (illegal)
					illegalStates.add(state);
			} else { // legal if one child is legal
				boolean illegal = true;
				for (Map.Entry<Event, ArrayList<State>> transitinEntry : state
						.getAllTransitions().entrySet()) {
					if (!illegalStates
							.contains(transitinEntry.getValue().get(0)))
						illegal = false;
				}
				if (illegal)
					illegalStates.add(state);
			}
		}
		System.out.println("===== illegal states =====");
		for (State state : illegalStates){
			System.out.println(state.getName());
		}
		
	}

	private static FSM trimIllegalStates() {
		FSM resultFSM = new FSM();
		supconFSM.clearFlags();
		State supconInitial = supconFSM.getInitialStateList().get(0);
		if (illegalStates.contains(supconInitial))
			return resultFSM;
		else {
			Stack<State> stateStack = new Stack<State>();
			stateStack.add(supconInitial);
			while (!stateStack.isEmpty()) {
				State supconState = stateStack.pop();
				if (supconState.flagged)
					continue;
				State state = resultFSM.addState(supconState.getName(),
						supconState.isInitial(), supconState.isNonsecret());
				for (HashMap.Entry<Event, ArrayList<State>> transitionEntry : supconState
						.getAllTransitions().entrySet()) {
					if (illegalStates.contains(transitionEntry.getValue()
							.get(0)))
						continue;
					else {
						State supconNextState = transitionEntry.getValue().get(
								0);
						stateStack.add(supconNextState);
						State nextState = resultFSM.addState(
								supconNextState.getName(),
								supconNextState.isInitial(),
								supconNextState.isMarked());
						state.addTransition(transitionEntry.getKey(), nextState);
					}
				}
				state.updateNumberOfTransitions();
				supconState.flagged = true;
			}
		}
		resultFSM.updateNumberOfInitialStates();
		resultFSM.updateNumberOfStates();
		return resultFSM;
	}


}
