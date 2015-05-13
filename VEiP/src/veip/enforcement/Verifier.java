package veip.enforcement;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import veip.fsm.FSM;
import veip.fsm.ParallelCompositionUtilities;
import veip.fsm.State;
import veip.fsm.FSM.Event;

public class Verifier {

	// TODO perhaps we could implement the verifier for both insertion and
	// replacement: buildInsertionVerifier(), buildReplacementVerifier

	private FSM estimator;
	private FSM safeEstimator;
	private FSM insertionEstimator;
	private FSM verifier;

	public Verifier(FSM estimator) {
		this.estimator = estimator;
		buildSafeEstimator();
		buildInsertionEstimator();
		verifier = ParallelCompositionUtilities
				.pairwiseParallelComposition_singleInitialState(safeEstimator,
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
						.getKey().getName().concat("i"), true, true);
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
						.getKey().concat("i"), true, true);
				insertedEvent.setInserted(true);
				state.addTransition(insertedEvent, state);
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
		System.out.println("printing SafeEstimator in the verifier");
		safeEstimator.printFSM();
	}

	public void printInsertionEstimator() {
		System.out.println("printing InsertionEstimator in the verifier");
		insertionEstimator.printFSM();
	}

	public void printEstimator() {
		System.out.println("printing Estimator in the verifier");
		estimator.printFSM();
	}

	public void printVerifier() {
		System.out.println("printing verifier in the verifier");
		verifier.printFSM();
	}

	public static void main(String[] args) throws FileNotFoundException {
		FSM estimator = new FSM("testFSM/test0/obsG.fsm");
		System.out.println("printing Estimator");
		estimator.printFSM();

		Verifier verifier = new Verifier(estimator);
		verifier.printEstimator();
		verifier.printSafeEstimator();
		verifier.printInsertionEstimator();
		verifier.printVerifier();
	}

}
