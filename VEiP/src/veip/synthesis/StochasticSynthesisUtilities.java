package veip.synthesis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.ejml.simple.SimpleMatrix;

import veip.fsm.FSM;
import veip.fsm.GameStructure;
import veip.fsm.PFA;
import veip.fsm.State;
import veip.fsm.FSM.Event;
import veip.verification.CurrentStateEstimator;
import veip.verification.VerificationUtilities;

public class StochasticSynthesisUtilities {

	public static class StateDistributionPair {
		public State gameState;
		public SimpleMatrix distribution;

		private StateDistributionPair(State gameState, SimpleMatrix distribution) {
			this.gameState = gameState;
			this.distribution = distribution;
		}
	}

	public static class StatePair {
		public State first;
		public State second;

		private StatePair(State first, State second) {
			this.first = first;
			this.second = second;
		}
	}

	public static class IAValuePair {
		InsertionAutomaton ia;
		Double value;

		public IAValuePair(InsertionAutomaton ia, Double value) {
			this.ia = ia;
			this.value = value;
		}
	}

	public static GameStructure constructMDP(FSM vu, PFA pfa) {

		GameStructure mdp = new GameStructure();
		// compute event matrices
		int n = pfa.getNumberOfStates();
		SimpleMatrix uoEventMatrix = new SimpleMatrix(n, n);
		for (Map.Entry<String, Event> eventEntry : pfa.getLocalEventMap()
				.entrySet()) {
			Event event = eventEntry.getValue();
			if (!event.isObservable())
				uoEventMatrix.plus(pfa.getEventMatrix(event));
		}
		// A_eo = A_uo*Ae
		SimpleMatrix uoReachMatrix = (SimpleMatrix.identity(n)
				.minus(uoEventMatrix)).invert();
		Map<Event, SimpleMatrix> obsEventMatrixMap = new HashMap<Event, SimpleMatrix>();
		for (Map.Entry<String, Event> eventEntry : pfa.getLocalEventMap()
				.entrySet()) {
			Event event = eventEntry.getValue();
			if (event.isObservable()) {
				obsEventMatrixMap.put(event,
						uoReachMatrix.mult((pfa.getEventMatrix(event))));
			}
		}

		// construct mdp
		HashMap<Event, HashMap<StatePair, Double>> transitionProbabilityMap = new HashMap<FSM.Event, HashMap<StatePair, Double>>();
		HashMap<Event, HashMap<StatePair, Double>> costMatrixMap = new HashMap<FSM.Event, HashMap<StatePair, Double>>();
		HashMap<State, StateDistributionPair> stateDistributionMap = new HashMap<State, StochasticSynthesisUtilities.StateDistributionPair>();

		StateDistributionPair initialPair = new StateDistributionPair(vu
				.getInitialStateList().get(0), pfa.getInitialDistribution());
		State initialState = mdp.addState(generateStateName(initialPair), true,
				initialPair.gameState.isMarked());
		stateDistributionMap.put(initialState, initialPair);

		Stack<State> stack = new Stack<State>();
		State reveal = mdp.addState("reveal");
		Event noInsertion = mdp.addEvent("-");
		costMatrixMap.put(noInsertion,
				new HashMap<StochasticSynthesisUtilities.StatePair, Double>());

		stack.push(initialState);
		while (!stack.isEmpty()) {
			State state = stack.pop();
			if (state.flagged)
				continue;
			else {
				// System.out.println(state.getName());
				State gameState = stateDistributionMap.get(state).gameState;
				SimpleMatrix distribution = stateDistributionMap.get(state).distribution;
				// expandYState
				if (state.isMarked()) {
					for (Map.Entry<Event, ArrayList<State>> transitionEntry : gameState
							.getAllTransitions().entrySet()) {
						Event event = mdp.addEvent(transitionEntry.getKey()
								.getName());
						SimpleMatrix eMatrix = obsEventMatrixMap.get(event);
						StateDistributionPair nextPair = new StateDistributionPair(
								transitionEntry.getValue().get(0),
								nextDistribution(distribution, eMatrix));
						State nextZState = mdp.addState(
								generateStateName(nextPair), false, false);
						stateDistributionMap.put(nextZState, nextPair);
						stack.push(nextZState);
						state.addTransition(event, nextZState);
						if (!transitionProbabilityMap.containsKey(event))
							transitionProbabilityMap
									.put(event,
											new HashMap<StochasticSynthesisUtilities.StatePair, Double>());
						transitionProbabilityMap.get(event).put(
								new StatePair(state, nextZState),
								transitionProbability(distribution, eMatrix));
					}
					state.updateNumberOfTransitions();
					state.flagged = true;
				}
				// expandZState
				else {
					if (gameState.getAllTransitions().size() == 0) {
						state.addTransition(noInsertion, reveal);
						costMatrixMap.get(noInsertion).put(
								new StatePair(state, reveal), new Double(1));
					} else {
						for (Map.Entry<Event, ArrayList<State>> transitionEntry : gameState
								.getAllTransitions().entrySet()) {
							Event event = mdp.addEvent(transitionEntry.getKey()
									.getName());
							StateDistributionPair nextPair = new StateDistributionPair(
									transitionEntry.getValue().get(0),
									distribution);
							State nextYState = mdp
									.addState(generateStateName(nextPair));
							stateDistributionMap.put(nextYState, nextPair);
							stack.push(nextYState);
							state.addTransition(event, nextYState);
							if (!costMatrixMap.containsKey(event))
								costMatrixMap
										.put(event,
												new HashMap<StochasticSynthesisUtilities.StatePair, Double>());
							costMatrixMap.get(event).put(
									new StatePair(state, nextYState),
									new Double(0));
						}
					}
					state.updateNumberOfTransitions();
					state.flagged = true;
				}
			}
		}
		// printTransitionProbabilityMap(transitionProbabilityMap);
		mdp.updateNumberOfInitialStates();
		mdp.updateNumberOfStates();

		// construct transition matrix and reward matrix
		mdp.constructTransitionProbabilityMatrixMap(transitionProbabilityMap);
		mdp.constructRewardMatrixMap(costMatrixMap);
		mdp.updateNumberOfStates();
		return mdp;
	}

