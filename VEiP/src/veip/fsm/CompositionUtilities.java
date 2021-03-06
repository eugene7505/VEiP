package veip.fsm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

public final class CompositionUtilities {
	private CompositionUtilities() {
	}

	/*
	 * This function computes the product composition for two automata,
	 * automata may have multiple initial states
	 */
	public static FSM pairwiseProduct(FSM fsm1, FSM fsm2) {
		FSM productFSM = new FSM();

		if (fsm1.initialStateList.size() != 1
				|| fsm2.initialStateList.size() != 1) {
			System.out.println("composition with multiple initial states.");
		}

		HashMap<StatePair, State> statePairMap = new HashMap<StatePair, State>();
		Stack<StatePair> statePairsStack = new Stack<StatePair>();

		// generate the initial state
		ArrayList<State> initialStateList1 = fsm1.initialStateList;
		ArrayList<State> initialStateList2 = fsm2.initialStateList;

		for (int i = 0; i < initialStateList1.size(); i++) {
			for (int j = 0; j < initialStateList2.size(); j++) {
				State initialState1 = initialStateList1.get(i);
				State initialState2 = initialStateList2.get(j);
				StatePair initialStatePair = new StatePair(initialState1,
						initialState2);
				State initialState = productFSM.createState(
						generateNameFromStatePair(initialStatePair), true,
						isStatePairNonsecret(initialState1, initialState2));
				statePairMap.put(initialStatePair, initialState);
				statePairsStack.push(initialStatePair);
			}
		}
		productFSM.updateNumberOfInitialStates();

		while (!statePairsStack.isEmpty()) {
			StatePair statePair = statePairsStack.pop();
			//System.out.println(statePair.first.getName()+","+statePair.second.getName());
			State state = statePairMap.get(statePair);
			if (state.flagged)
				continue;
			for (Map.Entry<String, Event> eventEntry : fsm2.localEventMap
					.entrySet()) {
				Event event = eventEntry.getValue();
				ArrayList<State> nextStates1;
				ArrayList<State> nextStates2;

				if (fsm1.localEventMap.containsValue(event)) {
					nextStates1 = statePair.first.getNextStateList(event);
					nextStates2 = statePair.second.getNextStateList(event);
				} 
				else continue;
				if (nextStates1 == null || nextStates2 == null)
					continue;
				for (int i = 0; i < nextStates1.size(); i++) {
					for (int j = 0; j < nextStates2.size(); j++) {
						StatePair nextStatePair = new StatePair(
								nextStates1.get(i), nextStates2.get(j));
						State nextState = productFSM.createState(
								generateNameFromStatePair(nextStatePair),
								isStatePairInitial(nextStates1.get(i),
										nextStates2.get(j)),
								isStatePairNonsecret(nextStates1.get(i),
										nextStates2.get(j)));
						statePairMap.put(nextStatePair, nextState);
						state.createTransition(event, nextState);
						statePairsStack.push(nextStatePair);
					}
				}
			}
			state.updateNumberOfTransitions();
			state.flagged = true;
		}
		productFSM.updateNumberOfStates();
		return productFSM;
	}
	

