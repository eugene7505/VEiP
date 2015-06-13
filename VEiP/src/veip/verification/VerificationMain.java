package veip.verification;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import veip.fsm.FSM;
import veip.fsm.HMM;
import veip.fsm.PFA;

public class VerificationMain {

	public static void main(String args[]) throws FileNotFoundException {
		final long startTime = System.currentTimeMillis();
		if (args.length == 1 && (args[0]).equals("-help")){
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
				VerificationUtilities.isCurrentStateOpaqueAnswer(fsm);
			} else if ((args[0]).equals("-p")) {
//				String pfaFile = args[1];
//				PFA pfa = new PFA(pfaFile);
//				pfa.printPFA();
				int n = 4;
				int o = 2;
				SimpleMatrix A = new SimpleMatrix(n,n);
				A.set(0, 1, 0.9);
				A.set(0, 0, 0.1);
				A.set(1, 2, 0.7);
				A.set(1, 3, 0.3);
				A.set(2, 0, 0.1);
				A.set(2, 1, 0.9);
				A.set(3, 2, 0.7);
				A.set(3, 3, 0.3);
				SimpleMatrix B = new SimpleMatrix(n, o);
				B.set(0, 0, 0.2);
				B.set(0, 1, 0.8);
				B.set(1, 0, 0.3);
				B.set(1, 1, 0.7);
				B.set(2, 0, 0.8);
				B.set(2, 1, 0.2);
				B.set(3, 0, 0.9);
				B.set(3, 1, 0.1);
				SimpleMatrix pi = new SimpleMatrix(1, n);
				pi.set(0, 0.4);
				pi.set(2, 0.42);
				pi.set(3, 0.18);
				
				ArrayList<String> observations = new ArrayList<String>();
				observations.add("a");
				observations.add("b");
				HMM hmm = new HMM(n, o, A, B, pi, observations);
				//hmm.printHMM();
				System.out.println();
			
				PFA pfa = new PFA(hmm);
				pfa.addSecretState("1");
				pfa.printPFA();				
				VerificationUtilities.isCurrentStateOpaqueAnswer(pfa);			
				
				double opacityLevel = VerificationUtilities.computeOpacityLevel(pfa);
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
