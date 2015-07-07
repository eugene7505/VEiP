package veip.synthesis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.ejml.simple.SimpleMatrix;

import veip.fsm.GameStructure;
import veip.fsm.FSM.Event;
import veip.fsm.State;

public final class GameUtilities {

	private GameUtilities() {
	}

	public static void valueIteration_MinAverageCost(GameStructure gameGraph) {
		int n = gameGraph.getNumberOfStates();
		gameGraph.initializeOptimalActions(n);
		SimpleMatrix stateValueVector;
		SimpleMatrix newStateValueVector = new SimpleMatrix(n, 1);
		SimpleMatrix transitionProbabilityMatrix = new SimpleMatrix(n, n);

		for (Map.Entry<Event, SimpleMatrix> entry : gameGraph
				.getYtoZMatrixMap().entrySet()) {
			SimpleMatrix matrix = entry.getValue();
			transitionProbabilityMatrix = transitionProbabilityMatrix
					.plus(matrix);
		}
		ArrayList<State> stateList = gameGraph.getStateList();
		do {
			stateValueVector = new SimpleMatrix(newStateValueVector);
			for (int i = 0; i < stateList.size(); i++) {
				double newValue = 0;
				Event optimalEvent = null;
				if (stateList.get(i).isMarked()) {// Y state, take average
					for (int j = 0; j < stateList.size(); j++) {
						newValue += transitionProbabilityMatrix.get(i, j)
								* stateValueVector.get(j);
					}
				} else {// Z state
					Iterator<Map.Entry<Event, ArrayList<State>>> iterator = stateList
							.get(i).getAllTransitions().entrySet().iterator();
					Map.Entry<Event, ArrayList<State>> entry = iterator.next();
					int j = entry.getValue().get(0).getIndex();
					Event event = entry.getKey();
					double transitionReward = gameGraph.getZtoYMatrix(event, i,
							j);
					newValue = transitionReward + stateValueVector.get(j);
					optimalEvent = event;
					while (iterator.hasNext()) {
						entry = iterator.next();
						j = entry.getValue().get(0).getIndex();
						event = entry.getKey();
						transitionReward = gameGraph.getZtoYMatrix(event, i, j);
						if (transitionReward + stateValueVector.get(j) > newValue) {
							newValue = transitionReward
									+ stateValueVector.get(j);
							optimalEvent = event;
						}
					}
					gameGraph.setOptimalAction(i, optimalEvent);
				}
				newStateValueVector.set(i, newValue);
			}

		} while (stateValueVector.minus(newStateValueVector).elementMaxAbs() > epsilon);
		gameGraph.setValues(stateValueVector);
	}

	private static double epsilon = 5.96e-8;
}
