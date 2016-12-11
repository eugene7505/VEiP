# VEiP - Verification and Enforcement using Insertion for oPacity




##Introduction
VEiP is a Java based toolbox for verification and enforcement of opacity properties formulated in Discrete Event Systems. While some features are still under development, VEiP can be downloaded for features that are already implemented. The full version of VEiP expects to provide the following functionality:

Logical models:
* Verification of opacity
* Synthesis of an insertion function that enforces opacity
* Synthesis of an optimal insertion function that minimizes insertion cost (under developement)

Stochastic models:
* Computation of the level of opacity
* Synthesis of an optimal insertion function that maximizes opacity level 

VEiP is distributed under BSD 3-Clause license. This repository is no longer updated. The current version of VEiP can be found at https://gitlab.eecs.umich.edu/M-DES-tools.

##Access, Modify, Build 
To access, modify, build, and use the code, follow the instructions below:

1) Install git if it is not already installed. 

2) In a Linux environment, enter a directory where you would like to clone the file structure. EXAMPLE: <br />
$ mkdir VEiP <br />
$ cd VEiP <br />

3) Clone the remote repositoty onto your machine. <br />
$ git clone https://github.com/eugene7505/

4) Enter the main directory (the directory one level above src/) and compile the java applications. <br />
$ javac -d ./bin/ -cp ./src/ src/veip/verification/VerificationMain.java  <br />
$ javac -d ./bin/ -cp src/ src/veip/synthesis/SynthesisMain.java  <br />

5) If the compile is successful, all byte-codes should have been written to bin/. <br />
$ cd ./bin

##Instruction
1) Verification. EXAMPLE: 
* Verify logical opacity: 
$ java veip.verification.VerificationMain -f ../testFSM/test1/G.fsm
* Compute opacity level:
$ java veip.verification.VerificationMain -p ../testFSM/stochastic/H.pfa

2) Synthesis. EXAMPLE: 
* Synthesis an insertion function: 
$ java veip.synthesis.SynthesisMain -g ../testFSM/test2/G.fsm ../testFSM/test2/IA.fsm
* Synthesis an insertion function maximizing the opacity level: 
$ java veip.verification.VerificationMain -s ../testFSM/acc2015RunningExample/H.pfa ../testFSM/acc2015RunningExample/IA.fsm 

## Automata File Formats:
Two models are considered: 

1) Finite-State Automata (FSA)

The .fsm format used in VEiP is adapted from the .fsm format used in DESUMA (https://wiki.eecs.umich.edu/desuma/index.php/DESUMA). The .fsm format here allows multiple initial states; also, controllability feature for events is removed because it is irrelevant to the insertion mechanism. 
Below is a sample .fsm file:

2	1  //number of states, number of initial states 

state0	1	1  //state name, whether it is marked, number of outgoing transitions <br />
a	state1	uo   //transition event, next state, whether the event is observable  <br />

state1	0	1 <br />
b	state0	o <br />

2) Probabilistic Finite-State Automata (PFA)

The file format is similar to .fsm file format. We add initial probability distribution to every state and transition probability to every transition from a state. Below is a sample .pfa file:

2 //number of states

state0	 1	2  0.5  //state name, whether it is marked, number of outgoing transitions, initial probability <br />
a	state1	uo  0.7   //transition event, next state, whether the event is observable, transition probability  <br />
b	state0	o  0.3   

state1	0	 1  0.5 <br />
b	state0	o  1 <br />
