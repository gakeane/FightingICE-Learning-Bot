# FightingICE-Learning-Bot
The code provided here implements a contestant for the FightingICE AI tournament. The proposed bot uses the Bellman equations to learn the best action given the current state of the environment.

There are 3 parts to this project

_____________________________________________________________________________
_____________________________________________________________________________

				     Code
_____________________________________________________________________________
Code is provided for a leanring agent and three reactive agents used to test 
and train the learning agent. See included report for details on the opperation
and implementation of these agents

REACTIVE AGENTS
	1. Charge AI
	2. Jump AI
	3. Projectile AI

LEARNING AGENTS
	1. Learning AI

Note that to run this code will require the fightICE game platform which can 
be obtained at <http://www.ice.ci.ritsumei.ac.jp/~ftgaic/>. The path names of
the files containing the Bellman information will also need to be adjusted

Finally there is an extra java file which is used to reinitalise the Bellman
information

_____________________________________________________________________________
_____________________________________________________________________________

				     Videos
_____________________________________________________________________________
There are three videos included. These show the learning AI fighting each of 
the three reactive AIs after the policy had converged. Note how in each video
the learning AI has adopted a different fighting style.

_____________________________________________________________________________
_____________________________________________________________________________

			        Bellman Information
_____________________________________________________________________________
There are several files included here which contain the required information
for the Bellman equation updates, These are required to transfer information 
between fights and matches. There is also some matlab code which can be used 
to display all the Bellman Information.

	1. Action Value Function	-	This is the expected value of every state/action pair
	2. Value Function		-	This is the expected value of every state
	3. Policy			- 	This determines what actions are used in which states
	4. Rewards			-	Cumulative reward for transistion between two states given a certain action
	5. Transistion Counts		-	Number of times a transistion between two certain states occured given a certain action
	6. Transistion Probabilities	-	Probability of Transistioning between two certain states given a certain action
	7. Expected Rewards		-	The expected reward for transistioning between two states given a certain action
