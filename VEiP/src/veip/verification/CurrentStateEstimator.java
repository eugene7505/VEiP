package veip.verification;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import veip.fsm.FSM;
import veip.fsm.FSM.Event;
import veip.fsm.State;

public class CurrentStateEstimator {

	FSM fsm;
	int numberOfStates;
	EstimatorState initialEstimate;
	HashMap<String, EstimatorState> estimatorStateMap;
	HashMap<String, Event> localEventMap;

	public CurrentStateEstimator(FSM fsm) {
		this.fsm = fsm;
		buildCurrentStateEstimator();
	}

	/*
	 * This function builds the current state estimator by determinizing the
	 * corresponding fsm. Notice that the input fsm could have a set of initial state and could be nondeterministic 
	 * However, the resulting estimator is always deterministic 
	 */
	private void buildCurrentStateEstimator() {
		numberOfStates = 0;
		estimatorStateMap = new HashMap<String, EstimatorState>();
		localEventMap = new HashMap<String, FSM.Event>();

		ArrayList<State> initialStateWithUnobservableReach = unobserverbleReach(fsm
				.getInitialStateList());
		initialEstimate = addEstimate(initialStateWithUnobservableReach);
		initialEstimate.setInitial(true);
		// BFS to build the cse

		Stack<EstimatorState> stack = new Stack<EstimatorState>();
		stack.push(initialEstimate);
		while (!stack.empty()) {
			EstimatorState estimate = stack.pop();
			if (estimate.explored)
				continue;
			for (Map.Entry<String, Event> obsEventEntry : FSM.globalObsEventMap
					.entrySet()) {
				ArrayList<State> nextStates = new ArrayList<State>();
				Event event = obsEventEntry.getValue();
				boolean eventEnabled = false;
				for (int i = 0; i < estimate.stateEstimate.size(); i++) {
					State state = estimate.stateEstimate.get(i);
					if (state.getNextStateList(event) != null) { //has this transition, should add to transition
						localEventMap.put(event.getName(), event);
						unionStateList(nextStates,
								state.getNextStateList(event));
						eventEnabled = true;
					}
				}
				nextStates = unobserverbleReach(nextStates);
				if (eventEnabled == true) {
					EstimatorState nextEstimate = addEstimate(nextStates);
					estimate.addTransition(event, nextEstimate);
					stack.push(nextEstimate);					
				}
			}
			estimate.explored = true;
			estimate.updateNumberOfTransitions();
		}
	}

	/*
	 * check if already exists
	 * TODO: before creating/comparing the estimate, sort the states in stateList
	 */
	private EstimatorState addEstimate(ArrayList<State> stateList) {
		
		EstimatorState newEstimate = new EstimatorState(stateList);
		String newEstimateName = newEstimate.getName();
		if (estimatorStateMap.containsKey(newEstimateName)) {
			return estimatorStateMap.get(newEstimateName);
		} else{
			estimatorStateMap.put(newEstimateName, newEstimate);
			numberOfStates++;
			//System.out.println("creating" + newEstimateName);
		}
		return newEstimate;
	}

	/*
	 * This function builds the unobservable reach from a given list of states
	 * To guarantee that names are consistent, we sort the state list before
	 * return
	 * 
	 * @return return the unobservable reach
	 * 
	 * @param the list of states that we build unobservable reach from TODO sort
	 * ArrayList of states, based on the order of their names
	 */
	private ArrayList<State> unobserverbleReach(ArrayList<State> stateList) {
		@SuppressWarnings("unchecked")
		ArrayList<State> unobservableReachStates = (ArrayList<State>) stateList
				.clone();
		for (int i = 0; i < unobservableReachStates.size(); i++) {
			State state = unobservableReachStates.get(i);
			for (Map.Entry<String, Event> unobsEventEntry : FSM.globalUnobsEventMap
					.entrySet()) {
				Event euo = unobsEventEntry.getValue();
				if (state.getNextStateList(euo) != null) {
					unionStateList(unobservableReachStates,
							state.getNextStateList(euo));
				}

			}
		}
		return unobservableReachStates;
	}
	
	/*
	 * This function returns the union of two state lists Specifically, elements
	 * in list2 are added to list1 if they are not already in list1
	 * 
	 * @param list1 where elements from list2 are added to
	 */
	private void unionStateList(ArrayList<State> list1, ArrayList<State> list2) {
		for (int i = 0; i < list2.size(); i++) {
			if (!list1.contains(list2.get(i)))
				list1.add(list2.get(i));
		}
	}

	public boolean isCurrentStateOpaque(){
		for (Map.Entry<String, EstimatorState> estimateEntry: estimatorStateMap.entrySet()){
			if (!estimateEntry.getValue().isOpaque()) {
				return false;
			}
		}
		return true;
	}
	
	public void printEstimator() {
		System.out.println(numberOfStates + "\t" + "1"); // #initial = 1
		System.out.println();
		EstimatorState initial = initialEstimate;
		System.out.println(initial.getName() + "\t"
				+ ((initial.isOpaque()) ? 1 : 0) + "\t"
				+ initial.getNumberOfTransitions());
		for (Map.Entry<Event, ArrayList<State>> transitionEntry : initial.getAllTransitions()
				.entrySet()) {
			Event event = transitionEntry.getKey();
			ArrayList<State> nextEstimateList = transitionEntry.getValue();
			if (nextEstimateList.size() != 1)
				System.out
						.println("the estimator is not deterministic. Wrong!");
			else {
				System.out.println(event.getName() + "\t"
						+ nextEstimateList.get(0).getName() + "\t"
						+ ((event.isControllable()) ? "c" : "uc") + "\t"
						+ ((event.isObservable()) ? "o" : "uo"));
			}
		}
		for (Map.Entry<String, EstimatorState> entry : estimatorStateMap
				.entrySet()) {
			EstimatorState estimate = entry.getValue();
			if (((State)estimate).isInitial()) continue;
			System.out.println();
			System.out.println(estimate.getName() + "\t"
					+ ((estimate.isOpaque()) ? 1 : 0) + "\t"
					+ estimate.getNumberOfTransitions());
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : estimate
					.getAllTransitions().entrySet()) {
				Event event = transitionEntry.getKey();
				ArrayList<State> nextEstimateList = transitionEntry.getValue();
				if (nextEstimateList.size() != 1)
					System.out
							.println("the estimator is not deterministic. Wrong!");
				else {
					System.out.println(event.getName() + "\t"
							+ nextEstimateList.get(0).getName() + "\t"
							+ ((event.isControllable()) ? "c" : "uc") + "\t"
							+ ((event.isObservable()) ? "o" : "uo"));
				}
			}
		}
	}

	public static void main(String args[]) throws FileNotFoundException {
		String file = "testFSM/G2.fsm";
		FSM fsm = new FSM(file);
		// fsm.printFSM();
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				fsm);
		currentStateEstimator.printEstimator();
		System.out.println("current state opaque? " + currentStateEstimator.isCurrentStateOpaque());
		
	}
}
