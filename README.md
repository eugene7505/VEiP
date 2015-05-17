# VEiP
This is the repository for the VEiP tool (Verification and Enforcement using Insertion for oPacity)

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

6a) Run the verification application. EXAMPLE: 
$ java veip.verification.VerificationMain ../testFSM/test1/G.fsm

6b) Run the synthesis application. EXAMPLE: 
$ java veip.synthesis.SynthesisMain ../testFSM/test2/G.fsm ../testFSM/test2/IA.fsm

7) Outputs will appear in the directory from which the call was made.


* The input/output file format of VEiP is slightly modified from the original .fsm format in order to allow the system to start from a set of initial states. Specifically, we add the number of initial states (say k) next to the number of total states in the first line of the file. The first k states in the file are the initial states. See testFSM/test2/G.fsm for example.




