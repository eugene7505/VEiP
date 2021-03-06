package veip.fsm;

/*TODO 
 Potentially we want to make State as an inner class, and separate State and Transitions
 (Separating State and Transitions may change the code structure greatly)
 But this way, we can have DFA and NFA data structure 
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class State {

	public State() {
		transitions = new HashMap<Event, ArrayList<State>>();
	};

	public State(String stateName, boolean isInitial, boolean isNonsecret) {
		name = stateName;
		initial = isInitial;
		nonsecret = isNonsecret;
		marked = isNonsecret;
		transitions = new HashMap<Event, ArrayList<State>>();
	}
	/*public State (State s){
		name = s.name;
		initial = s.initial;
		nonsecret = s.nonsecret;
		marked = s.marked;
		transitions = new HashMap<Event, ArrayList<State>>();
		for (Map.Entry<Event, ArrayList<State>> transitionEntry: s.getAllTransitions().entrySet())
			transitions.put(transitionEntry.getKey(), transitionEntry.getValue());
	}
*/
	public State(String stateName) {
		name = stateName;
		transitions = new HashMap<Event, ArrayList<State>>();
	}

	public void createTransition(Event event, State nextState) {
		if (!transitions.containsKey(event))
			transitions.put(event, new ArrayList<State>());
		(transitions.get(event)).add(nextState);
		updateNumberOfTransitions();
	}
	/*
	 * This function does not update number of transitions
	 * It fill out the transition table
	 */
	public void addTransition(Event event, State nextState) {
		if (!transitions.containsKey(event))
			transitions.put(event, new ArrayList<State>());
		(transitions.get(event)).add(nextState);
	}
	public void setTransitions(HashMap<Event, ArrayList<State>> newTransitions) {
		transitions = newTransitions;
	}

	public boolean isNonsecret() {
		return nonsecret;
	}

	public void setNonsecret(boolean nonsecret) {
		this.nonsecret = nonsecret;
	}

	public boolean isInitial() {
		return initial;
	}

	public void setInitial(boolean initial) {
		this.initial = initial;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public int getNumberOfTransitions() {
		return numberOfTransitions;
	}

	public void updateNumberOfTransitions() {
		numberOfTransitions = 0;
		for (Map.Entry<Event, ArrayList<State>> transitionEntry : transitions
				.entrySet())
			numberOfTransitions += transitionEntry.getValue().size();
	}

	public HashMap<Event, ArrayList<State>> getAllTransitions() {
		return transitions;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ArrayList<State> getNextStateList(Event event) {
		if ((transitions.containsKey(event)))
			return transitions.get(event);
		else
			return null;
	}

	public static class StateComparator implements Comparator<State> {
		@Override
		public int compare(State s1, State s2) {
			return (s1.getName().compareTo(s2.getName()));
		}
	}

	protected int index = -1;
	public boolean flagged = false;
	protected String name;
	protected boolean nonsecret = true;
	protected boolean marked = true;
	protected boolean initial = false;
	protected int numberOfTransitions = 0;
	protected HashMap<Event, ArrayList<State>> transitions;

}