package veip.fsm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import veip.fsm.FSM.Event;

public class State {
		
	public State(){
		transitions = new HashMap<Event, ArrayList<State>>();
	};
	
	public State(String stateName, boolean isInitial, boolean isNonsecret) {
		name = stateName;
		initial = isInitial;
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
	
	public void setTransitions(HashMap<Event, ArrayList<State>> newTransitions){
		transitions = newTransitions;			
	}
	
	public boolean isNonsecret (){
		return nonsecret;
	}	
	
	public boolean isInitial(){
		return initial;
	}
	public void setInitial(boolean initial){
		this.initial = initial;
	}
	
	public int getNumberOfTransitions(){
		return numberOfTransitions;
	}
	
	public void updateNumberOfTransitions(){
		numberOfTransitions = 0;
		for (Map.Entry<Event, ArrayList<State>> transitionEntry : transitions.entrySet())
			numberOfTransitions += transitionEntry.getValue().size();
	}
	
	public HashMap<Event, ArrayList<State>> getAllTransitions(){
		return transitions;
	} 
	
	public String getName(){
		return name;
	}
	public ArrayList<State> getNextStateList(Event event){
		if ((transitions.containsKey(event)))
			return transitions.get(event);
		else return null;
	}
	
	
	public static class StateComparator implements Comparator<State>{
		@Override
		public int compare (State s1, State s2){
			return (s1.getName().compareTo(s2.getName()));			
		}
	}
	
	
	public boolean explored = false;
	protected String name;
	protected boolean nonsecret = true;
	protected boolean initial = false;
	protected int numberOfTransitions = 0;
	protected HashMap<Event, ArrayList<State>> transitions;
	
}