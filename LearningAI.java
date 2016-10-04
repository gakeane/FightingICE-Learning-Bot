import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.Random;
import java.util.Scanner;

import commandcenter.CommandCenter;
import enumerate.Action;
import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;

// This is the main learning AI. This AI is designed to update the Bellman equations
// after each match (3 fights). Each update of the bellman equations changes the AIs policy 
// until it converges to some final policy based on the experience gained fighting its opponent
// The goal of this AI is that it will be able to learn how to defeat simple reactive AI through 
// repeated exploration

public class LearningAI implements AIInterface {

	Key inputKey;				// used to pass keystroke inputs to FightingICE
	boolean playerNumber;		// Stores whether the AI is character 1 (true or false)
	boolean landing;			// keeps track of whether character has just landed from a jump
	int previousEnemyHP;		// keeps track of enemies HP before current action
	int myPreviousHP;			// keeps track of my HP before current action
	int previousState;			// Tracks the previous environmental state the AI was in
	int previousAction;			// Tracks the previous action the AI executed
	Random rnd;					// used to generate random numbers
	FrameData frameData;		// Used to store frame data received from fighting ICE
	GameData gameData;			// use to store game data received from fighting ICE on initialization
	CommandCenter cc;			// Used to store actions to be used
	
		// Files printers and Scanners used for file I/O
	File file1, file2, file3, file4, file5, file6, file7;
	PrintWriter pw1, pw2, pw3, pw4, pw5, pw6, pw7;
	Scanner scan1, scan2, scan3, scan4, scan5;
	
	int numActions = 5;			// size of action space
	int numStates = 4;			// size of environmental state space
	
		// set of actions available to Learning AI
		// action set is kept small to allow quick convergence of policy
	String actionSet[] = {
			"FORWARD_WALK",				// action 0   (Walk Forwards)	(MOVE)
 			"FOR_JUMP",					// action 1   (Jump Forwards)	(DODGE)
			"STAND_GUARD",				// action 2   (Standing Guard)	(BLOCK)
			"STAND_A",					// action 3   (Weak Punch)		(ATTACK)
			"STAND_D_DF_FA",			// action 4   (Weak Projectile)	(SHOOT)
	};	
	
		// set of all possible stances our AI can exist in
	String stanceSet[] = {
			"STAND",			// standing
			"CROUCH",			// crouched
			"AIR",				// jumped
			"DOWN"				// knocked over
	};
	
	double[] valueFunction = new double[numStates];										// stores the value function for each state
	double[][] actionValueFunction = new double[numStates][numActions];					// stores the action value function for each action given a specific state
	double[][] Policy = new double[numStates][numActions];								// stores the policy (which action is used given each state)
	double[][][] Rewards = new double[numStates][numStates][numActions];				// total reward for changing from state s to state s' given action a
	double[][][] Transistion = new double[numStates][numStates][numActions];			// Probability of changing from state s to state s' given action a
	double[][][] TransistionCounter = new double[numStates][numStates][numActions];		// counts number of times each transition occurs
	double[][][] ExpectedRewards = new double[numStates][numStates][numActions];		// stores the expected reward for each transition
	
		// this function runs once at the end of a match (three fights)
	@Override
	public void close() {
		policyEvaluation();			// evaluate how effect current policy is (calculates value and action value functions for each state)
		policyUpdate();				// updates policy based on evaluation
		writeData();				// Writes new policy, rewards, transition probabilities ,counts and value functions to file

	}

		// This function is used to tell FightingICE which character the AI wants to be
		// This is run once at the start of each match
	@Override
	public String getCharacter() {

		return CHARACTER_ZEN;
	}

		// This function receives frame information from FightingICE
		// this function runs for every frame with a 0.25s delay
	@Override
	public void getInformation(FrameData frameData) {
		
		this.frameData = frameData;							// read in the current frame data		
		cc.setFrameData(this.frameData, playerNumber);		// update command center object based on current frame data and your player number
	}

