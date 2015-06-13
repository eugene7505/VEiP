package veip.verification;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import veip.fsm.FSM;
import veip.fsm.PFA;
import veip.fsm.CompositionUtilities;
import veip.fsm.State;

public final class VerificationUtilities {
	private VerificationUtilities() {
	}

	// TODO: use UR for now, change it to no UR
	/*
	 * This function computes the opacity level of a given pfa. It implements
	 * the algorithm proposed in "Current-State Opacity Formulations in
	 * Probabilistic Finite Automata" by Anooshiravan Saboori and Christoforos
	 * N. Hadjicostis. 
	 * Specifically, it first obtain Hpfa, composition of pfa and its observer
	 * automaton. Then, compute the absorption probability of Hpfa: (I-A)p = b
	 * b is an nx1 matrix where bi=0 if state i is safe, and bi=1 otherwise (0-step absorption probability)
	 * A is the transition matrix but we preprocess it such that aij = 0 for all j if
	 * state i unsafe (i.e., state i is absorbing)
	 * I is the identical matrix
	 * We solve for p which is the absorption probability vector
	 * Finally, opacity level = initialDistribution * p
	 */

	public static double computeOpacityLevel(PFA pfa) {
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				pfa, true);
		FSM obsfsm = new FSM(currentStateEstimator);
		pfa.resetAllSecretStates();
		PFA Hpfa = CompositionUtilities
				.pairwiseParallelComposition(pfa, obsfsm);
		int n = Hpfa.getNumberOfStates();

		SimpleMatrix A = Hpfa.getTransitionMatrix();
		
		SimpleMatrix b = new SimpleMatrix(n, 1);
		for (int i = 0; i < n; i++) {
			if (!(Hpfa.getState(i)).isNonsecret()) {
				b.set(i, 1);
				for (int j = 0; j < n; j++) {
					A.set(i, j, 0);
				}
			}
		}
		SimpleMatrix I = SimpleMatrix.identity(n);
		//TODO wrong calculation, absorbing probability should be 0
		(I.minus(A)).print();
		b.print();
		SimpleMatrix absorptionVector = (I.minus(A)).solve(b);
		
		// absorptionVector.print();

		// absorption should be an 1x1 matrix
		SimpleMatrix absorption = (Hpfa.getInitialDistribution())
				.mult(absorptionVector);
		// Hpfa.getInitialDistribution().print();
		double opacityLevel = 1- absorption.get(0);
		return opacityLevel;
	}

	public static boolean isCurrentStateOpaque(FSM fsm){
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				fsm, true);
		return currentStateEstimator.isCurrentStateOpaque();
	}

	public static void isCurrentStateOpaqueAnswer(FSM fsm){
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				fsm, true);
		currentStateEstimator.printEstimator();
		if (currentStateEstimator.isCurrentStateOpaque())
			System.out.println("Current state opaque? Yes");
		else {
			System.out.print("Current state opaque? No ");
			System.out.print("(Estimates ");
			currentStateEstimator.printUnsafeStates();
			System.out.println("reveal the secret)");
		}	
	}
	
	public static void main(String[] args) throws FileNotFoundException {

		String file = "testFSM/stochastic/H.pfa";
		PFA pfa = new PFA(file);
		double opacityLevel = VerificationUtilities.computeOpacityLevel(pfa);
		System.out.println("Opacity level: " + opacityLevel);
		
		
		
		
	}

}
