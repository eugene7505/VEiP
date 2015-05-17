# VEiP
This is the repository for the VEiP tool (Verification and Enforcement using Insertion for oPacity)

To access, modify, build, and use the code, follow the instructions below:

1) Install git if it is not already installed. 

2) In a Linux environment, enter a directory where you would like to clone the file structure. EXAMPLE: 
$ mkdir VEiP 
$ cd VEiP

3) Clone the remote repositoty onto your machine. EXAMPLE: 
$ git clone https://github.com/eugene7505/

4) Enter the main directory (the directory one level above src/) and compile the java applications
$ javac -d ./bin/ -cp ./src/ src/veip/verification/VerificationMain.java 
$ javac -d ./bin/ -cp src/ src/veip/synthesis/SynthesisMain.java 

5) If the compile is successful, all byte-codes have been written to bin/.
cd ./bin

6a) Run the verification application 
java veip.verification.VerificationMain <fsmFile>

6b) Run the synthesis application 
java veip.synthesis.SynthesisMain <fsmFile> <IAFile>

7) Outputs will appear in the directory from which the call was made.