	/*
	 * This function computes the parallel composition for two automata,
	 * automata may have multiple initial states
	 */
	public static FSM pairwiseParallelComposition(FSM fsm1, FSM fsm2) {
		FSM compositeFSM = new FSM();

		if (fsm1.initialStateList.size() != 1
				|| fsm2.initialStateList.size() != 1) {
			System.out.println("composition with multiple initial states.");
		}

		HashSet<Event> sharedEventSet = new HashSet<Event>();
		HashSet<Event> privateEventSet1 = new HashSet<Event>();
		HashSet<Event> privateEventSet2 = new HashSet<Event>();
		computeEventSets(fsm1.localEventMap, fsm2.localEventMap,
				compositeFSM.localEventMap, sharedEventSet, privateEventSet1,
				privateEventSet2);
		HashMap<StatePair, State> statePairMap = new HashMap<StatePair, State>();

		Stack<StatePair> statePairsStack = new Stack<StatePair>();

		// generate the initial state
		ArrayList<State> initialStateList1 = fsm1.initialStateList;
		ArrayList<State> initialStateList2 = fsm2.initialStateList;

		for (int i = 0; i < initialStateList1.size(); i++) {
			for (int j = 0; j < initialStateList2.size(); j++) {
				State initialState1 = initialStateList1.get(i);
				State initialState2 = initialStateList2.get(j);
				StatePair initialStatePair = new StatePair(initialState1,
						initialState2);
				State initialState = compositeFSM.createState(
						generateNameFromStatePair(initialStatePair), true,
						isStatePairNonsecret(initialState1, initialState2));
				statePairMap.put(initialStatePair, initialState);
				statePairsStack.push(initialStatePair);
			}
		}
		compositeFSM.updateNumberOfInitialStates();

		while (!statePairsStack.isEmpty()) {
			StatePair statePair = statePairsStack.pop();
			State state = statePairMap.get(statePair);
			if (state.flagged)
				continue;
			for (Map.Entry<String, Event> eventEntry : compositeFSM.localEventMap
					.entrySet()) {
				Event event = eventEntry.getValue();
				ArrayList<State> nextStates1;
				ArrayList<State> nextStates2;

				if (sharedEventSet.contains(event)) {
					nextStates1 = statePair.first.getNextStateList(event);
					nextStates2 = statePair.second.getNextStateList(event);
				} else if (privateEventSet1.contains(event)) {
					nextStates1 = statePair.first.getNextStateList(event);
					nextStates2 = new ArrayList<State>();
					nextStates2.add(statePair.second);
				} else {
					nextStates1 = new ArrayList<State>();
					nextStates1.add(statePair.first);
					nextStates2 = statePair.second.getNextStateList(event);
				}

				if (nextStates1 == null || nextStates2 == null)
					continue;
				for (int i = 0; i < nextStates1.size(); i++) {
					for (int j = 0; j < nextStates2.size(); j++) {
						StatePair nextStatePair = new StatePair(
								nextStates1.get(i), nextStates2.get(j));
						State nextState = compositeFSM.createState(
								generateNameFromStatePair(nextStatePair),
								isStatePairInitial(nextStates1.get(i),
										nextStates2.get(j)),
								isStatePairNonsecret(nextStates1.get(i),
										nextStates2.get(j)));
						statePairMap.put(nextStatePair, nextState);
						state.createTransition(event, nextState);
						state.updateNumberOfTransitions();
						statePairsStack.push(nextStatePair);
					}
				}
				state.flagged = true;
			}
		}
		compositeFSM.updateNumberOfStates();
		return compositeFSM;
	}

	/*
	 * This function implements parallel composition for a PFA and a FSM. The
	 * first automaton is PFA
	 */
	/*
	 * This function computes the parallel composition for two automata,
	 * automata may have multiple initial states Since the number of states of
	 * the composite PFA is not known a priori, To initialize PFA, we assume the
	 * number of states = n1*n2 We then resize after the composite automaton is
	 * completed. 
	 */

