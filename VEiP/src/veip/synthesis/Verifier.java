package veip.synthesis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import veip.fsm.CompositionUtilities;
import veip.fsm.Event;
import veip.fsm.FSM;
import veip.fsm.State;

public class Verifier {

	private FSM estimator;
	private FSM safeEstimator;
	private FSM insertionEstimator;
	private FSM verifier;

	public Verifier(FSM estimator) throws FileNotFoundException {
		this.estimator = estimator;
		buildSafeEstimator();
		buildInsertionEstimator();
		verifier = CompositionUtilities
				.pairwiseParallelComposition(safeEstimator,
						insertionEstimator);
	}

	/*
	 * notice that an event e should still in the localEventMap even if all
	 * transitions labeled with e are removed Hence, we need not manage eventMap
	 * in this case
	 */
	private void buildSafeEstimator() {
		safeEstimator = new FSM(estimator);
		// remove unsafe state
		Iterator<Map.Entry<String, State>> stateIterator = safeEstimator
				.getStateMap().entrySet().iterator();
		while (stateIterator.hasNext()) {
			State state = stateIterator.next().getValue();
			if (!state.isNonsecret())
				stateIterator.remove();
			else {
				Iterator<Map.Entry<Event, ArrayList<State>>> transitionIterator = state
						.getAllTransitions().entrySet().iterator();
				while (transitionIterator.hasNext()) {
					ArrayList<State> nextStates = transitionIterator.next()
							.getValue();
					for (int i = nextStates.size() - 1; i >= 0; i--) {
						if (!nextStates.get(i).isNonsecret())
							nextStates.remove(i);
					}
					if (nextStates.isEmpty())
						transitionIterator.remove();
				}
			}
		}
		safeEstimator.updateNumberOfStates();
		// add inserted alias
		for (Map.Entry<String, State> stateEntry : safeEstimator.getStateMap()
				.entrySet()) {
			State state = stateEntry.getValue();
			HashMap<Event, ArrayList<State>> newTransitions = new HashMap<Event, ArrayList<State>>();
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : state
					.getAllTransitions().entrySet()) {
				newTransitions.put(transitionEntry.getKey(),
						transitionEntry.getValue());
				Event insertedEvent = safeEstimator.addEvent(transitionEntry
						.getKey().getName().concat("i"), true);
				insertedEvent.setInserted(true);
				newTransitions.put(insertedEvent, transitionEntry.getValue());
			}
			state.setTransitions(newTransitions);
			state.updateNumberOfTransitions();
		}
	}

	/*
	 * This function builds the insertion estimator from the estimator
	 * Specifically, insertionEstimator is obtained by adding to the estimator a
	 * selfloop transition for each event
	 * 
	 * @return insertionEstimator
	 */
	private void buildInsertionEstimator() {
		insertionEstimator = new FSM(estimator);
		for (Map.Entry<String, State> stateEntry : insertionEstimator
				.getStateMap().entrySet()) {
			State state = stateEntry.getValue();
			for (Map.Entry<String, Event> eventEntry : estimator
					.getLocalEventMap().entrySet()) {
				Event insertedEvent = insertionEstimator.addEvent(eventEntry
						.getKey().concat("i"), true);
				insertedEvent.setInserted(true);
				state.createTransition(insertedEvent, state);
			}
			state.updateNumberOfTransitions();
		}
	}
	
	public FSM getSafeEstimatorFSM() {
		return safeEstimator;
	}
	
	public FSM getEstimatorFSM(){
		return estimator;
	}
	
	public FSM getInsertionEstimatorFSM() {
		return insertionEstimator;
	}
	
	public FSM getVerifierFSM(){
		return verifier;
	}

	public void printSafeEstimator() {
		safeEstimator.printFSM();
	}

	public void printInsertionEstimator() {
		insertionEstimator.printFSM();
	}

	public void printEstimator() {
		estimator.printFSM();
	}

	public void printVerifier() {
		verifier.printFSM();
	}


}
