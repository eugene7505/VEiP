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

	private FSM estimator;
	private FSM estimatorD;
	private FSM estimatorF;
	private FSM verifier;
	
	public Verifier(FSM estimator) {
		this.estimator = estimator;		
		buildEstimatorD();
		buildEstimatorF();
		verifier = ParallelCompositionUtilities.pairwiseParallelComposition_singleInitialState(estimatorD, estimatorF);
	}

	/* 
	 * notice that an event e should still in the localEventMap even if all
	 * transitions labeled with e are removed Hence, we need not manage eventMap
	 * in this case
	 */
	private void buildEstimatorD() {
		estimatorD = new FSM(estimator);
		// remove unsafe state
		Iterator<Map.Entry<String, State>> stateIterator = estimatorD
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
		estimatorD.updateNumberOfStates();
		// add inserted alias
		for (Map.Entry<String, State> stateEntry : estimatorD.getStateMap()
				.entrySet()) {
			State state = stateEntry.getValue();
			HashMap<Event, ArrayList<State>> newTransitions = new HashMap<Event, ArrayList<State>>();
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : state.getAllTransitions().entrySet()) {
				newTransitions.put(transitionEntry.getKey(), transitionEntry.getValue());
				Event insertedEvent = estimatorD
						.addEvent(transitionEntry.getKey().getName().concat("i"), true, true);
				insertedEvent.setInserted(true);	
				newTransitions.put(insertedEvent, transitionEntry.getValue());
			}
			state.setTransitions(newTransitions);
			state.updateNumberOfTransitions();
		}
	}

	/* This function builds the insertion estimator from the estimator
	 * Specifically, insertionEstimator is obtained by adding to the estimator a selfloop transition for each event  
	 * @return insertionEstimator 
	 */
	private void buildEstimatorF() {
		estimatorF = new FSM(estimator);
		for (Map.Entry<String, State> stateEntry : estimatorF.getStateMap()
				.entrySet()) {
			State state = stateEntry.getValue();
			for (Map.Entry<String, Event> eventEntry : estimator
					.getLocalEventMap().entrySet()) {
				Event insertedEvent = estimatorF
						.addEvent(eventEntry.getKey().concat("i"), true, true);
				insertedEvent.setInserted(true);
				state.addTransition(insertedEvent, state);
			}
			state.updateNumberOfTransitions();
		}
	}
	
	public void printEstimatorD(){
		System.out.println("printing Estimator-d in the verifier");
		estimatorD.printFSM();
	}
	
	public void printEstimatorF(){
		System.out.println("printing Estimator-f in the verifier");
		estimatorF.printFSM();
	}

	public void printEstimator(){
		System.out.println("printing Estimator in the verifier");
		estimator.printFSM();
	}

	public void printVerifier(){
		System.out.println("printing verifier in the verifier");
		verifier.printFSM();
	}

	public static void main(String[] args) throws FileNotFoundException {
		FSM estimator = new FSM("testFSM/test2/obsG.fsm");
		System.out.println("printing Estimator");
		estimator.printFSM();
		
		Verifier verifier = new Verifier(estimator);
		verifier.printEstimator();
		verifier.printEstimatorD();
		verifier.printEstimatorF();
		verifier.printVerifier();
	}

}
