package veip.synthesis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.ejml.simple.SimpleMatrix;

import veip.fsm.Event;
import veip.fsm.GameStructure;
import veip.fsm.MealyAutomaton;
import veip.fsm.State;

public final class QuantitativeSynthesisUtilities {

	private QuantitativeSynthesisUtilities() {
	}
	/*
	 * TODO: original algorithm is too slow, need "early termination" private
	 * function that performs value iteration for Mean Payoff Game, in this
	 * case, the cost structure should be integer. however, i don't know how to
	 * enforce integer type. so, accepting double for now
	 */
	public static void valueIterationMPG(GameStructure gameGraph) {
		game = gameGraph;
		n = game.getNumberOfStates();
		Wmax = (int)game.getMaxWeight();
		game.initializeOptimalActions();
		synthesizeOptimalMPGStrategy();
	}
	
	private static SimpleMatrix valueIteration(int horizon,
			SimpleMatrix weightMatrix) {
		SimpleMatrix newValueVector = new SimpleMatrix(1, n);
		// use stateValueVector = new SimpleMatrix to store the value
		for (int k = 0; k < horizon; k++) {
			System.out.println(k);
			SimpleMatrix valueVector = new SimpleMatrix(newValueVector);
			for (int i = 0; i < n; i++) {
				State s = game.getStateList().get(i);
				double newValue;
				if (s.isMarked()) {// max
					Iterator<Map.Entry<Event, ArrayList<State>>> iterator = s
							.getAllTransitions().entrySet().iterator();
					Map.Entry<Event, ArrayList<State>> entry = iterator.next();
					int j = entry.getValue().get(0).getIndex();
					// transition cost must be 0, as it's system's action
					// double transitionCost =
					// game.getMatrixEntry(entry.getKey(),i, j);
					newValue = valueVector.get(j);
					while (iterator.hasNext()) {
						entry = iterator.next();
						j = entry.getValue().get(0).getIndex();
						if (valueVector.get(j) > newValue)
							newValue = valueVector.get(j);
					}
				} else {// min
					Iterator<Map.Entry<Event, ArrayList<State>>> iterator = s
							.getAllTransitions().entrySet().iterator();
					Map.Entry<Event, ArrayList<State>> entry = iterator.next();
					int j = entry.getValue().get(0).getIndex();
					double transitionCost = game.getMatrixEntry(entry.getKey(),
							i, j);
					newValue = transitionCost + valueVector.get(j);
					while (iterator.hasNext()) {
						entry = iterator.next();
						j = entry.getValue().get(0).getIndex();
						transitionCost = game.getMatrixEntry(entry.getKey(), i,
								j);
						if (transitionCost + valueVector.get(j) < newValue)
							newValue = transitionCost + valueVector.get(j);
					}
				}
				newValueVector.set(i, newValue);
			}
		}
		return newValueVector;
	}

	// TODO: implement early termination, as in Quasy, to speed up
	private static void computeOptimalMeanCost() {
		System.out.println("computing optimal mean cost...");
		int h = n * n * n * Wmax;
		SimpleMatrix weightMatrix = new SimpleMatrix(n, n);
		for (Map.Entry<String, Event> eventEntry : game.getLocalEventMap()
				.entrySet())
			//TODO this should not be plus, two output pattern can go to the same next y state
			weightMatrix = weightMatrix.plus(game.getMatrix(eventEntry
					.getValue()));
		SimpleMatrix hTotalCost = valueIteration(h, weightMatrix);
		SimpleMatrix hMeanCost = hTotalCost.divide(h);
		// hMeanCost.print();
		double alpha = (double) 1 / (2 * n * (n - 1));
		SimpleMatrix optimalMeanCost = new SimpleMatrix(1, n);
		for (int i = 0; i < n; i++) {
			// find rational denominator
			double upperbound = hMeanCost.get(i) + alpha;
			double lowerbound = hMeanCost.get(i) - alpha;
			// System.out.println(lowerbound + " ,  "+ upperbound);
			for (int denominator = 2; denominator <= n; denominator++) {
				if ((upperbound - lowerbound) * denominator < 1 + epsilon) {
					optimalMeanCost.set(i, Math.ceil(lowerbound * denominator)
							/ denominator);
					break;
				}
			}
		}
		game.setValueVector(optimalMeanCost);
	}

