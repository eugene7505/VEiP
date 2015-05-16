package veip.enforcement;

import java.io.FileNotFoundException;

import veip.fsm.FSM;

public class AIS {

	
	public static void main(String[] args) throws FileNotFoundException {
		
		final long startTime = System.currentTimeMillis();
		
		FSM estimator = new FSM("testFSM/test0/obsG.fsm");
		Verifier verifier = new Verifier(estimator);
		UnfoldedVerifier unfoldedVerifier = new UnfoldedVerifier(verifier);
		
		System.out.println("===== print Vu =====");		
		FSM vufsm = unfoldedVerifier.getUnfoldedVerifierFSM();
		vufsm.printFSM();
		
		FSM result = SupconUtilities.stateBasedSupconNonblocking(vufsm);
		System.out.println("===== print supcon =====");
		result.printFSM();

		final long endTime = System.currentTimeMillis();
		System.out.println("Total computation time : " + (endTime - startTime) + " ms");
		
		double currentMemory = ((double) ((double) (Runtime.getRuntime()
                .totalMemory() / 1024) / 1024))
                - ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
        System.out.println("Current Memory usage " + currentMemory);
	
	}
	
}
