package veip.verification;

import java.io.FileNotFoundException;
import veip.fsm.FSM;
import veip.fsm.PFA;

public class VerificationMain {

	public static void main(String args[]) throws FileNotFoundException {
		final long startTime = System.currentTimeMillis();
		if (args.length == 1 && (args[0]).equals("-help")) {
			System.out
					.println("Usage: veip.verification.VerificationMain [Options] <fsmFile> <mcFile>");
			System.out.println("Options:");
			System.out.println("-help Instructions");
			System.out
					.println("-f Opacity verification for finite state automata");
			System.out
					.println("-p Opacity verification for probabilistic finite state automata");
			return;
		}
		if (args.length == 2) {
			if ((args[0]).equals("-f")) {
				String fsmFile = args[1];
				FSM fsm = new FSM(fsmFile);
				VerificationUtilities.isCurrentStateOpaque(fsm, true);
			} else if ((args[0]).equals("-p")) {
				String pfaFile = args[1];
				PFA pfa = new PFA(pfaFile);
				VerificationUtilities.isCurrentStateOpaque(pfa, true);

				double opacityLevel = VerificationUtilities
						.computeOpacityLevel(pfa);
				System.out.println("Opacity level: " + opacityLevel);
			}
		} else {
			System.out
					.println("veip.verification.VerificationMain -help for instructions");
			return;
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
