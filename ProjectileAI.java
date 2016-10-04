import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import commandcenter.CommandCenter;

// This is a reactive AI designed to test our Learning AI
// The goal of this AI is to Mimic a strategy commonly employed in fighting games by human players
// The strategy involves keeping away from the opponents while attacking with projectiles
// If the opponents gets in to close then the projectile AI will throw them away

public class ProjectileAI implements AIInterface {

	Key inputKey;					// used to pass keystroke inputs to FightingICE
	boolean playerNumber;			// Stores whether the AI is character 1 (true or false)
	FrameData frameData;			// Used to store frame data received from fighting ICE
	GameData gameData;				// use to store game data received from fighting ICE on initialization
	CommandCenter cc;				// Used to store actions to be used
	
		// set of actions available to Projectile AI
	String actionSet[] = {
			"FOR_JUMP",			// action 0 (Jump Forwards)
			"BACK_JUMP",		// action 1 (Slide Backwards)
			"STAND_D_DF_FA",	// action 2 (Standard Projectile)
			"STAND_D_DF_FB",	// action 3 (Powerful Projectile)
			"THROW_A",			// action 4 (Standard Throw)
			"THROW_B",			// action 5 (Powerful Throw)
			"DASH"	     		// action 6 (Move Forward)
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
			
			int Energy = cc.getMyEnergy();			// Calculate our energy level
			int Distance = cc.getDistanceX();		// calculate our horizontal distance to opponent
			int State = 0;							
			int Action = 0;
			boolean Wall = againstWall();			// determine whether our character has backed against a wall
			
				// if an attack is being implemented by the command center continue doing this (i.e. complete current action)
			if (cc.getskillFlag()) {
				inputKey = cc.getSkillKey();
			}
			
				// if an attack isn't being input we need to decide on the next action
			else {
				State = getState(Energy, Distance, Wall);		// determine which state our character is in based on energy level, distance to opponent and whether it has backed against a wall
				Action = getAction(State);						// choose an action to be implemented based on the state we are in
				cc.commandCall(actionSet[Action]);				// send the chosen action to the command center to be implemented
				
//				System.out.println("The Energy Level is " + Energy);
//				System.out.println("The Distance is " + Distance);
//				System.out.println("Against the wall is " + Wall);
//				System.out.println("The State is " + State);
//				System.out.println("The Chosen Action is " + actionSet[Action]);
			}
		}
	}

		// This function determine if the AI is against the Wall
	private Boolean againstWall() {
		
		boolean direction = cc.getMyCharacter().isFront();		// get the direction we are facing
		boolean againstWall = false;
		
			// if we are facing right then we are against the wall the distance from our back to the left edge of the arena is less than 50 pixels
		if (direction) {
			if (cc.getMyCharacter().getLeft() < 50) {
				againstWall = true;
			}
		}
			// if we are facing Left then we are against the wall the distance from our back to the right edge of the arena is less than 50 pixels
		else {
			if ((gameData.getStageXMax() - cc.getMyCharacter().getRight()) < 50) {
				againstWall = true;
			}
		}
				
		return (againstWall);
	}
	
		// this function determines the Environmental State
	private int getState(int Energy, int Distance, Boolean Wall) {
		
		int state = 1;
		
		if (Energy >= 30 && Distance >= 300) 							{state = 1;}		// state 1 (Strong projectile)
		if (Energy < 30 && Distance >= 300) 							{state = 2;}		// state 2,3,4 (weak Projectile)
		if (Energy >= 30 && Distance < 300 && Distance >= 100 && Wall) 	{state = 3;}		// state 5 (strong projectile)
		if (Energy < 30 && Distance < 300 && Distance >= 100 && Wall) 	{state = 4;}		// state 6,7,8 ((weak Projectile)
		if (Distance < 300 && Distance >= 100 && !Wall) 				{state = 5;}		// state 9,10,11,12 (Jump Back)
		if (Energy >= 20 && Distance < 100) 							{state = 6;}		// state 13,14 (Strong Throw)
		if (Energy >= 5 && Energy < 20 && Distance < 100) 				{state = 7;}		// state 15 (weak throw)
		if (Energy < 5 && Distance < 100) 								{state = 8;}		// state 16 (Jump Forward)
		if (Distance > 520)												{state = 9;}		// out of range (move forward)
		System.out.println(Distance);
		
		return (state);
	}
	
		// This function chooses an action from the action set based on which state we are in
		// this is essentially the policy for the projectile AI
	private int getAction(int State) {
        
		int Action = 0;
		
		switch (State) {
        case 1:  Action = 3;	break;
        case 2:  Action = 2;	break;
        case 3:  Action = 3;	break; 
        case 4:  Action = 2;	break;
        case 5:  Action = 1;	break;
        case 6:  Action = 5;	break;
        case 7:  Action = 4;	break;
        case 8:  Action = 0;	break;
        case 9:  Action = 6;	break;
        default: Action = 0;	break;
		}

        return (Action);
	}
}
