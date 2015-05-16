package veip.fsm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import veip.fsm.FSM.Event;

public final class ParallelCompositionUtilities {
	private ParallelCompositionUtilities() {
	}

	public static class StatePair {
		public StatePair(State s1, State s2) {
			first = s1;
			second = s2;
		}

		State first;
		State second;
	}

	/*
	 * This function computes the parallel composition for two automata, both
	 * with one single initial state
	 * TODO extend it to a list of initial states
	 */
	public static FSM pairwiseParallelComposition_singleInitialState(FSM fsm1,
			FSM fsm2) {
		FSM compositeFSM = new FSM();

		if (fsm1.initialStateList.size() != 1
				|| fsm2.initialStateList.size() != 1) {
			System.out
					.println("input fsm should have only one single initial state. Wrong!");
		}

		HashSet<Event> sharedEventSet = new HashSet<Event>();
		HashSet<Event> privateEventSet1 = new HashSet<Event>();
		HashSet<Event> privateEventSet2 = new HashSet<Event>();
		computeEventSets(fsm1.localEventMap, fsm2.localEventMap,
				compositeFSM.localEventMap, sharedEventSet, privateEventSet1,
				privateEventSet2);
		HashMap<StatePair, State> statePairMap = new HashMap<StatePair, State>();

		// generate the initial state
		State initialState1 = fsm1.initialStateList.get(0);
		State initialState2 = fsm2.initialStateList.get(0);

		StatePair initialStatePair = new StatePair(initialState1, initialState2);
		State initialState = compositeFSM.addState(
				generateNameFromStatePair(initialStatePair), true,
				isStatePairNonsecret(initialState1, initialState2));
		compositeFSM.updateNumberOfInitialStates();
		statePairMap.put(initialStatePair, initialState);

		// BFS to build the composite FSM
		Stack<StatePair> statePairsStack = new Stack<StatePair>();
		statePairsStack.push(initialStatePair);

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
						State nextState = compositeFSM.addState(
								generateNameFromStatePair(nextStatePair),
								isStatePairInitial(nextStates1.get(i),
										nextStates2.get(j)),
								isStatePairNonsecret(nextStates1.get(i),
										nextStates2.get(j)));
						statePairMap.put(nextStatePair, nextState);
						state.addTransition(event, nextState);
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

	private static String generateNameFromStatePair(StatePair statePair) {
		String name = new String("(");
		name += statePair.first.getName();
		name += ",";
		name += statePair.second.getName();
		name += ")";
		return name;
	}

	private static boolean isStatePairInitial(State s1, State s2) {
		return (s1.isInitial() && s2.isInitial());
	}

	private static boolean isStatePairNonsecret(State s1, State s2) {
		return (s1.isNonsecret() && s2.isNonsecret());
	}

	private static void computeEventSets(HashMap<String, Event> eventMap1,
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

	public static void main(String[] args) throws FileNotFoundException {
		FSM fsm1 = new FSM("testFSM/dailyBehavior/Estd.fsm");
		FSM fsm2 = new FSM("testFSM/dailyBehavior/Estf.fsm");

		System.out.println("print Estd");
		fsm1.printFSM();

		System.out.println();
		System.out.println("print Estf");
		fsm2.printFSM();
		FSM fsm12 = ParallelCompositionUtilities
				.pairwiseParallelComposition_singleInitialState(fsm1, fsm2);

		System.out.println();
		System.out.println("print Estd||Estf");
		fsm12.printFSM();

	}

}
