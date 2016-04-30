package veip.fsm;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MealyAutomaton extends FSM {

	HashMap<State, HashMap<Event, Event>> transitionOutputMap;

	public MealyAutomaton() {
		super();
		transitionOutputMap = new HashMap<State, HashMap<Event, Event>>();
	}

	public void addTransitionOuput(State state, Event event, Event outputEvent) {
		if (!transitionOutputMap.containsKey(state)) {
			transitionOutputMap.put(state, new HashMap<Event, Event>());
			transitionOutputMap.get(state).put(event, outputEvent);
		} else {
			HashMap<Event, Event> transitionOutput = transitionOutputMap
					.get(state);
			if (transitionOutput.containsKey(event)) {
				if (transitionOutput.get(event) != outputEvent)
					System.out
							.println("transition is mapped to a different output. WRONG!");
			} else
				transitionOutput.put(event, outputEvent);
		}
	}

	public void renameStates() {
		int k = 0;
		HashMap<String, State> newStateMap = new HashMap<String, State>();

		for (int i = 0; i < numberOfInitialState; i++, k++) {
			State state = initialStateList.get(i);
			state.setName(Integer.toString(k));
			newStateMap.put(Integer.toString(k), state);
		}
		for (Map.Entry<String, State> entry : stateMap.entrySet()) {
			State state = entry.getValue();
			if (state.isInitial())
				continue;
			state.setName(Integer.toString(k));
			newStateMap.put(Integer.toString(k), state);
			k++;
		}
		stateMap = newStateMap;
	}

	public void printMA() {
		System.out.println(numberOfStates + "\t" + numberOfInitialState);
		for (int i = 0; i < numberOfInitialState; i++) {
			State state = initialStateList.get(i);
			System.out.println();
			System.out.println(state.getName() + "\t"
					+ ((state.isNonsecret()) ? 1 : 0) + "\t"
					+ state.getNumberOfTransitions());
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : state
					.getAllTransitions().entrySet()) {
				Event inputEvent = transitionEntry.getKey();
				Event outStringEvent = transitionOutputMap.get(state).get(
						inputEvent);
				ArrayList<State> nextStateList = transitionEntry.getValue();
				for (int j = 0; j < nextStateList.size(); j++) {
					System.out.println(inputEvent.getName() + "/"
							+ outStringEvent.getName() + "\t"
							+ nextStateList.get(j).getName() + "\t"
							+ ((inputEvent.isObservable()) ? "o" : "uo"));
				}
			}
		}
		for (Map.Entry<String, State> entry : stateMap.entrySet()) {
			State state = entry.getValue();
			if (state.isInitial())
				continue;
			else {
				System.out.println();
				System.out.println(state.getName() + "\t"
						+ ((state.isNonsecret()) ? 1 : 0) + "\t"
						+ state.getNumberOfTransitions());
				for (Map.Entry<Event, ArrayList<State>> transitionEntry : state
						.getAllTransitions().entrySet()) {
					Event inputEvent = transitionEntry.getKey();
					Event outStringEvent = transitionOutputMap.get(state).get(
							inputEvent);
					ArrayList<State> nextStateList = transitionEntry.getValue();
					for (int j = 0; j < nextStateList.size(); j++) {
						System.out.println(inputEvent.getName() + "/"
								+ outStringEvent.getName() + "\t"
								+ nextStateList.get(j).getName() + "\t"
								+ ((inputEvent.isObservable()) ? "o" : "uo"));
					}
				}
			}
		}
	}

	@Override
	public void exportFSM(String outFileName, boolean renumberStates)
			throws FileNotFoundException {
		PrintWriter fileWriter = new PrintWriter(outFileName);
		fileWriter.println(numberOfStates + "\t" + numberOfInitialState);
		for (int i = 0; i < numberOfInitialState; i++) {
			State state = initialStateList.get(i);
			fileWriter.println();
			if (renumberStates)
				fileWriter.println(state.getIndex() + "\t"
						+ ((state.isMarked()) ? 1 : 0) + "\t"
						+ state.getNumberOfTransitions());
			else
				fileWriter.println(state.getName() + "\t"
						+ ((state.isMarked()) ? 1 : 0) + "\t"
						+ state.getNumberOfTransitions());
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : state
					.getAllTransitions().entrySet()) {
				Event inputEvent = transitionEntry.getKey();
				Event outStringEvent = transitionOutputMap.get(state).get(
						inputEvent);
				ArrayList<State> nextStateList = transitionEntry.getValue();
				if (renumberStates) {
					for (int j = 0; j < nextStateList.size(); j++) {
						fileWriter.println(inputEvent.getName() + "/"
								+ outStringEvent.getName() + "\t"
								+ nextStateList.get(j).getIndex() + "\t"
								+ ((inputEvent.isObservable()) ? "o" : "uo"));
					}
				} else {
					for (int j = 0; j < nextStateList.size(); j++) {
						fileWriter.println(inputEvent.getName() + "/"
								+ outStringEvent.getName() + "\t"
								+ nextStateList.get(j).getName() + "\t"
								+ ((inputEvent.isObservable()) ? "o" : "uo"));
					}
				}
			}
		}
		for (Map.Entry<String, State> entry : stateMap.entrySet()) {
			State state = entry.getValue();
			if (state.isInitial())
				continue;
			else {
				fileWriter.println();
				if (renumberStates)
					fileWriter.println(state.getIndex() + "\t"
							+ ((state.isMarked()) ? 1 : 0) + "\t"
							+ state.getNumberOfTransitions());
				else
					fileWriter.println(state.getName() + "\t"
							+ ((state.isMarked()) ? 1 : 0) + "\t"
							+ state.getNumberOfTransitions());
				for (Map.Entry<Event, ArrayList<State>> transitionEntry : state
						.getAllTransitions().entrySet()) {
					Event inputEvent = transitionEntry.getKey();
					Event outStringEvent = transitionOutputMap.get(state).get(
							inputEvent);
					ArrayList<State> nextStateList = transitionEntry.getValue();
					if (renumberStates) {
						for (int j = 0; j < nextStateList.size(); j++) {
							fileWriter
									.println(inputEvent.getName()
											+ "/"
											+ outStringEvent.getName()
											+ "\t"
											+ nextStateList.get(j).getIndex()
											+ "\t"
											+ ((inputEvent.isObservable()) ? "o"
													: "uo"));
						}
					} else {
						for (int j = 0; j < nextStateList.size(); j++) {
							fileWriter
									.println(inputEvent.getName()
											+ "/"
											+ outStringEvent.getName()
											+ "\t"
											+ nextStateList.get(j).getName()
											+ "\t"
											+ ((inputEvent.isObservable()) ? "o"
													: "uo"));
						}
					}
				}
			}
		}
		fileWriter.close();
	}
}
