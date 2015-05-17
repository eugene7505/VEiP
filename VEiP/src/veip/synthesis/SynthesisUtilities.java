package veip.synthesis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import veip.fsm.FSM;
import veip.fsm.State;
import veip.fsm.FSM.Event;

public final class SynthesisUtilities {
	static FSM aisFSM;

	private SynthesisUtilities() {
	}

	public static InsertionAutomaton greedySynthesis(AIS ais) {
		aisFSM = ais.getAisFSM();
		InsertionAutomaton ia = synthesizeGreedyInsertionAutomaton();
		return ia;
	}

	private static InsertionAutomaton synthesizeGreedyInsertionAutomaton() {
		InsertionAutomaton ia = new InsertionAutomaton();
		aisFSM.clearFlags();
		Stack<State> stateStack = new Stack<State>();
		stateStack.add(aisFSM.getInitialStateList().get(0));

		State initialAisState = aisFSM.getInitialStateList().get(0);
		stateStack.add(initialAisState);

		while (!stateStack.isEmpty()) {
			State ystate = stateStack.pop();
			if (ystate.flagged)
				continue;
			State iaState = ia.addState(ystate.getName(), ystate.isInitial(),
					true);
			for (HashMap.Entry<FSM.Event, ArrayList<State>> transitionEntry : ystate
					.getAllTransitions().entrySet()) {
				Event event = transitionEntry.getKey();
				State zstate = transitionEntry.getValue().get(0);
				String shortestInsertion = null;
				State nextYState = new State();
				for (HashMap.Entry<Event, ArrayList<State>> insertionEntry : zstate
						.getAllTransitions().entrySet()) {
					if (shortestInsertion == null
							|| insertionEntry.getKey().getName().length() < shortestInsertion
									.length()) {
						shortestInsertion = insertionEntry.getKey().getName();
						nextYState = insertionEntry.getValue().get(0);
					}
				}
				State nextIaState = ia.addState(nextYState.getName());
				iaState.addTransition(event, nextIaState);
				Event outputEvent = new Event(shortestInsertion
						+ event.getName());
				ia.addTransitionOuput(iaState, event, outputEvent);
				stateStack.push(nextYState);
			}
			iaState.updateNumberOfTransitions();
			ystate.flagged = true;
		}
		ia.updateNumberOfInitialStates();
		ia.updateNumberOfStates();
		return ia;
	}
}