		// This function is run once at the start of every match
		// any data being used in the match is initialized here
	@Override
	public int initialize(GameData gameData, boolean playerNumber) {
		
		this.playerNumber = playerNumber;		// need this to update command center
		this.gameData = gameData;				// used to get size of arena
		this.landing = false;					// true if just landed
		this.previousEnemyHP = 0;				// Keeps Track of Enemy HP (for Rewards)
		this.myPreviousHP = 0;					// keeps track of my HP (for Rewards)
		this.previousState = 0;					// keeps track of the last state (for counts and transition updates)
		this.previousAction = 0;				// keeps track of previous action
		inputKey = new Key();					// need this to send key input (command) to ICE
		cc = new CommandCenter();				// this will manage the commands
		frameData = new FrameData();			// this will hold the frame data
		rnd = new Random();						// generates random numbers
		
			// files we will print data to
		file1 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/TransistionCounts.txt");
		file2 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/TransistionProbs.txt");
		file3 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/Rewards.txt");
		file4 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/ExpectedRewards.txt");
		file5 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/Policy.txt");
		file6 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/ActionValueFunction.txt");
		file7 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/ValueFunction.txt");
		
		// Read in values for policy, rewards and transition probabilities from previous fight
		try {
			scan1 = new Scanner(file1);		// transition counts
			scan2 = new Scanner(file2);		// transition probabilities
			scan3 = new Scanner(file3);		// total rewards
			scan4 = new Scanner(file4);		// expected Rewards
			scan5 = new Scanner(file5);		// policy
			
			for (int i = 0; i < numStates; i++) {
				for (int j = 0; j < numStates; j++) {
					for (int k = 0; k < numActions; k++) {
						double value1 = scan1.nextDouble();
						double value2 = scan2.nextDouble();
						double value3 = scan3.nextDouble();
						double value4 = scan4.nextDouble();
						TransistionCounter[i][j][k] = value1;		// transition counts
						Transistion[i][j][k] = value2;				// transition probabilities
						Rewards[i][j][k] = value3;					// total rewards
						ExpectedRewards[i][j][k] = value4;			// expected Rewards
					}
				}
			}
			
			for (int i = 0; i < numStates; i++) {
				for (int j = 0; j < numActions; j++) {
					double value5 = scan5.nextDouble();
					Policy[i][j] = value5;
					actionValueFunction[i][j] = 0;			// policy
				}
			}
			
			scan1.close();
			scan2.close();
			scan3.close();
			scan4.close();
			scan5.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < numStates; i++) {
			valueFunction[i] = 0;					// Initialize Value function
		}
		
		return 0;
	}
	
		// this function sends the Keystroke specified by inputKey to FightingICE
		// this function runs for every frame 
	@Override
	public Key input() {

		return inputKey;						// send inputs to ICE
	}
	
		// This is the main loop of the AI
		// This runs for every frame and it is here that we decided which action will be taken
	@Override
	public void processing() {
		
			// check that there is still time in the match and that the frame data hasn't returned a null value
		if (!frameData.getEmptyFlag() && frameData.getRemainingTime() > 0) {
			
				// if an attack is being implemented by the command center continue doing this (i.e. complete current action)
			if (cc.getskillFlag()) {
				inputKey = cc.getSkillKey();
			}
			
				// if an attack isn't being input we need to decide on the next action
			else {
				
				// boolean inControl = cc.getMyCharacter().isControl();
				
				// if (inControl) {
					int Distance = cc.getDistanceX();		// calculate our horizontal distance to opponent
					int EnemyHP = changeEnemyHP();			// get change in enemy HP from previous action (used to calculate Reward)
					int MyHP = changeMyHP();				// get change in my HP from previous action (used to calculate Reward)
					int state = 1;							 
					int action = 0;
					
					Action enemyAction = cc.getEnemyCharacter().getAction();	// Get the current action (attack) of our opponent	
					
					state = getState(Distance, enemyAction);		// determine which state our character is in based distance to opponent if the opponent is attacking
					updateTransistions(state);						// update the transition probabilities and counts
					updateRewards(state, EnemyHP, MyHP);			// update the total and expected rewards
					action = getAction(state);						// choose an action to be implemented based on the state we are in
					cc.commandCall(actionSet[action]);				// send the chosen action to the command center to be implemented
				// }
			}
		}
	}
	
		// this function determines the Environmental State
	private int getState(int Distance, Action EnemyAction) {
		
		int state = 1;
		boolean attackRange = inAttackRange(Distance);			// determine if enemy is within attack range
		boolean EnemyAttack = EnemyAttacking(EnemyAction);		// determine if enemy is attacking
		
			// only four states are used to ensure quick convergence of policy (easy to add more states)
		if (!attackRange && !EnemyAttack) 											{state = 0;}		// far away and passive enemy
		if (!attackRange && EnemyAttack) 											{state = 1;}		// far away and aggressive enemy
		if (attackRange && !EnemyAttack) 											{state = 2;}		// close up and passive enemy
		if (attackRange && EnemyAttack) 											{state = 3;}		// close up and aggressive enemy
	
		return (state);
	}
	
