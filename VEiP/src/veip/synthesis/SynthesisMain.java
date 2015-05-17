package veip.synthesis;

import java.io.FileNotFoundException;

import veip.fsm.FSM;
import veip.verification.CurrentStateEstimator;

public class SynthesisMain {

	public static void main(String[] args) throws FileNotFoundException {

		final long startTime = System.currentTimeMillis();
		String fsmFile;
		String iaFile;
		if (args.length == 2) {
			fsmFile = args[0];
			iaFile = args[1];
		} else {
			System.out.println("Usage: veip.synthesis.SynthesisMain <fsmFile> <IAFile>. "
					+ "Run test1 for now.");
			fsmFile = "/Users/yi-chinwu/git/VEiP/VEiP/testFSM/test1/G.fsm";
			iaFile = "/Users/yi-chinwu/git/VEiP/VEiP/testFSM/test1/IA.fsm";
		}

		FSM fsm = new FSM(fsmFile);
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				fsm);
		if (currentStateEstimator.isCurrentStateOpaque())
			System.out.println("G is current-state opaque");
		else if (!currentStateEstimator.isInitialStateSafe())
			System.out
					.println("G is unsafe at the initial state. Not i-enforceable!");
		else {

			FSM estimator = new FSM(currentStateEstimator);
			System.out.println("===== print estimator =====");
			estimator.printFSM();

			Verifier verifier = new Verifier(estimator);
			System.out.println("===== print verifier =====");
			verifier.printVerifier();
			UnfoldedVerifier unfoldedVerifier = new UnfoldedVerifier(verifier,
					false);
			System.out.println("===== print unfoldedVerifier =====");
			unfoldedVerifier.printUnfoldedVerifier();
			AIS ais = new AIS(unfoldedVerifier);
			System.out.println("===== print AIS =====");
			ais.printAIS();

			if (ais.getAisFSM().isEmptyFSM())
				System.out.println("AIS is empty. Not i-enforceable");
			else {
				InsertionAutomaton ia = SynthesisUtilities.greedySynthesis(ais);
				ia.renameStates();
				System.out.println("===== print IA =====");
				ia.printIA();
				ia.exportFSM(iaFile);
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
