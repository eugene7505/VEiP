package veip.synthesis;

import veip.fsm.FSM;

public class AIS {
	private FSM aisFsm;
	private FSM vuFSM;
	
	public AIS(UnfoldedVerifier unfoldedVerifier){
		vuFSM = unfoldedVerifier.getUnfoldedVerifierFSM();
		aisFsm = SupconUtilities.stateBasedSupconNonblocking(vuFSM);
	}
	public FSM getAisFSM(){
		return aisFsm; 
	}
	public void printAIS(){
		aisFsm.printFSM();
	}
	/*
	public static void main(String[] args) throws FileNotFoundException {
		
		final long startTime = System.currentTimeMillis();
		
		FSM estimator = new FSM("testFSM/test2/obsG.fsm");
		Verifier verifier = new Verifier(estimator);
		UnfoldedVerifier unfoldedVerifier = new UnfoldedVerifier(verifier, false);
		AIS ais = new AIS(unfoldedVerifier);
		
		System.out.println("===== print Vu =====");		
		FSM vufsm = unfoldedVerifier.getUnfoldedVerifierFSM();
		vufsm.printFSM();
		
		System.out.println("===== print AIS =====");
		FSM aisFSM = ais.getAisFSM();
		aisFSM.printFSM();

		final long endTime = System.currentTimeMillis();
		System.out.println("Total computation time : " + (endTime - startTime) + " ms");
		
		double currentMemory = ((double) ((double) (Runtime.getRuntime()
                .totalMemory() / 1024) / 1024))
                - ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
        System.out.println("Current Memory usage " + currentMemory + " MB");
	
	}*/
	
}
