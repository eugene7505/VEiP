package veip.synthesis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.ejml.simple.SimpleMatrix;

import veip.fsm.Event;
import veip.fsm.FSM;
import veip.fsm.GameStructure;
import veip.fsm.InsertionAutomaton;
import veip.fsm.PFA;
import veip.fsm.State;
import veip.fsm.StatePair;
import veip.verification.CurrentStateEstimator;
import veip.verification.VerificationUtilities;

public class StochasticSynthesisUtilities {

	public static class StateDistributionPair {
		public State state;
		public SimpleMatrix distribution;

		private StateDistributionPair(State state, SimpleMatrix distribution) {
			this.state = state;
			this.distribution = distribution;
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
		mdp.initiateEventSet(pfa.getLocalEventMap());
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
		HashMap<Event, HashMap<StatePair, Double>> transitionProbabilityMap = new HashMap<Event, HashMap<StatePair, Double>>();
		HashMap<Event, HashMap<StatePair, Double>> costMatrixMap = new HashMap<Event, HashMap<StatePair, Double>>();
		// mapping from mdp game position to (state,distribution) pair
		HashMap<State, StateDistributionPair> stateDistributionMap = new HashMap<State, StochasticSynthesisUtilities.StateDistributionPair>();

		StateDistributionPair initialPair = new StateDistributionPair(vu
				.getInitialStateList().get(0), pfa.getInitialDistribution());
		State initialPosition = mdp.createState(generateStateName(initialPair),
				true, initialPair.state.isMarked());
		stateDistributionMap.put(initialPosition, initialPair);

		Stack<State> stack = new Stack<State>();
		State bottom = mdp.createState("bot");
		Event otherInsertions = mdp.addEvent("others");
		costMatrixMap.put(otherInsertions, new HashMap<StatePair, Double>());

		stack.push(initialPosition);
		while (!stack.isEmpty()) {
			State position = stack.pop();
			if (position.flagged)
				continue;
			else {
				// System.out.println(state.getName());
				State vuState = stateDistributionMap.get(position).state;
				SimpleMatrix distribution = stateDistributionMap.get(position).distribution;
				// expandS1Position
				if (position.isMarked()) {
					for (Map.Entry<Event, ArrayList<State>> transitionEntry : vuState
							.getAllTransitions().entrySet()) {
						Event event = mdp.addEvent(transitionEntry.getKey()
								.getName());
						SimpleMatrix eMatrix = obsEventMatrixMap.get(event);
						StateDistributionPair nextPair = new StateDistributionPair(
								transitionEntry.getValue().get(0),
								nextDistribution(distribution, eMatrix));
						State nextS2Position = mdp.createState(
								generateStateName(nextPair), false, false);
						stateDistributionMap.put(nextS2Position, nextPair);
						stack.push(nextS2Position);
						position.createTransition(event, nextS2Position);
						if (!transitionProbabilityMap.containsKey(event))
							transitionProbabilityMap.put(event,
									new HashMap<StatePair, Double>());
						transitionProbabilityMap.get(event).put(
								new StatePair(position, nextS2Position),
								transitionProbability(distribution, eMatrix));
					}
					position.updateNumberOfTransitions();
					position.flagged = true;
				}
				// expandS2Position
				else {
					for (Map.Entry<Event, ArrayList<State>> transitionEntry : vuState
							.getAllTransitions().entrySet()) {
						Event event = mdp.addEvent(transitionEntry.getKey()
								.getName());
						StateDistributionPair nextPair = new StateDistributionPair(
								transitionEntry.getValue().get(0), distribution);
						State nextS1Position = mdp.createState(
								generateStateName(nextPair), false, true);
						stateDistributionMap.put(nextS1Position, nextPair);
						stack.push(nextS1Position);
						position.createTransition(event, nextS1Position);
						if (!costMatrixMap.containsKey(event))
							costMatrixMap.put(event,
									new HashMap<StatePair, Double>());
						costMatrixMap.get(event).put(
								new StatePair(position, nextS1Position),
								new Double(0));
					}

					position.createTransition(otherInsertions, bottom);
					costMatrixMap.get(otherInsertions).put(
							new StatePair(position, bottom), new Double(1));

					position.updateNumberOfTransitions();
					position.flagged = true;
				}
			}
		}
		// printTransitionProbabilityMap(transitionProbabilityMap);
		mdp.updateNumberOfInitialStates();
		mdp.updateNumberOfStates();

		// construct transition matrix and reward matrix
		mdp.constructYtoZWeightMatrixMap(transitionProbabilityMap);
		mdp.constructZtoYWeightMatrixMap(costMatrixMap);
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
		String name = pair.state.getName();
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
		valueIteration_MinAverageCost(gameGraph);
		InsertionAutomaton ia = synthesizeOptimalOpacityLevelIA();
		IAValuePair pair = new IAValuePair(ia, gameGraph.getValue(gameGraph
				.getInitialStateList().get(0)));
		return pair;
	}

