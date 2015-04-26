package veip.fsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import veip.fsm.FSM.Event;

public class State {
	protected String name;
	protected boolean nonsecret = true;
	protected boolean initial = false;
	protected int numberOfTransitions = 0;
	protected HashMap<Event, ArrayList<State>> transitions;
	public boolean explored = false;

	public State(){};
	public State(String stateName, boolean isNonsecret) {
		name = stateName;
		nonsecret = isNonsecret;
		transitions = new HashMap<Event, ArrayList<State>>();
	}

	public State(String stateName) {
		name = stateName;
		transitions = new HashMap<Event, ArrayList<State>>();
	}

	public void addTransition(Event event, State nextState) {
		if (!transitions.containsKey(event))
			transitions.put(event, new ArrayList<State>());
		(transitions.get(event)).add(nextState);
	}

	public boolean isNonsecret ()
	{
		return nonsecret;
	}

	public boolean isInitial(){
		return initial;
	}
	
	public int getNumberOfTransitions(){
		return numberOfTransitions;
	}
	
	public HashMap<Event, ArrayList<State>> getTransitions(){
		return transitions;
	} 
	
	public String getName(){
		return name;
	}
}