	public static PFA pairwiseParallelComposition(PFA pfa1, FSM fsm2) {
		PFA compositePFA = new PFA(pfa1.numberOfStates * fsm2.numberOfStates);

		HashSet<Event> sharedEventSet = new HashSet<Event>();
		HashSet<Event> privateEventSet1 = new HashSet<Event>();
		HashSet<Event> privateEventSet2 = new HashSet<Event>();
		computeEventSets(pfa1.localEventMap, fsm2.localEventMap,
				compositePFA.localEventMap, sharedEventSet, privateEventSet1,
				privateEventSet2);
		HashMap<StatePair, State> statePairMap = new HashMap<StatePair, State>();
		Stack<StatePair> statePairsStack = new Stack<StatePair>();

		// generate the initial state
		ArrayList<State> initialStateList1 = pfa1.initialStateList;
		ArrayList<State> initialStateList2 = fsm2.initialStateList;

		for (int i = 0; i < initialStateList1.size(); i++) {
			for (int j = 0; j < initialStateList2.size(); j++) {
				State initialState1 = initialStateList1.get(i);
				State initialState2 = initialStateList2.get(j);
				StatePair initialStatePair = new StatePair(initialState1,
						initialState2);
				State initialState = compositePFA.addState(
						generateNameFromStatePair(initialStatePair), true,
						isStatePairNonsecret(initialState1, initialState2));
				compositePFA.initialDistribution.set(initialState.index,
						pfa1.initialDistribution.get(initialState1.index));
				statePairMap.put(initialStatePair, initialState);
				statePairsStack.push(initialStatePair);
			}
		}
		compositePFA.updateNumberOfInitialStates();

		while (!statePairsStack.isEmpty()) {
			StatePair statePair = statePairsStack.pop();
			State state = statePairMap.get(statePair);
			if (state.flagged)
				continue;
			for (Map.Entry<String, Event> eventEntry : compositePFA.localEventMap
					.entrySet()) {
				Event event = eventEntry.getValue();
				ArrayList<State> nextStates1;
				ArrayList<State> nextStates2;

				if (sharedEventSet.contains(event)) {
					nextStates1 = statePair.first.getNextStateList(event);
					nextStates2 = statePair.second.getNextStateList(event);
				} else if (privateEventSet1.contains(event)) {
					nextStates1 = statePair.first.getNextStateList(event);
					nextStates2 = new ArrayList<State>();
					nextStates2.add(statePair.second);
				} else {
					nextStates1 = new ArrayList<State>();
					nextStates1.add(statePair.first);
					nextStates2 = statePair.second.getNextStateList(event);
				}

				if (nextStates1 == null || nextStates2 == null)
					continue;
				for (int i = 0; i < nextStates1.size(); i++) {
					for (int j = 0; j < nextStates2.size(); j++) {
						StatePair nextStatePair = new StatePair(
								nextStates1.get(i), nextStates2.get(j));
						State nextState = compositePFA.addState(
								generateNameFromStatePair(nextStatePair),
								isStatePairInitial(nextStates1.get(i),
										nextStates2.get(j)),
								isStatePairNonsecret(nextStates1.get(i),
										nextStates2.get(j)));
						statePairMap.put(nextStatePair, nextState);
						state.createTransition(event, nextState);
						double p = pfa1
								.getEventMatrix(event, statePair.first.index,
										nextStates1.get(i).index);
						compositePFA.setEventMatrix(event, state.index,
								nextState.index, p);
						//state.updateNumberOfTransitions();
						statePairsStack.push(nextStatePair);
					}
				}
				state.flagged = true;
			}
		}
		compositePFA.updateNumberOfStates();
		compositePFA.updateSize();
		return compositePFA;
	}

	public static String generateNameFromStatePair(StatePair statePair) {
		String name = new String("(");
		name += statePair.first.getName();
		name += ";";
		name += statePair.second.getName();
		name += ")";
		return name;
	}

	public static boolean isStatePairInitial(State s1, State s2) {
		return (s1.isInitial() && s2.isInitial());
	}

	public static boolean isStatePairNonsecret(State s1, State s2) {
		return (s1.isNonsecret() && s2.isNonsecret());
	}

	public static void computeEventSets(HashMap<String, Event> eventMap1,
			HashMap<String, Event> eventMap2,
			HashMap<String, Event> eventMapUnion,
			HashSet<Event> sharedEventSet, HashSet<Event> privateEventSet1,
			HashSet<Event> privateEventSet2) {
		for (Map.Entry<String, Event> eventEntry : eventMap1.entrySet()) {
			eventMapUnion.put(eventEntry.getKey(), eventEntry.getValue());
			privateEventSet1.add(eventEntry.getValue());
		}
		for (Map.Entry<String, Event> eventEntry : eventMap2.entrySet()) {
			Event event = eventEntry.getValue();
			// if not in eventMap1, then it is a private event for fsm2
			if (!eventMapUnion.containsValue(event)) {
				eventMapUnion.put(event.getName(), event);
				privateEventSet2.add(event);
			}
			// already in eventMap1, then shared event, move from
			// privateEventSet to sharedEventSet
			else {
				privateEventSet1.remove(event);
				sharedEventSet.add(event);
			}
		}
	}
	
	private static void printEvents(HashSet<Event> eventSet){
		for (Event eventEntry: eventSet){
			System.out.println(eventEntry.getName());
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		FSM safeFsm = new FSM ("../VEiP/testFSM/Office/safe.fsm");
		FSM editFsm = new FSM ("../VEiP/testFSM/Office/edit.fsm");
		FSM safeEditPatternFSM = CompositionUtilities
				.pairwiseParallelComposition(editFsm, safeFsm);
		safeEditPatternFSM.printFSM();

	}

}
