package veip.verification;
import java.util.ArrayList;
import java.util.jar.Attributes.Name;

import veip.fsm.*;

public class EstimatorState extends State{
	
	ArrayList<State> stateEstimate;
	
	public EstimatorState (ArrayList<State> stateEstimate) {
		super();
		this.stateEstimate = stateEstimate;
		this.name = generateStateName(stateEstimate);
		this.nonsecret = isSecretOpaque(stateEstimate);
	}
	
	public boolean isOpaque (){
		return nonsecret;
	}
	
	private boolean isSecretOpaque (ArrayList<State> stateEstimate)
	{
		for (int i = 0; i < stateEstimate.size(); i++) {
			if ((stateEstimate.get(i)).isNonsecret())
				return true;
		}
		return false;
	}
	
	private String generateStateName(ArrayList<State> stateEstimate){
		String name = new String("");
		for (int i = 0; i < stateEstimate.size(); i++) {
			name += stateEstimate.get(i).getName();
			if (i != stateEstimate.size()-1)
				name += ",";
		}
		return name;
	}
}
