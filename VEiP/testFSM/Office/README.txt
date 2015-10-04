===== This is the README File for Office ====

Office is the model of the secretary and the boss. in”k” represents a person entering from door k. out”k” represents a person leaving from door k. 


Estd models the desired behavior. Este models the “editing” behavior, with replacement (event_name+r), erasure (er), and insertion (event_name+i)

Opacity requirement: (0, 2) is the secret, meaning that the boss is in the office alone.

We also add “utility” requirement:
The goal is that whenever there is a person, we the smart system should know that. But who is in the office or how many people in the office not matter.

edit.fsm recognizes all safe edit patterns; each state is a (real, fake) estimate pair.
In this particular example, no state pair violates the utility requirement. 
For the opacity requirement: should never have (0,2) or (0,1) as the fake estimate
For the utility requirement: should never confuse between (0,0) with others

editGame.fsm is the game structure between the system and the edit function.
