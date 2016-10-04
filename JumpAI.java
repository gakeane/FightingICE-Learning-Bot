import commandcenter.CommandCenter;
import enumerate.State;
import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;

public class JumpAI implements AIInterface {

	Key inputKey;					// used to pass keystroke inputs to FightingICE
	boolean playerNumber;			// Stores whether the AI is character 1 (true or false)
	boolean Landing;				// keeps track of whether character has just landed from a jump
	FrameData frameData;			// Used to store frame data received from fighting ICE
	GameData gameData;				// use to store game data received from fighting ICE on initialization
	CommandCenter cc;				// Used to store actions to be used
	
		// set of actions available to Jump AI
	String actionSet[] = {
			"FOR_JUMP",			// action 0 (Jump Forwards)
			"JUMP",				// action 1 (Jump Up)
			"AIR_B",			// action 2 (Jump Kick)
			"AIR_UB",			// action 3 (Jump Stamp)
			"AIR_D_DF_FA",		// action 4 (air Projectile)
			"STAND_B"			// action 5 (ground kick)
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
		this.Landing = false;					// Set to true on landing
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
			int Distance = cc.getDistanceX();							// Calculate our horizontal distance to opponent
			State myStance = cc.getMyCharacter().getState();			// Determine what stance our character is in
			int State = 0;
			int Action = 0;			
			
				// if an attack is being implemented by the command center continue doing this (i.e. complete current action)
			if (cc.getskillFlag()) {
				inputKey = cc.getSkillKey();
			}
			
				// if an attack isn't being input we need to decide on the next action
			else {
				State = getState(Energy, Distance, myStance);						// determine which state our character is in based on energy level, distance to opponent and our stance
				Action = getAction(State);											// choose an action to be implemented based on the state we are in
				cc.commandCall(actionSet[Action]);									// send the chosen action to the command center to be implemented
				
//				System.out.println("The state is: " + State);
//				System.out.println("The action is: " + Action);
			}
		}

	}
	
		// this function determines the Environmental State
		// also used to set landing variable
	private int getState(int Energy, int Distance, State myStance) {
		
		int state = 1;
		String Stance = myStance.toString();
	
		
		if (Distance >= 100 && Stance == stanceSet[0] && !Landing) 							{state = 1;}		// (Jump Forward)
		else if (Distance < 100 && Stance == stanceSet[0] && !Landing) 						{state = 2;}		// (Jump up)
		else if (Stance == stanceSet[2])									{Landing = true; state = 3;}		// (jump Stamp)			// if we're in the air set landing to true
		else if (Stance == stanceSet[0] && Landing)     					{Landing = false ;state = 4;} 		// (Ground Kick)		// if were on the ground and landing is true then set it to false

		else {
			System.out.println("Didn't Enter any of the possible States");										// print this if no state was entered
		}
		
		return (state);
	}
	
		// This function chooses an action from the action set based on which state we are in
		// this is essentially the policy for the projectile AI
	private int getAction(int State) {
	    
		int Action = 0;
		
		switch (State) {
	    case 1:  Action = 0;	break;
	    case 2:  Action = 1;	break;
	    case 3:  Action = 2;    break;
	    case 4:  Action = 5;	break;
	
	    default: Action = 0;	break;
		}
	
	    return (Action);
	}

}
