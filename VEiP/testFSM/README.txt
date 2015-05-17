===== This is the README File for testFSM ====

test1/
This testcase is a fully observable automaton (G = obsG) that reveals the secret.
The resulting AIS is empty. There is no i-enforceable insertion function that enforces current-state opacity.


test2/
This testcase is a partially observable automaton that is not current-state opaque.
The AIS is not empty. The current algorithm synthesizes a greedy insertion automaton.


Notice that VEiP currently only consider current-state opacity and implements the current-state estimator.
