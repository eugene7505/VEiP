package veip.synthesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import veip.fsm.FSM;
import veip.fsm.FSM.Event;
import veip.fsm.State;

public final class SynthesisUtilities {
	static FSM gameGraph;

	private SynthesisUtilities() {
	}

	public static InsertionAutomaton greedySynthesis(AIS ais) {
		gameGraph = ais.getAisFSM();
		InsertionAutomaton ia = synthesizeGreedyInsertionAutomaton();
		return ia;
	}

	public static InsertionAutomaton optimalSynthesis(AIS ais) {
		gameGraph = ais.getAisFSM();
		InsertionAutomaton ia = synthesizeOptimalIA();
		return ia;
	}

	public static InsertionAutomaton ppSynthesis(AIS ais) {
		gameGraph = ais.getAisFSM();
		InsertionAutomaton ia = synthesizePublicIA();
		return ia;
	}

	 /* =======================
	  * Private functions for greedy synthesis
	  */	
	private static InsertionAutomaton synthesizeGreedyInsertionAutomaton() {
		InsertionAutomaton ia = new InsertionAutomaton();
		gameGraph.clearFlags();
		Stack<State> stateStack = new Stack<State>();
		stateStack.add(gameGraph.getInitialStateList().get(0));

		State initialAisState = gameGraph.getInitialStateList().get(0);
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

	/* =======================
     * Private functions for optimal synthesis
     */
	// TODO
	private static InsertionAutomaton synthesizeOptimalIA() {
		InsertionAutomaton ia = new InsertionAutomaton();
		return ia;
	}

	 /* =======================
	  * Private functions for pp-enforcing synthesis
	  */
	// TODO
	private static InsertionAutomaton synthesizePublicIA() {
		InsertionAutomaton ia = new InsertionAutomaton();
		return ia;
	}
}