		// This function chooses an action from the action set based on which state we are in
		// This is done using the current policy for our Learning AI
	private int getAction(int state) {
	    
		// given that we are in a current state this function will chose an action with probability
		// assigned by the current policy
		
		int Action = 0;
		int random = rnd.nextInt(10) + 1;			// random number between 1 and 10
		double max = random*Policy[state][0];		// Initialize current highest probability as first possible action in current state times a random number between 1 and 10
		double value;
		
			// for every possible action given the current state
		for (int i = 1; i < numActions; i++) {
			random = rnd.nextInt(10) + 1;				// generate a new random number
			value = random*Policy[state][i];			// multiply the probability of this action being chosen  based on the current policy by the random value
			if (value > max) {							// if this is larger than the current maximum then this become the new maximum
				max = value;
				Action = i;
			}
			
			else if (value == max) {					// if there is a tie randomly chose one or the other
				random = rnd.nextInt(10);
				if (random > 5) {
					Action = i;
				}
			}
		}
	
		// return the action which resulted in the maximum value
		
		previousAction = Action;
	    return (Action);
	}
			
		// calculate the change in enemy HP
	private int changeEnemyHP () {
		
			// if HP increases its due to start of next round (ignore this)
		if (cc.getEnemyHP() > previousEnemyHP) {
			return (0);
		}
		
		int HP = previousEnemyHP - cc.getEnemyHP();			// calculate change in HP from when last action was taken
		previousEnemyHP = cc.getEnemyHP();					// update previous HP value
		return (HP);
	}
		
		// calculate the change in my HP
	private int changeMyHP () {
		
			// if HP increases its due to start of next round (ignore this)
		if (cc.getMyHP() > myPreviousHP) {
			return (0);
		}
		
		int HP = myPreviousHP - cc.getMyHP();			// calculate change in HP from when last action was taken
		myPreviousHP = cc.getMyHP();					// update previous HP value
		return (HP);
	}

		// update the Transition probabilities
	private void updateTransistions (int state) {
		
		TransistionCounter[previousState][state][previousAction] = TransistionCounter[previousState][state][previousAction] + 1;		// given current state, previous state and action taken (increase counts)
		int rowsum = 0;
		for (int i = 0; i < numStates; i++) {
			rowsum += TransistionCounter[previousState][i][previousAction];				// given a state and and action calculate the sum of transitions to any other state
		}
		
		// the transition probabilities are the ratio of transitioning to a specific state vs transitioning to any state given a certain state and action
		for (int i = 0; i < numStates; i++) {
			Transistion[previousState][i][previousAction] = TransistionCounter[previousState][i][previousAction]/rowsum;
		}
		System.out.println("["+previousState+"]["+state+"]["+previousAction+"]");		// print the state transition
		previousState = state;															// update the previous state with the current state
	}

		// Update the Reward probabilities
	private void updateRewards (int state, int EnemyHP, int MyHP) {
		
		// reward function is the change in enemy HP - change in my HP
		// total reward is the sum of rewards gained whenever a specific transition occurred
		// expected reward is the total reward for a specific transition divided by the counts for that transition (i.e. the mean)
		
		Rewards[previousState][state][previousAction] += (EnemyHP - MyHP);
		ExpectedRewards[previousState][state][previousAction] = Rewards[previousState][state][previousAction]/TransistionCounter[previousState][state][previousAction];
	}
	
