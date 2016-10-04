import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ResetBellman {
	public static void main (String args[]) {
		double numActions = 5;			// size of action space
		double numStates = 4;			// size of enviromental state space
		
		try {
			File file1, file2, file3, file4, file5;
			PrintWriter pw1, pw2, pw3, pw4, pw5;
			file1 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/TransistionProbs.txt");
			file2 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/TransistionCounts.txt");
			file3 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/Rewards.txt");
			file4 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/ExpectedRewards.txt");
			file5 = new File("C:/Users/gavan/Documents/Eclipse/Projects/FightingICE/data/aiData/Bellman/policy.txt");
			pw1 = new PrintWriter(new BufferedWriter (new FileWriter(file1)));
			pw2 = new PrintWriter(new BufferedWriter (new FileWriter(file2)));
			pw3 = new PrintWriter(new BufferedWriter (new FileWriter(file3)));
			pw4 = new PrintWriter(new BufferedWriter (new FileWriter(file4)));
			pw5 = new PrintWriter(new BufferedWriter (new FileWriter(file5)));
			
			for (int i = 0; i < numStates*numStates*numActions; i++) {
				double value1 = 1/numStates;
				pw1.println(value1);			// Initial values of transition probabilities 
				pw2.println(1);					// Initial value of counts
				pw3.println(0);					// Initial value of total rewards
				pw4.println(0);					// Initial value of expected rewards
			}
			
				// print initial values of policy to file
			for (int i = 0; i < numStates*numActions; i++) {
				double value2 = 1/numActions;
				pw5.println(value2);
			}
			pw1.close();
			pw2.close();
			pw3.close();
			pw4.close();
			pw5.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
