import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;

import commandcenter.CommandCenter;
import enumerate.Action;
import enumerate.State;

//This is a reactive AI designed to test our Learning AI
//The goal of this AI is to Mimic a strategy commonly employed in fighting games by human players
//The strategy involves charging down the opponent as quickly as possible and then hitting as quick as possible when in close combat
//This AI will jump over any incoming enemy projectiles

public class ChargeAI implements AIInterface {

	Key inputKey;					// used to pass keystroke inputs to FightingICE
	boolean playerNumber;			// Stores whether the AI is character 1 (true or false)
	FrameData frameData;			// Used to store frame data received from fighting ICE
	GameData gameData;				// use to store game data received from fighting ICE on initialization
	CommandCenter cc;				// Used to store actions to be used
	
		// set of actions available to Charge AI
	String actionSet[] = {
			"FOR_JUMP",			// action 0 (Jump Forwards)
			"DASH",				// action 1 (charge Forward)
			"STAND_FB",			// action 2 (Middle Kick)
			"STAND_A",			// action 3 (Crouch Punch)
			"STAND_D_DB_BB",	// action 4 (Strong Middle Kick)
			"CROUCH_FA"			// action 5 (Upper cut)
	};	
	
		// set of all possible stances our AI can exist in
	String stanceSet[] = {
			"STAND",			// standing
			"CROUCH",			// crouched
			"AIR",				// jumped
			"DOWN"				// knocked over
	};
	
		// this function runs once at the end of a match (three fights)
	@Override
	public void close() {
	
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
		cc.setFrameData(this.frameData, playerNumber);		// update command center object based on current frame data and player number
	}
	
		// This function is run once at the start of every match
		// any data being used in the match is initialized here
	@Override
	public int initialize(GameData gameData, boolean playerNumber) {
		
		this.playerNumber = playerNumber;		// need this to update command center
		this.gameData = gameData;				// used to get size of arena
		inputKey = new Key();					// need this to send key input (command) to ICE
		cc = new CommandCenter();				// this will manage the commands
		frameData = new FrameData();			// this will hold the frame data
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
			
			int Energy = cc.getMyEnergy();								// Calculate our energy level
			int Distance = cc.getDistanceX();							// calculate our horizontal distance to opponent
			State EnemyStance = cc.getEnemyCharacter().getState();		// Get the Stance of our opponent
			Action EnemyAction = cc.getEnemyCharacter().getAction();	// Get the current action (attack) of our opponent
			int State = 0;
			int Action = 0;			
			
				// if an attack is being implemented by the command center continue doing this (i.e. complete current action)
			if (cc.getskillFlag()) {
				inputKey = cc.getSkillKey();
			}
			
				// if an attack isn't being input we need to decide on the next action
			else {
				State = getState(Energy, Distance, EnemyStance, EnemyAction);		// determine which state our character is in based on energy level, distance to opponent, enemy stance and enemy action
				Action = getAction(State);											// choose an action to be implemented based on the state we are in
				cc.commandCall(actionSet[Action]);									// send the chosen action to the command center to be implemented
				
//				System.out.println("The Energy Level is " + Energy);
//				System.out.println("The Distance is " + Distance);
//				System.out.println("The Enemy Stance is" + EnemyStance.toString());
//				System.out.println("The Enemy Action is" + EnemyAction.toString());
//				System.out.println("The State is " + State);
//				System.out.println("The Chosen Action is " + actionSet[Action]);
			}
		}
	}
	
		// this function determines the Environmental State
	private int getState(int Energy, int Distance, State enemyStance, Action EnemyAction) {
		
		int state = 2;
		String Stance = enemyStance.toString();
		boolean Projectile = isProjectile(EnemyAction);		// set to true if enemy has launched a projectile

		
		if (Distance >= 65 && !Projectile) 										{state = 1;}		// (Dash Forward)
		else if (Distance >= 65 && Projectile)										{state = 2;}		// (Jump Forward
		else if (Distance < 65 && Energy >= 50)	 								{state = 3;}		// (strong Middle)
		else if (Distance < 65 && Energy < 50 && Stance.equals(stanceSet[0])) 		{state = 4;}		// (crouch punch)
		else if (Distance < 65 && Energy < 50 && Stance.equals(stanceSet[1]))  	{state = 5;}		// (weak Middle)
		else if (Distance < 65 && Energy < 50 && Stance.equals(stanceSet[2]))		{state = 6;}		// (upper cut)
		else {																							// print this if no state was entered
			System.out.println("Didn't Enter any of the possible States");
			System.out.println("Distance: " + Distance + "Energy: " + Energy + "Stance: " + Stance + "Projectile: " + Projectile);
		}
		
		return (state);
	}
	
		// This function chooses an action from the action set based on which state we are in
		// this is essentially the policy for the projectile AI
	private int getAction(int State) {
        
		int Action = 0;
		
		switch (State) {
        case 1:  Action = 1;	break;
        case 2:  Action = 0;	break;
        case 3:  Action = 4;	break;
        case 4:  Action = 3;	break;
        case 5:  Action = 2;	break;
        case 6:  Action = 5;	break;

        default: Action = 0;	break;
		}

        return (Action);
	}	
	
		// This function determines whether the enemy attacked with a projectile
	private boolean isProjectile(Action EnemyAction) {
        
		boolean Projectile = false;
		
			// checks if enemy action was one of the three projectile moves
		if ((EnemyAction.toString() == "STAND_D_DF_FA") || (EnemyAction.toString() == "STAND_D_DF_FB") || (EnemyAction.toString() == "STAND_D_DF_FC")) {
			Projectile = true;
		}

        return (Projectile);
	}	
}
