package veip.verification;

import java.io.FileNotFoundException;

import veip.fsm.FSM;

public class VerificationMain {
	public static void main(String args[]) throws FileNotFoundException {
		final long startTime = System.currentTimeMillis();
		String fsmFile;
		if (args.length == 1) {
			fsmFile = args[0];
		} else {
			System.out.println("Usage: veip.verification.VerificationMain <fsmFile>. "
					+ "Run test1 for now.");
			fsmFile = "/Users/yi-chinwu/git/VEiP/VEiP/testFSM/test1/G.fsm";
		}
		
		FSM fsm = new FSM(fsmFile);
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				fsm);
		currentStateEstimator.printEstimator();
		if (currentStateEstimator.isCurrentStateOpaque())
			System.out.println("Current state opaque? Yes");
		else {
			System.out.print("Current state opaque? No ");
			System.out.print("(Estimates ");
			currentStateEstimator.printUnsafeStates();
			System.out.println("reveal the secret)");
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