	private static double recomputeOptimalMeanCost(State s) {
		int h = n * n * n * Wmax;
		SimpleMatrix weightMatrix = new SimpleMatrix(n, n);
		for (Map.Entry<String, Event> eventEntry : game.getLocalEventMap()
				.entrySet())
			weightMatrix = weightMatrix.plus(game.getMatrix(eventEntry
					.getValue()));
		SimpleMatrix hTotalCost = valueIteration(h, weightMatrix);
		SimpleMatrix hMeanCost = hTotalCost.divide(h);
		hMeanCost.print();
		double alpha = (double) 1 / (2 * n * (n - 1));
		int i = s.getIndex();
		double upperbound = hMeanCost.get(i) + alpha;
		double lowerbound = hMeanCost.get(i) - alpha;
		// System.out.println(lowerbound + " ,  "+ upperbound);
		for (int denominator = 2; denominator <= n; denominator++) {
			if ((upperbound - lowerbound) * denominator < 1 + epsilon) {
				return Math.ceil(lowerbound * denominator) / denominator;
			}
		}
		System.out.println("no valid meal cost. WRONG!");
		return -1;
	}

	/*
	 * this function computes the optimal action for state and return the
	 * optimal next state
	 */
	private static State computeOptimalAction(State state) {
		System.out.println("computing optimal action for state "
				+ state.getName());
		if (state.getAllTransitions().size() == 0)
			return null;
		while (state.getNumberOfTransitions() > 1) {
			State scopy = new State(state.getName(), state.isInitial(),
					state.isMarked()); // copy of the original state s, stored
			scopy.setIndex(state.getIndex()); // for potential recovery
			int count = 0;
			int m = state.getNumberOfTransitions();
			Iterator<Map.Entry<Event, ArrayList<State>>> iterator = state
					.getAllTransitions().entrySet().iterator();
			while (iterator.hasNext() && count < m / 2) {
				Map.Entry<Event, ArrayList<State>> entry = iterator.next();
				scopy.createTransition(entry.getKey(), entry.getValue().get(0));
				iterator.remove();
				count++;
			}
			// value changed
			state.updateNumberOfTransitions();
			if (recomputeOptimalMeanCost(state) > game.getValue(state)
					+ epsilon
					|| recomputeOptimalMeanCost(state) < game.getValue(state)
							- epsilon) {
				state = scopy;
			}
			// state.updateNumberOfTransitions();
		}
		Map.Entry<Event, ArrayList<State>> entry = state.getAllTransitions()
				.entrySet().iterator().next();
		game.setOptimalAction(state.getIndex(), entry.getKey());
		State nextState = entry.getValue().get(0);
		return nextState;
	}

	private static void synthesizeOptimalMPGStrategy() {
		game.clearFlags();
		Stack<State> stack = new Stack<State>();
		stack.push(game.getInitialStateList().get(0));
		while (!stack.isEmpty()) {
			State state = stack.pop();
			if (state.flagged)
				continue;
			if (state.isMarked()) {
				for (Map.Entry<Event, ArrayList<State>> transitionEntry : state
						.getAllTransitions().entrySet()) {
					stack.push(transitionEntry.getValue().get(0));
				}
			} else {
				State nextState = computeOptimalAction(state);
				stack.push(nextState);
			}
			state.flagged = true;
		}
	}

	private static double epsilon = 5.96e-8;
	private static int n;
	private static GameStructure game;
	private static int Wmax = 0;
}
