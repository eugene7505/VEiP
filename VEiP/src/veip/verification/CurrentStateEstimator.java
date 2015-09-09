package veip.verification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import veip.fsm.Event;
import veip.fsm.FSM;
import veip.fsm.State;

public class CurrentStateEstimator {

	FSM fsm;
	int numberOfStates;
	EstimatorState initialEstimate;
	HashMap<String, EstimatorState> estimatorStateMap;
	HashMap<String, Event> localEventMap;

	public CurrentStateEstimator(FSM fsm) {
		this.fsm = fsm;
		buildCurrentStateEstimator_UR();
	}

	// This constructor is deprecated because we should not need observer
	// without unobservable reach
	@Deprecated
	public CurrentStateEstimator(FSM fsm, boolean unobsReach) {
		this.fsm = fsm;
		if (unobsReach)
			buildCurrentStateEstimator_UR();
		else
			buildCurrentStateEstimator_UP();
	}

	/*
	 * This function builds the current state estimator by determinizing the
	 * corresponding fsm. Notice that the input fsm could have a set of initial
	 * state and could be nondeterministic However, the resulting estimator is
	 * always deterministic
	 */
	private void buildCurrentStateEstimator_UR() {
		numberOfStates = 0;
		estimatorStateMap = new HashMap<String, EstimatorState>();
		localEventMap = new HashMap<String, Event>();

		ArrayList<State> initialStateWithUnobservableReach = unobserverbleReach(fsm
				.getInitialStateList());
		initialEstimate = addEstimate(initialStateWithUnobservableReach);
		initialEstimate.setInitial(true);

		// BFS to build the cse
		Stack<EstimatorState> stack = new Stack<EstimatorState>();
		stack.push(initialEstimate);
		while (!stack.empty()) {
			EstimatorState estimate = stack.pop();
			if (estimate.flagged)
				continue;
			for (Map.Entry<String, Event> obsEventEntry : FSM.globalObsEventMap
					.entrySet()) {
				ArrayList<State> nextStates = new ArrayList<State>();
				Event event = obsEventEntry.getValue();
				boolean eventEnabled = false;
				for (int i = 0; i < estimate.stateEstimate.size(); i++) {
					State state = estimate.stateEstimate.get(i);
					if (state.getNextStateList(event) != null) { // has this
																	// transition,
																	// should
																	// add to
																	// transition
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
			estimate.flagged = true;
			estimate.updateNumberOfTransitions();
		}
	}

	@Deprecated
	private void buildCurrentStateEstimator_UP() { // unobservable prefix:
													// consider uo a for
													// transition a
		numberOfStates = 0;
		estimatorStateMap = new HashMap<String, EstimatorState>();
		localEventMap = new HashMap<String, Event>();

		ArrayList<State> initialState = fsm.getInitialStateList();
		initialEstimate = addEstimate(initialState);
		initialEstimate.setInitial(true);

		// BFS to build the cse
		Stack<EstimatorState> stack = new Stack<EstimatorState>();
		stack.push(initialEstimate);
		while (!stack.empty()) {
			EstimatorState estimate = stack.pop();
			if (estimate.flagged)
				continue;
			for (Map.Entry<String, Event> obsEventEntry : FSM.globalObsEventMap
					.entrySet()) {
				ArrayList<State> urStates = unobserverbleReach(estimate.stateEstimate);
				ArrayList<State> nextStates = new ArrayList<State>();
				Event event = obsEventEntry.getValue();
				boolean eventEnabled = false;
				for (int i = 0; i < urStates.size(); i++) {
					State state = urStates.get(i);
					if (state.getNextStateList(event) != null) { // has this
																	// transition,
																	// should
																	// add to
																	// transition
						localEventMap.put(event.getName(), event);
						unionStateList(nextStates,
								state.getNextStateList(event));
						eventEnabled = true;
					}
				}
				if (eventEnabled == true) {
					EstimatorState nextEstimate = addEstimate(nextStates);
					estimate.addTransition(event, nextEstimate);
					stack.push(nextEstimate);
				}
			}
			estimate.flagged = true;
			estimate.updateNumberOfTransitions();
		}

	}

	/*
	 * This function adds a new estimator state if the estimate does not already
	 * exists The input is a list of states that the estimate contains and the
	 * output is an EstimateState instance The input state list is sorted before
	 * we check if the corresponding estimate exists
	 * 
	 * @param stateList that contains the list of states we build the estimate
	 * from
	 * 
	 * @return EstimatorState
	 */
	private EstimatorState addEstimate(ArrayList<State> stateList) {
		Collections.sort(stateList, new State.StateComparator());
		EstimatorState newEstimate = new EstimatorState(stateList);
		String newEstimateName = newEstimate.getName();
		if (estimatorStateMap.containsKey(newEstimateName)) {
			return estimatorStateMap.get(newEstimateName);
		} else {
			estimatorStateMap.put(newEstimateName, newEstimate);
			numberOfStates++;
			// System.out.println("creating" + newEstimateName);
		}
		return newEstimate;
	}

	/*
	 * This function returns the unobservable reach from a given list of states
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

	public int getNumberOfStates() {
		return numberOfStates;
	}

	public EstimatorState getInitialEstimate() {
		return initialEstimate;
	}

	public HashMap<String, EstimatorState> getEstimatorStateMap() {
		return estimatorStateMap;
	}

	public HashMap<String, Event> getLocalEventMap() {
		return localEventMap;
	}

	/*
	 * This function returns whether the fsm is current state opaque or not.
	 * 
	 * @return true if the fsm is opaque and false if the fsm is not opaque
	 */
	public boolean isCurrentStateOpaque() {
		for (Map.Entry<String, EstimatorState> estimateEntry : estimatorStateMap
				.entrySet()) {
			if (!estimateEntry.getValue().isOpaque()) {
				return false;
			}
		}
		return true;
	}

	public void printUnsafeStates() {
		for (Map.Entry<String, EstimatorState> estimateEntry : estimatorStateMap
				.entrySet()) {
			if (!estimateEntry.getValue().isOpaque()) {
				System.out.print("{" + estimateEntry.getKey() + "} ");
				;
			}
		}
	}

	public void printEstimator() {
		System.out.println(numberOfStates + "\t" + "1"); // #initial = 1
		System.out.println();
		EstimatorState initial = initialEstimate;
		System.out.println(initial.getName() + "\t"
				+ ((initial.isOpaque()) ? 1 : 0) + "\t"
				+ initial.getNumberOfTransitions());
		for (Map.Entry<Event, ArrayList<State>> transitionEntry : initial
				.getAllTransitions().entrySet()) {
			Event event = transitionEntry.getKey();
			ArrayList<State> nextEstimateList = transitionEntry.getValue();
			if (nextEstimateList.size() != 1)
				System.out
						.println("the estimator is not deterministic. Wrong!");
			else {
				System.out.println(event.getName() + "\t"
						+ nextEstimateList.get(0).getName() + "\t"
						+ ((event.isObservable()) ? "o" : "uo"));
			}
		}
		for (Map.Entry<String, EstimatorState> entry : estimatorStateMap
				.entrySet()) {
			EstimatorState estimate = entry.getValue();
			if (((State) estimate).isInitial())
				continue;
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
							+ ((event.isObservable()) ? "o" : "uo"));
				}
			}
		}
	}

	public boolean isInitialStateSafe() {
		return initialEstimate.isNonsecret();
	}
}
