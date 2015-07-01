package veip.markov;

import org.ejml.simple.SimpleMatrix;

public class markovUtilities {

	public static class CommClassResult {
		SimpleMatrix communicatingMatrix;
		SimpleMatrix closedClassesVector;
	}

	private markovUtilities() {
	}

	public static CommClassResult computeCommClasses(
			SimpleMatrix transitionMatrix) {
		CommClassResult result = new CommClassResult();

		// compute communicating classes
		int n = transitionMatrix.numCols();
		assert (n == transitionMatrix.numRows());
		SimpleMatrix toMatrix = new SimpleMatrix(n, n);
		// Floyd-Warshall
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (transitionMatrix.get(i, j) > 0)
					toMatrix.set(i, j, 1);
			}
		}
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if ((toMatrix.get(i, j) == 1)
							|| ((toMatrix.get(i, k) == 1) && (toMatrix
									.get(k, j) == 1)))
						toMatrix.set(i, j, 1);
				}
			}
		}
		SimpleMatrix fromMatrix = toMatrix.transpose();
		SimpleMatrix commMatrix = toMatrix.elementMult(fromMatrix);

		SimpleMatrix closedVector = new SimpleMatrix(1, n);
		for (int i = 0; i < n; i++) {
			closedVector.set(i, 1);
			for (int j = 0; j < n; j++) {
				if ((toMatrix.get(i, j) == 1) && (fromMatrix.get(i, j) == 0))
					closedVector.set(i, 0);
			}
		}

		result.communicatingMatrix = commMatrix;
		result.closedClassesVector = closedVector;
		return result;
	}

	public static SimpleMatrix computeAbsorptionProbabilities(
			SimpleMatrix transitionMatrix, SimpleMatrix unsafeStateVector) {
		int n = transitionMatrix.numCols();
		SimpleMatrix A = new SimpleMatrix(transitionMatrix);
		// step1: make unsafe state absorbing
		for (int i = 0; i < n; i++) {
			if (unsafeStateVector.get(i) == 0)
				continue;
			else {
				for (int j = 0; j < n; j++)
					A.set(i, j, 0);
			}
		}
		// step2: compute communicating classes and closed classes
		SimpleMatrix closedClassMatrix = computeCommClasses(A).closedClassesVector;
		// step3: compute vector b and A
		// note that unsafe states are single absorbing states by construction; thus they are single closed class
		SimpleMatrix b = new SimpleMatrix(n, 1);
		for (int i = 0; i < n; i++) {
			if (closedClassMatrix.get(i) == 1) { // closed class
				for (int j = 0; j < n; j++) {
					A.set(i, j, 0);
				}
				if (unsafeStateVector.get(i) == 1)
					b.set(i, 1);
			}
		}
		SimpleMatrix I = SimpleMatrix.identity(n);
		SimpleMatrix absorptionProbabilityVector = (I.minus(A)).solve(b);
		return absorptionProbabilityVector;
	}

	public static void main(String args[]) {
		SimpleMatrix matrix = new SimpleMatrix(3, 3);
		matrix.set(0, 0.3);
		matrix.set(1, 0.2);
		matrix.set(2, 0.5);
		matrix.set(4, 1);
		matrix.set(6, 1);

		SimpleMatrix unsafeStateVector = new SimpleMatrix(1, 3);
		unsafeStateVector.set(1, 1);

		SimpleMatrix abs = markovUtilities.computeAbsorptionProbabilities(
				matrix, unsafeStateVector);
		abs.print();
	}
}