	private static void printTransitionProbabilityMap(
			HashMap<Event, HashMap<StatePair, Double>> map) {
		for (Map.Entry<Event, HashMap<StatePair, Double>> mapEntry : map
				.entrySet()) {
			Event event = mapEntry.getKey();
			System.out.println(event.getName());
			for (Map.Entry<StatePair, Double> innerEntry : mapEntry.getValue()
					.entrySet()) {
				StatePair pair = innerEntry.getKey();
				System.out.println(pair.first.getName() + "->"
						+ pair.second.getName() + ":" + innerEntry.getValue());
			}
		}
	}

	private static String generateStateName(StateDistributionPair pair) {
		String name = pair.gameState.getName();
		for (int i = 0; i < pair.distribution.numCols(); i++) {
			name += " ";
			name += String.valueOf(pair.distribution.get(i));
		}
		return name;
	}

	private static SimpleMatrix nextDistribution(SimpleMatrix distribution,
			SimpleMatrix eoMatrix) {
		SimpleMatrix nextDistribution = unnormalizedNextDistribution(
				distribution, eoMatrix);
		return nextDistribution.divide(nextDistribution.elementSum());
	}

	private static SimpleMatrix unnormalizedNextDistribution(
			SimpleMatrix distribution, SimpleMatrix eoMatrix) {
		SimpleMatrix nextDistribution = distribution.mult(eoMatrix);
		return nextDistribution;
	}

	private static double transitionProbability(SimpleMatrix distribution,
			SimpleMatrix eoMatrix) {
		return unnormalizedNextDistribution(distribution, eoMatrix)
				.elementSum();
	}

	public static IAValuePair optimalOpacityLevelSynthesis(FSM vu, PFA pfa) {
		gameGraph = constructMDP(vu, pfa);
		// gameGraph.printFSM();
		GameUtilities.valueIteration_MinAverageCost(gameGraph);
		InsertionAutomaton ia = synthesizeOptimalOpacityLevelIA();
		IAValuePair pair = new IAValuePair(ia, gameGraph.getValue(gameGraph
				.getInitialStateList().get(0)));
		return pair;
	}

	// assume gameGraph has optimal actions and values
	private static InsertionAutomaton synthesizeOptimalOpacityLevelIA() {
		InsertionAutomaton ia = new InsertionAutomaton();
		Stack<State> stack = new Stack<State>();
		stack.push(gameGraph.getInitialStateList().get(0));
		gameGraph.clearFlags();
		while (!stack.isEmpty()) {
			State ystate = stack.pop();
			if (ystate.flagged)
				continue;
			State iaState = ia.addState(ystate.getName(), ystate.isInitial(),
					true);
			for (Map.Entry<Event, ArrayList<State>> transitionEntry : ystate
					.getAllTransitions().entrySet()) {
				Event event = transitionEntry.getKey();
				State zstate = transitionEntry.getValue().get(0);
				Event optimalInsertion = gameGraph.getOptimalAction(zstate);
				State nextYState = zstate.getNextStateList(optimalInsertion)
						.get(0);
				State nextIaState = ia.addState(nextYState.getName(),
						nextYState.isInitial(), true);
				iaState.addTransition(event, nextIaState);
				Event outputEvent;
				if (optimalInsertion.getName().equals("-"))
					outputEvent = optimalInsertion;
				else
					outputEvent = new Event(optimalInsertion.getName()
							+ event.getName());
				ia.addTransitionOuput(iaState, event, outputEvent);
				stack.push(nextYState);
			}
			ystate.flagged = true;
			iaState.updateNumberOfTransitions();
		}
		ia.updateNumberOfInitialStates();
		ia.updateNumberOfStates();
		return ia;
	}

	public static GameStructure gameGraph;

	public static void main(String[] args) throws FileNotFoundException {
		String automatonFile = "/Users/yi-chinwu/git/VEiP/VEiP/testFSM/stochastic/H.pfa";
		PFA pfa = new PFA(automatonFile);
		double opacityLevel = VerificationUtilities.computeOpacityLevel(pfa);
		System.out.println("opacity level = " + opacityLevel);
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				pfa, false);
		FSM estimator = new FSM(currentStateEstimator);
		Verifier verifier = new Verifier(estimator);
		UnfoldedVerifier unfoldedVerifier = new UnfoldedVerifier(verifier, true);
		FSM vu = unfoldedVerifier.getUnfoldedVerifierFSM();
		vu.exportFSM("/Users/yi-chinwu/git/VEiP/VEiP/testFSM/stochastic/vu.fsm");

		// check optimal synthesis, wrong IA
		IAValuePair iaValuePair = optimalOpacityLevelSynthesis(vu, pfa);
		iaValuePair.ia.renameStates();
		iaValuePair.ia.printIA();
	}
}

// given vu and pfa, compute the optimal strategy.
