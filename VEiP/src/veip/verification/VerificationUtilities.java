package veip.verification;

import java.io.FileNotFoundException;
import org.ejml.simple.SimpleMatrix;
import veip.fsm.FSM;
import veip.fsm.PFA;
import veip.fsm.CompositionUtilities;
import veip.markov.markovUtilities;

public final class VerificationUtilities {
	private VerificationUtilities() {
	}

	/*
	 * This function computes the opacity level of a given pfa. It implements
	 * the algorithm proposed in "Current-State Opacity Formulations in
	 * Probabilistic Finite Automata" by Anooshiravan Saboori and Christoforos
	 * N. Hadjicostis. Specifically, it first obtain Hpfa, composition of pfa
	 * and its observer automaton. Then, compute the absorption probability of
	 * Hpfa: (I-A)p = b b is an nx1 matrix where bi=1 if state i is unsafe, and
	 * bi=0 otherwise (0-step absorption probability) A is the transition matrix
	 * but we pre-process it such that aij = 0 for all j if state i is in a
	 * closed class (i.e., state i is absorbing) I is the identical matrix We
	 * solve for p which is the absorption probability vector Finally, opacity
	 * level = initialDistribution * p
	 */

	public static double computeOpacityLevel(PFA pfa) {
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				pfa);
		FSM obsfsm = new FSM(currentStateEstimator);
		// pfa.resetAllSecretStates();
		PFA Hpfa = CompositionUtilities
				.pairwiseParallelComposition(pfa, obsfsm);
		//Hpfa.printPFA();
		int n = Hpfa.getNumberOfStates();

		SimpleMatrix unsafeEstimateVector = new SimpleMatrix(n, 1);
		for (int i = 0; i < n; i++) {
			if (!Hpfa.getState(i).isNonsecret())
				unsafeEstimateVector.set(i, 1);
		}

		//the absorptionVector is the probability the i-th state gets absorbed in a unsafe estimate
		SimpleMatrix absorptionVector = markovUtilities
				.computeAbsorptionProbabilities(Hpfa.getTransitionMatrix(),
						unsafeEstimateVector);

		// absorption should be an 1x1 matrix
		SimpleMatrix absorption = (Hpfa.getInitialDistribution())
				.mult(absorptionVector);
		// Hpfa.getInitialDistribution().print();
		double opacityLevel = 1 - absorption.get(0);
		return opacityLevel;
	}

	public static boolean isCurrentStateOpaque(FSM fsm, boolean printMessages) {
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				fsm);
		boolean isOpaque = currentStateEstimator.isCurrentStateOpaque();
		if (printMessages){
			if (isOpaque)
				System.out.println("Current state opaque? Yes");
			else {
				System.out.print("Current state opaque? No ");
				System.out.print("(Estimates ");
				currentStateEstimator.printUnsafeStates();
				System.out.println("reveal the secret)");
			}
		}
		return isOpaque;
	}

	// The two functions are deprecated because we should not need observers w/o
	// unobservable reach
	@Deprecated
	public static void answerCurrentStateOpacity_UR(FSM fsm) {
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

	@Deprecated
	public static void answerCurrentStateOpacity_UP(FSM fsm) {
		CurrentStateEstimator currentStateEstimator = new CurrentStateEstimator(
				fsm, false);
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
