# VEiP - Verification and Enforcement using Insertion for oPacity

##Introduction 
VEiP is a Java based toolbox for verification and enforcement of opacity properties formulated in Discrete Event Systems. The toolbox is still under active development. Its full version expects to provide the following functionality:

Logical models:
* Verification of opacity
* Synthesis of an insertion function that enforces opacity
* Synthesis of an optimal insertion function that minimizes insertion cost (under developement)

Stochastic models:
* Computation of the level of opacity
* Synthesis of an optimal insertion function that maximizes opacity level (under developement)

##Access, Modify, Build 
To access, modify, build, and use the code, follow the instructions below:

1) Install git if it is not already installed. 

2) In a Linux environment, enter a directory where you would like to clone the file structure. EXAMPLE: 
$ mkdir VEiP 
$ cd VEiP

3) Clone the remote repositoty onto your machine.  
$ git clone https://github.com/eugene7505/

4) Enter the main directory (the directory one level above src/) and compile the java applications
$ javac -d ./bin/ -cp ./src/ src/veip/verification/VerificationMain.java 
$ javac -d ./bin/ -cp src/ src/veip/synthesis/SynthesisMain.java 

5) If the compile is successful, all byte-codes should have been written to bin/.
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


## Automata File Format:
Two kinds of models are considered: 
1) Finite-State Automata (FSA)
The file format of follows the .fsm file format used in DESUMA (https://wiki.eecs.umich.edu/desuma/index.php/DESUMA). However, .fsm files assume there is only one initial state. To allow multiple initial states, we add the number of initial states (say k) next to the number of total states in the first line of the file. The first k states in the file are the initial states. See testFSM/test2/G.fsm for example.

2) Probabilistic Finite-State Automata (PFA)
The file format is similar to .fsm file format. We add initial probability distribution to every state and transition probability to every transition from a state. See testFSM/stochastic/H.pfa for example. 


