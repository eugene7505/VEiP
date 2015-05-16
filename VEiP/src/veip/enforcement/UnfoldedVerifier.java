package veip.enforcement;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import veip.fsm.FSM;
import veip.fsm.GraphUtilities;
import veip.fsm.State;
import veip.fsm.FSM.Event;

public class UnfoldedVerifier {
	FSM verifier;
	FSM estimator;
	//(m1,m2) maps to their shortest dashed path if they are dashed connected; or null otherwise
	HashMap<State, HashMap<State, String>> shortestDashedPath;   
	HashMap<State, HashMap<State, Boolean>> dashedConnectivity;   
	FSM unfoldedVerifier;
	HashMap<State, YEventPair> ZtoYEventMap; //Zstate to YState and Event, all in UnfoldedVerifier
	HashMap<State, State> YtoMStateMap; //Ystate in UnfoldedVerifier to MState in Verifier
	HashMap<State, State> YtoEstimateMap; //Ystate in Vu to mState in Estimator
	Stack<State> stateStack;

	
	public UnfoldedVerifier(Verifier verifier) {
		this.verifier = verifier.getVerifierFSM();
		this.estimator = verifier.getEstimatorFSM();
		shortestDashedPath = GraphUtilities.shortestDashedPath(this.verifier);
		dashedConnectivity = GraphUtilities.dashedConnectivity(this.verifier);
		unfoldedVerifier = new FSM();
		ZtoYEventMap = new HashMap<State, UnfoldedVerifier.YEventPair>();
		YtoMStateMap = new HashMap<State, State>();
		YtoEstimateMap = new HashMap<State, State>();
		stateStack = new Stack<State>();
		
		buildUnfoldedVerifier();
	}

	public class YEventPair{
		public YEventPair(State ystate, Event event){
			this.ystate = ystate;
			this.event = event;
		}
		State ystate;
		Event event;
	}
	
	private void buildUnfoldedVerifier() {
		// notice that all y states are marked (i.e., nonsecret)
		State initialYState = unfoldedVerifier.addState(verifier
				.getInitialStateList().get(0).getName(), true, true);
		YtoMStateMap.put(initialYState, verifier
				.getInitialStateList().get(0));
		stateStack.push(initialYState);

		while (!stateStack.isEmpty()) {
			State state = stateStack.pop();
			if (state.flagged)
				continue;
			else {
				if (state.isNonsecret())
					expandYState(state);
				else
					expandZState(state);
				state.flagged = true;
			}
		}
	}

	private void expandYState(State ystate) {
		State estimate = getEstimate(ystate);
		for (Map.Entry<Event, ArrayList<State>> transitionEntry : estimate
				.getAllTransitions().entrySet()) {
			Event event = transitionEntry.getKey();
			State nextZstate = unfoldedVerifier.addState(
					generateZStateName(ystate, event), false, false);
			stateStack.push(nextZstate);
			addZtoYEventMap(nextZstate, ystate, event);
			ystate.addTransition(event, nextZstate);
		}
	}

	private void expandZState(State zstate) {
		State mState = YtoMStateMap.get(getYEventPair(zstate).ystate);
		Event eventPart = getYEventPair(zstate).event;
		for (Map.Entry<String, State> stateEntry : verifier.getStateMap().entrySet()){
			State mprimeState = stateEntry.getValue();
			if (!dashedConnectivity.get(mState).get(mprimeState).booleanValue())
				continue;
			else {
				ArrayList<State> nextMStates = mprimeState.getAllTransitions().get(eventPart);
				if ( nextMStates == null)
					continue;
				State nextYState = unfoldedVerifier.addState(nextMStates.get(0).getName(), false, true);
				stateStack.push(nextYState);
				addYtoMStateMap(nextYState, nextMStates.get(0));
				Event mprimEvent = unfoldedVerifier.addEvent(mprimeState.getName(), false, true);
				zstate.addTransition(mprimEvent, nextYState);
			}
		}
		
	}
	
	private void addZtoYEventMap(State zstate, State ystate, Event event){
		if (!ZtoYEventMap.containsKey(zstate))
			ZtoYEventMap.put(zstate, new YEventPair(ystate,event));
		else {
			YEventPair yEventPair = ZtoYEventMap.get(zstate);
			if (yEventPair.ystate != ystate || yEventPair.event != event)
				System.out.println("zstate " + zstate.getName() + "is mapped to two differnet yEventPairs. WRONG!");
		}	
	}
	
	private YEventPair getYEventPair(State zstate){
		if (!ZtoYEventMap.containsKey(zstate)){
			System.out.println("zstate" + zstate.getName() + "is not in the ZtoYEventMap. WRONG!");
			return null;
		}
		else return ZtoYEventMap.get(zstate);
	}
	
	private State getMState(State ystate){
		if (!YtoMStateMap.containsKey(ystate)){
			State mstate = verifier.getState(ystate.getName());
			YtoMStateMap.put(ystate, mstate);
			return mstate;
		}
		else return YtoMStateMap.get(ystate);
	}
	
	private void addYtoMStateMap(State ystate, State mstate){
		if (!YtoMStateMap.containsKey(ystate))
			YtoMStateMap.put(ystate, mstate);
		else {
			State existingmState= YtoMStateMap.get(ystate);
			if (existingmState != mstate)
				System.out.println("ystate " + ystate.getName() + "is mapped to two differnet yEventPairs. WRONG!");
		}	
	}
	
	
	public String generateZStateName(State ystate, Event event) {

		String zStateName = new String("(");
		zStateName += ystate.getName();
		zStateName += ",";
		zStateName += event.getName();
		zStateName += ")";
		return zStateName;
	}

	public State getEstimate(State ystate) {
		if (!YtoEstimateMap.containsKey(ystate)){
			int beginIndex = ystate.getName().indexOf(',');
			int endIndex = ystate.getName().length() - 1 ;
			String estimateName = ystate.getName().substring(beginIndex + 1, endIndex);
			State estimateState = estimator.getState(estimateName);
			YtoEstimateMap.put(ystate, estimateState);
			return estimateState;
		}
		else return YtoEstimateMap.get(ystate);
	}
		
	
	
	public FSM getUnfoldedVerifierFSM() {
		return unfoldedVerifier;
	}

	public void printUnfoldedVerifier() {
		unfoldedVerifier.printFSM();
	}

	public static void main(String[] args) throws FileNotFoundException {
		final long startTime = System.currentTimeMillis();
		FSM estimator = new FSM("testFSM/test1/obsG.fsm");
		Verifier verifier = new Verifier(estimator);
		UnfoldedVerifier unfoldedVerifier = new UnfoldedVerifier(verifier);
		final long endTime = System.currentTimeMillis();
		System.out.println("Total computation time : " + (endTime - startTime) + " ms");
		
		double currentMemory = ((double) ((double) (Runtime.getRuntime()
                .totalMemory() / 1024) / 1024))
                - ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
        System.out.println("Current Memory usage " + currentMemory);
				
		unfoldedVerifier.printUnfoldedVerifier();
		
	}

}
