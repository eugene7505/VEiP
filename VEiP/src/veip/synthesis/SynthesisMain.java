package veip.synthesis;

import java.io.FileNotFoundException;

import veip.fsm.FSM;
import veip.verification.CurrentStateEstimator;

public class SynthesisMain {

	public static final int GREEDY = 0;
	public static final int OPTIMAL = 1;
	public static final int PP = 2;
	private static int option;

	public static void main(String[] args) throws FileNotFoundException {

		final long startTime = System.currentTimeMillis();
		String fsmFile;
		String iaFile;
		if (args.length == 3) {
			fsmFile = args[1];
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
			default:
				System.out.println(args[0]
						+ " is not a valid option. Terminate.");
				return;
			}
		} else {
			System.out
					.println("Usage: veip.synthesis.SynthesisMain [Options] <fsmFile> <IAFile>.");

			fsmFile = "/Users/yi-chinwu/git/VEiP/VEiP/testFSM/test2/G.fsm";
			iaFile = "/Users/yi-chinwu/git/VEiP/VEiP/testFSM/test2/IA.fsm";
			option = GREEDY;

			System.out.println("Run example:\nveip.synthesis.SynthesisMain -g "
					+ fsmFile + " " + iaFile + ".");
			System.out.println("Options:");
			System.out.println("-g  Synthesis with the greedy algorithm.");
			System.out
					.println("-o  Synthesis with the optimization algorithm. Each inserted event costs 1.");
			System.out.println("-p  Synthesis with the privalic algorithm. \n");
		}

		FSM fsm = new FSM(fsmFile);
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				fsm, true);
		if (currentStateEstimator.isCurrentStateOpaque())
			System.out.println("G is current-state opaque");
		else if (!currentStateEstimator.isInitialStateSafe())
			System.out
					.println("G is unsafe at the initial state. Not i-enforceable!");
		else {

			FSM estimator = new FSM(currentStateEstimator);
//			System.out.println("===== print estimator =====");
//			estimator.printFSM();

			Verifier verifier = new Verifier(estimator);
//			System.out.println("===== print verifier =====");
//			verifier.printVerifier();

			UnfoldedVerifier unfoldedVerifier = new UnfoldedVerifier(verifier,
					false);
//			System.out.println("===== print unfoldedVerifier =====");
//			unfoldedVerifier.printUnfoldedVerifier();

			AIS ais = new AIS(unfoldedVerifier);
//			System.out.println("===== print AIS =====");
//			ais.printAIS();

			if (ais.getAisFSM().isEmptyFSM())
				System.out.println("AIS is empty. Not i-enforceable");
			else {
				InsertionAutomaton ia = new InsertionAutomaton();
				switch (option) {
				case GREEDY:
					ia = SynthesisUtilities.greedySynthesis(ais);
					break;
				case OPTIMAL:
					ia = SynthesisUtilities.optimalSynthesis(ais);
					System.out.println("oops. Optimal option has not been implemented yet!");
					break;
				case PP:
					ia = SynthesisUtilities.ppSynthesis(ais);
					System.out.println("oops. Privalic option has not been implemented yet!");
					break;
				}
				if (ia.getNumberOfStates() != 0) {
					ia.renameStates();
					System.out.println("===== print IA =====");
					ia.printIA();
					ia.exportFSM(iaFile);
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
