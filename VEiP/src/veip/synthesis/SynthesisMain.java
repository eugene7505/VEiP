package veip.synthesis;

import java.io.FileNotFoundException;

import veip.fsm.FSM;
import veip.fsm.PFA;
import veip.synthesis.StochasticSynthesisUtilities.IAValuePair;
import veip.verification.CurrentStateEstimator;
import veip.verification.VerificationUtilities;

public class SynthesisMain {

	public static final int GREEDY = 0;
	public static final int OPTIMAL = 1;
	public static final int PP = 2;
	public static final int STOCHASTIC = 3;
	private static int option;

	public static void main(String[] args) throws FileNotFoundException {

		// parse the input
		final long startTime = System.currentTimeMillis();
		String automatonFile;
		String iaFile;
		if (args.length == 3) {
			automatonFile = args[1];
			iaFile = args[2];
			switch (args[0]) {
			case "-g":
				option = GREEDY;
				break;
			case "-o":
				option = OPTIMAL;
				break;
			case "-p":
				option = PP;
				break;
			case "-s":
				option = STOCHASTIC;
				break;
			default:
				System.out.println(args[0]
						+ " is not a valid option. Terminate.");
				return;
			}
		} else {
			System.out
					.println("Usage: veip.synthesis.SynthesisMain [Options] <fsmFile/pfaFile> <IAFile>.");

			automatonFile = "/Users/yi-chinwu/git/VEiP/VEiP/testFSM/test2/G.fsm";
			iaFile = "/Users/yi-chinwu/git/VEiP/VEiP/testFSM/test2/IA.fsm";
			option = GREEDY;

			System.out.println("Run example:\nveip.synthesis.SynthesisMain -g "
					+ automatonFile + " " + iaFile + ".");
			System.out.println("Options:");
			System.out.println("-g  Synthesis with the greedy algorithm.");
			System.out
					.println("-o  Synthesis with the optimization algorithm. Each inserted event costs 1.");
			System.out.println("-p  Synthesis with the privalic algorithm.");
			System.out
					.println("-s  Synthesis of an insertion function with the optimal opacity level. \n");
		}

		// routine
		FSM fsm;
		CurrentStateEstimator currentStateEstimator;
		Verifier verifier;
		UnfoldedVerifier unfoldedVerifier;
		AIS ais;
		if (option == STOCHASTIC) {
			PFA pfa = new PFA(automatonFile);
			fsm = (FSM) pfa;
			currentStateEstimator = new CurrentStateEstimator(fsm, false);
			if (currentStateEstimator.isCurrentStateOpaque())
				System.out.println("G is current-state opaque");
			else if (!currentStateEstimator.isInitialStateSafe())
				System.out
						.println("G is unsafe at the initial state. Opacity cannot be enforced using insertion functions!");
			else {
				FSM estimator = new FSM(currentStateEstimator);
				double opacityLevel = VerificationUtilities.computeOpacityLevel(pfa);
				System.out.println("opacity level = " + opacityLevel + ". Synthesize an insertion function that maximizes the opacity level:");
				verifier = new Verifier(estimator);
				unfoldedVerifier = new UnfoldedVerifier(verifier, true);
				IAValuePair iaValuePair =  StochasticSynthesisUtilities.optimalOpacityLevelSynthesis(
						unfoldedVerifier.getUnfoldedVerifierFSM(), pfa);
				iaValuePair.ia.renameStates();
				iaValuePair.ia.printIA();
				iaValuePair.ia.exportFSM(iaFile);
				System.out.println("optimal opacity level = " + (1 + iaValuePair.value));
			}
		}

		else {
			fsm = new FSM(automatonFile);
			currentStateEstimator = new CurrentStateEstimator(fsm, false);
			if (currentStateEstimator.isCurrentStateOpaque())
				System.out.println("G is current-state opaque");
			else if (!currentStateEstimator.isInitialStateSafe())
				System.out
						.println("G is unsafe at the initial state. Opacity cannot be enforced using insertion functions!");
			else {
				InsertionAutomaton ia;
				FSM estimator = new FSM(currentStateEstimator);
				verifier = new Verifier(estimator);
				unfoldedVerifier = new UnfoldedVerifier(verifier, true);
				ais = new AIS(unfoldedVerifier);

				if (ais.getAisFSM().isEmptyFSM())
					System.out.println("AIS is empty. Not i-enforceable");
				else {
					switch (option) {
					case GREEDY:
						ia = SynthesisUtilities.greedySynthesis(ais);
						break;
					// TODO
					case OPTIMAL:
						ia = SynthesisUtilities.optimalSynthesis(ais);
						System.out
								.println("oops. Optimal option has not been implemented yet!");
						break;
					// TODO
					case PP:
						ia = SynthesisUtilities.ppSynthesis(ais);
						System.out
								.println("oops. Privalic option has not been implemented yet!");
						break;
					}
				}
			}
		}

		final long endTime = System.currentTimeMillis();
		System.out.println("Total computation time : " + (endTime - startTime)
				+ " ms");

		double currentMemory = ((double) ((double) (Runtime.getRuntime()
				.totalMemory() / 1024) / 1024))
				- ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
		System.out.println("Memory usage " + currentMemory + " MB");
	}
}