		// Write Data to file so it can be loaded next round
	private void writeData () {
		try {
			pw1 = new PrintWriter(new BufferedWriter (new FileWriter(file1)));
			pw2 = new PrintWriter(new BufferedWriter (new FileWriter(file2)));
			pw3 = new PrintWriter(new BufferedWriter (new FileWriter(file3)));
			pw4 = new PrintWriter(new BufferedWriter (new FileWriter(file4)));
			
			for (int i = 0; i < numStates; i++) {
				for (int j = 0; j < numStates; j++) {
					for (int k = 0; k < numActions; k++) {
						pw1.println(TransistionCounter[i][j][k]);		// write transition counts to file
						pw2.println(Transistion[i][j][k]);				// write transition probabilities to file
						pw3.println(Rewards[i][j][k]);					// write total rewards to file
						pw4.println(ExpectedRewards[i][j][k]);			// write expected rewards to file
					}
				}
			}
			
			System.out.println("Printed Rewards and Transistion Probability data to file");
			pw1.close();
			pw2.close();
			pw3.close();
			pw4.close();
			
			pw5 = new PrintWriter(new BufferedWriter (new FileWriter(file5)));
			pw6 = new PrintWriter(new BufferedWriter (new FileWriter(file6)));
			for (int j = 0; j < numStates; j++) {
				for (int k = 0; k < numActions; k++) {
					pw5.println(Policy[j][k]);							// write policy to file
					pw6.println(actionValueFunction[j][k]);				// write action value functions to file
				}
			}
			pw5.close();
			pw6.close();
			
			pw7 = new PrintWriter(new BufferedWriter (new FileWriter(file7)));
			for (int i = 0; i < numStates; i++) {
				pw7.println(valueFunction[i]);					// write value function to file
			}
			pw7.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
		// determine whether opponent is in attack range
	private boolean inAttackRange(int Distance) {
		
			// if distance to opponent is less than 70 pixels then the opponent is in range for an attack
		boolean inRange = false;
		if (Distance < 65) {
			inRange = true;
		}
		
		return(inRange);
	}
	
		// determine whether enemy is attacking
	private boolean EnemyAttacking(Action EnemyAction) {
		
			// if the enemy action string matches one of the attack actions then the enemy is attacking
		boolean EnemyAttacking = false;
		String EA = EnemyAction.toString();
		if ((EA == "THROW_A") || (EA == "THROW_B") || (EA == "STAND_A") || (EA == "STAND_B") ||
			(EA == "CROUCH_A") || (EA == "CROUCH_B") || (EA == "AIR_A") || (EA == "AIR_B") ||
			(EA == "AIR_DA") || (EA == "AIR_DB") || (EA == "STAND_FA") || (EA == "STAND_FB") ||
			(EA == "CROUCH_FA") || (EA == "CROUCH_FB") || (EA == "AIR_FA") || (EA == "AIR_FB") ||
			(EA == "AIR_UA") || (EA == "AIR_DA") || (EA == "AIR_UB") || (EA == "STAND_D_DF_FA") ||
			(EA == "STAND_D_DF_FB") || (EA == "STAND_F_D_DFA") || (EA == "STAND_F_D_DFB") || (EA == "STAND_D_DB_DA") ||
			(EA == "STAND_D_DB_BB") || (EA == "AIR_D_DF_FA") || (EA == "AIR_D_DF_FB") || (EA == "AIR_F_D_DFA") ||
			(EA == "AIR_F_D_DFB") || (EA == "AIR_D_DB_BA") || (EA == "AIR_D_DB_BB") || (EA == "STAND_D_DF_FC")) 
		{
			EnemyAttacking = true;
		}
		return(EnemyAttacking);
	}
	
		// Evaluate Current Policy
	private void policyEvaluation () {
		// initialize each of the value functions to zero (for each state)
		// policy evaluation is done for each state (four in this case)
		// for each state we require the policy, transition probabilities, rewards, current value function and gamma factor
		
		double gamma = 0.9;				// this is a wait
		double thres = 0.1;				// convergence threshold
		double maxdiff = thres;			// difference between current iteration and previous iteration
		
			// while we haven't dropped within our threshold keep improving evaluation
		while (maxdiff >= thres) {
			maxdiff = 0;		// need to reset difference on each iteration
			
				// for every state
			for (int state = 0; state < numStates; state++) {
				double prevValue = valueFunction[state];									// store a copy of the old value function
				valueFunction[state] = BellmanUpdate(state, gamma);							// calculate new value function from bellman equations (also stores each action value function)
				maxdiff = Math.max(maxdiff, Math.abs(prevValue - valueFunction[state]));	// calculate the difference between the old and updated value function
			}
		}
	}
	
		// Calculate Bellman equations (used to update value functions)
	private double BellmanUpdate (int state, double gamma) {
		
		// this function calculates the value function for that state passed to it
		// also calculates all the action value functions for the state passed to it
		
		double valueUpdate = 0;			// stores the value of the value function
		
		// for all the actions available in the current state
		for (int action = 0; action < numActions; action++) {
			
			double actionValue = 0;			// stores the value of the action value function
			
			// for all the possible transition states
			for (int transState = 0; transState < numStates; transState++) {
				actionValue = actionValue + Transistion[state][transState][action]*(ExpectedRewards[state][transState][action] + gamma*valueFunction[transState]);		// solve bellman equation to get action value function for give state and action 
				// System.out.println("action Value Function is: " + actionValue);
			}
			
			actionValueFunction[state][action] = actionValue;					// store each action value function
			valueUpdate = valueUpdate + Policy[state][action]*actionValue;		// value function the sum of action value functions for each action times the policy for each action and the given state
		}
		
		return (valueUpdate);
	}
	
		// updates the policy based on the value functions and action value functions
	private void policyUpdate () {
		// for each state find largest action value function (will need to be able to handle ties)
		// if this is larger than the value function change policy so that this action is taken when in this state
		
			// for every state
		for (int state = 0; state < numStates; state++) {
			double maxAction = actionValueFunction[state][0];			// find the largest action value function and its associated action
			int maxindex = 0;
			for (int action = 1; action < numActions; action++) {
				if (maxAction < actionValueFunction[state][action]) {	
					maxAction = actionValueFunction[state][action];
					maxindex = action;
				}
			}
			
				// if the largest action value function is greater than the current value function then update the policy t the action associated to the action value function
			if (maxAction > valueFunction[state]) {
				for (int action = 0; action < numActions; action++) {
					if (action == maxindex) {
						Policy[state][action] = 1;
					}
					else {
						Policy[state][action] = 0;
					}
				}
			}
		}
	}
	
}