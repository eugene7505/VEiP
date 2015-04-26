package veip.verification;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import veip.fsm.FSM;
import veip.fsm.State;
import veip.fsm.FSM.Event;

public class CurrentStateEstimator {

	FSM fsm;
	int numberOfStates;
	EstimatorState initialEstimate; 
	HashMap<String, EstimatorState> estimatorStateMap;
	HashMap<String, Event> localEventMap;

	
	public CurrentStateEstimator(FSM fsm){
		this.fsm = fsm;
		buildCurrentStateEstimator();		
	}
	
	//TODO: finish building the cse. Currently the function only builds the initial state
	private void buildCurrentStateEstimator(){
		initialEstimate = new EstimatorState(fsm.getInitialStateList());
		System.out.println(initialEstimate.getName());
	}
	

	public static void main (String args[]) throws FileNotFoundException{
		String file = "testFSM/G.fsm";
		FSM fsm = new FSM(file);
		fsm.printFSM();
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(fsm);
		
	}
}
