
import java.util.*;
public class warsimulation {
			
		private int numOfGuess = 0;// keep track of torpedo launches
		private commandlaunch center = new commandlaunch();// instantiation of command launch center.
		private ArrayList<battleship> fleet = new ArrayList<battleship>();// create new ArrayList named fleet with reference to battleship objects
		
		private void setUpGame(){
			// create and place battleships on sea.
			battleship atlantic = new battleship();// create battleship object
			atlantic.setName("Captain Nemo");// call it something
			battleship pacific = new battleship();
			pacific.setName("Admiral Nelson");
			battleship arctic = new battleship();
			arctic.setName("Admiral Yamamoto");
			
			fleet.add(atlantic);//add battleships to arrayList fleet
			fleet.add(pacific);
			fleet.add(arctic);
			
			System.out.println("Your goal is to sink three battleships commanded by:");//  introduction and directions 
			System.out.println("Captain Nemo, Admiral Nelson, Admiral Yamamoto");
			System.out.println("You have limited ammunition, try to sink them quick.");
			System.out.println("The war takes place on a grid with latitude range(abcdefg)and longitude range(0-7)");
			System.out.println("To fire a torpedo assign a valid latitude and longitude command, ex A7");
			
			for(battleship target:fleet){// enhanced for each in loop to go through the fleet arraylist collection element references;battleships
				ArrayList<String> newLocation = center.placebattleship(3);
				target.setTargetCells(newLocation);
			}// close for loop
		}// close setUpgame method.
		
		private void startPlaying(){
			while(!fleet.isEmpty()){// as long as there are ships on the board keep playing
				String torpedoGuess = center.getUserInput("The battle has begun!assign location to fire torpedo:");// get coordinates for torpedo launch
				
				radarCheck(torpedoGuess);// check coordinates
			}// close while
			finishGame();
		}// close startPlaying method
		
		private void radarCheck(String torpedoGuess){
			numOfGuess++;// increment number of torpedoes launched after each volley.
			String result = "miss";
			
			for(battleship target: fleet){// for each of the element references in the arraylist check if hit registered or not.
				result = target.radarCheck(torpedoGuess);
				if(result.equals("hit!")){
					break;// if yes then break out
				}
				if(result.equals("kill confirmed")){
					fleet.remove(target);
					break;
				}
			}// close for
			System.out.println(result);
		}// close method
		
		private void finishGame(){
			System.out.println("The enemy fleet is destroyed!");
			if(numOfGuess <= 30){
				System.out.println("Well done! you only used" +numOfGuess+ "torpedos.");
				System.out.println("You've been promted ! And you survived the war out on sea.");
			} else{
				System.out.println("That was a close one! you used " +numOfGuess+ " torpedoes.");
				System.out.println("Luckily the enemy ran out of ammunition before you!");
			}
		}// close method
		
		public static void main(String[]args){// main method, create a new war simulation, set it up and start playing.
			warsimulation game = new warsimulation();
			game.setUpGame();
			game.startPlaying();
		}// close method
	
	}
		
	
		