	public static void valueIteration_MinAverageCost(GameStructure gameGraph) {
		int n = gameGraph.getNumberOfStates();
		gameGraph.initializeOptimalActions();
		SimpleMatrix stateValueVector;
		SimpleMatrix newStateValueVector = new SimpleMatrix(n, 1);
		SimpleMatrix transitionProbabilityMatrix = new SimpleMatrix(n, n);

		for (Map.Entry<Event, SimpleMatrix> entry : gameGraph.getMatrixMap()
				.entrySet()) {
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
				} else {// Z state, minimize cost to go
					Iterator<Map.Entry<Event, ArrayList<State>>> iterator = stateList
							.get(i).getAllTransitions().entrySet().iterator();
					Map.Entry<Event, ArrayList<State>> entry = iterator.next();
					int j = entry.getValue().get(0).getIndex();
					Event event = entry.getKey();
					double transitionCost = gameGraph.getMatrixEntry(event, i,
							j);
					newValue = transitionCost + stateValueVector.get(j);
					optimalEvent = event;
					while (iterator.hasNext()) {
						entry = iterator.next();
						j = entry.getValue().get(0).getIndex();
						event = entry.getKey();
						transitionCost = gameGraph.getMatrixEntry(event, i, j);
						if (transitionCost + stateValueVector.get(j) < newValue) {
							newValue = transitionCost + stateValueVector.get(j);
							optimalEvent = event;
						}
					}
					gameGraph.setOptimalAction(i, optimalEvent);
				}
				newStateValueVector.set(i, newValue);
			}

		} while (stateValueVector.minus(newStateValueVector).elementMaxAbs() > epsilon);
		gameGraph.setValueVector(stateValueVector);
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
			else {
				State iaState = ia.createState(ystate.getName(),
						ystate.isInitial(), true);
				if (ystate.getName().equals("bot")) {
					for (Event e: gameGraph.getEventSet()){
						iaState.createTransition(e, iaState);
						ia.addTransitionOuput(iaState, e, e);
					}
						
				} else {
					for (Map.Entry<Event, ArrayList<State>> transitionEntry : ystate
							.getAllTransitions().entrySet()) {
						Event event = transitionEntry.getKey();
						State zstate = transitionEntry.getValue().get(0);
						Event optimalInsertion = gameGraph
								.getOptimalAction(zstate);
						State nextYState = zstate.getNextStateList(
								optimalInsertion).get(0);
						State nextIaState = ia.createState(
								nextYState.getName(), nextYState.isInitial(),
								true);
						iaState.createTransition(event, nextIaState);
						Event outputEvent;
						if (optimalInsertion.getName().equals("others"))
							outputEvent = event;
						else
							outputEvent = new Event(optimalInsertion.getName()
									+ event.getName());
						ia.addTransitionOuput(iaState, event, outputEvent);
						stack.push(nextYState);
					}
				}
				ystate.flagged = true;
				iaState.updateNumberOfTransitions();
			}
		}
		ia.updateNumberOfInitialStates();
		ia.updateNumberOfStates();
		return ia;
	}

	public static GameStructure gameGraph;
	private static double epsilon = 5.96e-8;

	public static void main(String[] args) throws FileNotFoundException {
		String automatonFile = "/Users/yi-chinwu/git/VEiP/VEiP/testFSM/acc2015RunningExample/H.pfa";
		PFA pfa = new PFA(automatonFile);
		VerificationUtilities.isCurrentStateOpaque(pfa, true);
		double opacityLevel = VerificationUtilities.computeOpacityLevel(pfa);
		System.out.println("opacity level = " + opacityLevel);
		System.out
				.println("Synthesizing an f_I that maximizes the opacity level:");
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				pfa);
		FSM estimator = new FSM(currentStateEstimator);
		Verifier verifier = new Verifier(estimator);
		UnfoldedVerifier unfoldedVerifier = new UnfoldedVerifier(verifier, true);
		FSM vu = unfoldedVerifier.getUnfoldedVerifierFSM();

		IAValuePair iaValuePair = optimalOpacityLevelSynthesis(vu, pfa);
		iaValuePair.ia.printIA();
		iaValuePair.ia
				.exportFSM(
						"/Users/yi-chinwu/git/VEiP/VEiP/testFSM/acc2015RunningExample/IA.fsm",
						true);
	}
}

