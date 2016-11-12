
import java.util.*;

public class battleship {
	
	private ArrayList<String> targetCells;// an arraylist that holds strings, encapsulated
	private String name;// a string retainer
	
	
	public void setTargetCells(ArrayList<String> locs){// setter method to place battleships on the map.
		targetCells = locs;
	}
	
	public void setName(String n){// setter method to differentiate individual battleships.
		name = n;
	}

	public String radarCheck(String torpedoGuess){// perform radar check to see if the torpedo hit the ship
		String result = "miss";
		
		
		int index = targetCells.indexOf(torpedoGuess);
		if(index >= 0){
			targetCells.remove(index);
			
			if(targetCells.isEmpty()){// if targetCells are all empty then kill confirmed.
				result = "kill confirmed";
				System.out.println("You have sunk "+ name + " :(");// print out confirmation of kill, destroyed commandant name.
			}else {
				result = "hit!";// declare a hit every time the torpedoGuess index matches the targetCell index.
			}//close if
		}// close outer if
		
		return result;
	
	}//close method
}//close class

			
